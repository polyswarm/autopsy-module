/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.polyswarm.swarmit;

import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.polyswarm.swarmit.apiclient.SwarmItApiClient;
import io.polyswarm.swarmit.apiclient.SwarmItVerdictEnum;
import io.polyswarm.swarmit.datamodel.SwarmItDb;
import io.polyswarm.swarmit.datamodel.SwarmItDbException;
import io.polyswarm.swarmit.datamodel.SwarmItPendingSubmission;
import io.polyswarm.swarmit.optionspanel.SwarmItMarketplaceSettings;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Worker;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.sleuthkit.autopsy.casemodule.Case;
import org.sleuthkit.autopsy.ingest.IngestServices;
import org.sleuthkit.autopsy.ingest.ModuleDataEvent;
import org.sleuthkit.datamodel.AbstractFile;
import org.sleuthkit.datamodel.BlackboardArtifact;
import org.sleuthkit.datamodel.BlackboardAttribute;
import org.sleuthkit.datamodel.TskCoreException;
import org.sleuthkit.datamodel.TskData;
import org.sleuthkit.datamodel.TskDataException;

/**
 * Thread that runs in the background to process AbstractFiles that have
 * been submitted using SwarmIt right-click menu option.
 * 
 * AbstractFile ID and PolySwarm UUID are kept in sqlite db.
 * 
 * This process will periodically check the sqlite db for new entries.
 * For each entry, this process will contact the PolySwarm API 
 * to get the analysis results.
 * It will then add a PolySwarm Results artifact to the AbstractFile.
 * These artifacts will be displayed in the left pane in a sub-tree
 * called "PolySwarm Results."
 * 
 */
public class SwarmItController {
    private static final Logger LOGGER = Logger.getLogger(SwarmItController.class.getName());
    
    private final Case autopsyCase;
    private final SwarmItMarketplaceSettings apiSettings;
    private final SwarmItDb dbInstance;
    private ListeningScheduledExecutorService dbExecutor;
    private static final String POLYSWARM_ARTIFACT_TYPE_NAME = "POLYSWARM_VERDICT";
    private static final String POLYSWARM_ARTIFACT_TYPE_DISPLAY_NAME = "PolySwarm Results";
    private static final String POLYSWARM_ARTIFACT_ATTRIBUTE_MALICIOUS = "Malicious";
    private static final String POLYSWARM_ARTIFACT_ATTRIBUTE_NONMALICIOUS = "Non-Malicious";

    public Case getAutopsyCase() {
        return autopsyCase;
    }
    
    SwarmItController(Case newCase) throws TskCoreException, SwarmItDbException {
        this.autopsyCase = Objects.requireNonNull(newCase);
        this.apiSettings = new SwarmItMarketplaceSettings();
        this.dbInstance = SwarmItDb.getInstance();
        
        dbExecutor = getNewDBExecutor();
        createCustomArtifactType(this.autopsyCase);
        dbExecutor.scheduleAtFixedRate(new ResolvePendingSubmissionsTask(this.dbInstance, this.autopsyCase), 0, 30, TimeUnit.SECONDS);
    }
    
    /**
     * Create the POLYSWARM_VERDICT custom artifact type and add it to the blackboard.
     * 
     * @param autopsyCase Open Case
     */
    private void createCustomArtifactType(Case autopsyCase) {
        try {
            if (autopsyCase.getSleuthkitCase().getArtifactType(POLYSWARM_ARTIFACT_TYPE_NAME) == null) {
                LOGGER.log(Level.INFO, "Adding POLYSWARM_VERDICT custom artifact type"); 
                autopsyCase.getSleuthkitCase().addBlackboardArtifactType(POLYSWARM_ARTIFACT_TYPE_NAME, POLYSWARM_ARTIFACT_TYPE_DISPLAY_NAME);
            }
        } catch (TskCoreException | TskDataException ex) {
            LOGGER.log(Level.SEVERE, "Failed to create POLYSWARM_VERDICT custom artifact type", ex);
        }
    }
    
    public void reset() {
        try {
            // close all connections to the REST API and db.
            dbInstance.shutdownConnections();
            shutDownDBExecutor();
            dbExecutor = getNewDBExecutor();
        } catch (SwarmItDbException ex) {
            LOGGER.log(Level.SEVERE, "Failed to shutdown database connections.", ex); // NON-NLS
        }
    }
    
