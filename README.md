# PolySwarm Autopsy Module

## Installation

After installing Autopsy you can install this plugin.

You can find the official release on github [here](https://github.com/polyswarm/autopsy-module/releases/)

1. Download the latest release `.nbm`.
1. This is the NetBeans module file format that Autopsy supports.
1. After the module has download, open Autopsy.
1. Using the toolbar at the top select `Tools` then `Plugins`.
1. Go to the `Downloaded` tab in the Plugins window.
1. Click Add Plugin, then navigate to the nbm file you downloaded earlier.
1. Click Install
1. Follow prompts on the Plugin Installer
1. When the validation warning pops up, hit continue.
1. Restart Autopsy


## Get Your API Key

The PolySwarm Autopsy Module requires an API Key to get valid responses from PolySwarm.
You can get an API key from [PolySwarm](https://polyswarm.network)

1. Sign up for an account with PolySwarm on polyswarm.network
1. Optionally create a team if working together with others.
1. From your accont, or the team account select `Settings` => `API Keys`
1. Copy any API key from your/your team's list

Once you have your API Key copied, go back into Autopsy

1. Select `Tools` => `Options`
1. Go to `SwarmIt` and enter your API key in the proper text field
1. Apply, then Ok

You should now be ready to use the PolySwarm Autopsy Module

## Using PolySwarm Module

### Scan File

Scanning files requires an open Case, with some files.

Simply right click the intended file, and click `Scan on PolySwarm`.
This will send the file to PolySwarm where it will be scanned.
Scans take a minimum of 25 seconds, but the delay can increase if PolySwarm is under load.

After the scan completes, a new `Extracted Content` field named `PolySwarm Results` will appear.
Subsequent scans will increase the number to it's right.
Click on the `PolySwarm Results` name in the tree to see results.
This will show all the results from all the scans in this case.

Alternatively, you can view the results in the results tab at the bottom of the `Listing` view.


### Hash Lookup

Hash Lookup requires an open Case with some files, that have been hashed.
Specially, it requires an md5 hash to have been generated for the file already.

To start a Hash Lookup simply right click the intended file, and click `Lookup Hash on PolySwarm`.
The hash lookup should complete in a couple of seconds.

The results form a Hash lookup are show in the `PolySwarm Results` artifact from scanning.
The details are the same, with one exception.
If PolySwarm has not seen the file with that hash before, it will add a not found comment.

## Features

### Right-click options to Scan or Hash Lookup on PolySwarm

When an Autopsy case is open and has ingested files, two new options show up on right clicks: `Scan On PolySwarm` and `Hash Lookup on PolySwarm`.

### Custom artifact type

Results from PolySwarm are shown in a new leaf in the tree under `Extracted Content` call `PolySwarmResults`.
Each scan/hash lookup creates new artifact under that heading, so past results are isolated and immutable.

### PolyScore™

All scans and hash searches now include the PolyScore.
Find out more about PolyScore on `https://polyswarm.network`.

### Assertions

Received a list of all assertions un Author:verdict pairs.
No guesswork why PolySwarm deemed a file malicious.

### Malware Families

Displays a list of malware families that engines have provided with their assertions.

### Tags

Get tags PolySwarm has added to kickstart analysis.

### Known bad

The PolySwarm Autopsy Module will mark files  as `Known Bad` based on PolyScore and Assertion results
