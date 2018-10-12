# autopsy-module
Module for Autopsy to enable forensic analysts to submit files into PolySwarm marketplace for analysis.

# Features

## Right-click option to submit to PolySwarm (SwarmIt)

When an Autopsy case is open and has ingested files, a new right-click menu option is now available. This option is called "SwarmIt", when clicked, the file will be submitted to the PolySwarm marketplace for analysis.

## Custom artifact type

When the results are returned from the PolySwarm marketplace for a submission, a new subtree will be added to the left result pane called "PolySwarm Results". Under this tree, each file that is submitted to PolySwarwm in this case will have it's results listed there.

## Known bad

If a file is submitted to PolySwarm and the result comes back "malicious", the file will be tagged as Known Bad.

## Options Panel

In the Options Panel, there is a purple "P" logo for this module. On that options panel, the user can set the parameters of the SwarmIt module. These include

* Submission API URL
* API Key
* NCT Amount to pay per submisison.

Note: currently the API key and NCT amount are visible, but not implemented, so you can ignore them.

