/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.polyswarm.swarmit.contextmenu;

import io.polyswarm.swarmit.optionspanel.SwarmItMarketplaceSettings;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.Action;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;
import org.sleuthkit.autopsy.corecomponentinterfaces.ContextMenuActionsProvider;
import org.sleuthkit.datamodel.AbstractFile;

/**
 * This creates a single context menu item for submitting an
 * artifact to PolySwarm for analysis.
 */
@ServiceProvider(service = ContextMenuActionsProvider.class)
public class SwarmItContextMenuActionsProvider implements ContextMenuActionsProvider {
    private static final Logger LOGGER = Logger.getLogger(SwarmItContextMenuActionsProvider.class.getName());
    
    @Override
    @Messages({"SwarmItContextMenuActionsProvider.menuItemStr.text=SwarmIt"})
    public List<Action> getActions() {
        ArrayList<Action> actions = new ArrayList<>();
        
        final Collection<? extends AbstractFile> selectedFiles = Utilities.actionsGlobalContext().lookupAll(AbstractFile.class);
        
        for (AbstractFile abstractFile : selectedFiles) {
            
            if (abstractFile != null && abstractFile.isFile()) {
                String menuItemStr = Bundle.SwarmItContextMenuActionsProvider_menuItemStr_text();
                actions.add(new AddSwarmItAction(menuItemStr, abstractFile));
            }
        }
        return actions;
    }
}
