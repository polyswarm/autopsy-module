/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.polyswarm.swarmit.apiclient;

import java.io.IOException;
import java.util.logging.Logger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;

/**
 * Handle responses from PolySwarm API and return the result.
 */
public class SwarmItApiResponseHandler implements ResponseHandler<String> {
    private final static Logger LOGGER = Logger.getLogger(SwarmItApiResponseHandler.class.getName());

    @Override
    public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
        Integer statusCode = response.getStatusLine().getStatusCode();
        HttpEntity responseEntity = response.getEntity();
        String responseString = null;
        if (responseEntity != null) {
            responseString = EntityUtils.toString(responseEntity);
        }
        
        if (statusCode == 200) {
            return responseString;

        } else {
            throw new ClientProtocolException(String.format("Client request failed. Status code: %s, Response: %s.", statusCode, responseString));
        }
    }
}
