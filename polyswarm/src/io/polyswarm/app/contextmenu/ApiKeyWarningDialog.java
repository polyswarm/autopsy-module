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

import javax.swing.JOptionPane;
import org.netbeans.api.options.OptionsDisplayer;
import org.openide.windows.WindowManager;

/**
 *
 * @author rl
 */
public class ApiKeyWarningDialog {

    @org.openide.util.NbBundle.Messages({"ApiKeyWarningDialog.title=API Key Required.",
        "ApiKeyWarningDialog.message=Missing required API Key. Please open the options menu and enter a key."})
    public static void show() {
        int response = JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(),
                Bundle.ApiKeyWarningDialog_message(),
                Bundle.ApiKeyWarningDialog_title(),
                JOptionPane.ERROR_MESSAGE);

        if (response == JOptionPane.YES_OPTION) {
            OptionsDisplayer.getDefault().open("io.polyswarm.app.optionspanel");
        }
    }
}
