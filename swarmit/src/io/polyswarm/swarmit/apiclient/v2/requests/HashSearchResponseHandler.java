
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
package io.polyswarm.swarmit.apiclient.v2.requests;

import io.polyswarm.swarmit.apiclient.v2.requests.utils.ArtifactInstance;
import io.polyswarm.swarmit.apiclient.BadRequestException;
import io.polyswarm.swarmit.apiclient.NotAuthorizedException;
import io.polyswarm.swarmit.apiclient.NotFoundException;
import io.polyswarm.swarmit.apiclient.RateLimitException;
import java.io.IOException;
import java.util.logging.Logger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 *
 * @author rl
 */


public class HashSearchResponseHandler implements ResponseHandler<ArtifactInstance> {
    private final static Logger LOGGER = Logger.getLogger(HashSearchResponseHandler.class.getName());

    @Override
    public ArtifactInstance handleResponse(HttpResponse response) throws NotAuthorizedException, BadRequestException, ClientProtocolException, RateLimitException, IOException, JSONException {
        Integer statusCode = response.getStatusLine().getStatusCode();
        HttpEntity responseEntity = response.getEntity();
        String responseString = null;
        if (responseEntity != null) {
            responseString = EntityUtils.toString(responseEntity);
        }
        if (statusCode == 204 ) {
            throw new NotFoundException(responseString);
        } else if (statusCode / 100 == 2) {
            JSONTokener tokener = new JSONTokener(responseString);
            JSONObject output = new JSONObject(tokener);
            JSONArray result = output.getJSONArray("result");
            if (result.length() > 0) {
                // get first, with is the last scan
                return new ArtifactInstance(result.getJSONObject(0));
            } else {
                throw new NotFoundException(responseString);
            }
        } else {
            handle4xx(statusCode, responseString);
            throw new ClientProtocolException(String.format("Client request failed. Status code: %s, Response: %s.", statusCode, responseString));
        }
    }
    
    private void handle4xx(Integer statusCode, String responseString) throws BadRequestException, NotAuthorizedException, RateLimitException, ClientProtocolException {
        switch (statusCode) {
            case 400: throw new BadRequestException(responseString);
            case 401: throw new NotAuthorizedException(responseString);
            case 429: throw new RateLimitException(responseString);
            default: throw new ClientProtocolException(String.format("Client request failed. Status code: %s, Response: %s.", statusCode, responseString));
        }
    }
}
