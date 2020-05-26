/*
 * The MIT License
 *
 * Copyright 2018 PolySwarm PTE. LTD.
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
package io.polyswarm.swarmit.optionspanel;

import io.polyswarm.swarmit.apiclient.ApiClientV2;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.sleuthkit.autopsy.coreutils.ModuleSettings;

/**
 * Manage settings for the connection to the PolySwarm marketplace
 */
public final class SwarmItMarketplaceSettings {


    private final static Logger LOGGER = Logger.getLogger(SwarmItMarketplaceSettings.class.getName());
    private final String DEFAULT_API_KEY = ""; // NON-NLS
    private final String DEFAULT_URL = "https://api.polyswarm.network/v2"; // NON-NLS
    private final String DEFAULT_COMMUNITY = "default"; // NON-NLS
    private final String API_STATUS_ENDPOINT = "status"; // NON-NLS
    private final String MODULE_NAME = "PolySwarm"; // NON-NLS
    private final String SETTINGS_TAG_API_URL = "polyswarm.url"; // NON-NLS
    private final String SETTINGS_TAG_API_KEY = "polyswarm.apikey"; // NON-NLS
    private final String SETTINGS_TAG_COMMUNITY = "polyswarm.community"; // NON-NLS
    private String apiUrl;
    private String apiKey;
    private String community;

    public SwarmItMarketplaceSettings() {
        loadSettings();
    }

    /**
     * Read the settings from the module's config. If any are missing, set to defaults.
     */
    public void loadSettings() {
        apiUrl = ModuleSettings.getConfigSetting(MODULE_NAME, SETTINGS_TAG_API_URL);
        if (apiUrl == null || apiUrl.isEmpty()) {
            apiUrl = DEFAULT_URL;
        }

        apiKey = ModuleSettings.getConfigSetting(MODULE_NAME, SETTINGS_TAG_API_KEY);
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = DEFAULT_API_KEY;
        }

        community = ModuleSettings.getConfigSetting(MODULE_NAME, SETTINGS_TAG_COMMUNITY);
        if (community == null || community.isEmpty()) {
            community = DEFAULT_COMMUNITY;
        }
    }

    public void saveSettings() {
        ModuleSettings.setConfigSetting(MODULE_NAME, SETTINGS_TAG_API_URL, getApiUrl());
        ModuleSettings.setConfigSetting(MODULE_NAME, SETTINGS_TAG_API_KEY, getApiKey());
        ModuleSettings.setConfigSetting(MODULE_NAME, SETTINGS_TAG_COMMUNITY, getCommunity());
    }

    public boolean testSettings() {
        return ApiClientV2.testConnection(this).passed;
    }

    public boolean isChanged() {
        String urlString = ModuleSettings.getConfigSetting(MODULE_NAME, SETTINGS_TAG_API_URL);
        String jsonString = ModuleSettings.getConfigSetting(MODULE_NAME, SETTINGS_TAG_API_KEY);

        return !getApiUrl().equals(urlString)
                || !getApiKey().equals(jsonString);
    }

    public String getApiUrl() {
        return apiUrl;
    }

    /**
     * Get the URI object containing the URI for the status endpoint.
     *
     * The status endpoint is /status
     *
     * @return URI object
     * @throws URISyntaxException
     */
    public String getStatusUrl() throws URISyntaxException {
        return getApiUrl().concat(String.format("consumer/community/%s/status", getCommunity()));
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getCommunity() {
        return community;
    }

    /**
     * Set the new URL and test if it's a valid URI.
     * If valid, save it to this instance.
     *
     * @param newUrl New URL
     * @return  true if valid and set, else false
     */
    public boolean setApiUrl(String newUrl) {

        if (!newUrl.isEmpty()) {
            try {
                apiUrl = newUrl;
                if (!newUrl.endsWith("/")) {
                    apiUrl = String.format("%s/", newUrl);
                }
                URI u = new URI(newUrl);
                return true;
            } catch (URISyntaxException ex) {
                return false;
            }
        }

        return false;
    }

    /**
     * Set the new API Key and test if it's valid.
     *
     * The service works with API Keys or without, depending on the setup
     * We allow the user to clear the API key if they want
     *
     * @param newApiKey New API Key
     * @return true if valid and set, else false
     */
    public boolean setApiKey(String newApiKey) {
        apiKey = newApiKey;
        return true;
    }

    public boolean setCommunity(String newCommunity) {
        if (newCommunity != null && !newCommunity.isEmpty()) {
            community = newCommunity;
            return true;
        }
        return false;
    }
}
