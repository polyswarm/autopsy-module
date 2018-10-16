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
