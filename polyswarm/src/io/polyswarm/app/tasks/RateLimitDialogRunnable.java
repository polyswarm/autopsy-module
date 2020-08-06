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
package io.polyswarm.app.tasks;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.openide.windows.WindowManager;

/**
 * Shows a JOptionPane with a prompt to go to polyswarm.network and upgrade
 */
public class RateLimitDialogRunnable implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(RateLimitDialogRunnable.class.getName());

    final String requestName;

    public RateLimitDialogRunnable(String requestName) {
        this.requestName = requestName;
    }

    @Override
    @org.openide.util.NbBundle.Messages({"RateLimitDialogRunnable.title=Usage Limits Reached",
        "RateLimitDialogRunnable.messageFormat=Reached max uses of %s or Daily Requests. \nPlease visit https://polyswarm.network to upgrade your accord. \nWould you like to go there now?"})
    public void run() {
        int response = JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(),
                String.format(io.polyswarm.app.tasks.Bundle.RateLimitDialogRunnable_messageFormat(), requestName),
                io.polyswarm.app.tasks.Bundle.RateLimitDialogRunnable_title(),
                JOptionPane.ERROR_MESSAGE);

        if (response == JOptionPane.YES_OPTION) {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                try {
                    desktop.browse(new URI("https://polyswarm.network"));
                } catch (URISyntaxException | IOException ex) {
                    LOGGER.log(Level.SEVERE, "Cannot open https://polyswarm.network in the browser");
                }
            }

        }
    }

}
