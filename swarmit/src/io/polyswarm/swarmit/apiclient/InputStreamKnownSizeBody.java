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
