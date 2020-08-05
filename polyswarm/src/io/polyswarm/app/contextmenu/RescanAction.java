/*
 * The MIT License
 *
 * Copyright 2020 PolySwarm PTE. LTD.
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
import static io.polyswarm.app.PolySwarmController.POLYSWARM_ARTIFACT_TYPE_NAME;
import io.polyswarm.app.PolySwarmModule;
import io.polyswarm.app.datamodel.PolySwarmDb;
import io.polyswarm.app.datamodel.PolySwarmDbException;
import io.polyswarm.app.optionspanel.PolySwarmMarketplaceSettings;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import org.openide.windows.WindowManager;
import org.sleuthkit.autopsy.casemodule.NoCurrentCaseException;
import org.sleuthkit.datamodel.AbstractFile;
import org.sleuthkit.datamodel.BlackboardArtifact;
import org.sleuthkit.datamodel.BlackboardAttribute;
import org.sleuthkit.datamodel.TskCoreException;

/**
 * Adds a hash lookup action that queries polyswarm about the artifact in question Update the blackboard with results
 * from the hash lookup
 */
public class RescanAction extends AbstractAction {

    private static final long serivalVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(RescanAction.class.getName());
    private final AbstractFile abstractFile;

    RescanAction(String menuItemStr, AbstractFile abstractFile) {
        super(menuItemStr);
        this.abstractFile = abstractFile;
    }

    @Override
    @org.openide.util.NbBundle.Messages({"RescanAction.submitError.message=Failed to submit rescan to PolySwarm.",
        "RescanAction.dbError.message=Failed to record rescan in pending task database.",
        "RescanAction.alreadyPending.message=Rescan is already in progress.",
        "RescanAction.missingArtifact.message=Must have performed a valid hash lookup or scan on file before rescan.",
        "RescanAction.messageDialog.title=Rescan on PolySwarm"})
    public void actionPerformed(ActionEvent event) {
        PolySwarmMarketplaceSettings apiSettings = new PolySwarmMarketplaceSettings();
        if (apiSettings.getApiKey().isEmpty()) {
            ApiKeyWarningDialog.show();
            return;
        }

        if (abstractFile != null) {
            addPendingRescan(abstractFile);
        }
    }

    /**
     * Write the new hash lookup to the DB
     */
    private void addPendingRescan(AbstractFile abstractFile) {
        try {

            PolySwarmController controller = PolySwarmModule.getController();
            String sha256Hash = controller.getSha256(abstractFile);
            Long abstractFileId = abstractFile.getId();
            PolySwarmDb instance = PolySwarmDb.getInstance();
            if (!instance.isPendingRescan(abstractFileId)) {
                // add hash to pending search
                instance.newPendingRescan(abstractFileId, sha256Hash);
                LOGGER.log(Level.FINE, String.format("Added rescan to pending db: abstractFileId: %s, sha256Hash: %s.",
                        abstractFileId.toString(),
                        sha256Hash));
            } else {
                LOGGER.log(Level.INFO, "Rescan is already pending, not re-submitting.");
                JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                        Bundle.RescanAction_alreadyPending_message(),
                        Bundle.RescanAction_messageDialog_title(),
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (NoCurrentCaseException | TskCoreException ex) {

        } catch (UnsupportedOperationException ex) {
            LOGGER.log(Level.SEVERE, "Error no PolySwarm Results artifact.", ex);
            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                    Bundle.RescanAction_missingArtifact_message(),
                    Bundle.RescanAction_messageDialog_title(),
                    JOptionPane.ERROR_MESSAGE);
        } catch (PolySwarmDbException ex) {
            LOGGER.log(Level.SEVERE, "Error adding new rescan to sqlite db.", ex);
            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                    Bundle.RescanAction_dbError_message(),
                    Bundle.RescanAction_messageDialog_title(),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

}
