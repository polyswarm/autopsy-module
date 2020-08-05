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
package io.polyswarm.app.optionspanel;

import java.util.logging.Logger;
import org.sleuthkit.autopsy.coreutils.ModuleSettings;

/**
 * Manage settings for the connection to the PolySwarm marketplace
 */
public final class PolySwarmMarketplaceSettings {

    private final static Logger LOGGER = Logger.getLogger(PolySwarmMarketplaceSettings.class.getName());
    private final String DEFAULT_API_KEY = ""; // NON-NLS
    private final String DEFAULT_URL = "https://api.polyswarm.network/v2/"; // NON-NLS
    private final String DEFAULT_COMMUNITY = "default"; // NON-NLS
    private final String MODULE_NAME = "PolySwarm"; // NON-NLS
    private final String SETTINGS_TAG_API_KEY = "polyswarm.apikey"; // NON-NLS
    private final String SETTINGS_TAG_COMMUNITY = "polyswarm.community"; // NON-NLS
    private String apiKey;
    private String community;

    public PolySwarmMarketplaceSettings() {
        loadSettings();
    }

    /**
     * Read the settings from the module's config. If any are missing, set to defaults.
     */
    public void loadSettings() {
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
        ModuleSettings.setConfigSetting(MODULE_NAME, SETTINGS_TAG_API_KEY, getApiKey());
        ModuleSettings.setConfigSetting(MODULE_NAME, SETTINGS_TAG_COMMUNITY, getCommunity());
    }

    public boolean isChanged() {
        String jsonString = ModuleSettings.getConfigSetting(MODULE_NAME, SETTINGS_TAG_API_KEY);

        return !getApiKey().equals(jsonString);
    }

    public String getApiUrl() {
        return DEFAULT_URL;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getCommunity() {
        return community;
    }

    /**
     * Set the new API Key and test if it's valid.
     *
     * The service works with API Keys or without, depending on the setup We allow the user to clear the API key if they
     * want
     *
     * @param newApiKey New API Key
     * @return true if valid and set, else false
     */
    public boolean setApiKey(String newApiKey) {
        apiKey = newApiKey;
        return true;
    }

    public boolean validateApiKey(String newApiKey) {
        return true;
    }

    /**
     * Set the new community and test if it's valid.
     *
     *
     * @param newCommunity new community
     * @return true if valid and set, else false
     */
    public boolean setCommunity(String newCommunity) {
        if (validateCommunity(newCommunity)) {
            community = newCommunity;
            return true;
        }
        return false;
    }

    public boolean validateCommunity(String newCommunity) {
        return newCommunity != null && !newCommunity.isEmpty();
    }
}
