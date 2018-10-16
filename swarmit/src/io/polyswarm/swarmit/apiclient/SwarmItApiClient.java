/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.polyswarm.swarmit.apiclient;

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
import org.json.JSONTokener;
import org.sleuthkit.datamodel.AbstractFile;
import org.sleuthkit.datamodel.ReadContentInputStream;
import static io.polyswarm.swarmit.apiclient.SwarmItJsonHelper.getUUIDFromResult;
import static io.polyswarm.swarmit.apiclient.SwarmItJsonHelper.getJSONObjectFromResult;

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
            HttpGet httpget = new HttpGet(new URI(apiSettings.getApiUrl() + uuid));
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
    public static SwarmItVerdictEnum getVerdict(JSONObject submissionStatus) {
            LOGGER.log(Level.INFO, "Got Submission status: `{0}`", submissionStatus);

            // TODO: revisit how we determine the single verdict. Adjust this accordingly.
            
            // get verdict of the file from submission status result 'files' array
            // {'uuid': submission.uuid, 'files': files, 'status': submission.get_status()}
            JSONArray filesArray = submissionStatus.getJSONArray("files");
            // examine content of filesArray to find the result of this one file
            // consolidate result to a single answer as necessary.
            if (filesArray == null || filesArray.length() == 0) {
                return SwarmItVerdictEnum.UNKNOWN;
            }
            // get the first file in the files array, the get its list of assertions.
            JSONArray assertions = filesArray.getJSONObject(0).getJSONArray("assertions");
            
            int trueCount = 0;
            for (int i = 0; i < assertions.length(); i++) {
                if (assertions.getJSONObject(i).getBoolean("verdict") == true) { 
                    trueCount++;
                }
            }
            
            if (trueCount == 0) {
                return SwarmItVerdictEnum.BENIGN;
            }
            
            return SwarmItVerdictEnum.MALICIOUS;


    }
}
