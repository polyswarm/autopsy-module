/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
