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

import io.polyswarm.swarmit.apiclient.SwarmItApiClient;
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
    private final Double DEFAULT_NCT_AMOUNT = 0.5;
    private final String DEFAULT_API_KEY = "PLACEHOLDER0123456789ABCDEF"; // NON-NLS
    private final String DEFAULT_URL = "https://consumer.prod.polyswarm.network/"; // NON-NLS
    private final String API_STATUS_ENDPOINT = "status"; // NON-NLS
    private final String MODULE_NAME = "PolySwarm"; // NON-NLS
    private final String SETTINGS_TAG_API_URL = "polyswarm.url"; // NON-NLS
    private final String SETTINGS_TAG_API_KEY = "polyswarm.apikey"; // NON-NLS
    private final String SETTINGS_TAG_NCT = "polyswarm.nct"; // NON-NLS
    private String apiUrl;
    private String apiKey;
    private Double nctAmount;
    
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
        
        String nctAmountString = ModuleSettings.getConfigSetting(MODULE_NAME, SETTINGS_TAG_NCT);
        if (nctAmountString == null || nctAmountString.isEmpty()) {
            nctAmount = DEFAULT_NCT_AMOUNT;
        } else {
            try {
                nctAmount = Double.parseDouble(nctAmountString);
                if (nctAmount <= 0.0) {
                    nctAmount = DEFAULT_NCT_AMOUNT;
                }
            } catch (NumberFormatException nex) {
                nctAmount = DEFAULT_NCT_AMOUNT;
            }
        }
    }

    public void saveSettings() {        
        ModuleSettings.setConfigSetting(MODULE_NAME, SETTINGS_TAG_API_URL, getApiUrl());
        ModuleSettings.setConfigSetting(MODULE_NAME, SETTINGS_TAG_API_KEY, getApiKey());
        ModuleSettings.setConfigSetting(MODULE_NAME, SETTINGS_TAG_NCT, getNctAmountString());
    }

    public boolean testSettings() {
        try {
            return SwarmItApiClient.testConnection(this);
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, "Connection text failed with exception.", ex);
            return false;
        }
    }
    
    public boolean isChanged() {
        String urlString = ModuleSettings.getConfigSetting(MODULE_NAME, SETTINGS_TAG_API_URL);
        String jsonString = ModuleSettings.getConfigSetting(MODULE_NAME, SETTINGS_TAG_API_KEY);
        String nctString = ModuleSettings.getConfigSetting(MODULE_NAME, SETTINGS_TAG_NCT);
        
        return !getApiUrl().equals(urlString)
                || !getApiKey().equals(jsonString)
                || !getNctAmountString().equals(nctString);
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
    public URI getStatusUri() throws URISyntaxException {
        return new URI(String.format("%s%s", getApiUrl(), "status"));
    }
    
    public String getApiKey() {
        return apiKey;
    }
    
    public String getNctAmountString() {
        return nctAmount.toString();
    }
    
    public Double getNctAmount() {
        return nctAmount;
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
     * NOTE: API Key is not used in the current version.
     * TODO: When it is used, uncomment the if test and implement
     * some level of validation, like:
     * - not empty
     * - has valid api_key regex
     * 
     * @param newApiKey New API Key
     * @return true if valid and set, else false
     */
    public boolean setApiKey(String newApiKey) {
        if (newApiKey.isEmpty()) {
            return false;
        }
        apiKey = newApiKey;
        return true;
    }
    
    /**
     * Set the new NCT amount and test if it's valid
     * 
     * NOTE: NCT amount is not used in the current version.
     * TODO: When it is used, uncomment the try/catch block to
     * re-enable validation.
     * 
     * @param newNctAmount New NCT amount
     * @return true if valid and set, else false
     */
    public boolean setNctAmount(String newNctAmount) {
//        double nctDouble;
//        try {
//            nctDouble = Double.parseDouble(newNctAmount);
//            if (nctDouble <= 0.0) {
//                LOGGER.log(Level.WARNING, "NCT Amount is invalid. Must be greater than 0.0.");
//                return false;
//            }
//        } catch (NumberFormatException nfe) {
//            LOGGER.log(Level.WARNING, "NCT Amount is invalid. Must be a number greater than 0.0.");
//            return false;
//        }
//        nctAmount = nctDouble;
        nctAmount = DEFAULT_NCT_AMOUNT;
        
        return true;
    }
}
