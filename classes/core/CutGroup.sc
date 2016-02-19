//This file is part of The BBCut Library. Copyright (C) 2001  Nick M.Collins distributed under the terms of the GNU General Public License full notice in file BBCutLibrary.help

//CutGroup 27/12/04  by N.M.Collins

//could have a permanent group (for cutsynth setup) and a temporary group for synths which can be
//aborted without losing setup synths

//require a mixgroup, synthgroup,fxgroup to avoid trouble
//but what about cut based functions that don't do synthesis themselves? How to avoid a CutGroup entirely?
//possibly unrealistic to think you can...

//no need to pass in Server- the group knows this

CutGroup {
    var <group,<bus,<index,<numChannels,<server;
    var <>cutsynths;
    var <synthgroup, <fxgroup, <mixergroup; //separate out different cutsynths
    var freebus, freegroup;

    *new {arg cutsynths,group,bus,numChannels;

        ^super.new.initCutGroup(cutsynths,group,bus,numChannels);
    }

    initCutGroup { arg cs,g,b,chan;
        var cutmixer;
        cutsynths=cs; //do still works even if singleton
        //if(cs.isKindOf(Array),{
        //		cutsynths=cs;
        //	},{
        //		//put in array
        //		cutsynths= [cs]; //[cs ?? {CutSynth.new}];
        //	});

        freegroup=false;

        //server=s ?? {Server.default};
        group=g ?? {freegroup=true; Group.new;};

        server=group.server;

        synthgroup=Group.head(group);
        fxgroup=Group.after(synthgroup);
        mixergroup=Group.after(fxgroup);

        numChannels=chan ? 1; //may only support mono

        freebus=false; //don't free if bus passed in, responsibility of client code
        bus= b ?? {

            //must free eventually if not passed in
            freebus=true;

            Bus.audio(server,numChannels);

        };//{Bus.new(\audio,0,numChannels,server)}; //defaults
        index=bus.index;

        //Post << [\cutgroupindex,index, \numchan, numChannels]<< nl;


        //check how many allocated already
        //[\busallocated, index].postln;

        //always create an appropriate Mixer, stereo or mono?

        // If cutsynths isn't an array, make it into a single-element array
        if(cutsynths.isKindOf(Array).not, {
            cutsynths = [cutsynths];
        });

        //if there's no CutMixer, make one so that defaults are heard on main out rather than in hidden bus
        if(cutsynths.any(_.isKindOf(CutMixer)).not, {
            cutmixer = CutMixer(0, 1.0, 1.0, 0.0);
            cutsynths = cutsynths.add(cutmixer);
        });

        //setup for cutsynths
        // All cutsynths are initialized after adding the CutMixer.
        // Some cutsynths such as CutFXSwap1 need to know the entire cutsynth list when they are initialized.
        cutsynths.do({ |cutsynth|
            cutsynth.initCutSynth(this);
        });

    }

    renderBlock {arg block,clock;

        cutsynths.do({arg cutsynth; cutsynth.renderBlock(block,clock)});

    }

    //assumes everything within this group, doesn't bother with any destructors
    //for individual cutsynths
    free {
        //group.freeAll;

        cutsynths.do({arg cutsynth; cutsynth.free});

        //in case anything left over
        //group.free;

        if(freebus, {

            //["freeing bus ",bus.index].postln;

            bus.free;});

        if(freegroup, {

            //["freeing bus ",bus.index].postln;

            group.free;});

    }

    //addcutsynth

    add{arg cutsynth;

        cutsynth.initCutSynth(this);

        //Array so must allow for growing
        cutsynths=cutsynths.add(cutsynth);

    }

    //removecutsynth, will free when removed, though some to be scheduled messages might still refer to it
    removeAt {arg ind;
        var removed;
        //Array so must allow for growing
        removed=cutsynths.removeAt(ind);
        removed.free;
    }

    amp_ {arg amp=0.1;

        cutsynths.do({arg cutsynth;

            if(cutsynth.isKindOf(CutMixer),{cutsynth.volume_(amp)});

        });

    }

    pan_ {arg pan=0.0;

        cutsynths.do({arg cutsynth;

            if(cutsynth.isKindOf(CutMixer),{cutsynth.panfunc_(pan)});

        });

    }


}