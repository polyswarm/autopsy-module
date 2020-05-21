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
package io.polyswarm.swarmit.tasks;

import io.polyswarm.swarmit.apiclient.BadRequestException;
import io.polyswarm.swarmit.apiclient.NotAuthorizedException;
import io.polyswarm.swarmit.apiclient.RateLimitException;
import io.polyswarm.swarmit.datamodel.PendingTask;
import io.polyswarm.swarmit.datamodel.SwarmItDb;
import io.polyswarm.swarmit.datamodel.SwarmItDbException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.util.NbBundle;
import org.sleuthkit.autopsy.casemodule.Case;
import org.sleuthkit.datamodel.TskCoreException;


/**
 * Processes all Tasks in a background thread so the UI is not blocked during file & network IO.
 *
 */
public class ProcessPendingTask extends BackgroundTask {
    private static final Logger LOGGER = Logger.getLogger(ProcessPendingTask.class.getName());
    private final SwarmItDb dbInstance;
    private final Case autopsyCase;

    private ProgressHandle progressHandle;

    public SwarmItDb getDbInstance() {
        return dbInstance;
    }

    public Case getAutopsyCase() {
        return autopsyCase;
    }

    public ProcessPendingTask(SwarmItDb dbInstance, Case autopsyCase) {
        super();
        this.dbInstance = dbInstance;
        this.autopsyCase = autopsyCase;
    }

    @Override
    public void run() {
        progressHandle = getInitialProgressHandle();
        progressHandle.start();

        try {
            // check db for any pending tasks
            List<PendingTask> pendingList = new ArrayList<>();
            pendingList.addAll(getDbInstance().getPendingHashLookups());
            pendingList.addAll(getDbInstance().getPendingSubmissions());

            if (pendingList.isEmpty()) {
                LOGGER.log(Level.INFO, "No pending submissions found.");
                return;
            }
            LOGGER.log(Level.INFO, "Found {0} pending tasks. Starting processing...", pendingList.size());
            progressHandle.switchToDeterminate(pendingList.size());
            updateProgress(0.0);
            int workDone = 0;

            // for each pending submission entry, contact API to get status info
            for (PendingTask pendingTask : pendingList) {
                boolean success = false;
                try {
                    // allow the task to do all the work
                    success = pendingTask.process(getAutopsyCase());
                } catch (NotAuthorizedException ex) {
                    LOGGER.log(Level.SEVERE, "Invalid API Key");
                } catch (RateLimitException ex) {
                    LOGGER.log(Level.SEVERE, "Exeeded rate limits, you need to purchase a larger package, or wait a moment before trying again.", ex);
                } catch (BadRequestException ex) {
                    LOGGER.log(Level.SEVERE, "Bad Request", ex);
                } catch (SwarmItDbException ex) {
                    LOGGER.log(Level.SEVERE, "Failed to update pending task in db.",ex);
                } catch (TskCoreException ex) {
                    LOGGER.log(Level.SEVERE, "Failed to get abstractFile from current case", ex);
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, "Failed to make request to PolySwarm", ex);
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Unexpected exception while processing task", ex);
                }
                if (success) {
                    workDone++;
                    progressHandle.progress(pendingTask.toString(), workDone);
                    updateProgress(workDone - 1 / (double) pendingList.size());
                    updateMessage(pendingTask.toString());
                }
            }

            LOGGER.log(Level.INFO, "Completed processing pending tasks.");

        } catch (SwarmItDbException ex) {
            LOGGER.log(Level.SEVERE, "Failed to get list of pending tasks from db.",ex);
        } finally {
            progressHandle.finish();
        }
    }

    @NbBundle.Messages({"ProcessPendingTask.populatingDb.status=Processing Requests to PolySwarm.",})
    ProgressHandle getInitialProgressHandle() {
        return ProgressHandle.createHandle(Bundle.ProcessPendingTask_populatingDb_status(), this);
    }
}
