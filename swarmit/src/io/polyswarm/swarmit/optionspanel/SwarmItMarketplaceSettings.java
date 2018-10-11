/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.polyswarm.swarmit.optionspanel;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.sleuthkit.autopsy.coreutils.ModuleSettings;

/**
 * Manage settings for the connection to the PolySwarm marketplace
 */
public final class SwarmItMarketplaceSettings {
    
    private final static Logger LOGGER = Logger.getLogger(SwarmItMarketplaceSettings.class.getName());
    private final Double DEFAULT_NCT_AMOUNT = 0.5;
    private final String DEFAULT_API_KEY = "PLACEHOLDER0123456789ABCDEF";
    private final String DEFAULT_URL = "https://consumer.stage.polyswarm.network"; // NON-NLS
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
     * Set the new URL and test if it's valid.
     * 
     * For now - make sure it's not empty and it starts with 'http'.
     * TODO: do something smarter..
     * 
     * @param newUrl New URL
     * @return  true if valid and set, else false
     */
    public boolean setApiUrl(String newUrl) {
        
        if (newUrl.isEmpty() || !newUrl.startsWith("http")) {
            return false;
        }

        apiUrl = newUrl;
        
        return true;
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
//        if (newApiKey.isEmpty()) {
//            return false;
//        }
//        apiKey = newApiKey;

        apiKey = DEFAULT_API_KEY;
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