    synchronized private void shutDownDBExecutor() {
        if (dbExecutor != null) {
            dbExecutor.shutdownNow();
            try {
                dbExecutor.awaitTermination(30, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                LOGGER.log(Level.WARNING, "SwarmIt failed to shutdown DB Task Executor in a timely fashion.", ex);
            }
        }
    }

    private static ListeningScheduledExecutorService getNewDBExecutor() {
        return MoreExecutors.listeningDecorator(Executors.newSingleThreadScheduledExecutor(
                new ThreadFactoryBuilder().setNameFormat("SwarmIt-DB-Worker-Thread-%d").build()));
    }

    /**
     * Abstract base class for tasks
     */
    @NbBundle.Messages({"SwarmItController.InnerTask.progress.name=progress",
        "SwarmItController.InnerTask.message.name=status"})
    static public abstract class BackgroundTask implements Runnable, Cancellable {

        private final SimpleObjectProperty<Worker.State> state = new SimpleObjectProperty<>(Worker.State.READY);
        private final SimpleDoubleProperty progress = new SimpleDoubleProperty(this, Bundle.SwarmItController_InnerTask_progress_name());
        private final SimpleStringProperty message = new SimpleStringProperty(this, Bundle.SwarmItController_InnerTask_message_name());

        protected BackgroundTask() {
        }

        public double getProgress() {
            return progress.get();
        }

        public final void updateProgress(Double workDone) {
            this.progress.set(workDone);
        }

        public String getMessage() {
            return message.get();
        }

        public final void updateMessage(String Status) {
            this.message.set(Status);
        }

        public SimpleDoubleProperty progressProperty() {
            return progress;
        }

        public SimpleStringProperty messageProperty() {
            return message;
        }

        public Worker.State getState() {
            return state.get();
        }

        public ReadOnlyObjectProperty<Worker.State> stateProperty() {
            return new ReadOnlyObjectWrapper<>(state.get());
        }

        @Override
        public synchronized boolean cancel() {
            updateState(Worker.State.CANCELLED);
            return true;
        }

        protected void updateState(Worker.State newState) {
            state.set(newState);
        }

        protected synchronized boolean isCancelled() {
            return getState() == Worker.State.CANCELLED;
        }
    }

    public static class ResolvePendingSubmissionsTask extends BackgroundTask {
        private final SwarmItDb dbInstance;
        private final Case autopsyCase;

        private ProgressHandle progressHandle;

        public SwarmItDb getDbInstance() {
            return dbInstance;
        }

        public Case getAutopsyCase() {
            return autopsyCase;
        }

        ResolvePendingSubmissionsTask(SwarmItDb dbInstance, Case autopsyCase) {
            super();
            this.dbInstance = dbInstance;
            this.autopsyCase = autopsyCase;
        }

        @Override
        public void run() {
            progressHandle = getInitialProgressHandle();
            progressHandle.start();

            try {
                // check pending submissions db for any entries
                List<SwarmItPendingSubmission> pList = getDbInstance().getPendingSubmissions();
                
                if (pList.isEmpty()) {
                    LOGGER.log(Level.INFO, "No pending submissions found.");
                    return;
                }
                LOGGER.log(Level.INFO, "Found {0} pending submissions. Starting lookup...", pList.size());
                progressHandle.switchToDeterminate(pList.size());
                updateProgress(0.0);
                int workDone = 0;

                // for each pending submission entry, contact API to get status info
                for (SwarmItPendingSubmission pSub : pList) {
                    JSONObject statusResult = SwarmItApiClient.getSubmissionStatus(pSub.getSubmissionUUID());
                    SwarmItVerdictEnum verdict = SwarmItApiClient.getVerdict(statusResult);
                    
                    if (verdict != SwarmItVerdictEnum.UNKNOWN) {

                        try {
                            // do lookup for abstractFile ID in the current case
                            AbstractFile af = autopsyCase.getSleuthkitCase().getAbstractFileById(pSub.getAbstractFileID());

                            // add PolySwarm custom artifact to abstractFile
                            BlackboardArtifact artifact = af.newArtifact(autopsyCase.getSleuthkitCase()
                                    .getArtifactType(POLYSWARM_ARTIFACT_TYPE_NAME).getTypeID());
                            
                            // add attribute to the artifact to store the verdict
                            if (verdict == SwarmItVerdictEnum.MALICIOUS) {                                
                                artifact.addAttribute(new BlackboardAttribute(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_COMMENT,
                                        SwarmItModule.getModuleName(),
                                        POLYSWARM_ARTIFACT_ATTRIBUTE_MALICIOUS));
                                // if accumulated verdict is malicious, set file to known bad
                                af.setKnown(TskData.FileKnown.BAD);
                            } else {
                                artifact.addAttribute(new BlackboardAttribute(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_COMMENT,
                                        SwarmItModule.getModuleName(),
                                        POLYSWARM_ARTIFACT_ATTRIBUTE_NONMALICIOUS));
                            }
                        
                            // notify UI to update and display this result
                            IngestServices.getInstance().fireModuleDataEvent(new ModuleDataEvent(SwarmItModule.getModuleName(), 
                                    autopsyCase.getSleuthkitCase().getArtifactType(POLYSWARM_ARTIFACT_TYPE_NAME)));

                        } catch (TskCoreException ex) {
                            LOGGER.log(Level.SEVERE, "Failed to get abstractFile from current case", ex);
                        }
                        
                        // delete entry from pending results db
                        getDbInstance().deletePendingSubmission(pSub);
                        
                    }
                    workDone++;
                    progressHandle.progress(pSub.getSubmissionUUID(), workDone);
                    updateProgress(workDone - 1 / (double) pList.size());
                    updateMessage(pSub.getSubmissionUUID());
                }
                LOGGER.log(Level.INFO, "Completed processing pending submissions.");
                
            } catch (SwarmItDbException ex) {
                LOGGER.log(Level.SEVERE, "Failed to get list of pending submissions from db.",ex);
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Failed to get status for submission.", ex);
            } finally {
                progressHandle.finish();

            }
        }
        
        @NbBundle.Messages({"ResolvePendingSubmissionsTask.populatingDb.status=checking verdict of submitted artifacts.",})
        ProgressHandle getInitialProgressHandle() {
            return ProgressHandle.createHandle(Bundle.ResolvePendingSubmissionsTask_populatingDb_status(), this);
        }
    }
}
