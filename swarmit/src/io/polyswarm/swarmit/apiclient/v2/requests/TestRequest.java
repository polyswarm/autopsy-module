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

import io.polyswarm.swarmit.apiclient.BadRequestException;
import io.polyswarm.swarmit.apiclient.NotAuthorizedException;
import io.polyswarm.swarmit.optionspanel.SwarmItMarketplaceSettings;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Logger;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * Test to see if PolySwarm is live
 */
public class TestRequest implements Request<TestResponse> {
    private final static Logger LOGGER = Logger.getLogger(HashSearchRequest.class.getName());

    public TestRequest() {}


    @Override
    public TestResponse makeRequest() throws URISyntaxException, NotAuthorizedException, BadRequestException, IOException {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            SwarmItMarketplaceSettings apiSettings = new SwarmItMarketplaceSettings();

            URIBuilder builder = new URIBuilder(apiSettings.getStatusUrl());
            HttpGet httpget = new HttpGet(builder.build());

            String apikey = apiSettings.getApiKey();
            if (apikey != null && !apikey.isEmpty()) {
                httpget.addHeader("Authorization", apiSettings.getApiKey());
            }

            ResponseHandler<TestResponse> responseHandler = new TestResponseHandler();
            return httpclient.execute(httpget, responseHandler);
        }
    }
}