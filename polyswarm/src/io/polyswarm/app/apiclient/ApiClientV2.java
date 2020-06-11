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
package io.polyswarm.app.apiclient;

import io.polyswarm.app.apiclient.v2.requests.HashSearchRequest;
import io.polyswarm.app.apiclient.v2.requests.utils.ArtifactInstance;
import io.polyswarm.app.apiclient.v2.requests.ArtifactSubmissionStatusRequest;
import io.polyswarm.app.apiclient.v2.requests.RescanRequest;
import io.polyswarm.app.apiclient.v2.requests.SubmitArtifactRequest;
import io.polyswarm.app.apiclient.v2.requests.TagRequest;
import io.polyswarm.app.apiclient.v2.requests.TestRequest;
import io.polyswarm.app.apiclient.v2.requests.TestResponse;
import io.polyswarm.app.apiclient.v2.requests.utils.Tag;
import io.polyswarm.app.optionspanel.PolySwarmMarketplaceSettings;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.sleuthkit.datamodel.AbstractFile;

/**
 * Make requests to PolySwarm API and manage responses and parse responses.
 */
public class ApiClientV2 {

    private final static Logger LOGGER = Logger.getLogger(ApiClientV2.class.getName());

    /**
     * Make this all static methods.
     */
    private ApiClientV2() {
    }

    /**
     * Submit file content to PolySwarm to scan API endpoint.
     *
     * @param abstractFile AbstractFile to scan
     * @return ArtifactInstance
     *
     * @throws IOException, BadRequestException, RateLimitException, NotAuthorizedException, NotFoundException
     */
    public static ArtifactInstance submitFile(AbstractFile abstractFile) throws IOException, BadRequestException, RateLimitException, NotAuthorizedException, NotFoundException, ServerException {
        try {
            return new SubmitArtifactRequest(abstractFile).makeRequest();
        } catch (URISyntaxException ex) {
            LOGGER.log(Level.SEVERE, "Invalid API URI.", ex);
            throw new IOException(ex);
        } catch (JSONException ex) {
            LOGGER.log(Level.SEVERE, "Invalid API Response.", ex);
            throw new IOException(ex);
        }
    }

    /**
     * Rescan a file in PolySwarm by sha256 hash
     *
     *
     * @param sha256Hash sha256 hash of the file to rescan
     * @return ArtifactInstance
     *
     * @throws IOException, BadRequestException, RateLimitException, NotAuthorizedException, NotFoundException
     */
    public static ArtifactInstance rescanFile(String sha256Hash) throws IOException, BadRequestException, RateLimitException, NotAuthorizedException, NotFoundException, ServerException {
        try {
            return new RescanRequest(sha256Hash).makeRequest();
        } catch (URISyntaxException ex) {
            LOGGER.log(Level.SEVERE, "Invalid API URI.", ex);
            throw new IOException(ex);
        } catch (JSONException ex) {
            LOGGER.log(Level.SEVERE, "Invalid API Response.", ex);
            throw new IOException(ex);
        }
    }

    /**
     * Check PolySwarm for results on a given md5 hash
     *
     * @param md5Hash String with the hash
     * @return ArtifactInstance object
     *
     * @throws IOException, BadRequestException, RateLimitException, NotAuthorizedException, NotFoundException
     */
    public static ArtifactInstance searchHash(String md5Hash) throws IOException, BadRequestException, RateLimitException, NotAuthorizedException, NotFoundException, ServerException {
        try {
            return new HashSearchRequest(md5Hash).makeRequest();
        } catch (URISyntaxException ex) {
            LOGGER.log(Level.SEVERE, "Invalid API URI.", ex);
            throw new IOException(ex);
        } catch (JSONException ex) {
            LOGGER.log(Level.SEVERE, "Invalid API Response.", ex);
            throw new IOException(ex);
        }
    }

    /**
     * Check the submission status for a submission with the given id
     *
     * @param submissionId ID of the submission to check
     * @return ArtifactInstance
     *
     * @throws IOException, BadRequestException, RateLimitException, NotAuthorizedException, NotFoundException
     */
    public static ArtifactInstance getSubmissionStatus(String submissionId) throws IOException, BadRequestException, RateLimitException, NotAuthorizedException, NotFoundException, ServerException {
        try {
            return new ArtifactSubmissionStatusRequest(submissionId).makeRequest();
        } catch (URISyntaxException ex) {
            LOGGER.log(Level.SEVERE, "Invalid API URI.", ex);
            throw new IOException(ex);
        } catch (JSONException ex) {
            LOGGER.log(Level.SEVERE, "Invalid API Response.", ex);
            throw new IOException(ex);
        }
    }

    /**
     * Get a list of Tags for a file
     *
     * @param artifactInstance ArtifactInstance to get tags
     * @return List of Tag objects
     *
     * @throws IOException, BadRequestException, RateLimitException
     */
    public static List<Tag> getTags(ArtifactInstance artifactInstance) throws IOException, BadRequestException, RateLimitException, ServerException {
        try {
            return new TagRequest(artifactInstance).makeRequest();
        } catch (URISyntaxException ex) {
            LOGGER.log(Level.SEVERE, "Invalid API URI.", ex);
            throw new IOException(ex);
        } catch (JSONException ex) {
            LOGGER.log(Level.SEVERE, "Invalid API Response.", ex);
            throw new IOException(ex);
        } catch (NotFoundException | NotAuthorizedException ex) {
            // Not found is fine, just empty list
            return new ArrayList<>();
        }
    }

    /**
     * Do a GET request for the client API status.
     *
     * @param apiSettings - Instance of the API settings
     * @return true if connection successful, else false
     */
    public static TestResponse testConnection(PolySwarmMarketplaceSettings apiSettings) {

        try {
            return new TestRequest().makeRequest();
        } catch (URISyntaxException ex) {
            LOGGER.log(Level.SEVERE, "Invalid API URI.", ex);
        } catch (NotAuthorizedException ex) {
            LOGGER.log(Level.SEVERE, "Invalid API Key.", ex);
        } catch (BadRequestException ex) {
            LOGGER.log(Level.SEVERE, "Invalid UUID.", ex);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Error testing connection", ex);
        }
        return new TestResponse(false);
    }
}
