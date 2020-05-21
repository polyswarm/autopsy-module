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
package io.polyswarm.swarmit.apiclient.v2.requests.utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author rl
 */


public class Assertion {
    public final String name;
    public final boolean mask;
    public final boolean verdict;
    public final String malwareFamily;
    
    public Assertion(String name, boolean mask, boolean verdict, String malwareFamily) {
        this.name = name;
        this.mask = mask;
        this.verdict = verdict;
        this.malwareFamily = malwareFamily;
    }
    
    public Assertion(JSONObject assertion) throws JSONException {
        name = assertion.getString("author_name");
        mask = assertion.getBoolean("mask");
        verdict = assertion.getBoolean("verdict");
        
        JSONObject metadata = assertion.getJSONObject("metadata");
        malwareFamily = metadata.getString("malware_family");
    }
    
    public String getHumanReadableVerdict() {
        return verdict ? "Malicious" : "Benign";
    }
}
