/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.polyswarm.swarmit.contextmenu;

import io.polyswarm.swarmit.optionspanel.SwarmItMarketplaceSettings;
import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import org.sleuthkit.datamodel.AbstractFile;

/**
 * Action added to right-click menu on an abstractFile.
 */
public class AddSwarmItAction extends AbstractAction {
    
    private static final long serivalVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(AddSwarmItAction.class.getName());
    private final AbstractFile abstractFile;
    private final SwarmItMarketplaceSettings settings;
    
    AddSwarmItAction(String menuItemStr, AbstractFile abstractFile, SwarmItMarketplaceSettings settings) {
        super(menuItemStr);
        this.abstractFile = abstractFile;
        this.settings = settings;
    }
    
    @Override
    public void actionPerformed(ActionEvent event) {
        this.settings.loadSettings();

        // TODO: send file/artifact to PolySwarm for analysis
        LOGGER.log(Level.INFO, "Sent file to PolySwarm.");

        // TODO: add file info to db

    }
}
