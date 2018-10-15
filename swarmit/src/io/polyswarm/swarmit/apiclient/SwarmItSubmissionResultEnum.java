/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.polyswarm.swarmit.apiclient;

/**
 * Enum for the potential results of a SwarmIt submission.
 */
public enum SwarmItSubmissionResultEnum {
    UNKNOWN("Unknown"),
    MALICIOUS("Malicious"),
    BENIGN("Benign");
    
    private final String name;
    
    SwarmItSubmissionResultEnum(String name) {
        this.name = name;
    }
    
    @Override
    public String toString() {
        return name;
    }
    
    public static SwarmItSubmissionResultEnum fromString(String pName) {
        if (null == pName) {
            return UNKNOWN;
        }

        for (SwarmItSubmissionResultEnum p : SwarmItSubmissionResultEnum.values()) {
            if (p.toString().equalsIgnoreCase(pName)) {
                return p;
            }
        }
        return UNKNOWN;
    }
}
