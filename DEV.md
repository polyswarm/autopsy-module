# Setting up your module development environment.

Development needs to happen on a Windows host (Windows 7 is what I've been using).

Autopsy API documentation is here: https://sleuthkit.org/autopsy/docs/api-docs/4.8.0/

How to setup a development environment is covered here: https://sleuthkit.org/autopsy/docs/api-docs/4.8.0/mod_dev_page.html

## Install Java 8 SE JDK

Autopsy uses Java 8, so get the Windows x64 JDK.

You must create an Oracle account to get access to Java 8 JDk.
While some pages indicate it may cost money to access older JDK downloads, but Java 8 is free.

https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html

## Install Autopsy

Since we are only building a module, we do not need to compile/build the autopsy/tsk source. We can simply install the latest version of Autopsy and use the Platform that it comes with. I've been using Autopsy 4.15.0 64bit for windows. I installed it with the .msi. If you want any of the Autopsy source code for easy browsing in NetBeans, then you can also grab the .zip.

Let it install into the default path.

https://www.sleuthkit.org/autopsy/download.php

## Install NetBeans

Autopsy is all NetBeans all day, so install the latest version of NetBeans. I've been using version 8.2 Java EE variant.

https://netbeans.org/downloads/

### Tips and Tricks

You may want to download the latest Java JDK in addition to Java 8. Run NetBeans in the latest Java, while the project is set to Java 8.

The Netbeans executable Installer does not work with Java 14, so you will need to download the zip in that case.

## Install Git Client

Netbeans has a built-in git client, so you can use that if you want. If you don't, grab Git for Windows.

https://gitforwindows.org/

## Clone repo

Clone this repo to your dev box.

## Open this project in NetBeans

When you open NetBeans, choose to open an existing project and select the swarmit subdir.

### Tips and Tricks

If you find NetBeans stuck at the `Initializing Project` state, delete the cache to load.
The cache can be found in %USERPROFILE$/AppData/Local/NetBeans/Cache

## Set Autopsy as NetBeans Platform

We want to be able to run our module in Autopsy w/o having to start Autopsy ourselves while we are building/testing/developing this module.
To do that, we need to define Autopsy as our NetBeans Platform.

1. Right-click on the SwarmIt project
2. Go to Libraries
3. Go to Manage Platforms and add your Autopsy install as a platform.
4. Select Autopsy sa your platform.

## Libs

I created a folder called "thirdparty" for all external libs that SwarmIt needs. When they were added to the SwarmIt project, NetBeans copied them to the releases/modules/ext/ folder. So we technically have 2 copies of them. I kept both in the repo to just make thing hopefully easier.

If you have any problems with the libs throwing ClassNotFound or similar errors, you can re-add them by going to Properties -> Libraries -> Wrapped JARs.
In that window, use the Add JAR button to select all of the jars in the thirdparty folder.

## Testing

In NetBeans, you can use the Run menu to Clean, Build, and Run the project. When you run the project, it will start Autopsy with the module already added.

Once Autopsy is started, you can go to the Tools -> Options menu to see the Options Panel. You can create a new case to kick off the background thread. In an open case, you can ingest some files so that there will be a right-click menu available to Swarmit.

You can work with the Options Panel regardless of a case being open. But the rest of the SwarmIt functionality is only available when a case is open.

When done testing Autopsy, just close it and you are back to NetBeans.

When Autopsy is running the log output is in the lower "Output" pane of the NetBeans window.

Note: I have not added any disk images or testing files to the repo. Autopsy allows you to add individual files or a folder of files to an Autopsy case, so you don't need a disk image for testing. We need to create a test files directory in the repo and have at least an EICAR file and a couple of other files to use for testing.


