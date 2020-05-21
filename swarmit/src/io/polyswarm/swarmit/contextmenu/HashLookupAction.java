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
package io.polyswarm.swarmit.contextmenu;

import io.polyswarm.swarmit.datamodel.SwarmItDb;
import io.polyswarm.swarmit.datamodel.SwarmItDbException;
import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import org.openide.windows.WindowManager;
import org.sleuthkit.datamodel.AbstractFile;


/**
 * Adds a hash lookup action that queries polyswarm about the artifact in question
 * Update the blackboard with results from the hash lookup
 */
public class HashLookupAction extends AbstractAction {
    private static final long serivalVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(HashLookupAction.class.getName());
    private final AbstractFile abstractFile;

    HashLookupAction(String menuItemStr, AbstractFile abstractFile) {
        super(menuItemStr);
        this.abstractFile = abstractFile;
    }

    @Override
    @org.openide.util.NbBundle.Messages({"HashLookupAction.submitError.message=Failed to submit hash to PolySwarm.",
        "HashLookupAction.dbError.message=Failed to record hash in pending hashes database.",
        "HashLookupAction.alreadyPending.message=Hash lookup is already in progress.",
        "HashLookupAction.messageDialog.title=PolySwarm Hash Lookup"})
    public void actionPerformed(ActionEvent event) {
        if (abstractFile != null) {
            addPendingHashLookup(abstractFile);
        }
    }

    /**
     * Write the new hash lookup to the DB
     */
    private void addPendingHashLookup(AbstractFile abstractFile) {
        try {
            String md5Hash = abstractFile.getMd5Hash();
            Long abstractFileId = abstractFile.getId();
            SwarmItDb instance = SwarmItDb.getInstance();
            if (!instance.isPendingHashLookup(abstractFileId)) {
                // add hash to pending search
                instance.newPendingHashLookup(abstractFileId, md5Hash);
                LOGGER.log(Level.FINE, String.format("Added hash search to pending db: abstractFileId: %s, md5Hash: %s.",
                abstractFileId.toString(),
                md5Hash));
            } else {
                LOGGER.log(Level.INFO, "Hash is already pending, not re-submitting.");
                JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                    Bundle.ScanAction_alreadyPending_message(),
                    Bundle.ScanAction_messageDialog_title(),
                    JOptionPane.ERROR_MESSAGE);
            }

        } catch (SwarmItDbException ex) {
            LOGGER.log(Level.SEVERE, "Error adding new hash lookup to sqlite db.", ex);
            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                    Bundle.HashLookupAction_dbError_message(),
                    Bundle.HashLookupAction_messageDialog_title(),
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
