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
    private final String DEFAULT_URL = "https://polyswarm.io"; // NON-NLS
    private final String MODULE_NAME = "PolySwarm"; // NON-NLS
    private final String SETTINGS_TAG_URL = "polyswarm.url"; // NON-NLS
    private final String SETTINGS_TAG_JSON = "polyswarm.json"; // NON-NLS
    private final String SETTINGS_TAG_NCT = "polyswarm.nct"; // NON-NLS
    private String url;
    private String json;
    private Double nctAmount;
    
    public SwarmItMarketplaceSettings() {
        loadSettings();
    }
    
    /**
     * Read the settings from the module's config. If any are missing, set to defaults.
     */
    public void loadSettings() {
        url = ModuleSettings.getConfigSetting(MODULE_NAME, SETTINGS_TAG_URL);
        if (url == null || url.isEmpty()) {
            url = DEFAULT_URL;
        }
        
        json = ModuleSettings.getConfigSetting(MODULE_NAME, SETTINGS_TAG_JSON);
        if (json == null || json.isEmpty()) {
            json = "";
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
        ModuleSettings.setConfigSetting(MODULE_NAME, SETTINGS_TAG_URL, getUrl());
        ModuleSettings.setConfigSetting(MODULE_NAME, SETTINGS_TAG_JSON, getJson());
        ModuleSettings.setConfigSetting(MODULE_NAME, SETTINGS_TAG_NCT, getNctAmountString());
    }

    public boolean isChanged() {
        String urlString = ModuleSettings.getConfigSetting(MODULE_NAME, SETTINGS_TAG_URL);
        String jsonString = ModuleSettings.getConfigSetting(MODULE_NAME, SETTINGS_TAG_JSON);
        String nctString = ModuleSettings.getConfigSetting(MODULE_NAME, SETTINGS_TAG_NCT);
        
        return !getUrl().equals(urlString)
                || !getJson().equals(jsonString)
                || !getNctAmountString().equals(nctString);
    }

    public String getUrl() {
        return url;
    }
    
    public String getJson() {
        return json;
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
    public boolean setUrl(String newUrl) {
        
        if (newUrl.isEmpty() || !newUrl.startsWith("http")) {
            return false;
        }

        url = newUrl;
        
        return true;
    }

    /**
     * Set the new JSON blob and test if it's valid.
     * 
     * TODO: do something more than check for empty string; like verify
     * - it can be parsed as JSON
     * - it has valid api_key
     * - it has valid eth json
     * 
     * @param newJson New JSON blob
     * @return true if valid and set, else false
     */
    public boolean setJson(String newJson) {
        if (newJson.isEmpty()) {
            return false;
        }
        json = newJson;
        
        return true;
    }
    
    /**
     * Set the new NCT amount and test if it's valid
     * 
     * @param newNctAmount New NCT amount
     * @return true if valid and set, else false
     */
    public boolean setNctAmount(String newNctAmount) {
        double nctDouble;
        try {
            nctDouble = Double.parseDouble(newNctAmount);
            if (nctDouble <= 0.0) {
                LOGGER.log(Level.WARNING, "NCT Amount is invalid. Must be greater than 0.0.");
                return false;
            }
        } catch (NumberFormatException nfe) {
            LOGGER.log(Level.WARNING, "NCT Amount is invalid. Must be a number greater than 0.0.");
            return false;
        }
        nctAmount = nctDouble;
        
        return true;
    }
}
