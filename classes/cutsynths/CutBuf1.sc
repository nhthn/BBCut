//This file is part of The BBCut Library. Copyright (C) 2001  Nick M.Collins distributed under the terms of the GNU General Public License full notice in file BBCutLibrary.help

//CutBuf1 26/12/04  by N.M.Collins

//draft class for offset playback from a buffer based on blocks
//mono only, no enveloping at the moment, no ampfunc panfunc etc

//permanently existing looper, with retrigger to new offsets

CutBuf1 : CutSynth {
    var synthid, trigbus;
    var <>bbcutbuf, <>offset;

    *initClass {

        //dur=0.1, EnvGen.kr(Env([1,1],[dur]),doneAction:2)

        StartUp.add({


            2.do({arg i;

                SynthDef.writeOnce(\cb1playbuf++((i+1).asSymbol),{arg bufnum=0,outbus=0,rate=1,startPos=0,trig=1;
                    var tmp;

                    tmp=PlayBuf.ar(i+1,bufnum,BufRateScale.kr(bufnum)*rate,InTrig.kr(trig),startPos,1);

                    Out.ar(outbus,tmp);

                });

            });

        });

    }

    //will get Server from BBCutGroup later
    *new{arg bbcutbuf, offset;

        ^super.new.initCutBuf1(bbcutbuf, offset);
    }

    initCutBuf1 {arg bcb, off;

        bbcutbuf=bcb;
        offset= off ? 0.0;
    }


    setup { var s;

        //"setup CutBif1!".postln;

        cutgroup.postln;

        s=cutgroup.server;

        //initialise at 0 play rate
        trigbus=Bus.control(s,1);
        synthid= s.nextNodeID;

        s.sendMsg(\s_new, \cb1playbuf++(cutgroup.numChannels.asSymbol), synthid, 0,cutgroup.synthgroup.nodeID,\outbus, cutgroup.index,\trig, trigbus.index,\bufnum,bbcutbuf.bufnum,\rate,0.0);

    }

    free {
        cutgroup.server.sendMsg(\n_free,synthid);
        trigbus.free;
    }

    //there should be a free function to release this, I guess group release will sort that out

    //could refine CPU use by rendering one playbuf for all later repeats (need to know block.length-cuts[0][0] and sending retrigger messages for each cut

    //will have bbcutgroups providing s later, miss that bit for now in draft
    renderBlock {arg block,clock;
        var startpos,rate;

        startpos= if(block.offset.isNil,{

            bbcutbuf.chooseoffset(block, offset);
        },{
            bbcutbuf.convertoffset(block);
        });

        rate=(clock.tempo)/(bbcutbuf.bps); //bps/buffertempo

        block.cuts.do({arg cut,i;
            //var dur;
            //
            //		dur=cut[1];

            block.msgs[i].add([\n_set, synthid,\startPos,startpos,\rate,rate]);

            if(trace.notNil,{trace.msg(block, i, 0.0, 0.0, \offset,startpos/(bbcutbuf.numFrames))});

            block.msgs[i].add([\c_set, trigbus.index,1]);
        });

        //don't need to return block, updated by reference
    }


}