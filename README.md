# autopsy-module
Module for Autopsy to enable forensic analysts to submit files into PolySwarm marketplace for analysis.

# Features

## Right-click option to submit to PolySwarm (SwarmIt)

When an Autopsy case is open and has ingested files, a new right-click menu option is now available. This option is called "SwarmIt", when clicked, the file will be submitted to the PolySwarm marketplace for analysis.

## Custom artifact type

When the results are returned from the PolySwarm marketplace for a submission, a new subtree will be added to the left result pane under Results -> Extracted Content, called "PolySwarm Results".
Under this sub-tree, you can click on the PolySwarm Result node and the upper right frame of the Autopsy UI will display all of the files and results that have been submitted to the PolySwarm marketplace.
If you click on one of the files in the upper right frame, it will populate a table in the bottom right.
This table will show up to three comments that inform the user about the file & state as it progresses through a PolySwarm bounty.

### Assertions: [verdict]

If any assertion identifies the file as malicious, this field will show `Assertions: Malicious`.
Otherwise, `Assertions: NonMalicious`

### Votes: [verdict]

Votes is a bit more complex.
If the arbiters reach a quroum, this will be the majority response.
If the arbiters fail to reach a quorum, this will be identified as malicious, if even one arbiter votes malicious.
You can tell if they reached a quorum by the presense of the `Quorum: [verdict]` comment.
If it exists, they reached a quorum, if it does not exists, they did not.
Like assertions, the comment will display as either `Votes: Malicious` or `Votes: NonMalicious`.

### Quorum: [verdict]

If the arbiters reached a quorum on the file, this comment will show in the table.
It will show as either `Quorum: Malicious` or `Quorum: NonMalicious` based on the majority vote.

## Known bad

If a file is submitted to PolySwarm and the arbiters reach a quorum identifying the file as "malicious", it will be tagged Known Bad.

## Options Panel

In the Options Panel, there is a purple "P" logo for this module. On that options panel, the user can set the parameters of the SwarmIt module.

### Submission API URL

This is the url of the hosted service that will submit the user's files to polyswarm.

*This must be a valid service.*

### API Key

Consumer on the PolySwarm staging and production environment is protected via API Key.
Keys can be obtained from the PolySwarm team directly.

If targeting a development environment, such as orchestration, leave this field blank.

### NCT Amount to pay per submisison.

*Note: currently NCT amount is visible, but not implemented, so you can ignore it.*
