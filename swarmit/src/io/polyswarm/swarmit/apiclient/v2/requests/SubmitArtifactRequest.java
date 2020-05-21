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
import io.polyswarm.swarmit.apiclient.InputStreamKnownSizeBody;
import io.polyswarm.swarmit.apiclient.NotAuthorizedException;
import io.polyswarm.swarmit.optionspanel.SwarmItMarketplaceSettings;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpEntity;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.sleuthkit.datamodel.AbstractFile;
import org.sleuthkit.datamodel.ReadContentInputStream;

/**
 *
 * @author rl
 */


public class SubmitArtifactRequest implements Request<ArtifactInstance> {
    private final static Logger LOGGER = Logger.getLogger(SubmitArtifactRequest.class.getName());
    AbstractFile abstractFile;
    
    public SubmitArtifactRequest(AbstractFile abstractFile) {
        this.abstractFile = abstractFile;
    }
    
    @Override
    public ArtifactInstance makeRequest() throws URISyntaxException, NotAuthorizedException, BadRequestException, IOException {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            SwarmItMarketplaceSettings apiSettings = new SwarmItMarketplaceSettings();
            String apikey = apiSettings.getApiKey();

            // FIXME allow user to select community
            String uri = String.format("%sconsumer/submission/%s", apiSettings.getApiUrl(), apiSettings.getCommunity());
            URIBuilder builder = new URIBuilder(uri);
            HttpPost httppost = new HttpPost(builder.build());
            if (apikey != null && !apikey.isEmpty()) {
                httppost.addHeader("Authorization", apiSettings.getApiKey());
            }

            LOGGER.log(Level.INFO, "Submitting file with request {0}.", httppost.getRequestLine());
            InputStreamKnownSizeBody inputStreamBody = new InputStreamKnownSizeBody(
                    new ReadContentInputStream(abstractFile),
                    (int) abstractFile.getSize(),
                    ContentType.DEFAULT_BINARY,
                    abstractFile.getName());

            HttpEntity reqEntity = MultipartEntityBuilder.create()
                    .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                    .addPart("file", inputStreamBody)
                    .addTextBody("artifact-type", "file")
                    .build();

            httppost.setEntity(reqEntity);

            ResponseHandler<ArtifactInstance> responseHandler = new ArtifactInstanceResponseHandler();
            return httpclient.execute(httppost, responseHandler);
        }
    }
}