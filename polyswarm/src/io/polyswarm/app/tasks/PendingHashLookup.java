/*
 * The MIT License
 *
 * Copyright 2020 PolySwarm PTE. LTD.
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
 * Pending task to do a hash search in the ProcessPendingTask background task.
 */
public class PendingHashLookup extends PendingTask {

    private static final Logger LOGGER = Logger.getLogger(PendingHashLookup.class.getName());

    private final String md5Hash;
    private final Long abstractFileId;
    private final Boolean cancelled;

    public PendingHashLookup(long abstractFileId, String md5Hash, Boolean cancelled) {
        this.abstractFileId = abstractFileId;
        this.md5Hash = md5Hash;
        this.cancelled = cancelled;
    }

    public String getMd5Hash() {
        return md5Hash;
    }

    public Long getAbstractFileId() {
        return abstractFileId;
    }

    @Override
    public boolean process(Case autopsyCase) throws PolySwarmDbException, NotAuthorizedException, BadRequestException, NotFoundException, RateLimitException, IOException, TskCoreException {
        if (cancelled) {
            removeFromDB();
            return true;
        }

        return lookupHash(autopsyCase);
    }

    /**
     * Makes the hash search request on PolySwarm and updates the blackboard with results, either the ArtifactInstance,
     * or the Not Found message
     *
     * @param autopsyCase open case
     */
    public boolean lookupHash(Case autopsyCase) throws PolySwarmDbException, NotAuthorizedException, BadRequestException, RateLimitException, IOException, TskCoreException {
        LOGGER.log(Level.FINE, "Looking up Hash {0}", md5Hash);
        try {
            ArtifactInstance artifactInstance = ApiClientV2.searchHash(md5Hash);

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

            updateBlackboard(autopsyCase, abstractFileId, artifactInstance, tags);
        } catch (NotFoundException ex) {
            updateNotFound(autopsyCase, abstractFileId);
        } finally {
            removeFromDB();
        }
        return true;
    }

    @Override
    public boolean cancel() {
        try {
            getDbInstance().cancelPendingHashLookup(this);
            return true;
        } catch (PolySwarmDbException e) {
            LOGGER.log(Level.SEVERE, "Error cancelling Pending Rescan");
            return false;
        }
    }

    private void removeFromDB() throws PolySwarmDbException {
        getDbInstance().deletePendingHashLookup(this);
    }

    @Override
    public String getHumanReadableName() {
        return "Hash Lookup";
    }

    @Override
    public String toString() {
        return String.format("PendingHashLookup(abstractFileID: %s, md5: %s)", abstractFileId, md5Hash);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof PendingHashLookup) {
            PendingHashLookup otherHashLookup = (PendingHashLookup) other;
            return otherHashLookup.md5Hash.equals(md5Hash) && otherHashLookup.abstractFileId.equals(abstractFileId);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + Objects.hashCode(this.md5Hash);
        hash = 59 * hash + Objects.hashCode(this.abstractFileId);
        return hash;
    }
}
