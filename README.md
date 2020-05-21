# PolySwarm Autopsy Module

## Installation

This module only works with Autopsy, so you need to have that installed before installing this module.
You can find Autopsy [here](https://www.autopsy.com/download/)

New release of this module are published on github.
You can find them [here](https://github.com/polyswarm/autopsy-module/releases/)

### Steps

1. Download the latest release. You want the file with extension `.nbm`.
1. After the module has download, open Autopsy.
1. Using the toolbar at the top select `Tools` then `Plugins`.
1. Go to the `Downloaded` tab in the Plugins window.
1. Click `Add Plugin`, then browse to the module you downloaded earlier.
1. Click Install
1. Follow prompts on the Plugin Installer
1. When the validation warning pops up, hit continue.
1. Restart Autopsy.

At this point you just need an API key to get started.


## Get Your API Key

The PolySwarm Autopsy Module requires an API Key to get valid responses from PolySwarm.
You can get an API key from PolySwarm at [https://polyswarm.network](https://polyswarm.network)

1. Sign up for an account with PolySwarm on polyswarm.network
1. Optionally create a team if working together with others.
1. From your account, or the team account, select `Settings` => `API Keys`
1. Copy any API key from your/your team's list

Once you have your API Key copied, go back into Autopsy

1. Select `Tools` => `Options`
1. Go to `SwarmIt` and enter your API key in the proper text field
1. `Apply`, then `Ok`

You should now be ready to use the PolySwarm Autopsy Module

## Using PolySwarm Module

### Scan File

Scanning files requires an open Case, with some files.

Right click the intended file, and click `Scan on PolySwarm`.
This will send the file to PolySwarm where it will be scanned.
Scans take a minimum of 25 seconds, but the delay can increase if PolySwarm is under load.

After the scan completes, a new `Extracted Content` field named `PolySwarm Results` will appear.
You can view details of ther scan there.
Subsequent scans will be reported under the same `PolySwarm Results`, but as a separate result.

Alternatively, you can view the results in the results tab at the bottom of the `Listing` view.


### Hash Lookup

Hash Lookup requires an open Case with some files, that have been hashed.
Specially, it requires an md5 hash to have been generated for the file already.

Right click the intended file, and click `Lookup Hash on PolySwarm` to start a Hash Lookup.
It should complete in a couple of seconds.

The results from a Hash lookup are shown in the same `PolySwarm Results` described above.
If the file has not been seen before by PolySwarm, it will be labeled `Not Found`.

## Features

### Right-click options to Scan or Hash Lookup on PolySwarm

When an Autopsy case is open and has ingested files, two new options show up on right clicks: `Scan On PolySwarm` and `Hash Lookup on PolySwarm`.

### Custom artifact type

Results from PolySwarm are shown in a new leaf in the tree under `Extracted Content` called `PolySwarm Results`.
Each scan/hash lookup creates new artifact under that heading, so past results are isolated and immutable.

### PolyScore™

All scans and hash searches now include the PolyScore.
Find out more about PolyScore on [https://polyswarm.network](https://polyswarm.network).

### Assertions

All assertions are displayed as a list of `author, verdict` pairs.
No more guesswork why PolySwarm in figuring out if a file is malicious.

### Malware Families

Displays a list of malware families that engines have provided with their assertions.

### Tags

Presents tags that PolySwarm has identified to kickstart analysis.

### Known bad

Marks malicious files as `Known Bad` based on PolyScore and Assertion results.
