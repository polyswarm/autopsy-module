/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.polyswarm.swarmit.apiclient;

import io.polyswarm.swarmit.optionspanel.SwarmItMarketplaceSettings;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
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
import org.json.JSONObject;
import org.json.JSONTokener;
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
            HttpPost httppost = new HttpPost(apiSettings.getApiUrl());

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
            return httpclient.execute(httppost, responseHandler);
        } finally {
            httpclient.close();
        }
    }

    /**
     * Do a GET request for the submission status
     * 
     * @param uuid Submission UUID to check
     * @return Status of that submission
     * 
     * @throws IOException 
     */
    public static SwarmItSubmissionResultEnum getSubmissionStatus(String uuid) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        SwarmItMarketplaceSettings apiSettings = new SwarmItMarketplaceSettings();

        try {
            HttpGet httpget = new HttpGet(apiSettings.getApiUrl());

            ResponseHandler<String> responseHandler = new SwarmItApiResponseHandler();
            // get result from response
            String result = httpclient.execute(httpget, responseHandler);
            // get verdict of a file from submission status result files array
            // {'uuid': submission.uuid, 'files': files, 'status': submission.get_status()}
            JSONTokener tokener = new JSONTokener(result);
            JSONObject json = new JSONObject(tokener);
            JSONArray filesArray = json.getJSONArray("files");
            LOGGER.log(Level.INFO, "Got Submission status result: `{0}`", result);
            // TODO: examine content of filesArray to find the result of this one file
            // consolidate result to a single answer as necessary.
            return SwarmItSubmissionResultEnum.MALICIOUS;

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
}
