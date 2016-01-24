//This file is part of The BBCut Library. Copyright (C) 2001  Nick M.Collins distributed under the terms of the GNU General Public License full notice in file BBCutLibrary.help

//CutMixer 3/1/05  by N.M.Collins

//mono to stereo mixer for cutters with cut aware pan and amplitude changes
//could changes busses with a outbus_ function, must send set msg as well

///could be a control bus for volume so changes immediate?

//perhaps need to have a version which doesn't keep updating volume every cut, only update if different

CutMixer : CutSynth {
    var <outbus,<>ampfunc, <>panfunc, <>volume;
    var synthid;
    var lastvol, lastpan;

    *initClass {
        StartUp.add({
            2.do({arg i;

                SynthDef.writeOnce(\cutmixer++((i+1).asSymbol),{arg outbus=0,inbus=0,amp=1.0,pan=0.0;
                    var tmp;

                    tmp= amp*In.ar(inbus,i+1); //because of grouping, should just work
                    //amp*InFeedback.ar(inbus,1);

                    if(i==0,{tmp=Pan2.ar(tmp,pan)});

                    Out.ar(outbus,tmp);

                });
            });
        });
    }

    //will get Server from BBCutGroup later
    *new{arg outbus=0,volume=1.0,ampfunc=1.0,panfunc=0.0;

        ^super.new.initCutMixer(outbus,volume,ampfunc,panfunc);
    }

    initCutMixer {arg ob=0,vol=1.0,af=1.0,pf=0.0;

        //defaults cover against any nils
        outbus=ob;
        //not needed, uses cutgroup index inbus=ib;
        volume=vol;
        ampfunc=af;
        panfunc=pf;

        lastvol=0.0;
        lastpan=0.0;
    }


    //adds itself to mixergroup
    setup { var s;

        s=cutgroup.server;

        synthid= s.nextNodeID;

        //add to tail, final rendering after fx must create last, starts at zero volume for off
        s.sendMsg(\s_new, \cutmixer++(cutgroup.numChannels.asSymbol), synthid, 1,cutgroup.mixergroup.nodeID,\outbus,outbus,\inbus,cutgroup.index,\amp,0.0,\pan,0.0);

    }

    free {
        cutgroup.server.sendMsg(\n_free,synthid);
    }

    //can change all rendering functions to return an array of amplitudes?
    renderBlock {arg block,clock;

        ampfunc.tryPerform(\updateblock,  block);
        panfunc.tryPerform(\updateblock,  block);

        block.cuts.do({arg cut,i;
            var vol, pan;

            vol= (volume.value)*ampfunc.value(i,block);
            pan= panfunc.value(i,block);

            //if (not((vol.equalWithPrecision(lastvol)) && (pan.equalWithPrecision(lastpan))) ,
            //{
            block.msgs[i].add([\n_set, synthid,\amp,vol,\pan,pan]);

            //});

            lastvol=vol;
            lastpan=pan;
        });

    }
    //SQPusher1

}