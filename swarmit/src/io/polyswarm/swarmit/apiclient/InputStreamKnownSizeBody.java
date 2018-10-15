/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.polyswarm.swarmit.apiclient;

import java.io.InputStream;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.content.InputStreamBody;

/**
 * The consumer service is a Flask app, and Flask cannot handle file uploads
 * having file size -1 (unknown file size). This class makes sure that
 * we always provide an accurate file size.
 */
public class InputStreamKnownSizeBody extends InputStreamBody {
    private final int length;
    
    public InputStreamKnownSizeBody(
            InputStream in,
            int length,
            ContentType contentType,
            String filename) {
        super(in, contentType, filename);
        this.length = length;
    }
    
    @Override
    public long getContentLength() {
        return this.length;
    }
}
