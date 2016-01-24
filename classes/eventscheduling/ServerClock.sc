//This file is part of The BBCut Library. Copyright (C) 2001  Nick M.Collins distributed under the terms of the GNU General Public License full notice in file BBCutLibrary.help

//ServerClock 30/7/05  by N.M.Collins

//time gives you actual time message triggered compared to Main.elapsedTime may be a further compensation factor of around 1 mS- not used for now as perceptually negligible

ServerClock : ExternalClock {
    var <responder;

    //set tempo based
    play {arg trigID=0,s;

        s=s ?? {Server.default};

        //var beatstart;

        responder=OSCresponderNode(s.addr, '/tr', { arg time, responder, msg;

            if(msg[2]==trigID,{

                //"bbinduct beat".postln;
                //[time,msg].postln;

                //new beat recieved from beat induction, could be some network jitter+control period delay in passing this across,
                //shouldn't cancel any messages within s.latency of now?

                //update tempo, assuming always sensible
                lastBeat=lastBeat+1;
                lastBeatTime= Main.elapsedTime;
                lastTempo= msg[3];

                //lastTempo.postln;

                //tempoclock.tempo_(msg[3]);
                //schedule

                this.tick;

            });


        }).add;

    }


    getMaterial {

        providers.do({arg array;
            var list, provider;

            provider=array[0];

            //need next beat, allowing for latency, returns a SortedList
            list=provider.provideMaterial(lastBeat,1.0, lastTempo);

            //Post << "schedule me  "<<nl << list <<nl<<nl ;

            this.sched(list, lastref, provider, array[1]);
        });

        //beat=beat+1; //update this clock's internal beat count

    }


    tempo {
        ^lastTempo;
    }

    tempo_ {arg val;

        //does nothing here, can't control tempo of an external process
        //tempoclock.tempo_(val);
    }

    stop {

        responder.remove;

        //super.stop;
        //tempoclock.stop;

        providers=List.new;

        lastref.value=false;
        lastref=Ref(true);

    }


}




