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
package io.polyswarm.swarmit.apiclient;

import static io.polyswarm.swarmit.apiclient.SwarmItJsonHelper.getJSONObjectFromResult;
import static io.polyswarm.swarmit.apiclient.SwarmItJsonHelper.getUUIDFromResult;
import io.polyswarm.swarmit.optionspanel.SwarmItMarketplaceSettings;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sleuthkit.datamodel.AbstractFile;
import org.sleuthkit.datamodel.ReadContentInputStream;

/**
 * Make requests to PolySwarm API and manage responses and parse responses.
 */
public class SwarmItApiClient {
    private final static Logger LOGGER = Logger.getLogger(SwarmItApiClient.class.getName());

    /**
     * Make this all static methods.
     */
    private SwarmItApiClient() {
    }

    /**
     * Remove the file object from the abstractFile and submit it to the
     * API endpoint.
     *
     * @param abstractFile AbstractFile object that contains a file.
     * @return The submission uuid
     *
     * @throws IOException
     */
    public static String submitFile(AbstractFile abstractFile) throws IOException {

        CloseableHttpClient httpclient = HttpClients.createDefault();
        SwarmItMarketplaceSettings apiSettings = new SwarmItMarketplaceSettings();

        try {
            HttpPost httppost = new HttpPost(new URI(apiSettings.getApiUrl()));
            LOGGER.log(Level.INFO, "Submitting file with request {0}.", httppost.getRequestLine());
            InputStreamKnownSizeBody inputStreamBody = new InputStreamKnownSizeBody(
                    new ReadContentInputStream(abstractFile),
                    (int) abstractFile.getSize(),
                    ContentType.DEFAULT_BINARY,
                    abstractFile.getName());

            HttpEntity reqEntity = MultipartEntityBuilder.create()
                    .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                    .addPart("file", inputStreamBody)
                    .build();

            httppost.setEntity(reqEntity);

            ResponseHandler<String> responseHandler = new SwarmItApiResponseHandler();
            String responseBody = httpclient.execute(httppost, responseHandler);
            return getUUIDFromResult(responseBody);
        } catch (URISyntaxException ex) {
            LOGGER.log(Level.SEVERE, "Invalid API URI.", ex);
            throw new IOException(ex);
        } catch (JSONException ex) {
            LOGGER.log(Level.SEVERE, "Unable to parse response as JSON.", ex);
            throw new IOException(ex);
        } finally {
            httpclient.close();
        }
    }

    /**
     * Do a GET request for the submission status
     *
     * @param uuid Submission UUID to check
     * @return Status content of that submission uuid as a JSONArray
     *
     * @throws IOException
     */
    public static JSONObject getSubmissionStatus(String uuid) throws IOException, ClientProtocolException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        SwarmItMarketplaceSettings apiSettings = new SwarmItMarketplaceSettings();

