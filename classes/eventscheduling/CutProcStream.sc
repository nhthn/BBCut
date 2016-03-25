//This file is part of The BBCut Library. Copyright (C) 2001  Nick M.Collins distributed under the terms of the GNU General Public License full notice in file BBCutLibrary.help

//BBCutProc wrapper, following BBCut2 class code for backwards compatability

//just a simple solution for now, would ultimately replace the cut composers with Event, Pbind style system

CutProcStream : Stream {
    var <>proc;
    var phraseprop, offset, isroll; //backwards compatability
    var cache;
    var blocklength, blocknum,phrasepos, cutnum; //block data

    *new { arg proc;
        ^super.new.proc_(proc).init;
    }

    init {
        cache=LinkedList.new;
    }


    //Pbind Pattern

    next {arg inval;
        var nextcut;

        if(cache.isEmpty, {
            this.refillcache;
        });

        nextcut=cache.popFirst;

        inval.use({

            ~dur= nextcut[0]; //ioi
            ~sustain= nextcut[1]/nextcut[0];
            ~offset= (nextcut[2]) ? offset; //usually nil because offsets determined by cut synthesiser
            ~amp= nextcut[3]*0.1; //Julian R's comment: default to 0.1 as max volume
            ~blocklength= blocklength;
            ~blocknum= blocknum;
            ~cutnum=cutnum;
            ~phrasepos= phrasepos;
            ~phraseprop= phraseprop;
            ~isroll= isroll;
        });

        cutnum=cutnum+1;

        ^inval;
    }

    //if cache empty
    refillcache {
        var cuts;

        //reset offset to receive another
        if(offset.notNil,{offset=nil;});

        proc.chooseblock;

        cuts= proc.cuts;

        //backwards compatability
        if(not(cuts[0].isKindOf(Array)),{

            cuts= Array.fill(cuts.size,{arg i; [cuts[i],cuts[i], nil,1.0]});
        });

        cache= cache++cuts;

        blocklength=proc.blocklength; //in beats
        blocknum=proc.block;
        phrasepos=proc.phrasepos-blocklength; //this is phrasepos at start of block, for offset calc

        cutnum=0;
    }


}