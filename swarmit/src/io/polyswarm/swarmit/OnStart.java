/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.polyswarm.swarmit;

/**
 * Start the background thread to process SwarmIt requests
 * and store results as artifacts
 * 
 * The org.openide.modulesOnStart annotation tells NetBeans to
 * invoke this class's run method.
 */
@org.openide.modules.OnStart
public class OnStart implements Runnable {
    
    @Override
    public void run() {
        SwarmItModule.onStart();
    }
}
