/*
 * The MIT License
 *
 * Copyright 2018 PolySwarm PTE. LTD.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.polyswarm.swarmit;

import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.polyswarm.swarmit.datamodel.SwarmItDb;
import io.polyswarm.swarmit.datamodel.SwarmItDbException;
import io.polyswarm.swarmit.optionspanel.SwarmItMarketplaceSettings;
import io.polyswarm.swarmit.tasks.ProcessPendingTask;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.sleuthkit.autopsy.casemodule.Case;
import org.sleuthkit.datamodel.BlackboardAttribute;
import org.sleuthkit.datamodel.BlackboardAttribute.TSK_BLACKBOARD_ATTRIBUTE_VALUE_TYPE;
import org.sleuthkit.datamodel.TskCoreException;
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
    public static final String POLYSWARM_ARTIFACT_TYPE_NAME = "POLYSWARM_RESULTS";
    public static final String POLYSWARM_ARTIFACT_TYPE_DISPLAY_NAME = "PolySwarm Results";
    public static final String POLYSWARM_ARTIFACT_TYPE_ASSERTIONS_NAME = "POLYSWARM_ASSERTIONS";
    public static final String POLYSWARM_ARTIFACT_TYPE_ASSERTIONS_DISPLAY_NAME = "PolySwarm Assertions";
    public static final String POLYSWARM_ARTIFACT_TYPE_TAGS_NAME = "POLYSWARM_TAGS";
    public static final String POLYSWARM_ARTIFACT_TYPE_TAGS_DISPLAY_NAME = "PolySwarm Tags";
    public static final String POLYSWARM_ARTIFACT_TYPE_MALWARE_FAMILIES_NAME = "POLYSWARM_MALWARE_FAMILIES";
    public static final String POLYSWARM_ARTIFACT_TYPE_MALWARE_FAMILIES_DISPLAY_NAME = "PolySwarm Malware Families";
    
    public static final String POLYSWARM_ARTIFACT_ATTRIBUTE_MALICIOUS_DETECTIONS_NAME = "POLYSWARM_MALICIOUS_DETECTIONS";
    public static final String POLYSWARM_ARTIFACT_ATTRIBUTE_MALICIOUS_DETECTIONS_DISPLAY = "Malicious Detections";
    public static final String POLYSWARM_ARTIFACT_ATTRIBUTE_BENIGN_DETECTIONS_NAME = "POLYSWARM_BENIGN_DETECTIONS";
    public static final String POLYSWARM_ARTIFACT_ATTRIBUTE_BENIGN_DETECTIONS_DISPLAY = "Benign Detections";
    public static final String POLYSWARM_ARTIFACT_ATTRIBUTE_TOTAL_DETECTIONS_NAME = "POLYSWARM_TOTAL_DETECTIONS";
    public static final String POLYSWARM_ARTIFACT_ATTRIBUTE_TOTAL_DETECTIONS_DISPLAY = "Total Detections";
    public static final String POLYSWARM_ARTIFACT_ATTRIBUTE_MALWARE_FAMILY_NAME = "POLYSWARM_MALWARE_FAMILY";
    public static final String POLYSWARM_ARTIFACT_ATTRIBUTE_MALWARE_FAMILY_DISPLAY = "Malware Family";
    public static final String POLYSWARM_ARTIFACT_ATTRIBUTE_POLYSCORE_NAME = "POLYSWARM_POLYSCORE_STRING";
    public static final String POLYSWARM_ARTIFACT_ATTRIBUTE_POLYSCORE_DISPLAY = "PolyScore\u2122";
    public static final String POLYSWARM_ARTIFACT_ATTRIBUTE_TAG_NAME = "POLYSWARM_TAG";
    public static final String POLYSWARM_ARTIFACT_ATTRIBUTE_TAG_DISPLAY = "Tag";
    
    public static final String POLYSWARM_ARTIFACT_ATTRIBUTE_ASSERTION_NAME_FORMAT = "POLYSWARM_%s";
    
    public Case getAutopsyCase() {
        return autopsyCase;
    }

    SwarmItController(Case newCase) throws TskCoreException, SwarmItDbException {
        this.autopsyCase = Objects.requireNonNull(newCase);
        this.apiSettings = new SwarmItMarketplaceSettings();
        this.dbInstance = SwarmItDb.getInstance();

        dbExecutor = getNewDBExecutor();
        createCustomArtifactTypes(this.autopsyCase);
        createCustomArtifactAttributes(this.autopsyCase);
        dbExecutor.scheduleAtFixedRate(new ProcessPendingTask(this.dbInstance, this.autopsyCase), 0, 2, TimeUnit.SECONDS);
    }
    
    private static void createCustomArtifactTypes(Case autopsyCase) {
        createCustomArtifactType(autopsyCase, POLYSWARM_ARTIFACT_TYPE_NAME, POLYSWARM_ARTIFACT_TYPE_DISPLAY_NAME);
        createCustomArtifactType(autopsyCase, POLYSWARM_ARTIFACT_TYPE_TAGS_NAME, POLYSWARM_ARTIFACT_TYPE_TAGS_DISPLAY_NAME);
        createCustomArtifactType(autopsyCase, POLYSWARM_ARTIFACT_TYPE_MALWARE_FAMILIES_NAME, POLYSWARM_ARTIFACT_TYPE_MALWARE_FAMILIES_DISPLAY_NAME);
    }

    /**
     * Create the POLYSWARM_VERDICT custom artifact type and add it to the blackboard.
     *
     * @param autopsyCase Open Case
     */
    private static void createCustomArtifactType(Case autopsyCase, String name, String display) {
        try {
            if (autopsyCase.getSleuthkitCase().getArtifactType(name) == null) {
                LOGGER.log(Level.INFO, "Adding POLYSWARM_VERDICT custom artifact type");
                autopsyCase.getSleuthkitCase().addBlackboardArtifactType(name, display);
            }
        } catch (TskCoreException | TskDataException ex) {
            LOGGER.log(Level.SEVERE, "Failed to create POLYSWARM_VERDICT custom artifact type", ex);
        }
    }
    
    private static void createCustomArtifactAttributes(Case autopsyCase) {
            createCustomArtifactAttribute(autopsyCase, POLYSWARM_ARTIFACT_ATTRIBUTE_MALICIOUS_DETECTIONS_NAME, BlackboardAttribute.TSK_BLACKBOARD_ATTRIBUTE_VALUE_TYPE.INTEGER, POLYSWARM_ARTIFACT_ATTRIBUTE_MALICIOUS_DETECTIONS_DISPLAY);
            createCustomArtifactAttribute(autopsyCase, POLYSWARM_ARTIFACT_ATTRIBUTE_BENIGN_DETECTIONS_NAME, BlackboardAttribute.TSK_BLACKBOARD_ATTRIBUTE_VALUE_TYPE.INTEGER, POLYSWARM_ARTIFACT_ATTRIBUTE_BENIGN_DETECTIONS_DISPLAY);
            createCustomArtifactAttribute(autopsyCase, POLYSWARM_ARTIFACT_ATTRIBUTE_TOTAL_DETECTIONS_NAME, BlackboardAttribute.TSK_BLACKBOARD_ATTRIBUTE_VALUE_TYPE.INTEGER, POLYSWARM_ARTIFACT_ATTRIBUTE_TOTAL_DETECTIONS_DISPLAY);
            createCustomArtifactAttribute(autopsyCase, POLYSWARM_ARTIFACT_ATTRIBUTE_POLYSCORE_NAME, BlackboardAttribute.TSK_BLACKBOARD_ATTRIBUTE_VALUE_TYPE.STRING, POLYSWARM_ARTIFACT_ATTRIBUTE_POLYSCORE_DISPLAY);
            createCustomArtifactAttribute(autopsyCase, POLYSWARM_ARTIFACT_ATTRIBUTE_MALWARE_FAMILY_NAME, BlackboardAttribute.TSK_BLACKBOARD_ATTRIBUTE_VALUE_TYPE.STRING, POLYSWARM_ARTIFACT_ATTRIBUTE_MALWARE_FAMILY_DISPLAY);
            createCustomArtifactAttribute(autopsyCase, POLYSWARM_ARTIFACT_ATTRIBUTE_TAG_NAME, BlackboardAttribute.TSK_BLACKBOARD_ATTRIBUTE_VALUE_TYPE.STRING, POLYSWARM_ARTIFACT_ATTRIBUTE_TAG_DISPLAY);
    }
    
    public static void createCustomArtifactAttribute(Case autopsyCase, String name, TSK_BLACKBOARD_ATTRIBUTE_VALUE_TYPE valueType, String display) {
        try {
            if (autopsyCase.getSleuthkitCase().getAttributeType(name) == null) {
                autopsyCase.getSleuthkitCase().addArtifactAttributeType(name, valueType, display);
            }
        }  catch (TskCoreException | TskDataException ex) {
            LOGGER.log(Level.SEVERE, "Failed to create custom artifact attribute type", ex);
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
}
