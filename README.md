Saltr Android SDK
=============

This is the README file of the SALTR Android SDK.

To check out the project from GitHub:
<a href="https://github.com/plexonic/saltr-android-sdk.git">https://github.com/plexonic/saltr-android-sdk.git</a>

To download the latest ZIP:
<a href="https://github.com/plexonic/saltr-android-sdk/archive/master.zip">https://github.com/plexonic/saltr-android-sdk/archive/master.zip</a>



CONTENTS
========
1. INTRODUCTION
2. USAGE
3. PACKAGES
4. DOCUMENTATION

----

1. INTRODUCTION
===============

Saltr Android SDK is a library of classes which will help you to develop mobile
games that are integrated with SALTR platform.

SDK performs all necessary and possible action with SALTR REST API to connect, update, set 
and download data related to application's or game's  features or levels.

All data received from SALTR REST API is parsed and represented through set of classes, 
each carrying specific object and its properties.

Basically SDK, as the REST API, has few simple actions. The most important one is to connecting, 
which loads the app data objects containing features, experiments and level headers.

This and other actions will be described in the sections below.


2. USAGE
========

To use the SDK you need to download/checkout SDK repository, and then import files to your
project.

The recommended IDE's for Flash/ActionScript projects are Adobe Flash Builder or IntelliJ Idea.

The entry point in SDK is SLTSaltr.java.

Note: All classes in the package start with "SLT" prefix.

3. PACKAGES
======================

The SDK's library classes have the following packages:

- saltr - main and root package for the library;
- saltr.game - the game related classes contained here;
- saltr.game.cavas2d - classes related to 2D games;
- saltr.game.matching - classes related to matching or board based games;
- saltr.game.repository - local data repository classes (implementation widely varies with the platform);
- saltr.game.status - status classes representing warnings and error statuses withing library code;
- saltr.game.response - here are the set of classes that map with response data received from SALTR;


4. DOCUMENTATION
================

Detailed documentation for all public classes and methods is coming soon.
