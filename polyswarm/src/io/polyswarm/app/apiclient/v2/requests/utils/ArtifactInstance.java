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
package io.polyswarm.app.apiclient.v2.requests.utils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.math.RoundingMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents an ArtifactInstance as returned from PolySwarm Only contains relevant data, leaving a lot out
 */
public class ArtifactInstance {

    public final String id;
    public final List<Assertion> assertions;
    public final Detection detection;
    public final String sha256;
    public final String polyscore;
    public final boolean windowClosed;
    public final String firstSeen;
    public final String lastScanned;

    /**
     * Creates a HashSearchResponse from JSON
     *
     * @param json JSON from a HashSearch in V2
     * @throws JSONException
     */
    public ArtifactInstance(JSONObject json) throws JSONException {
        JSONArray assertions_json = json.getJSONArray("assertions");
        JSONObject detection_json = json.getJSONObject("detections");
        assertions = new ArrayList<>();
        for (Object obj : assertions_json) {
            if (obj instanceof JSONObject) {
                JSONObject assertion = (JSONObject) obj;
                assertions.add(new Assertion(assertion));
            }
        }

        DecimalFormat decimalFormat = new DecimalFormat("#.00");
        decimalFormat.setRoundingMode(RoundingMode.FLOOR);
        detection = new Detection(detection_json);
        polyscore = decimalFormat.format(json.getDouble("polyscore"));
        sha256 = json.getString("sha256");
        windowClosed = json.getBoolean("window_closed");
        id = json.getString("id");
        firstSeen = json.getString("first_seen");
        lastScanned = json.getString("last_scanned");
    }

    @Override
    public String toString() {
        return String.format("<ArtifactInstanceResponse id=%s sha256=%s windowClosed=%s", this.id, this.sha256, this.windowClosed);
    }
}
