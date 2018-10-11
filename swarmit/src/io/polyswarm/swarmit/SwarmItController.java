/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.polyswarm.swarmit;

import io.polyswarm.swarmit.datamodel.SwarmItDb;
import io.polyswarm.swarmit.datamodel.SwarmItDbException;
import io.polyswarm.swarmit.optionspanel.SwarmItMarketplaceSettings;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.sleuthkit.autopsy.casemodule.Case;
import org.sleuthkit.datamodel.TskCoreException;

/**
 * Thread that runs in the background to process AbstractFiles that have
 * been submitted using SwarmIt right-click menu option.
 * 
 * AbstractFile ID and PolySwarm UUID are kept in sqlite db.
 * 
 * This process will periodically check the sqlite db for new entries.
 * For each entry, this process will contact the PolySwarm API 
 * to get the analysis results.
 * It will then add a PolySwarm Results artifact to the AbstractFile.
 * These artifacts will be displayed in the left pane in a sub-tree
 * called "PolySwarm Results."
 * 
 */
public class SwarmItController {
    private static final Logger LOGGER = Logger.getLogger(SwarmItController.class.getName());
    
    private final Case autopsyCase;
    private final SwarmItMarketplaceSettings apiSettings;
    private final SwarmItDb dbInstance;

    public Case getAutopsyCase() {
        return autopsyCase;
    }
    
    SwarmItController(Case newCase) throws TskCoreException, SwarmItDbException {
        this.autopsyCase = Objects.requireNonNull(newCase);
        this.apiSettings = new SwarmItMarketplaceSettings();
        this.dbInstance = SwarmItDb.getInstance();
    }
    
    public void reset() {
        try {
            // TODO: close all connections to the REST API.
            dbInstance.shutdownConnections();
        } catch (SwarmItDbException ex) {
            LOGGER.log(Level.SEVERE, "Failed to shutdown database connections.", ex); // NON-NLS
        }
    }
}
