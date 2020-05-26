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
 * Action added to right-click menu on an abstractFile.
 */
public class ScanAction extends AbstractAction {
    
    private static final long serivalVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ScanAction.class.getName());
    private final AbstractFile abstractFile;
    
    ScanAction(String menuItemStr, AbstractFile abstractFile) {
        super(menuItemStr);
        this.abstractFile = abstractFile;
    }
    
    @Override
    @org.openide.util.NbBundle.Messages({"ScanAction.dbError.message=Failed to record submission in pending submissions database.",
        "ScanAction.submitError.message=Failed to submit file to PolySwarm.",
        "ScanAction.messageDialog.title=SwarmIt",
        "ScanAction.alreadyPending.message=This file was already submitted."})
    public void actionPerformed(ActionEvent event) {

        if (abstractFile != null) {
            Long abstractFileId = abstractFile.getId();

            try {
                SwarmItDb instance = SwarmItDb.getInstance();
                // TODO: here we check to see if a file was already submitted before re-submitting
                // we should allow the user to click YES/NO to force a re-submit.
                if (!instance.isPending(abstractFileId)) {
                    // add file info to pending submissions db
                    instance.newPendingSubmission(abstractFileId);
                    LOGGER.log(Level.FINE, String.format("Added submission to pending submissions db: abstractFileId: {0}.",
                        abstractFileId.toString()));
                } else {
                    LOGGER.log(Level.INFO, "File is already pending, not re-submitting.");
                    JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                        Bundle.ScanAction_alreadyPending_message(),
                        Bundle.ScanAction_messageDialog_title(), 
                        JOptionPane.ERROR_MESSAGE);
                }

            } catch (SwarmItDbException ex) {
                LOGGER.log(Level.SEVERE, "Error adding new submission data to sqlite db.", ex);
                JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                        Bundle.ScanAction_dbError_message(),
                        Bundle.ScanAction_messageDialog_title(), 
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
