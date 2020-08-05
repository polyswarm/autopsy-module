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
package io.polyswarm.app.optionspanel;

import java.util.logging.Logger;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

final class PolySwarmPanel extends javax.swing.JPanel {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(PolySwarmPanel.class.getName());
    private final PolySwarmOptionsPanelController controller;
    private final PolySwarmMarketplaceSettings settings;

    PolySwarmPanel(PolySwarmOptionsPanelController controller) {
        this.controller = controller;
        this.settings = new PolySwarmMarketplaceSettings();

        initComponents();
        customizeComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        popupMenu1 = new java.awt.PopupMenu();
        jOptionPane1 = new javax.swing.JOptionPane();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        optionsPanel = new javax.swing.JPanel();
        apiKeyPanel = new javax.swing.JPanel();
        apiKeyErrorMsgLabel = new javax.swing.JLabel();
        apiKeyTextField = new javax.swing.JTextField();
        jTextField1 = new javax.swing.JTextField();
        advanceOptionsPanel = new javax.swing.JPanel();
        communityPanel = new javax.swing.JPanel();
        communityErrorMsgLabel = new javax.swing.JLabel();
        communityTextField = new javax.swing.JTextField();

        popupMenu1.setLabel(org.openide.util.NbBundle.getMessage(PolySwarmPanel.class, "PolySwarmPanel.popupMenu1.label")); // NOI18N

        apiKeyPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, org.openide.util.NbBundle.getMessage(PolySwarmPanel.class, "PolySwarmPanel.apiKeyTitle.text"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 12))); // NOI18N
        apiKeyPanel.setFont(new java.awt.Font("Monospaced", 0, 13)); // NOI18N
        apiKeyPanel.setName(org.openide.util.NbBundle.getMessage(PolySwarmPanel.class, "PolySwarmPanel.apiKeyTitle.text")); // NOI18N

        apiKeyErrorMsgLabel.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        apiKeyErrorMsgLabel.setForeground(new java.awt.Color(255, 0, 0));
        org.openide.awt.Mnemonics.setLocalizedText(apiKeyErrorMsgLabel, org.openide.util.NbBundle.getMessage(PolySwarmPanel.class, "PolySwarmPanel.apiKeyErrorMsgLabel.text")); // NOI18N

        apiKeyTextField.setText(org.openide.util.NbBundle.getMessage(PolySwarmPanel.class, "PolySwarmPanel.apiKeyTextField.text")); // NOI18N
        apiKeyTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                apiKeyTextFieldActionPerformed(evt);
            }
        });

        jTextField1.setEditable(false);
        jTextField1.setBackground(java.awt.SystemColor.menu);
        jTextField1.setFont(new java.awt.Font("sansserif", 1, 12)); // NOI18N
        jTextField1.setForeground(new java.awt.Color(0, 0, 0));
        jTextField1.setText(org.openide.util.NbBundle.getMessage(PolySwarmPanel.class, "PolySwarmPanel.apiKeyHelpLabel.text")); // NOI18N
        jTextField1.setBorder(null);

        javax.swing.GroupLayout apiKeyPanelLayout = new javax.swing.GroupLayout(apiKeyPanel);
        apiKeyPanel.setLayout(apiKeyPanelLayout);
        apiKeyPanelLayout.setHorizontalGroup(
            apiKeyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(apiKeyPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(apiKeyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(apiKeyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(apiKeyTextField)
                        .addComponent(apiKeyErrorMsgLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 586, Short.MAX_VALUE))
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        apiKeyPanelLayout.setVerticalGroup(
            apiKeyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(apiKeyPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(apiKeyTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(apiKeyErrorMsgLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout optionsPanelLayout = new javax.swing.GroupLayout(optionsPanel);
        optionsPanel.setLayout(optionsPanelLayout);
        optionsPanelLayout.setHorizontalGroup(
            optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(optionsPanelLayout.createSequentialGroup()
                .addComponent(apiKeyPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 287, Short.MAX_VALUE))
        );
        optionsPanelLayout.setVerticalGroup(
            optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(optionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(apiKeyPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        apiKeyPanel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PolySwarmPanel.class, "PolySwarmPanel.apiKeyTitle.text")); // NOI18N
        apiKeyPanel.getAccessibleContext().setAccessibleDescription("");

        jTabbedPane2.addTab(org.openide.util.NbBundle.getMessage(PolySwarmPanel.class, "PolySwarmPanel.optionsPanel.TabConstraints.tabTitle"), optionsPanel); // NOI18N

        communityPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, org.openide.util.NbBundle.getMessage(PolySwarmPanel.class, "PolySwarmPanel.communityPanel.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 12))); // NOI18N
        communityPanel.setFont(new java.awt.Font("Monospaced", 0, 13)); // NOI18N

        communityErrorMsgLabel.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        communityErrorMsgLabel.setForeground(new java.awt.Color(255, 0, 0));
        org.openide.awt.Mnemonics.setLocalizedText(communityErrorMsgLabel, org.openide.util.NbBundle.getMessage(PolySwarmPanel.class, "PolySwarmPanel.communityErrorMsgLabel.text")); // NOI18N

        communityTextField.setText(org.openide.util.NbBundle.getMessage(PolySwarmPanel.class, "PolySwarmPanel.communityTextField.text")); // NOI18N

        javax.swing.GroupLayout communityPanelLayout = new javax.swing.GroupLayout(communityPanel);
        communityPanel.setLayout(communityPanelLayout);
        communityPanelLayout.setHorizontalGroup(
            communityPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(communityPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(communityTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 586, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, communityPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(communityErrorMsgLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 586, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        communityPanelLayout.setVerticalGroup(
            communityPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, communityPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(communityTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(communityErrorMsgLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout advanceOptionsPanelLayout = new javax.swing.GroupLayout(advanceOptionsPanel);
        advanceOptionsPanel.setLayout(advanceOptionsPanelLayout);
        advanceOptionsPanelLayout.setHorizontalGroup(
            advanceOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(advanceOptionsPanelLayout.createSequentialGroup()
                .addComponent(communityPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 287, Short.MAX_VALUE))
        );
        advanceOptionsPanelLayout.setVerticalGroup(
            advanceOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(advanceOptionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(communityPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(60, Short.MAX_VALUE))
        );

        communityPanel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PolySwarmPanel.class, "PolySwarmPanel.communityTitle.text")); // NOI18N

        jTabbedPane2.addTab(org.openide.util.NbBundle.getMessage(PolySwarmPanel.class, "PolySwarmPanel.advanceOptionsPanel.TabConstraints.tabTitle"), advanceOptionsPanel); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane2)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(193, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void apiKeyTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_apiKeyTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_apiKeyTextFieldActionPerformed

    private void customizeComponents() {
        // read settings and initialize GUI
        communityTextField.setText(settings.getCommunity());
        apiKeyTextField.setText(settings.getApiKey());

        // listen to changes in form fields and call controller.changed()
        communityTextField.getDocument().addDocumentListener(new MyDocumentListener());
        apiKeyTextField.getDocument().addDocumentListener(new MyDocumentListener());
    }

    private void clearErrorMessages() {
        // clear default error msgs
        communityErrorMsgLabel.setText("");
        apiKeyErrorMsgLabel.setText("");
    }

    void load() {
        communityTextField.setText(settings.getCommunity());
        apiKeyTextField.setText(settings.getApiKey());
        clearErrorMessages();
        valid();
    }

    void store() {
        // store modified settings
        settings.setCommunity(communityTextField.getText());
        settings.setApiKey(apiKeyTextField.getText());
        settings.saveSettings();
    }

    /**
     * Validate the API Key. Set error message if invalid.
     *
     * @return true if valid, else false
     */
    boolean validApiKey() {
        boolean isValid = settings.validateApiKey(apiKeyTextField.getText());
        if (!isValid) {
            apiKeyErrorMsgLabel.setText(NbBundle.getMessage(this.getClass(), "PolySwarmPanel.apiKeyErrorMsgLabel.text"));
        }

        return isValid;
    }

    /**
     * Validate the API Key. Set error message if invalid.
     *
     * @return true if valid, else false
     */
    boolean validCommunity() {
        boolean isValid = settings.validateCommunity(communityTextField.getText());
        if (!isValid) {
            communityErrorMsgLabel.setText(NbBundle.getMessage(this.getClass(), "PolySwarmPanel.communityErrorMsgLabel.text"));
        }

        return isValid;
    }

    /**
     * Validate the fields are completed correctly and that a connection test was successful.
     *
     * @return true if all valid and successful test, else false
     */
    boolean valid() {
        // check whether form data is complete and has valid content
        // check each element so we get error nofications for all invalid fields.
        boolean result = true;

        result &= validApiKey();
        result &= validCommunity();

        return result;
    }

    /**
     * Used to listen for changes in text boxes. It lets the panel know things have been updated and that validation
     * needs to happen.
     */
    private class MyDocumentListener implements DocumentListener {

        @Override
        public void changedUpdate(DocumentEvent e) {
            // Plain text components do not fire these events
            clearErrorMessages();
            controller.changed();
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            clearErrorMessages();
            controller.changed();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            clearErrorMessages();
            controller.changed();
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel advanceOptionsPanel;
    private javax.swing.JLabel apiKeyErrorMsgLabel;
    private javax.swing.JPanel apiKeyPanel;
    private javax.swing.JTextField apiKeyTextField;
    private javax.swing.JLabel communityErrorMsgLabel;
    private javax.swing.JPanel communityPanel;
    private javax.swing.JTextField communityTextField;
    private javax.swing.JOptionPane jOptionPane1;
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JPanel optionsPanel;
    private java.awt.PopupMenu popupMenu1;
    // End of variables declaration//GEN-END:variables
}
