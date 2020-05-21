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
package io.polyswarm.swarmit.datamodel;

import io.polyswarm.swarmit.apiclient.ApiClientV2;
import io.polyswarm.swarmit.apiclient.BadRequestException;
import io.polyswarm.swarmit.apiclient.NotAuthorizedException;
import io.polyswarm.swarmit.apiclient.NotFoundException;
import io.polyswarm.swarmit.apiclient.RateLimitException;
import io.polyswarm.swarmit.apiclient.v2.requests.utils.ArtifactInstance;
import io.polyswarm.swarmit.apiclient.v2.requests.utils.Tag;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.sleuthkit.autopsy.casemodule.Case;
import org.sleuthkit.datamodel.AbstractFile;
import org.sleuthkit.datamodel.TskCoreException;

/**
 * Class to hold details about a pending submission
 */
public class PendingSubmission extends PendingTask {
    private static final Logger LOGGER = Logger.getLogger(PendingSubmission.class.getName());
    private final String submissionId;
    private final Long abstractFileID;
    
    PendingSubmission(Long abstractFileID, String uuid) {
        this.abstractFileID = abstractFileID;
        this.submissionId = uuid;
    }

    /**
     * @return the submissionId
     */
    public String getSubmissionId() {
        return submissionId;
    }

    /**
     * @return the abstractFileID
     */
    public Long getAbstractFileId() {
        return abstractFileID;
    }
    
    public void submitFile(Case autopsyCase) throws SwarmItDbException, NotAuthorizedException, BadRequestException, NotFoundException, RateLimitException, IOException, TskCoreException {
        AbstractFile abstractFile = autopsyCase.getSleuthkitCase().getAbstractFileById(abstractFileID);
        ArtifactInstance artifactInstance = ApiClientV2.submitFile(abstractFile);
        getDbInstance().newPendingSubmissionId(abstractFileID, artifactInstance.id);
    }

    public void checkSubmission(Case autopsyCase) throws SwarmItDbException, NotAuthorizedException, BadRequestException, NotFoundException, RateLimitException, IOException, TskCoreException {
        LOGGER.log(Level.FINE, "Checking Submission {0}", abstractFileID);
        ArtifactInstance artifactInstance = ApiClientV2.getSubmissionStatus(submissionId);
        LOGGER.log(Level.FINE, "Got response{0}", artifactInstance.toString());
        if (!artifactInstance.windowClosed) {
            // Exit if not done
            return;
        }
        
        List<Tag> tags;
        try {
            tags = ApiClientV2.getTags(artifactInstance);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Failed to read tags from PolySwarm", ex);
            tags = new ArrayList<>();
        }
                
        try{
            updateBlackboard(autopsyCase, abstractFileID, artifactInstance, tags);
        } finally {
            getDbInstance().deletePendingSubmission(this);
        }
    }
    
    @Override
    public boolean process(Case autopsyCase) throws SwarmItDbException, NotAuthorizedException, BadRequestException, RateLimitException, IOException, TskCoreException {
        if (submissionId.isEmpty()) {
            try {
                submitFile(autopsyCase);
            } catch (NotAuthorizedException ex) {
                LOGGER.log(Level.SEVERE, "Invalid API Key.", ex);
            } catch (NotFoundException ex) {
                LOGGER.log(Level.SEVERE, "Error fetching submission results: Not found.", ex);
                return false;
            }
            return false;
        // Check results of files with submission ID
        } else {
            try {
                checkSubmission(autopsyCase);
            } catch (NotFoundException ex) {
                LOGGER.log(Level.SEVERE, "Error fetching submission results: Not found.", ex);
                return false;
            }
            return true;
        }
    }
    
    @Override
    public String toString() {
        return String.format("PendingSubmission(abstractFileID: {0}, submission_uuid:{1})", getAbstractFileId().toString(), getSubmissionId()); 
    }
}
