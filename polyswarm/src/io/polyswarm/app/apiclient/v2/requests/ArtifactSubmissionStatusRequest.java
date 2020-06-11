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
import io.polyswarm.app.optionspanel.PolySwarmMarketplaceSettings;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Logger;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * Makes a request to check on an active scan
 */
public class ArtifactSubmissionStatusRequest  implements Request<ArtifactInstance> {
    private final static Logger LOGGER = Logger.getLogger(ArtifactSubmissionStatusRequest.class.getName());
    String submissionId;

    public ArtifactSubmissionStatusRequest(String submissionId) {
        this.submissionId = submissionId;
    }

    @Override
    public ArtifactInstance makeRequest() throws URISyntaxException, NotAuthorizedException, BadRequestException, IOException {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            PolySwarmMarketplaceSettings apiSettings = new PolySwarmMarketplaceSettings();

            String uri = String.format("%sconsumer/submission/%s/%s", apiSettings.getApiUrl(), apiSettings.getCommunity(), submissionId);
            URIBuilder builder = new URIBuilder(uri);
            HttpGet httpget = new HttpGet(builder.build());

            String apikey = apiSettings.getApiKey();
            if (apikey != null && !apikey.isEmpty()) {
                httpget.addHeader("Authorization", apiSettings.getApiKey());
            }

            ResponseHandler<ArtifactInstance> responseHandler = new ArtifactInstanceResponseHandler();
            return httpclient.execute(httpget, responseHandler);
        }
    }
}
