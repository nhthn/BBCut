# BBCut2 #

BBCut is a SuperCollider package (both for client and server) by Nick Collins for automated breakbeat generation. BBCut 2.1, the most recent official version, was released in December 2006. This repository forks BBCut 2.1 and dusts it off with a new help system and a few other improvements.

## Installation ##

To install, copy this directory into `Platform.userExtensionDir`. This gives you all the main BBCut functionality.

The `sounds/` directory contains two audio files, `break.aiff` and `break2.aiff`. Many examples assume that these are in `Platform.resourceDir +/+ "sounds/"`, so copy them over there if you want the examples to run out of the box. This step is optional.

The ugens AutoTrack and AnalyseEvents2 are now in sc3-plugins, so they are no longer in this repository. The remaining ugen, DrumTrack, is now cross-platform, and you can compile it like so:

    cmake -DSC_PATH=/path/to/supercollider .
    make

This step is also optional. No other parts of BBCut2 depend on this ugen.

## Using both bbcut1 and 2 ##

If you already have bbcut1, you can use both bbcut and bbcut2 on one machine, but must avoid a few class duplications:

- remove the bbcutprocs folder from the bbcut1 classes folder. 
- remove the DDSlider.sc file from the UI subfolder of part of the bbcut1 classes folder.