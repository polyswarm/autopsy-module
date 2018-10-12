/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.polyswarm.swarmit.apiclient;

import java.io.IOException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Handle responses from PolySwarm API and return the result.
 */
public class SwarmItApiResponseHandler implements ResponseHandler<String> {

    @Override
    public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
        int statusCode = response.getStatusLine().getStatusCode();
        HttpEntity responseEntity = response.getEntity();
        String responseString = null;
        if (responseEntity != null) {
            responseString = EntityUtils.toString(responseEntity);
        }

        if (statusCode == 200) {
            // get result
            // {'status': 'OK', 'result': result})
            JSONTokener tokener = new JSONTokener(responseString);
            JSONObject json = new JSONObject(tokener);
            return json.getString("result");

        } else {
            // get errors
            // {'status': 'FAIL', 'errors': message})
            JSONTokener tokener = new JSONTokener(responseString);
            JSONObject json = new JSONObject(tokener);

            // parse failure message from JSON blob and append to exception string.
            String msg = json.getString("errors");
            throw new ClientProtocolException(String.format("Failed to submit file. {0} : {1}.", statusCode, msg));
        }
    }
}