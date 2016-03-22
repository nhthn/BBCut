//This file is part of The BBCut Library. Copyright (C) 2001  Nick M.Collins distributed under the terms of the GNU General Public License full notice in file BBCutLibrary.help

//CutTrig1 07/1/05  by N.M.Collins

//each block triggers a sample from a set of buffers buffunc, with prob of single or rolls to follow the block- always playback from start of buf, assumes each has a length

CutTrig1 : CutSynth {
    var <>bufarray, <>buffunc, <>rollprob, <>pbsfunc, <>dutycycle;
    var playdef;

    *initClass {
        StartUp.add({

            2.do({arg i;

                SynthDef(\ct1playbuf++((i+1).asSymbol),{arg bufnum=0,outbus=0,rate=1,dur=0.1;
                    var playbuf, env;

                    playbuf= PlayBuf.ar(i+1,bufnum,BufRateScale.kr(bufnum)*rate,1,0,0);

                    env= EnvGen.ar(Env.linen(0.0,dur,0.0,1.0,0),doneAction:2);

                    Out.ar(outbus,playbuf*env);
                }).add;
            });

        });
    }

    //will get Server from BBCutGroup later
    *new{arg bufarray, buffunc, rollprob, pbsfunc, dutycycle;

        ^super.new.initCutTrig1(bufarray, buffunc, rollprob, pbsfunc, dutycycle);
    }

    initCutTrig1 {arg ba,bf,rp,pf,dc;

        bufarray=ba;
        buffunc=bf;
        rollprob=rp ? 0.25;

        if ((rollprob.size)!=(bufarray.size),{rollprob= Array.fill(bufarray.size, {rollprob;})});
        pbsfunc=pf ? 1.0;
        dutycycle= dc ? 1.0;
    }

    setup {
        playdef= \ct1playbuf++(cutgroup.numChannels.asSymbol);
    }

    renderBlock {arg block,clock;
        var bufindex, bufnum, len;

        pbsfunc.tryPerform(\updateblock,  block.blocknum, block.phraseprop, block.cuts, block.isroll);
        dutycycle.tryPerform(\updateblock,  block.blocknum, block.phraseprop, block.cuts, block.isroll);

        bufindex= buffunc.value(block);

        bufnum= bufarray[bufindex].bufnum;

        len= bufarray[bufindex].length; //take account of any repitch?

        if(rollprob[bufindex].coin,{

            block.cuts.do({arg cut,i;
                //var dur;

                //dur=len.min(cut[1]);

                //grain so -1 for id
                block.msgs[i].add([\s_new, playdef, -1, 0,cutgroup.group.nodeID,\outbus,cutgroup.index,\bufnum,bufnum, \rate,(pbsfunc.value(i,block)),\dur, len*(dutycycle.value(i,block))]);

            });

        },{	//cb2playbuf
            block.msgs[0].add([\s_new, playdef, -1, 0,cutgroup.group.nodeID,\outbus,cutgroup.index,\bufnum,bufnum, \rate,(pbsfunc.value(0,block)),\dur, len*(dutycycle.value(0,block))]);

        });

        //don't need to return block, updated by reference
    }


}