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
package io.polyswarm.swarmit.datamodel;

/**
 * Class to hold details about a pending submission
 */
public class SwarmItPendingSubmission {
    private final String submission_uuid;
    private final Long abstractFileID;
    
    SwarmItPendingSubmission(Long abstractFileID, String uuid) {
        this.abstractFileID = abstractFileID;
        this.submission_uuid = uuid;
    }

    /**
     * @return the submission_uuid
     */
    public String getSubmissionUUID() {
        return submission_uuid;
    }

    /**
     * @return the abstractFileID
     */
    public Long getAbstractFileID() {
        return abstractFileID;
    }
    
    @Override
    public String toString() {
        return String.format("PendingSubmission(abstractFileID: {0}, submission_uuid:{1})", getAbstractFileID().toString(), getSubmissionUUID()); 
    }
}
