
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
package io.polyswarm.app.apiclient.v2.requests;

import io.polyswarm.app.apiclient.v2.requests.utils.ArtifactInstance;
import io.polyswarm.app.apiclient.BadRequestException;
import io.polyswarm.app.apiclient.NotAuthorizedException;
import io.polyswarm.app.apiclient.NotFoundException;
import io.polyswarm.app.apiclient.RateLimitException;
import io.polyswarm.app.apiclient.ServerException;
import java.io.IOException;
import java.util.logging.Logger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Reads an artifact instance from json response
 */
public class ArtifactInstanceResponseHandler implements ResponseHandler<ArtifactInstance> {

    private final static Logger LOGGER = Logger.getLogger(ArtifactInstanceResponseHandler.class.getName());

    @Override
    public ArtifactInstance handleResponse(HttpResponse response) throws NotAuthorizedException, BadRequestException, ClientProtocolException, RateLimitException, NotFoundException, ServerException, IOException, JSONException {
        Integer statusCode = response.getStatusLine().getStatusCode();
        HttpEntity responseEntity = response.getEntity();
        String responseString = null;
        if (responseEntity != null) {
            responseString = EntityUtils.toString(responseEntity);
        }

        switch (statusCode / 100) {
            case 2:
                JSONTokener tokener = new JSONTokener(responseString);
                JSONObject topLevel = new JSONObject(tokener);
                return new ArtifactInstance(topLevel.getJSONObject("result"));
            case 5:
                throw new ServerException(String.format("Received a server error response. Status code: %s, Response: %s", statusCode, responseString));
            default:
                handle4xx(statusCode, responseString);
                throw new ClientProtocolException(String.format("Client request failed. Status code: %s, Response: %s.", statusCode, responseString));
        }
    }

    /**
     * Throw exceptions based on statusCode
     *
     * @param statusCode Status code from PolySwarm
     * @param responseString Response from PolySwarm as a String
     */
    private void handle4xx(Integer statusCode, String responseString) throws BadRequestException, NotAuthorizedException, RateLimitException, NotFoundException, ClientProtocolException {
        switch (statusCode) {
            case 400:
                throw new BadRequestException(responseString);
            case 401:
                throw new NotAuthorizedException(responseString);
            case 404:
                throw new NotFoundException(responseString);
            case 429:
                throw new RateLimitException(responseString);
            default:
                throw new ClientProtocolException(String.format("Client request failed. Status code: %s, Response: %s.", statusCode, responseString));
        }
    }
}
