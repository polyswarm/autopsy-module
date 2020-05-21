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
 *
 * @author rl
 */
public class PendingHashLookup extends PendingTask {
    private static final Logger LOGGER = Logger.getLogger(PendingHashLookup.class.getName());
    public final String md5Hash;
    public final long abstractFileId;
    
    public PendingHashLookup(long abstractFileId, String md5Hash) {
        this.abstractFileId = abstractFileId;
        this.md5Hash = md5Hash;        
    }
    
    public PendingHashLookup(AbstractFile abstractFile) {
        abstractFileId = abstractFile.getId();
        md5Hash = abstractFile.getMd5Hash();
        
    }
    
    public void lookupHash(Case autopsyCase) throws SwarmItDbException, NotAuthorizedException, BadRequestException, RateLimitException, IOException, TskCoreException {
        LOGGER.log(Level.FINE, "Looking up Hash {0}", md5Hash);
        try {
            ArtifactInstance artifactInstance = ApiClientV2.searchHash(md5Hash);
        
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
            
            updateBlackboard(autopsyCase, abstractFileId, artifactInstance, tags);
        } catch (NotAuthorizedException ex) {
            LOGGER.log(Level.SEVERE, "Invalid API Key.", ex);
        } catch (NotFoundException ex) {
            updateNotFound(autopsyCase, abstractFileId);
        } finally {
            getDbInstance().deletePendingHashLookup(this);
        }
    }
    
    @Override
    public boolean process(Case autopsyCase) throws SwarmItDbException, NotAuthorizedException, BadRequestException, NotFoundException, RateLimitException, IOException, TskCoreException {
        lookupHash(autopsyCase);
        return true;
    }
    
    @Override
    public String toString() {
        return String.format("PendingSubmission(abstractFileID: {0}, submission_uuid:{1})", abstractFileId, md5Hash); 
    }
}
