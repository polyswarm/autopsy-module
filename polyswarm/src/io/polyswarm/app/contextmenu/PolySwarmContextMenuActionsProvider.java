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
package io.polyswarm.app.contextmenu;

import io.polyswarm.app.PolySwarmController;
import io.polyswarm.app.PolySwarmModule;
import io.polyswarm.app.datamodel.PolySwarmDbException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.Action;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;
import org.sleuthkit.autopsy.casemodule.NoCurrentCaseException;
import org.sleuthkit.autopsy.corecomponentinterfaces.ContextMenuActionsProvider;
import org.sleuthkit.datamodel.AbstractFile;
import org.sleuthkit.datamodel.TskCoreException;

/**
 * This creates context menu items for submitting an files/hashes to PolySwarm
 */
@ServiceProvider(service = ContextMenuActionsProvider.class)
public class PolySwarmContextMenuActionsProvider implements ContextMenuActionsProvider {

    private static final Logger LOGGER = Logger.getLogger(PolySwarmContextMenuActionsProvider.class.getName());

    @Override
    @org.openide.util.NbBundle.Messages({"PolySwarmContextMenuActionsProvider.scan.text=Scan on PolySwarm",
        "PolySwarmContextMenuActionsProvider.hash.text=Lookup Hash on PolySwarm",
        "PolySwarmContextMenuActionsProvider.rescan.text=Rescan on PolySwarm"})
    public List<Action> getActions() {
        ArrayList<Action> actions = new ArrayList<>();

        final Collection<? extends AbstractFile> selectedFiles = Utilities.actionsGlobalContext().lookupAll(AbstractFile.class);

        for (AbstractFile abstractFile : selectedFiles) {

            if (abstractFile != null && abstractFile.isFile()) {
                String scanTitle = Bundle.PolySwarmContextMenuActionsProvider_scan_text();
                actions.add(new ScanAction(scanTitle, abstractFile));

                if (abstractFile.getMd5Hash() != null) {
                    String hashLookupTitle = Bundle.PolySwarmContextMenuActionsProvider_hash_text();
                    actions.add(new HashLookupAction(hashLookupTitle, abstractFile));
                }

                if (isInPolyswarm(abstractFile)) {
                    String rescanTitle = Bundle.PolySwarmContextMenuActionsProvider_rescan_text();
                    actions.add(new RescanAction(rescanTitle, abstractFile));
                }
            }
        }
        return actions;
    }

    /**
     * Checks to see if the artifact is in polyswarm.
     *
     * First, tries to get a polyswarm results BlackboardArtifact. If bbartifact, checks to see if there is a polyscore
     * attribute.
     *
     * It checks for polyscore, because there is a not found attribute that is added when a hash search resutnrs nothing
     *
     * @param abstractFile file to check
     * @return
     */
    private boolean isInPolyswarm(AbstractFile abstractFile) {
        try {
            PolySwarmController controller = PolySwarmModule.getController();
            controller.getSha256(abstractFile);
            return true;
        } catch (TskCoreException | NoCurrentCaseException | PolySwarmDbException | UnsupportedOperationException ex) {
            return false;
        }
    }
}
