/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.polyswarm.swarmit.apiclient;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * JSON static helper methods for parsing API response strings
 */
public class SwarmItJsonHelper {
    
    private SwarmItJsonHelper(){
        
    }
    
    /**
     * In a String containing JSON get the String value for the 'result' key.
     * This literally means that the result has a value that is a String,
     * not a list, dict, etc.
     * 
     * {'status': 'OK', 'result': result})
     * 
     * @param response String containing JSON
     * @return String containing the value of the result key
     */
    public static JSONObject getResponseStringAsJsonObject(String response) throws JSONException {
        JSONTokener tokener = new JSONTokener(response);
        return new JSONObject(tokener);
    }
    
    public static String getStringFromJsonObject(JSONObject json, String key) throws JSONException {
        return json.getString(key);
    }
    
    /**
     * Submitting a file will return a JSON blob with the UUID as the value of the 'result' key.
     * @param responseBody String returned by API service
     * @return
     * @throws JSONException 
     */
    public static String getUUIDFromResult(String responseBody) throws JSONException {
        return getStringFromJsonObject(getResponseStringAsJsonObject(responseBody), "result");
    }
    
    /**
     * Getting the status of a submission returns a complex JSON blob.
     * This method extracts the JSONObject that is the value of the "results" key.
     * 
     * @param responseBody String returned by API service
     * @return
     * @throws JSONException 
     */
    public static JSONObject getJSONObjectFromResult(String responseBody) throws JSONException {
        return getResponseStringAsJsonObject(responseBody).getJSONObject("result");
    }
}
