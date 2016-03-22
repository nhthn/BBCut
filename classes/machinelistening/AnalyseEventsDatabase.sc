//This file is part of The BBCut Library. Copyright (C) 2001  Nick M.Collins distributed under the terms of the GNU General Public License full notice in file BBCutLibrary.help

//class to support on-the-fly Event Capture from the AnalyseEvents UGen
//the database can then be used in algorithmic composition

//OSC support removed for now, may add trace back in later

//can add rhythmic histogram for activity monitor as a further class running off this database

AnalyseEventsDatabase {
    var s, <length, <numChannels;
    var detectbuf, <soundbuf;
    var <list; //list of all events
    var responder, bufdataresponder; //OSCresponderNode to communicate with Server
    var <nodeID;
    var lastTime, lastSoundBufPos; //for keeping track of buffer location after pause
    var firstTime, deductTime;
    var oscout;
    var idcounter; //max 10000 events in the database, modulo this for ID

    //DEBUG
    //var <eventtimes;

    *new {arg length=10, numChannels=1, server, oscout;
        ^super.new.initAnalyseEventsDatabase(length, numChannels, server, oscout);
    }

    initAnalyseEventsDatabase {	arg len, numChan, server,oo; //, trigID;

        s= server ? Server.default;

        length=len;

        numChannels= numChan;

        //room for 15 events at 10 floats data per event, plus 2 marker floats
        detectbuf= Buffer.alloc(s,152,1);
        soundbuf= Buffer.alloc(s,s.sampleRate * length, numChannels);

        list= List.new; //will be updated as each event discovered, events older than length are also removed from the end

        oscout= oo; //will be nil if nothing passed in

        idcounter=0;

        //trigID= 77;

        //DEBUG
        //eventtimes= List.new;

    }

    //purge items from list that are older than some fixed time in the past
    removeold {arg check;
        //var test, popped;
        var firstsafe;

        //remove if older than check
        //Post << "remove" << check << nl;

        //test=true;
        //		while {test && (list.notEmpty)} {
        //
        //		popped=list.pop;
        //
        //		["pop",popped].postln;
        //
        //		if(popped.isNil,{test=false;},{
        //		if (popped[0]>check) {test=false; list.add(popped)};
        //		});
        //
        //		};
        //
        firstsafe=list.size-1;

        firstsafe= block {|break|
            list.reverseDo({arg val, i;  if(val[0]>check) { break.value(list.size-1-i)} });

            -1
        };

        //Post << [check,firstsafe,list.collect({arg val; val[0]})] << nl;

        if (firstsafe<(list.size-1), {list=list.copyRange(0,firstsafe)});


    }

    //can only call this once for each Object, not safe for re-entrant


    inbus_ {arg inbus;

        if(nodeID.notNil) {
            s.sendMsg(\n_set, nodeID,\inbus,inbus);
        }

    }


    threshold_ {arg val=0.34;

        if(nodeID.notNil) {
            s.sendMsg(\n_set, nodeID,\threshold,val);
        }

    }


    soundbufpos {arg now;

        ^(((now-lastTime) + lastSoundBufPos)%length);
    }

    //prototype in CaptureEventsManager
    analyse {arg inbus, trigID=101, group, threshold=0.34, clock, synthdef, moreargs;
        var latest = (-1);

        //var trigtime;
        var soundbufpos, eventlength, starttime, startbeat, quantbeat, timedev, pat, pitch, timbre;


        moreargs = moreargs ? [];


        synthdef = synthdef ? (\analyseeventsdatabase);

        if(nodeID.isNil, {

            inbus=inbus ?? {s.options.numOutputBusChannels};
            group=group ?? {Node.basicNew(s,1)};
            //clock= clock ?? {ExternalClock.new.play;}; //using TempoClock.default as default
            //leave clock nil if no quantise assessment required

            nodeID=s.nextNodeID;

            // r.remove
            bufdataresponder= 	OSCpathResponder(s.addr,['/b_setn',detectbuf.bufnum, 0],{arg time, r, msg;
                var array, timehere;

                timehere=Main.elapsedTime;

                //["timebuf",time, "now", timehere, "trigtime", trigtime].postln;

                array= msg.copyToEnd(8);

                eventlength=(array[1]-array[0])/(s.sampleRate);
                //starttime=trigtime-eventlength;

                starttime= (array[0]/44100)+lastTime-deductTime; //absolute start time

                //DEBUG- take off lastTime, assumes running continuously, just getting event positions
                //eventtimes.add(starttime-lastTime);

                //SystemClock.sched(1.0-eventlength,{Synth(\bleep); (starttime-lastTime).postln; nil});



                //Post << [\newevent,array,\length, eventlength, \starttime, starttime, \last, lastTime, \deduct, deductTime] << nl;

                //array[0] stops increasing while UGen paused
                //[\starttime,starttime].postln;

                //gulf up to 50mS! problem! Just use array measure? Do timing properties improve?
                //["match?", (array[0]/44100)+lastTime, starttime].postln;

                pat=array[3]; //BBCutBuffer.defaultPAT; //not used yet- array[3];

                pitch= array[6];

                timbre=array[4];

                soundbufpos= ((((starttime-lastTime) + lastSoundBufPos)%length)*(s.sampleRate)).round(1.0).asInteger;

                //removeold
                this.removeold(timehere-length);

                //add to list

                //[\starttime, starttime, \startsamp, soundbufpos, \eventlength, eventlength].postln;

                if(clock.notNil,{

                    //use clock or TempoClock.default to set a beattime
                    startbeat= (clock.lastBeat)+((starttime-(clock.lastBeatTime))*(clock.lastTempo));

                    //[\startbeat, startbeat].postln;

                    //[\startbeat, startbeat, \lastclock,clock.lastBeat, \starttime, starttime,\lastBeatTime, clock.lastBeatTime, \beatcorrect, (starttime-(clock.lastBeatTime))*(clock.lastTempo) ].postln;

                    quantbeat=startbeat.round(0.5); //should be 0.5, was 0.25

                    //[\startbeat,startbeat,\quantbeat,quantbeat].postln;

                    timedev= (startbeat-quantbeat)/(clock.lastTempo); //time deviation from the 16ths groove template

                    //if less than a mS, set to zero
                    if(abs(timedev)<0.001,{timedev=0.0});

                }
                //,{startbeat=0.0;quantbeat=0.0;timedev=0.0;} //left out, sets nils since no info
                );


                //added beat data later on

                list.addFirst([starttime,soundbufpos,eventlength,startbeat,quantbeat,timedev, pat, pitch, idcounter,timbre]); //+ further data later

                //Post << list << nl;
                //Post << ["add",starttime,soundbufpos,eventlength,startbeat,quantbeat,timedev, pat] << nl;

                //adjust for transmission time? endtime is just now, this osc was sent to lang at finalising of capture
                if(oscout.notNil, {oscout.addmessageglobal(["captrecordeventID",idcounter,"captstarttime",(eventlength).neg,"captendtime",0.0],0);}); //instance 0

                idcounter=(idcounter+1)%10000;

            } ).add;

            responder= OSCpathResponder(s.addr,['/tr',nodeID, trigID],{arg time,responder,msg;
                var latest; //id

                //["timetrig",time, "now", Main.elapsedTime].postln;
                //trigtime=time;

                latest=(msg[3]).asInteger;

                s.listSendMsg(["/b_getn",detectbuf.bufnum,0,2,(latest*10)+2,10]);
                //s.listSendMsg(["/b_getn",detectbuf.bufnum,(latest*10)+2,10]);

                //detectbuf.getn(0,2,{"bollocks!".postln});

            }).add;

            //start Synth with messaging style and latency so have exact start time

            //create at tail RootNode, not Node.basicNew(s,1)

            //["/s_new",synthdef++((numChannels).asSymbol), numChannels].postln;

            s.sendBundle(s.latency, ["/s_new",synthdef++((numChannels).asSymbol), nodeID,1,group.nodeID, \inbus, inbus, \soundbufnum, soundbuf.bufnum, \analysisbufnum, detectbuf.bufnum, \trigID, trigID, \threshold, threshold]++moreargs);

            //send Fredrik stop or start
            if(oscout.notNil, {oscout.addmessageglobal(["captonoff",1],0); });

            lastTime=(Main.elapsedTime)+(s.latency);
            firstTime=lastTime;
            deductTime=0.0;
            lastSoundBufPos=0;

        });


    }

    pause {
        var now;

        now=(Main.elapsedTime)+(s.latency);

        s.sendBundle(s.latency, ["/n_run",nodeID,0]);

        //scheduled in case any events received inbetween
        SystemClock.sched(s.latency,{
            lastSoundBufPos= ((now-lastTime) + lastSoundBufPos)%length;

            deductTime=deductTime+(now-lastTime); //cumulative sum this UGen has been running so far

            //[\pause, \deductTime,deductTime].postln;
        });

        //send Fredrik stop or start
        if(oscout.notNil, {oscout.addmessageglobal(["captonoff",0],0); });
        //instance 0 stop 0 start 1

        //lastTime=now+(s.latency);
    }

    resume {
        var now;

        now=Main.elapsedTime;
        lastTime=now+(s.latency);

        //[\resume, \lastTime,lastTime].postln;
        s.sendBundle(s.latency, ["/n_run",nodeID,1]);

        //send Fredrik stop or start
        if(oscout.notNil, {oscout.addmessageglobal(["captonoff",1],0); });
        //instance 0 stop 0 start 1

    }


    stop {
        s.sendBundle(s.latency, ["/n_free",nodeID]);
        responder.remove;
        bufdataresponder.remove;
    }


    makeIOIs {arg array;
        var tmp;

        //convert array to iois
        tmp=array.rotate(-1);
        tmp=tmp- array;

        if(tmp.size>1,{tmp=this.removeZeroIOIs(tmp)});

        //end with a nil element so ready for scheduling
        tmp[tmp.size-1]=nil;

        ^tmp; //.copyRange(0,tmp.size-2);
    }

    removeZeroIOIs {arg input;
        var output;

        output=List[];

        input.do({arg val; if(val>0.001,{output.add(val)})});

        if(output.isEmpty,{output=[0.0];});

        ^output;
    }


    //findEventAbsoluteStarts but returns events themselves?
    findEventList {arg start, end;
        var output,q,tmp, cont, index;

        output= List.new;

        //find position in quantised such that >=start-0.001 and <start+dur
        //binarysearch

        index=0;

        cont=true;

        //groovepos here used to be quantised
        while({(index<(list.size)) && cont},{

            tmp=list[index];

            q=tmp[0]; //get absolute time position

            if(q<start,{cont=false;});

            if((q>=start) && (q<=end),{

                output.add(tmp);
            });

            index=index+1;
        });

        ^output;
    }


    findEventAbsoluteStarts {arg start, end;
        var output,q,tmp, cont, index;

        output= List.new;

        //find position in quantised such that >=start-0.001 and <start+dur
        //binarysearch

        index=0;

        cont=true;

        //groovepos here used to be quantised
        while({(index<(list.size)) && cont},{

            tmp=list[index];

            q=tmp[0]; //get absolute time position

            if(q<start,{cont=false;});

            if((q>=start) && (q<=end),{

                output.add(q);
            });

            index=index+1;
        });

        ^output;
    }

    //return iois of all events within the given frame, in seconds
    getAbsoluteRhythm {arg start, end;
        var output;
        output=this.findEventAbsoluteStarts(start, end).reverse;
        if(output.isEmpty,{^[0.0,[]]});

        ^[output[0]-start,this.makeIOIs(output)];
    }


    //beat locations were determined at collection time from the clock
    //return iois in beats of all events within the given beat frame

    //return format- [startoffset, [list of iois, terminated by nil, empty if no events at all]]

    getRhythm {arg startbeat, endbeat, quantise=true;
        var output, tmp;

        output=(this.findevents(startbeat, endbeat)).reverse;

        if(output.isEmpty,{^[0.0,[]]});

        output=if(quantise,{output.collect({arg val; val[4]});},{
            output.collect({arg val; val[3]});
        });

        ^[output[0]-startbeat,this.makeIOIs(output)];
    }

    //analyse events falling into a time frame in a given metric frame
    reconsiderRhythm {arg start, end, startphase, bps, quantise=0.25;
        var output, wait;

        output=this.findEventAbsoluteStarts(start, end);
        if(output.isEmpty,{^[0.0,[]]});

        //now interpret as beats with respect to time frame- quantise if necessary
        //first will always be the distance to the first event from start
        //output=[start]++output;

        wait=(output[output.size-1]-start)*bps;

        //convert to beats
        output=output.collect({arg val; (val-start)*bps+startphase});

        output=output.round(quantise).reverse;

        ^[wait,this.makeIOIs(output)];
    }


    //works in beats
    //analogous to BBCutBuffer-findEvents but no wraparound required, just search absolute beat positions
    //also events are ordered such that the most recently detected is first in the list
    findevents {arg start, end;
        var output, teststart,q,tmp, cont, index;

        teststart= start-0.001; //to account for floating point errors

        output= List.new;

        //find position in quantised such that >=start-0.001 and <start+dur
        //binarysearch

        index=0;

        cont=true;

        //groovepos here used to be quantised
        while({(index<(list.size)) && cont},{

            tmp=list[index];

            q=tmp[4]; //get quantised beat position

            if(q<teststart,{cont=false;});

            if((q>=teststart) && (q<end),{

                output.add(tmp);
            });

            index=index+1;
        });

        ^output;
    }



    //return trimmed list of indices within bounds, removed wraparound code
    trimevents {arg start, dur, input, pre, post, tempo;
        var output, pos, pass, end;

        end=start+dur;

        output= List.new;

        input.do({arg val, i;

            //[starttime,soundbufpos,eventlength,startbeat,quantbeat,timedev, pat]
            //start=val[3];
            //end=start+(val[2]*tempo);

            //used to be quantised
            pos= val[3];

            pass=true;

            if(pos>=end,{pass=false;

                //Post << [\endfail, pos, end, index, groovepos] << nl;
            });

            //pre && ((val[4]+(timedeviations[index]))<(start-0.001))
            if(pre && (pos<(start-0.001)),{pass=false;

                //Post << [\prefail, pre, timedeviations[index]] << nl;
            });

            //use of original tempo here may mess up scheduling when have other playback tempi
            //but can't use current clock tempo since may be wrong!
            //actually, best estimate is current tempo, now passed in and used

            //post && ((pos+((timedeviations[index])*tempo))>end)
            if(post && (pos>end),{pass=false;

                //Post << [\postfail, pos, ((timedeviations[index])*tempo), end] << nl;

            });

            if(pass,{output.add(val)});

        });


        ^output;
    }




    playLastEvent{arg busindex=0,rate=1.0,lengthmult=1.0,amp=1.0, pan=0.0;
        var event;

        this.playEvent(0,busindex,rate,lengthmult,amp, pan);

        ^list[0][2];
    }

    playEvent {arg which=0, busindex=0,rate=1.0,lengthmult=1.0,amp=1.0, pan=0.0;
        var event,id;

        event= list.wrapAt(which);

        id= event[8];

        if(oscout.notNil,{oscout.addmessageglobal(["captplayeventID",id],0);}); //instance 0

        if (event.notNil,{
            s.sendMsg("/s_new", \AEPlayBuf++(numChannels), -1,0,0, \outbus,busindex, \bufnum,soundbuf.bufnum, \rate,rate,\startPos,event[1],\len,(event[2]*lengthmult),\amp,amp, \pan, pan);
        });

    }

    playEventParams {arg which;
        var event,id;

        which=which ? 0;

        event= list.wrapAt(which);

        id= event[8];

        if(oscout.notNil,{oscout.addmessageglobal(["captplayeventID",id],0);}); //instance 0

        if (event.notNil,{
            //bufnum, offset,dur
            ^ [soundbuf.bufnum,event[1],event[2]];
        });
    }

    playLastEventEnv{arg busindex=0,rate=1.0,lengthmult=1.0,amp=1.0, pan=0.0,envtime=0.01;
        var event;

        this.playEventEnv(0,busindex,rate,lengthmult,amp, pan,envtime);

        ^list[0][2];
    }

    playEventEnv {arg which=0, busindex=0,rate=1.0,lengthmult=1.0,amp=1.0, pan=0.0,envtime=0.01;
        var event,id;

        event= list.wrapAt(which);

        id= event[8];

        if(oscout.notNil,{oscout.addmessageglobal(["captplayeventID",id],0);}); //instance 0

        if (event.notNil,{
            s.sendMsg("/s_new", \AEPlayBufEnv++(numChannels), -1,0,0, \outbus,busindex, \bufnum,soundbuf.bufnum, \rate,rate,\startPos,event[1],\len,(event[2]*lengthmult),\amp,amp, \pan, pan,\envtime,envtime);
        });

    }

    //capture SynthDefs
    *initClass {

        StartUp.add({

            2.do({arg i;

                SynthDef(\analyseeventsdatabase++((i+1).asSymbol),{arg inbus=8, soundbufnum=0, analysisbufnum=1, trigID=101, threshold=0.34;
                    var recbuf, input, analysisinput;

                    input=In.ar(inbus, i+1);

                    recbuf= RecordBuf.ar(input,soundbufnum,0,1.0,0.0,1,1,1);

                    analysisinput= if(i==0, {input},{Mix(input)});

                    AnalyseEvents2.ar(analysisinput, analysisbufnum, threshold, trigID, 1, 0.0);

                }).add;


                SynthDef(\AEPlayBuf++((i+1).asSymbol),{arg outbus=0, bufnum=0, startPos=0, length=0.1, amp=1.0, rate=1.0, pan=0.0;
                    var playbuf, env,output;

                    env=EnvGen.ar(Env([1,1],[length]),doneAction:2);

                    playbuf=PlayBuf.ar(i+1, bufnum, BufRateScale.kr(bufnum)*rate, 1, startPos, 0)*amp;

                    output= if(i==0,{Pan2.ar(playbuf*env,pan)},{playbuf*env});
                    Out.ar(outbus, output);
                }).add;

                SynthDef(\AEPlayBufEnv++((i+1).asSymbol),{arg outbus=0, bufnum=0, startPos=0, length=0.1, amp=1.0, rate=1.0, envtime=0.01,pan=0.0;
                    var playbuf, env,output;

                    env=EnvGen.ar(Env([0,1,1,0],[envtime,length-(envtime*2),envtime]),doneAction:2);

                    playbuf=PlayBuf.ar(i+1, bufnum, BufRateScale.kr(bufnum)*rate, 1, startPos, 0)*amp;

                    output= if(i==0,{Pan2.ar(playbuf*env,pan)},{playbuf*env});
                    Out.ar(outbus, output);
                }).add;


            });

        });


    }


}