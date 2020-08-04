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
package io.polyswarm.app.tasks;

import io.polyswarm.app.apiclient.BadRequestException;
import io.polyswarm.app.apiclient.RateLimitException;
import io.polyswarm.app.datamodel.PolySwarmDb;
import io.polyswarm.app.datamodel.PolySwarmDbException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
    private final PolySwarmDb dbInstance;
    private final Case autopsyCase;
    private final HashMap<PendingTask, ProgressHandle> progressHandles;

    public ProcessPendingTask(PolySwarmDb dbInstance, Case autopsyCase) {
        super();
        this.dbInstance = dbInstance;
        this.autopsyCase = autopsyCase;
        progressHandles = new HashMap<>();
    }

    public PolySwarmDb getDbInstance() {
        return dbInstance;
    }

    public Case getAutopsyCase() {
        return autopsyCase;
    }

    @Override
    public void run() {
        try {
            // check db for any pending tasks
            PolySwarmDb db = getDbInstance();

            List<PendingTask> pendingList = new ArrayList<>();
            pendingList.addAll(db.getPendingHashLookups());
            pendingList.addAll(db.getPendingSubmissions());
            pendingList.addAll(db.getPendingRescans());
            LOGGER.log(Level.INFO, "Found {0} pending tasks. Starting processing...", pendingList.size());

            pendingList.forEach((pendingTask) -> {
                try {
                    if (!progressHandles.containsKey(pendingTask)) {
                        LOGGER.log(Level.INFO, "Creating a new progressbar for {0}", pendingTask);
                        ProgressHandle handle = pendingTask.getPendingTaskProgressHandle();
                        handle.start();
                        handle.switchToIndeterminate();
                        progressHandles.put(pendingTask, handle);
                    }
                    if (pendingTask.process(getAutopsyCase())) {
                        LOGGER.log(Level.INFO, "{0} finished", pendingTask);
                        progressHandles.get(pendingTask).finish();
                    }
                } catch (RateLimitException ex) {
                    LOGGER.log(Level.SEVERE, "Exeeded rate limits, you need to purchase a larger package, or wait a moment before trying again.");
                } catch (BadRequestException ex) {
                    LOGGER.log(Level.SEVERE, "Bad Request", ex);
                } catch (PolySwarmDbException ex) {
                    LOGGER.log(Level.SEVERE, "Failed to update pending task in db.", ex);
                } catch (TskCoreException ex) {
                    LOGGER.log(Level.SEVERE, "Failed to get abstractFile from current case", ex);
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, "Failed to make request to PolySwarm", ex);
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Unexpected exception while processing task", ex);
                }
            });
            LOGGER.log(Level.INFO, "Completed a pass on pending tasks.");

        } catch (PolySwarmDbException ex) {
            LOGGER.log(Level.SEVERE, "Failed to get list of pending tasks from db.", ex);
        }
    }

}
