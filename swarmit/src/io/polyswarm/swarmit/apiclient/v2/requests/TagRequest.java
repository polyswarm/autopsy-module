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
import io.polyswarm.swarmit.apiclient.v2.requests.utils.Tag;
import io.polyswarm.swarmit.optionspanel.SwarmItMarketplaceSettings;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.logging.Logger;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.sleuthkit.datamodel.AbstractFile;

/**
 *
 * @author rl
 */


public class TagRequest implements Request<List<Tag>> {
    private final static Logger LOGGER = Logger.getLogger(HashSearchRequest.class.getName());
    String sha256Hash;
    
    public TagRequest(ArtifactInstance artifactInstance) {
        sha256Hash = artifactInstance.sha256;
    }
    
    @Override
    public List<Tag> makeRequest() throws URISyntaxException, NotAuthorizedException, BadRequestException, IOException, NotFoundException {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            SwarmItMarketplaceSettings apiSettings = new SwarmItMarketplaceSettings();

            String uri = String.format("%stags/link", apiSettings.getApiUrl());
            URIBuilder builder = new URIBuilder(uri);
            builder.setParameter("hash", sha256Hash);
            HttpGet httpget = new HttpGet(builder.build());

            String apikey = apiSettings.getApiKey();
            if (apikey != null && !apikey.isEmpty()) {
                httpget.addHeader("Authorization", apiSettings.getApiKey());
            }

            ResponseHandler<List<Tag>> responseHandler = new TagResponseHandler();
            return httpclient.execute(httpget, responseHandler);
        }
    }
}
