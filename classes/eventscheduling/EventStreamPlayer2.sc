//This file is part of The BBCut Library. Copyright (C) 2001  Nick M.Collins distributed under the terms of the GNU General Public License full notice in file BBCutLibrary.help

//Event stream wrapper to play on an induct clock

//contains caching for upcoming events

EventStreamPlayer2 : PauseStream {
    var <>event, <>muteCount = 0;
    var cache; //material temporarily held here
    var upto;
    var <>alive; //special flag set to false once killed, checked by scheduler

    *new { arg stream, event;

        ^super.new(stream).event_(event ? Event.default).init;
    }

    init {

        //scheduling queue preparation
        upto=0.0;
        cache=LinkedList.new;    //could use a PriorityQueue but cheaper to avoid any searches of position

        //ioi=0.0;

        //stream=originalStream;

        //stream.reset;

        //alive=true;
    }


    //may need to call init again
    //check if clock exists already?
    //quant doesn't mean anything
    play { arg argClock, doReset = false, quant=1.0;
        if (stream.notNil) { "already playing".postln; ^this };
        if (doReset) { this.reset };
        clock = argClock ?? {ExternalClock.default};
        stream = originalStream;
        alive= clock.addprovider(this);
    }

    //unpausable for now
    pause {
    }

    resume {
    }

    stop {
        stream = nil;
        alive.value=false;
    }

    mute { muteCount = muteCount + 1; }
    unmute { muteCount = muteCount - 1; }

    next { //arg inTime;
        var server,latency;
        var nextTime;
        var outEvent = stream.next(event);

        if (outEvent.isNil) {
            stream = nil;

            //can't set alive here immediately! //Server.default.latency
            //now safe for re-entry
            ^[10,0.0,(ref:alive, play:{ ~ref.value=false;})];
        }{
            if (muteCount > 0) { outEvent.put(\freq, \rest) };

            //get outEvent[\dur], outEvent[\pat], outEvent[\timedev], outEvent.play deferred in a function;
            nextTime= outEvent[\dur] * outEvent[\stretch];

            server = (outEvent[\server]) ?? { Server.default };

            latency=server.latency;

            //allow a \beatdev field as well? Would need to resolve using tempo at this point...

            ^[nextTime, latency - (outEvent[\pat] ? 0.0) + (outEvent[\timedev] ? 0.0), outEvent];

        };
    }

    //update cache, prepare material for next beat, using next method of this class to keep getting enough Events
    provideMaterial {arg beatlocation, need=1.0, tempo;
        var want,have,fetched,block;
        var removed, toschedule, tokeep;
        var endsec;
        var iterate; //condition for while loop
        var cacheevent;
        var scheduletime;
        var beatNow;
        var server, latency;
        var ioi;	//in Events, dur*stretch property gives the ioi

        endsec= need/tempo;

        have= upto-need; //(beatlocation+need);

        want=0.5-have;

        if (have<0.5, //assume you have the data already if greater than
            {
                //get an Event at a time, keep asking for material if don't have enough, render as you go

                fetched=0.0;

                //keep getting blocks until have enough data to schedule

                while({fetched<want},{

                    block=this.next;

                    //to time stamp all events
                    beatNow=beatlocation+upto+fetched;

                    ioi=block[0];

                    block[0]= beatNow;

                    fetched=fetched+ioi;

                    cache=cache++[block];

                    //Post << "cache test " << cache << nl;

                });

                upto=upto+fetched;

        });


        //default event in case you need to wait
        toschedule=List[[0,nil]];

        if(cache.notEmpty,{

            tokeep=LinkedList.new;

            //pop one at a time, passing needed messages toschedule, else tokeep. If get sufficiently far from needed time, assume all
            //further events don't need to be scheduled yet. Very long PATs or Server latencies would mess this up

            iterate=true;

            while({iterate},{

                cacheevent= cache.popFirst;

                if(cacheevent.isNil, {iterate=false;},
                    {

                        server = (cacheevent[2][\server]) ?? { Server.default };

                        latency=server.latency;

                        scheduletime= (((cacheevent[0])-beatlocation)/tempo) + (cacheevent[1])- latency; //(clock.s.latency);

                        if(	scheduletime< endsec, {

                            //add toschedule

                            //Post << [\scheduletime, scheduletime, endsec, cacheevent[0], cacheevent[1], cacheevent[2]] << nl;

                            //if(scheduletime<0.0, {scheduletime=0.0}); //negative times can't be caught, so make asap
                            scheduletime = max(0, scheduletime); //negative times can't be caught, so make asap


                            toschedule.add([scheduletime, cacheevent[2]]); //([{"playing   ".post;  cacheevent[2].postln; cacheevent[2].play}]);

                        }, {

                            tokeep.add(cacheevent);

                            //finished, don't schedule any of remainder, assumes no huge PAT or deviations over 500mS from here
                            if (scheduletime> (endsec+0.5), {
                                iterate=false;
                            });

                        });

                });


            });

            cache= tokeep++cache;

            //sort toschedule into order, and prepare as deltas for sequential SystemClock scheduling

            toschedule= toschedule.sort({arg a,b; a[0]<b[0]});
            //into delta times

            toschedule.do({arg val,i;

                if(i<(toschedule.size-1),{

                    val[0]=(toschedule[i+1][0])-(val[0]);
                },{

                    //last ioi must be nil to kill sched
                    val[0]=nil;
                });

            });

            //every so often scale locations in cache back to range 0.0 to 100.0 beats?
        });

        //Post << "schedule test " << toschedule << nl;

        upto=upto-need;

        //toschedule will contain a mixture of msg Lists and individual msgs?
        ^toschedule;

    }

    asEventStreamPlayer2 { ^this }
}


