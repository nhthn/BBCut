# BBCut #

BBCut is a SuperCollider quark originally by Nick Collins for automated event analysis, beat induction and algorithmic audio splicing. Its terse syntax and hot-swappable design make it suitable for live coding applications.

## Installation ##

BBCut is available as a quark: `Quarks.install("BBCut")`. Alternatively, copy this directory into `Platform.userExtensionDir`. Either way, sc3-plugins is required.

The `sounds/` directory contains two audio files, `break.aiff` and `break2.aiff`. Many examples assume that these are in `Platform.resourceDir +/+ "sounds/"`, so copy them over there if you want the examples to run out of the box. This step is optional.
