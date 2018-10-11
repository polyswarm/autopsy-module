/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.polyswarm.swarmit;

import io.polyswarm.swarmit.datamodel.SwarmItDbException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.NbBundle.Messages;
import org.sleuthkit.autopsy.casemodule.Case;
import org.sleuthkit.autopsy.casemodule.NoCurrentCaseException;
import org.sleuthkit.autopsy.core.RuntimeProperties;
import org.sleuthkit.datamodel.TskCoreException;

/**
 *
 * @author user
 */
@Messages({"SwarmItModule.moduleName=SwarmIt"})
public class SwarmItModule {
    private static final Logger LOGGER = Logger.getLogger(SwarmItModule.class.getName());
    private static final String MODULE_NAME = Bundle.SwarmItModule_moduleName();
    private static SwarmItController controller;
    private static final Object CONTROLLER_LOCK = new Object();
    
    public static SwarmItController getController() throws TskCoreException, NoCurrentCaseException, SwarmItDbException {
        synchronized (CONTROLLER_LOCK) {
            if (controller == null) {
                controller = new SwarmItController(Case.getCurrentCaseThrows());
            }
            return controller;
        }
    }

    /**
     * Called by the run method in the OnStart class.
     */
    static void onStart() {
        LOGGER.log(Level.INFO, "Starting SwarmIt Module."); // NON-NLS
        
        Case.addPropertyChangeListener(new CaseEventListener());
    }

    static String getModuleName() {
        return MODULE_NAME;
    }

    /* only static utilities, do not allow instantiation */
    private SwarmItModule() {
    }

    /**
     * Listener for case events.
     */
    static private class CaseEventListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (RuntimeProperties.runningWithGUI() == false) {
                /*
                 * Running in "headless" mode, no need to process any events.
                 * This cannot be done earlier because the switch to core
                 * components inactive may not have been made at start up.
                 */
                Case.removePropertyChangeListener(this);
                return;
            }
            SwarmItController con;
            try {
                con = getController();
            } catch (NoCurrentCaseException ex) {
                LOGGER.log(Level.SEVERE, "Attempted to access SwarmIt Module with no case open.", ex); //NON-NLS
                return;
            } catch (TskCoreException ex) {
                LOGGER.log(Level.SEVERE, "Error getting SwarmItController.", ex); //NON-NLS
                return;
            } catch (SwarmItDbException ex) {
                LOGGER.log(Level.SEVERE, "Error getting SwarmitController's database.", ex); // NON-NLS
            }
            switch (Case.Events.valueOf(evt.getPropertyName())) {
                case CURRENT_CASE:
                    synchronized (CONTROLLER_LOCK) {
                        // case has changes: reset everything
                        if (controller != null) {
                            controller.reset();
                        }
                        controller = null;

                        Case newCase = (Case) evt.getNewValue();
                        if (newCase != null) {
                            // a new case has been opened: connect db, start worker thread
                            try {
                                controller = new SwarmItController(newCase);
                            } catch (TskCoreException ex) {
                                LOGGER.log(Level.SEVERE, "Error changing case in ImageGallery.", ex);
                            } catch (SwarmItDbException ex) {
                                LOGGER.log(Level.SEVERE, "Error getting SwarmitController's database.", ex); // NON-NLS
                            }
                        }
                    }
                    break;
                default:
                    //we don't need to do anything for other events.
                    break;
            }
        }
    }

}
