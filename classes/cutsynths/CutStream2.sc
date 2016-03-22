//This file is part of The BBCut Library. Copyright (C) 2001  Nick M.Collins distributed under the terms of the GNU General Public License full notice in file BBCutLibrary.help

//CutStream2 08/8/05  by N.M.Collins

//continual recording of a circular buffer, accessed according to last beat position and cutsize/startbeat


CutStream2 : CutSynth {
    var synthid;
    var <inbus,<>bbcutbuf, deallocate;
    var dutycycle, atkprop, relprop, curve;
    var offset, numbeats, pbsfunc;
    var playdef, recdef;
    var startTime;
    var length;

    *new{arg inbus, bbcutbuf, offset, pbsfunc, dutycycle, atkprop, relprop, curve;
        ^super.new.initCutStream2(inbus, bbcutbuf, offset, pbsfunc, dutycycle, atkprop, relprop, curve);
    }

    initCutStream2{arg ib, bcb, off, nb, pbs,dc,ap,rp,c;

        deallocate=false;
        inbus=ib ?? {Server.default.options.numOutputBusChannels};
        bbcutbuf = bcb; //could be nil, dealt with in setup
        offset=off ? 1;
        pbsfunc= pbs ? 1.0;
        dutycycle= dc ? 1.0;
        atkprop= ap ? 0.001;
        relprop= rp ? 0.001;
        curve= c ? 0;
    }

    setup {
        playdef= \cb2playbuf++(cutgroup.numChannels.asSymbol); //CutBuf2 playback Synths
        recdef= \cs2recordbuf++(cutgroup.numChannels.asSymbol);

        //have to wait until cutgroup known and have numChannels to create a Buffer
        bbcutbuf=bbcutbuf ?? {deallocate=true; BBCutBuffer.alloc(Server.default,Server.default.sampleRate*4,cutgroup.numChannels)};

        length= (bbcutbuf.numFrames)/bbcutbuf.sampleRate;

        //run record buf in a loop
        synthid= cutgroup.server.nextNodeID;

        //create at head? So always running before any Synth which reads from the same buffer?
        cutgroup.server.sendBundle(cutgroup.server.latency,[\s_new, recdef, synthid, 0,cutgroup.synthgroup.nodeID,\inbus,inbus,\bufnum,bbcutbuf.bufnum]);

        startTime= (Main.elapsedTime)+(cutgroup.server.latency); //so can determine record head position later on

        //Post << [\startTime, startTime, \length, length] << nl;

    }

    //change indbus and send update message to recorder synth
    inbus_ {arg in;

        inbus=in;

        cutgroup.server.sendMsg(\n_set, synthid,\inbus,inbus);
    }

    free {
        cutgroup.server.sendMsg(\n_free,synthid);
        if (deallocate,{bbcutbuf.free;});
    }




    //similar to CutBuf2 but with adjustment to find startPos from offset and lastBeat time, last tempo of clock
    renderBlock {arg block,clock;
        var startpos,rate, tmp;
        var recordheadpos, startbeat;
        var beatlength, beatstartpos;

        pbsfunc.tryPerform(\updateblock,  block);
        offset.tryPerform(\updateblock,  block);
        dutycycle.tryPerform(\updateblock,  block);
        atkprop.tryPerform(\updateblock,  block);
        relprop.tryPerform(\updateblock,  block);
        curve.tryPerform(\updateblock,  block);

        //(((Main.elapsedTime)- startTime)/44100)%(bbcutbuf.length); //update startTime occasionally for safety?
        //recbufpos at lastbeat

        //don't know length though, not set unless BBCutBuffer?  so for safety I initialise in this class

        recordheadpos= ((clock.lastBeatTime)- startTime)%(length);

        //now work on assumption that can choose anywhere within x beats before this? Have a beat base parameter

        beatlength=block.iois[0];
        beatstartpos= block.phrasepos%1.0;

        //startbeat= (beatstartpos+beatlength).asInteger; //required number of beats ago? Of course, by time start playing, have already recorded next beat (playhead can't overtake record)

        //referred to one beat past previous marked

        //if offsetpassed in, set it, within last 4 beats (need to hold stable o/w, will be some juddering with this setup)
        startbeat= beatstartpos-(if(block.offset.notNil,{(block.offset)*(offset.value(block))},{offset.value(block)})) ;

        startpos=((recordheadpos+ (startbeat*(clock.lastTempo)))%(length))*Server.default.sampleRate;

        //Post << [\lastclockbeat, clock.lastBeatTime, \recordheadpos, recordheadpos, \beatstartpos, beatstartpos, \startbeat, startbeat, \startpos, startpos] << nl;



        //use clock to find last beat Time and thus prepare a cut at a given offset startPos
        //modulo buffer length where necessary, else OK
        //this is wrong at this point, tempo may change while cached!
        //rate always 1.0 base for buffers, rate=(clock.tempo)/(bbcutbuf.bps); //bps/buffertempo

        rate=1.0;

        //["rate check", rate, clock.tempo, bbcutbuf.bps, pbsfunc.value(0,block)].postln;
        block.cuts.do({arg cut,i;
            var dur;

            dur=(cut[1])*(dutycycle.value(i,block));

            //synthid= s.nextNodeID;

            //grain so -1 for id
            block.msgs[i].add([\s_new,playdef, -1, 0,cutgroup.synthgroup.nodeID,\outbus,cutgroup.index,\bufnum,bbcutbuf.bufnum,\startPos, startpos, \rate,rate*(pbsfunc.value(i,block)),\dur, dur,\atkprop,atkprop.value(i,block),\relprop, relprop.value(i,block), \curve, curve.value(i,block)]);

            if(trace.notNil,{trace.msg(block, i,0.0,0.0, \offset,startpos/(bbcutbuf.numFrames),\repeatlength,dur)});


        });

        //don't need to return block, updated by reference
    }

    *initClass {
        StartUp.add({

            //CutBuf2 \cb2playbuf1 and 2 used for playback of slices
            2.do({arg i;

                SynthDef(\cs2recordbuf++((i+1).asSymbol),{arg bufnum=0,inbus=8;
                    var in;

                    in=In.ar(inbus,i+1);

                    RecordBuf.ar(in,bufnum,0,1,0,1,1,1);

                }).add;

            });
        });
    }

}