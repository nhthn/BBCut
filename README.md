# BBCut2 #

BBCut2 is a SuperCollider package (both for client and server) by Nick Collins for automated event analysis, beat induction and algorithmic audio splicing. This repository forks BBCut 2.1 (the most recent version, released in 2006) and dusts it off with a new help system and several other bugfixes and enhancements.

## Installation ##

To install, copy this directory into `Platform.userExtensionDir`. This gives you all the main BBCut functionality.

The `sounds/` directory contains two audio files, `break.aiff` and `break2.aiff`. Many examples assume that these are in `Platform.resourceDir +/+ "sounds/"`, so copy them over there if you want the examples to run out of the box. This step is optional.

The ugens AutoTrack and AnalyseEvents2 are now in sc3-plugins, so they are no longer in this repository. The remaining ugen, DrumTrack, is now cross-platform, and you can compile it like so:

    cmake -DSC_PATH=/path/to/supercollider .
    make

This step is also optional. No other parts of BBCut2 depend on this ugen.