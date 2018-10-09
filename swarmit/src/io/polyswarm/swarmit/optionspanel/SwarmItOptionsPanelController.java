/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.polyswarm.swarmit.optionspanel;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

@OptionsPanelController.TopLevelRegistration(
        categoryName = "#OptionsCategory_Name_SwarmIt",
        position = 25,
        iconBase = "io/polyswarm/swarmit/images/polyswarm-logo-32x32.png",
        keywords = "#OptionsCategory_Keywords_SwarmIt",
        keywordsCategory = "SwarmIt"
)
@org.openide.util.NbBundle.Messages({"OptionsCategory_Name_SwarmIt=SwarmIt", "OptionsCategory_Keywords_SwarmIt=PolySwarm Marketplace Scanning"})
public final class SwarmItOptionsPanelController extends OptionsPanelController {

    private SwarmItPanel panel;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private boolean changed;
    private static final Logger LOGGER = Logger.getLogger(SwarmItOptionsPanelController.class.getName());

    @Override
    public void update() {
        getPanel().load();
        changed = false;
    }

    @Override
    public void applyChanges() {
        getPanel().store();
        changed = false;
    }

    @Override
    public void cancel() {
        // need not do anything special, if no changes have been persisted yet
    }

    @Override
    public boolean isValid() {
        return getPanel().valid();
    }

    @Override
    public boolean isChanged() {
        return changed;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null; // new HelpCtx("...ID") if you have a help set
    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        return getPanel();
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        if (pcs.getPropertyChangeListeners().length == 0) {
            pcs.addPropertyChangeListener(l);
        }
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    private SwarmItPanel getPanel() {
        if (panel == null) {
            panel = new SwarmItPanel(this);
        }
        return panel;
    }

    void changed() {
        if (!changed) {
            changed = true;
            
            try {
                pcs.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "SwarmItOptionsPanelController listener threw exception on PROP_CHANGED.", e);            
            }
        }
        
        try {
            pcs.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "SwarmItOptionsPanelController listener threw exception on PROP_VALID.", e);
        }
    }
}