        try {
            String uri = String.format("%s/uuid/%s", apiSettings.getApiUrl(), uuid);
            HttpGet httpget = new HttpGet(new URI(uri));
            LOGGER.log(Level.INFO, "Querying status with request {0}", httpget.getRequestLine());
            ResponseHandler<String> responseHandler = new SwarmItApiResponseHandler();
            String responseBody = httpclient.execute(httpget, responseHandler);
            // get result from response
            return getJSONObjectFromResult(responseBody);
        } catch (URISyntaxException ex) {
            LOGGER.log(Level.SEVERE, "Invalid API URI.", ex);
            throw new IOException(ex);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Error executing query.", ex);
            throw new IOException(ex);
        } finally {
            httpclient.close();
        }
    }

    /**
     * Do a HEAD request for the client API status.
     *
     * @param apiSettings - Instance of the API settings
     * @return true if connection successful, else false
     * @throws IOException
     */
    public static boolean testConnection(SwarmItMarketplaceSettings apiSettings) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        try {
            HttpGet httpget = new HttpGet(apiSettings.getStatusUri());

            response = httpclient.execute(httpget);

            Integer statusCode = response.getStatusLine().getStatusCode();

            // consume the full response content
            HttpEntity entity = response.getEntity();
            EntityUtils.consume(entity);

            if (statusCode == 200) {
                return true;
            } else {
                HeaderIterator it = response.headerIterator();
                LOGGER.log(Level.WARNING, "Connection test returned status code: {0} in status line and headers as follows:", statusCode.toString());
                while (it.hasNext()) {
                    LOGGER.log(Level.WARNING, " {0}", it.next());
                }

            }
        } catch (URISyntaxException ex) {
            LOGGER.log(Level.SEVERE, "Invalid URI.", ex);
            return false;
        } finally {
            httpclient.close();
            if (response != null) {
                response.close();
            }
        }
        return false;
    }

    /**
     * Convert the submission status string into a single verdict.
     *
     * @param submissionStatus String returned by requesting status of UUID from the API client
     * @return SwarmItVerdictEnum
     */
    public static SwarmItVerdict getVerdict(JSONObject submissionStatus) {
        LOGGER.log(Level.INFO, "Got Submission status: `{0}`", submissionStatus);
        // get verdict of the file from submission status result 'files' array
        // {'uuid': submission.uuid, 'files': files, 'status': submission.get_status()}

        JSONArray filesArray = submissionStatus.getJSONArray("files");
        // examine content of filesArray to find the result of this one file
        // consolidate result to a single answer as necessary.
        // If no files exist, then show error. It didn't work.
        if (filesArray == null || filesArray.length() == 0) {
            return SwarmItVerdict.errorFactory();
        }
        // get the first file in the files array, the get its list of assertions and votes.
        JSONArray assertions = filesArray.getJSONObject(0).getJSONArray("assertions");
        JSONArray votes = filesArray.getJSONObject(0).getJSONArray("votes");
        String status =  filesArray.getJSONObject(0).getString("bounty_status");
        String guid =  filesArray.getJSONObject(0).getString("bounty_guid");
        String hash =  filesArray.getJSONObject(0).getString("hash");
        boolean resultIsNull = filesArray.getJSONObject(0).isNull("result");

        // If it doesn't have assertions, votes or a result, return UNKNOWN
        if (assertions.length() == 0 && votes.length() == 0 && resultIsNull && status.length() == 0) {
            return SwarmItVerdict.unknownFactory();
        }

        SwarmItVerdictEnum assertionVerdict = null;
        SwarmItVerdictEnum votesVerdict = null;
        SwarmItVerdictEnum quorumVerdict = null;

        // Result holds the majority rule when quorum is reached on a file. If this exists, give the result and exit.
        if (!resultIsNull) {
            boolean malicious = filesArray.getJSONObject(0).getBoolean("result");
            quorumVerdict = malicious ? SwarmItVerdictEnum.MALICIOUS : SwarmItVerdictEnum.BENIGN;
            // votes verdict will be the same as quorum verdict (because quorum is based on votes)
            votesVerdict = malicious ? SwarmItVerdictEnum.MALICIOUS : SwarmItVerdictEnum.BENIGN;
        } else if (status.equals("Settled")) {
            // if we don't have a quorum, but it is settled report if any malicious votes
            int maliciousVoteCount = maliciousCount(votes, "vote");
            votesVerdict = maliciousVoteCount == 0 ? SwarmItVerdictEnum.BENIGN : SwarmItVerdictEnum.MALICIOUS;
        }

        // After the reveal phase, add in the assertion vote
        if (votes.length() > 0) {
            // Find the total count of malicious votes.
            int maliciousAssertionCount = maliciousCount(assertions, "verdict");
            assertionVerdict = maliciousAssertionCount == 0 ? SwarmItVerdictEnum.BENIGN : SwarmItVerdictEnum.MALICIOUS;
        }

        return new SwarmItVerdict(guid, hash, assertionVerdict, votesVerdict, quorumVerdict);
    }

    private static int maliciousCount(JSONArray array, String boolName) {
        int trueCount = 0;
        for (int i = 0; i < array.length(); i++) {
            if (array.getJSONObject(i).getBoolean(boolName)) {
                trueCount++;
            }
        }
        return trueCount;
    }
}
