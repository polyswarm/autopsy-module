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
package io.polyswarm.swarmit.contextmenu;

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
