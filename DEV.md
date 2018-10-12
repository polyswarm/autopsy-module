# Setting up your module development environment.

Development needs to happen on a Windows host (Windows 7 is what I've been using).

Autopsy API documentation is here: https://sleuthkit.org/autopsy/docs/api-docs/4.8.0/

How to setup a development environment is covered here: https://sleuthkit.org/autopsy/docs/api-docs/4.8.0/mod_dev_page.html

## Install Java 8 SE JDK

Autopsy uses Java 8, so get the Windows x64 JDK.

https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html

## Install Autopsy

Since we are only building a module, we do not need to compile/build the autopsy/tsk source. We can simply install the latest version of Autopsy and use the Platform that it comes with. I've been using Autopsy 4.8.0 64bit for windows. I installed it with the .msi. If you want any of the Autopsy source code for easy browsing in NetBeans, then you can also grab the .zip.

Let it install into the default path.

https://www.sleuthkit.org/autopsy/download.php

## Install NetBeans

Autopsy is all NetBeans all day, so install the latest version of NetBeans. I've been using version 8.2 Java EE variant.

https://netbeans.org/downloads/

## Install Git Client

Netbeans has a built-in git client, so you can use that if you want. If you don't, grab Git for Windows.

https://gitforwindows.org/

## Clone repo

Clone this repo to your dev box.

## Open this project in NetBeans

When you open NetBeans, choose to open an existing project and select the swarmit subdir.

## Libs

I created a folder called "thirdparty" for all external libs that SwarmIt needs. When they were added to the SwarmIt project, NetBeans copied them to the releases/modules/ext/ folder. So we technically have 2 copies of them. I kept both in the repo to just make thing hopefully easier.

If you let Autopsy install into the default path, the link to the Autopsy Platform should work. If you didn't, you may need to update the libs/plaform settings on the SwarmIt project.

## Testing

In NetBeans, you can use the Run menu to Clean, Build, and Run the project. When you run the project, it will start Autopsy with the module already added.

Once Autopsy is started, you can go to the Tools -> Options menu to see the Options Panel. You can create a new case to kick off the background thread. In an open case, you can ingest some files so that there will be a right-click menu available to Swarmit.

You can work with the Options Panel regardless of a case being open. But the rest of the SwarmIt functionality is only available when a case is open.

When done testing Autopsy, just close it and you are back to NetBeans.

When Autopsy is running the log output is in the lower "Output" pane of the NetBeans window.

Note: I have not added any disk images or testing files to the repo. Autopsy allows you to add individual files or a folder of files to an Autopsy case, so you don't need a disk image for testing. We need to create a test files directory in the repo and have at least an EICAR file and a couple of other files to use for testing.


