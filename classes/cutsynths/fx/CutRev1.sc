//This file is part of The BBCut Library. Copyright (C) 2001  Nick M.Collins distributed under the terms of the GNU General Public License full notice in file BBCutLibrary.help

//CutRev1 N.M.Collins 13/1/05

//reverb burst on block start

CutRev1 : CutSynth {
	var <>amount,<>send;
	var synthid;

	//makes SynthDef for filter FX Synth
	*initClass {
		StartUp.add({
		2.do({arg i;

		SynthDef("cutrev1chan"++((i+1).asSymbol),{ arg inbus=0, outbus=0, amount=0, send=0.0;
		var input;
		var a,c,z,y,in;

		input= In.ar(inbus,1+1);

		in=Limiter.ar(send*input);

		c = 7; // number of comb delays
		a = 4; // number of allpass delays

		// reverb predelay time :
		z = DelayN.ar(in, 0.048,0.048);

		//for delaytime if want modulation-	//LFNoise1.kr(0.1.rand, 0.04, 0.05)
		y=Mix.arFill(c,{CombL.ar(z,0.1,rrand(0.01, 0.1),5)});

		// chain of 4 allpass delays on each of two channels (8 total) :
		a.do({ y = AllpassN.ar(y, 0.051, [rrand(0.01, 0.05),rrand(0.01, 0.05)], 1) });

		Out.ar( outbus, (amount*y));

		}).add;
		});

		});

	}

	*new{arg amount=0.0,send=0.0;

	^super.new.amount_(amount).send_(send);
	}

	setup {
	//tail of cutgroup
	synthid= cutgroup.server.nextNodeID;

	cutgroup.server.sendMsg(\s_new, \cutrev1chan++(cutgroup.numChannels.asSymbol), synthid, 1,cutgroup.fxgroup.nodeID,\inbus,cutgroup.index,\outbus,cutgroup.index, \amount,0,\send,0);

	}


//can't assume, individual free required for cut fx
//synth should be freed automatically by group free
	free {
		cutgroup.server.sendMsg(\n_free,synthid);
	}

	renderBlock {arg block,clock;

		block.cuts.do({arg cut,i;



		block.msgs[i].add([\n_set, synthid,\amount,amount.value(i,block),\send,send.value(i,block)]);

		});

		//don't need to return block, updated by reference
	}


}