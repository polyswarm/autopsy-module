/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.polyswarm.swarmit.datamodel;

/**
 * An exception thrown by sqlite db settings.
 */
public class SwarmItDbException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * COnstructs an exception to be thrown
     * @param message The exception message
     */
    public SwarmItDbException(String message) {
        super(message);
    }
    
    /**
     * Construction an exception to be thrown
     * 
     * @param message The exception message
     * @param cause   The exception cause
     */
    public SwarmItDbException(String message, Throwable cause) {
        super(message, cause);
    }
}
