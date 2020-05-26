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

import java.util.List;
import org.apache.http.client.ResponseHandler;
import io.polyswarm.swarmit.apiclient.v2.requests.utils.Tag;

import io.polyswarm.swarmit.apiclient.BadRequestException;
import io.polyswarm.swarmit.apiclient.NotAuthorizedException;
import io.polyswarm.swarmit.apiclient.NotFoundException;
import io.polyswarm.swarmit.apiclient.RateLimitException;
import io.polyswarm.swarmit.apiclient.v2.requests.utils.ArtifactInstance;
import java.io.IOException;
import java.util.logging.Logger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import io.polyswarm.swarmit.apiclient.RateLimitException;
import io.polyswarm.swarmit.apiclient.NotAuthorizedException;
import io.polyswarm.swarmit.apiclient.NotFoundException;
import io.polyswarm.swarmit.apiclient.BadRequestException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;

/**
 * Parse json response with a list of tags
 */
public class TagResponseHandler implements ResponseHandler<List<Tag>> {
    private final static Logger LOGGER = Logger.getLogger(TagResponseHandler.class.getName());

    @Override
    public List<Tag> handleResponse(HttpResponse response) throws NotAuthorizedException, BadRequestException, ClientProtocolException, RateLimitException, IOException, JSONException {
        Integer statusCode = response.getStatusLine().getStatusCode();
        HttpEntity responseEntity = response.getEntity();
        String responseString = null;
        if (responseEntity != null) {
            responseString = EntityUtils.toString(responseEntity);
        }

        if (statusCode / 100 == 2) {
            JSONTokener tokener = new JSONTokener(responseString);
            JSONObject output = new JSONObject(tokener);
            JSONObject result = output.getJSONObject("result");
            JSONArray jsonArray = result.getJSONArray("tags");

            List<Tag> tags = new ArrayList<>();
            for (Object tag : jsonArray) {
                tags.add(new Tag((String) tag));
            }
            return tags;

        } else {
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
    private void handle4xx(Integer statusCode, String responseString) throws BadRequestException, NotAuthorizedException, RateLimitException, ClientProtocolException {
        switch (statusCode) {
            case 400: throw new BadRequestException(responseString);
            case 401: throw new NotAuthorizedException(responseString);
            case 429: throw new RateLimitException(responseString);
            default: throw new ClientProtocolException(String.format("Client request failed. Status code: %s, Response: %s.", statusCode, responseString));
        }
    }
}
