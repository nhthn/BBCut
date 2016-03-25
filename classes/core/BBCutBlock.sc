//This file is part of The BBCut Library. Copyright (C) 2001  Nick M.Collins distributed under the terms of the GNU General Public License full notice in file BBCutLibrary.help

//BBCutBlock 24/12/04  by N.M.Collins

BBCutBlock {
    // Index of the block in phrase (0 is start of phrase, 1 is second block in phrase, etc.)
    var <>blocknum;
    // Length of the block in clock time
    var <>length;
    // Array of cut data of the form [ioi, dur, offsetparam, amp]
    var <>cuts;
    // Buffer offset position
    // If nil, use the time since start of phrase modulo buffer size
    var <>offset;
    // Clock time at the beginning of the buffer since start of phrase
    var <>phrasepos;
    // Flag to mark this block as a roll
    var <>isroll;
    // Length of current phrase in clock beats
    var <>currphraselength;

    // Inter-onset intervals (derived from cuts)
    var <>iois;
    // Cumulative times (derived from iois)
    var <>cumul;
    // Current fraction of phrase that has been covered, 0..1
    var <>phraseprop;

    // OSC messages exactly on cuts
    var <>msgs;
    // Messages timed from start of block, may be within cuts
    var <>timedmsgs;
    // Functions that must be scheduled
    var <>functions;

    // Not 100% sure what this is, BBCut2 uses it to timestamp the block I think? -NH
    var <>startbeat;

    *new {
        ^super.new.initBBCutBlock;
    }

    initBBCutBlock {
        timedmsgs = List();
        functions = List();
    }

    scaleDurations { |factor|
        cuts.do { |cut|
            cut[1] = cut[1] * factor;
        };
    }

    update {
        msgs = cuts.size.collect { List() };
        iois = cuts.collect(_[0]);
        cumul = cuts.size.collect { |i| iois.copyRange(0, i - 1).sum };
    }

    //timedelay is groovedeviation- perceptual attack time
    addtimedmsgtocut {arg whichcut,beatdelay, timedelay, msg;

        //or need to adapt scheduler to send single arrays as single Msgs  List[msg]
        timedmsgs.add([cumul[whichcut]+beatdelay, timedelay, msg]);

    }

    addtimedfunctiontocut { |whichcut, beatdelay, timedelay, func|
        functions.add([cumul[whichcut] + beatdelay, timedelay, func]);
    }


    //create [delta, bundle] list, will be sorted already
    cacheMsgs {arg cache, server;
        //var ioisum;
        var sortedorder;
        var event;

        sortedorder= List.new; //can sort Lists

        //ioisum=0.0;

        msgs.do({arg list,j;
            //var ioi;

            //ioi= iois[j];

            if(list.notEmpty,{

                event= (server:server, msg:list, play:{arg slave; var s; s=~server; s.listSendBundle(s.latency,~msg)});

                sortedorder.add([startbeat+(cumul[j]),0.0,event]);
            });

            //ioisum=ioisum+ioi;
        });

        //could simplify this if cutsynths added timedmsgs with knowledge of adding startbeat themselves
        timedmsgs.do({arg msg,j;

            event= (server: server, msg:msg[2], play:{arg slave; var s; s=~server; s.sendBundle(s.latency,~msg)});

            sortedorder.add([startbeat+(msg[0]),msg[1],event]);

        });

        //msg holds beatdev, time dev, function
        functions.do({arg msg;

            event= msg[2]; //(server:server, play:msg[2]);

            event.put(\server, server);

            sortedorder.add([startbeat+(msg[0]),msg[1],event]);
        });

        sortedorder= sortedorder.sort({arg a,b; (a[0])<(b[0])});

        //Post << "added"<< nl<< sortedorder << nl;

        cache= cache++sortedorder; //will become a LinkedList

        ^cache;
    }


}