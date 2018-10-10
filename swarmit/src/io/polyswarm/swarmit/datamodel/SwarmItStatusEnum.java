/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.polyswarm.swarmit.datamodel;

/**
 * Enum to track the status of a SwarmIt pending lookup in the sqlite db.
 * 
 * NEW - added to db, but not yet submitted to PolySwarm Marketplace
 * SUBMITTED - submitted to PolySwarm Marketplace
 * FAILED - attempt to get result failed, need to try again later.
 */
public enum SwarmItStatusEnum {
    NEW("New"),
    SUBMITTED("Submitted"),
    FAILED("Failed");
    
    private final String statusName;
    
    SwarmItStatusEnum(String name) {
        this.statusName = name;
    }
    
    public static SwarmItStatusEnum fromString(String statusName) {
        if (null == statusName) {
            return NEW;
        }

        for (SwarmItStatusEnum s : SwarmItStatusEnum.values()) {
            if (s.toString().equalsIgnoreCase(statusName)) {
                return s;
            }
        }
        return NEW;
    }
    
    @Override
    public String toString() {
        return statusName;
    }
}
