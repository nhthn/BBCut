//This file is part of The BBCut Library. Copyright (C) 2001  Nick M.Collins distributed under the terms of the GNU General Public License full notice in file BBCutLibrary.help

//CutStream3 09/8/05  by N.M.Collins

//combining on-the-fly event analysis with stream buffer playback.

//An offset at a given beat location and size is used to find all recent events with start times (quantised) within that cut.
//since no wraparound on absolute time tags, a reprieve from some of the complexity

CutStream3 : CutSynth {
    var synthid;
    var <>database, deallocate;
    var dutycycle, atk, rel, curve;
    var offset, numbeats, pbsfunc;
    var playdef;
    var startTime;
    var length;
    var <>deviationmult,<>pretrim,<>posttrim;
    var <>swing;

    *new{arg aed, offset, swing, deviationmult, pretrim, posttrim, pbsfunc, dutycycle, atk, rel, curve;
        ^super.new.initCutStream3(aed, offset, swing, deviationmult, pretrim, posttrim, pbsfunc, dutycycle, atk, rel, curve);
    }

    initCutStream3 {arg aed, off, sw, dm,pt,pstt, nb, pbs,dc,ap,rp,c;

        deallocate=false;
        //inbus=ib ?? {Server.default.options.numOutputBusChannels};
        database = aed; //can't be nil, dealt with in setup
        offset=off ? 1;

        swing= sw ? 0.0;
        deviationmult= dm ? 1.0; //maintain all time deviations in groove by default
        pretrim= pt ? true; //no anticipations carried around
        posttrim= pstt ? true; //no post cut events

        pbsfunc= pbs ? 1.0;
        dutycycle= dc ? 1.0;
        atk= ap ? 0.001;
        rel= rp ? 0.001;
        curve= c ? 0;
    }

    setup {
        playdef= \cb3playbuf++(cutgroup.numChannels.asSymbol); //CutBuf3 playback Synths

        length= database.length; //(bbcutbuf.numFrames)/bbcutbuf.sampleRate;

        startTime= (Main.elapsedTime)+(cutgroup.server.latency); //so can determine record head position later on

        //Post << [\startTime, startTime, \length, length] << nl;

    }


    free {
        //		if (deallocate,{bbcutbuf.free;});
    }




    //similar to CutBuf2 but with adjustment to find startPos from offset and lastBeat time, last tempo of clock
    renderBlock {arg block,clock;
        var startpos,rate, tmp;
        var startbeat;
        var beatlength, beatstartpos, beatoffset;
        var tempo;
        var maxdur, events, firstevent;

        pbsfunc.tryPerform(\updateblock,  block);
        offset.tryPerform(\updateblock,  block);
        dutycycle.tryPerform(\updateblock,  block);
        atk.tryPerform(\updateblock,  block);
        rel.tryPerform(\updateblock,  block);
        curve.tryPerform(\updateblock,  block);

        //position into record buffer at last beat- actually, just need absolute times/beats
        //recordheadpos= database.soundbufpos(clock.lastBeatTime); //((clock.lastBeatTime)- startTime)%(length);
        //now work on assumption that can choose anywhere within x beats before this? Have a beat base parameter

        beatlength=block.iois[0];
        beatstartpos= block.phrasepos%1.0;

        //if offsetpassed in, set it, within last 4 beats (need to hold stable o/w, will be some juddering with this setup)
        beatoffset= beatstartpos-(if(block.offset.notNil,{(block.offset)*(offset.value(block))},{offset.value(block)})) ;
        startbeat=(clock.lastBeat+ beatoffset); //((*(clock.lastTempo)));

        //now like CutBuf3
        maxdur= 0.0;

        block.cuts.do({arg cut,i;
            var dur;
            dur=cut[1];
            if(dur>maxdur,{maxdur= dur;});
        });


        //predicted but may change while cached, accepting this for the moment
        tempo= clock.tempo;

        //duration in beats via clock current tempo
        events= database.findevents(startbeat, startbeat+(maxdur*tempo));

        //Post << [\lastBeat, clock.lastBeat,\beatoffset,beatoffset, \startbeat, startbeat, \endbeat, startbeat+(maxdur*tempo), \events, events] << nl;

        //for each cut must discard any events longer than the cut- optionally, remove those before cut starts (anticipations)

        if(events.notEmpty,{

            block.cuts.do({arg cut,i;
                var dur, subevents;
                var pbs, dc, atkval, relval, crv;
                var swingval;

                pbs=pbsfunc.value(i,block);
                dc=dutycycle.value(i,block);
                atkval= atk.value(i,block);
                relval= rel.value(i,block);
                crv=curve.value(i,block);
                swingval=swing.value(i,block);


                dur=cut[1]*tempo;

                subevents= database.trimevents(startbeat, dur, events,pretrim.value(i,block),posttrim.value(i,block), tempo); //(,pretrim.value(i,block),posttrim.value(i,block),tempo);

                //schedule subevents

                //can quantise to a new groove at this point, options to add in delta*timedeviation,
                //delta=0.0 takes at quantise position only

                if(subevents.notEmpty,{

                    firstevent=subevents[0];

                    subevents.do({arg event,j;
                        var timedelay, delay, swingdelay;
                        var eventdur;

                        //if quant is 0.25 mod 0.5 add swingval
                        swingdelay=if((event[4]%0.5).equalWithPrecision(0.25),{swingval},0.0); //SimpleNumber

                        delay= (event[4])-startbeat+swingdelay;						//beat delay according to quantise position

                        //[starttime,soundbufpos,eventlength,startbeat,quantbeat,timedev, pat]

                        timedelay= ((deviationmult.value(i,block))*(event[5]))-(event[6]);   //timedev - pat
                        eventdur= (event[2])*dc;

                        block.addtimedmsgtocut(i,
                            delay,
                            timedelay,
                            [\s_new,playdef, -1, 0,cutgroup.synthgroup.nodeID,\outbus,cutgroup.index,\bufnum,database.soundbuf.bufnum,\startPos, event[1], \rate,pbs,\dur, eventdur,\atkprop,atkval,\relprop, relval, \curve, crv]);

                        //could pass pat, but assume 20mS multimodal integration herein
                        if(trace.notNil,{trace.msg(block, i, delay, timedelay, \eventdur,eventdur,\subevent,j, \uniqueeventID, 0)});
                        //event[8] is ID
                        //need to pass event's unique ID if set it during capture, for co-audiovisual activity

                        //Post << [\dur, (bbcutbuf.eventlengths[index])] << nl;

                    });

                });

            });

        });

    }

    //	*initClass {
    //
    //		//CutBuf2 \cb2playbuf1 and 2 used for playback of slices
    //		2.do({arg i;
    //
    //		SynthDef(\cs2recordbuf++((i+1).asSymbol),{arg bufnum=0,inbus=8;
    //		var in;
    //
    //		in=In.ar(inbus,i+1);
    //
    //		RecordBuf.ar(in,bufnum,0,1,0,1,1,1);
    //
    //		}).writeDefFile;
    //
    //		});
    //
    //	}
    //
}