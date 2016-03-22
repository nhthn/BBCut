//This file is part of The BBCut Library. Copyright (C) 2001  Nick M.Collins distributed under the terms of the GNU General Public License full notice in file BBCutLibrary.help

//CutBuf3 11/05/05  by N.M.Collins

//event respecting playback
//beat positions of events independent of playback, quantise positions
//BBCutBuffer

CutBuf3 : CutSynth {
    var <>bbcutbuf, <>offset, <>pbsfunc, <>dutycycle, <>atk, <>rel, <>curve;
    var <>grainfunc;
    var whichsynthdef;
    var <>deviationmult,<>pretrim,<>posttrim;

    *initClass {


        StartUp.add({

            2.do({arg i;

                SynthDef(\cb3playbuf++((i+1).asSymbol),{arg bufnum=0,outbus=0,rate=1,startPos=0,dur=0.1,atk=0.005, rel=0.005, curve=0;
                    var playbuf, env;

                    playbuf= PlayBuf.ar(i+1,bufnum,BufRateScale.kr(bufnum)*rate,1,startPos,1);

                    env= EnvGen.ar(Env([0,1,1,0],[atk,dur-atk-rel,rel],curve),doneAction:2);

                    Out.ar(outbus,playbuf*env);
                }).add;
            });
        });
    }

    //will get Server from BBCutGroup later
    *new{arg bbcutbuf, offset, deviationmult, pretrim, posttrim, pbsfunc, dutycycle, atk, rel, curve;

        ^super.new.initCutBuf2(bbcutbuf, offset, deviationmult, pretrim, posttrim, pbsfunc, dutycycle, atk, rel, curve);
    }

    initCutBuf2 {
        |bcb, off, dm, pt, pstt, pf, dc, ap, rp, c|

        bbcutbuf=bcb;
        offset= off ? 0.0;

        deviationmult= dm ? 1.0; //maintain all time deviations in groove by default
        pretrim= pt ? true; //no anticipations carried around
        posttrim= pstt ? true; //no post cut events

        pbsfunc=pf ? 1.0;
        dutycycle= dc ? 1.0; //{arg dur; 1.0};
        atk= ap ? 0.0;	//any enveloping may change the PAT
        rel= rp ? 0.0;
        curve= c ? 0;
    }


    setup {
        whichsynthdef= \cb3playbuf++(cutgroup.numChannels.asSymbol);
    }

    //there should be a free function to release this, I guess group release will sort that out

    //could refine CPU use by rendering one playbuf for all later repeats (need to know block.length-cuts[0][0] and sending retrigger messages for each cut

    renderBlock {arg block,clock;
        var startpos, tempo; //rate, tmp
        var maxdur, indices, firstindex;

        //predicted but may change while cached, accepting this for the moment
        tempo= clock.tempo;

        bbcutbuf.tryPerform(\updateblock,  block);
        pbsfunc.tryPerform(\updateblock,  block);
        offset.tryPerform(\updateblock,  block);
        dutycycle.tryPerform(\updateblock,  block);
        atk.tryPerform(\updateblock,  block);
        rel.tryPerform(\updateblock,  block);
        curve.tryPerform(\updateblock,  block);

        //find sub events within this block

        startpos= if(block.offset.isNil,{

            bbcutbuf.chooseoffset(block, offset,1);
        },{
            bbcutbuf.convertoffset(block,1);
        });

        //rate=(tempo)/(bbcutbuf.bps); //bps/buffertempo
        //["rate check", rate, clock.tempo, bbcutbuf.bps, pbsfunc.value(0,block)].postln;
        //find longest of cuts

        maxdur= 0.0;

        block.cuts.do({arg cut,i;
            var dur;

            dur=cut[1];

            if(dur>maxdur,{maxdur= dur;});

        });

        //Post << [\startpos, startpos,(startpos/(bbcutbuf.numFrames))*(bbcutbuf.beatlength) ]<<nl;

        //convert startpos back into beats, not sampleFrames
        //startpos= (startpos/(bbcutbuf.numFrames))*(bbcutbuf.beatlength);

        //Post << [\startpos, startpos]<<nl;

        //duration in beats via clock current tempo
        indices= bbcutbuf.findevents(startpos, maxdur*tempo);

        //Post <<indices <<nl;

        //for each cut must discard any events longer than the cut- optionally, remove those before cut starts (anticipations)

        if(indices.notEmpty,{

            block.cuts.do({arg cut,i;
                var dur, subevents;
                var pbs, dc, atkval, relval, crv;

                pbs=pbsfunc.value(i,block);
                dc=dutycycle.value(i,block);
                atkval= atk.value(i,block);
                relval= rel.value(i,block);
                crv=curve.value(i,block);

                dur=cut[1]*tempo;

                subevents= bbcutbuf.trimevents(startpos,dur,indices,pretrim.value(i,block),posttrim.value(i,block),tempo);

                //Post << [startpos, dur, pretrim, posttrim, subevents] << nl;

                //schedule subevents

                //can quantise to a new groove at this point, options to add in delta*timedeviation,
                //delta=0.0 takes at quantise position only

                if(not((subevents.isNil) || (subevents.isEmpty)),{

                    firstindex=subevents[0];

                    subevents.do({arg index,j;
                        var timedelay, delay;
                        var eventdur;

                        //used to be quantised
                        delay= (bbcutbuf.groovepos[index])-startpos;	//beat delay according to quantise position

                        if (index<firstindex, {//add on bbcutbuf.beatlength
                            delay=delay+(bbcutbuf.beatlength);
                        });

                        timedelay= ((deviationmult.value(i,block))*(bbcutbuf.timedeviations[index]))-(bbcutbuf.eventpats[index]);   //timedev - pat

                        eventdur= (bbcutbuf.eventlengths[index])*dc;

                        block.addtimedmsgtocut(
                            i,
                            delay,
                            timedelay,
                            [
                                \s_new, whichsynthdef, -1, 0, cutgroup.synthgroup.nodeID,
                                \outbus, cutgroup.index,
                                \bufnum, bbcutbuf.bufnum,
                                \startPos, bbcutbuf.eventstarts[index],
                                \rate, pbs,
                                \dur, eventdur,
                                \atkprop, atkval,
                                \relprop, relval,
                                \curve, crv
                            ]
                        );

                        grainfunc.notNil.if {
                            block.addtimedfunctiontocut(
                                i,
                                delay,
                                timedelay,
                                (
                                    block: block,
                                    clock: clock,
                                    whichcut: i,
                                    startPos: bbcutbuf.eventstarts[index],
                                    dur: eventdur,
                                    func: grainfunc,
                                    play: { ~func.value(~whichcut, ~block, ~clock, ~startPos, ~dur) }
                                )
                            );
                        };

                        //could pass pat, but assume 20mS multimodal integration herein
                        if(trace.notNil, {
                            trace.msg(block, i, delay, timedelay, \offset, (bbcutbuf.eventstarts[index])/(bbcutbuf.numFrames), \repeatlength, dur, \subevent, j)
                        });

                    });

                });

            });

        });

        //don't need to return block, updated by reference
    }
}