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

import io.polyswarm.app.apiclient.ApiClientV2;
import io.polyswarm.app.apiclient.BadRequestException;
import io.polyswarm.app.apiclient.NotAuthorizedException;
import io.polyswarm.app.apiclient.NotFoundException;
import io.polyswarm.app.apiclient.RateLimitException;
import io.polyswarm.app.apiclient.ServerException;
import io.polyswarm.app.apiclient.v2.requests.utils.ArtifactInstance;
import io.polyswarm.app.apiclient.v2.requests.utils.Tag;
import io.polyswarm.app.datamodel.PolySwarmDbException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.sleuthkit.autopsy.casemodule.Case;
import org.sleuthkit.datamodel.TskCoreException;

/**
 * Pending task to submit a file & get results in ProcessPendingTask background task
 */
public class PendingRescan extends PendingTask {

    private static final Logger LOGGER = Logger.getLogger(PendingRescan.class.getName());
    private final String rescanId;
    private final String sha256Hash;
    private final Long abstractFileID;
    private final Boolean cancelled;

    public PendingRescan(Long abstractFileID, String sha256Hash, String uuid, Boolean cancelled) {
        this.abstractFileID = abstractFileID;
        this.sha256Hash = sha256Hash;
        this.rescanId = uuid;
        this.cancelled = cancelled;
    }

    /**
     * @return the submissionId
     */
    public String getRescanId() {
        return rescanId;
    }

    public String getSha256Hash() {
        return sha256Hash;
    }

    /**
     * @return the abstractFileID
     */
    public Long getAbstractFileId() {
        return abstractFileID;
    }

    @Override
    public boolean process(Case autopsyCase) throws PolySwarmDbException, BadRequestException, RateLimitException, IOException, TskCoreException {
        if (cancelled) {
            removeFromDB();
            return true;
        } else if (rescanId.isEmpty()) {
            try {
                submitRescan();
                return false;
            } catch (IOException ex) {
                removeFromDB();
                throw ex;
            }
        } else {
            try {
                return checkSubmission(autopsyCase);
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Error checking rescan in PolySwarm.");
                removeFromDB();
                throw ex;
            }
        }
    }

    /**
     * Uploads the file to PolySwarm, and updates the task with the submissionId
     *
     * @param autopsyCase open case
     */
    public void submitRescan() throws PolySwarmDbException, NotAuthorizedException, BadRequestException, NotFoundException, RateLimitException, IOException, TskCoreException {
        ArtifactInstance artifactInstance = ApiClientV2.rescanFile(sha256Hash);
        getDbInstance().updatePendingRescanId(abstractFileID, artifactInstance.id);
    }

    /**
     *
     * Checks to see if a scan has finished. Fills in the ArtifactInstance if so. Also, deletes the task on successful
     * scan completion.
     *
     * @param autopsyCase open case
     */
    public boolean checkSubmission(Case autopsyCase) throws PolySwarmDbException, NotAuthorizedException, BadRequestException, NotFoundException, RateLimitException, IOException, TskCoreException {
        LOGGER.log(Level.FINE, "Checking Rescan {0}", abstractFileID);
        ArtifactInstance artifactInstance = ApiClientV2.getSubmissionStatus(rescanId);
        LOGGER.log(Level.FINE, "Got response{0}", artifactInstance.toString());
        if (!artifactInstance.windowClosed) {
            // Exit if not done
            return false;
        }

        List<Tag> tags;
        try {
            tags = ApiClientV2.getTags(artifactInstance);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Failed to read tags from PolySwarm", ex);
            tags = new ArrayList<>();
        }

        try {
            updateBlackboard(autopsyCase, abstractFileID, artifactInstance, tags);
        } finally {
            removeFromDB();
        }
        return true;
    }

    @Override
    public boolean cancel() {
        try {
            getDbInstance().cancelPendingRescan(this);
            return true;
        } catch (PolySwarmDbException e) {
            LOGGER.log(Level.SEVERE, "Error cancelling Pending Rescan");
            return false;
        }
    }

    private void removeFromDB() throws PolySwarmDbException {
        getDbInstance().deletePendingRescan(this);
    }

    @Override
    public String getHumanReadableName() {
        return "Rescan";
    }

    @Override
    public String toString() {
        return String.format("PendingRescan(abstractFileID: %s, sha256Hash: %s rescanUuid: %s)", getAbstractFileId().toString(), getSha256Hash(), getRescanId());
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof PendingRescan) {
            PendingRescan otherRescan = (PendingRescan) other;
            return otherRescan.abstractFileID.equals(abstractFileID) && otherRescan.sha256Hash.equals(sha256Hash);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + Objects.hashCode(this.sha256Hash);
        hash = 59 * hash + Objects.hashCode(this.abstractFileID);
        return hash;
    }
}
