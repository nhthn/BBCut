//This file is part of The BBCut Library. Copyright (C) 2001  Nick M.Collins distributed under the terms of the GNU General Public License full notice in file BBCutLibrary.help

//CutMod1 N.M.Collins 13/1/05

//ring modulation

CutMod1 : CutSynth {
	var <>modamount,<>modfreq,<>mamult,<>mfmult;
	var synthid;

	//makes SynthDef for filter FX Synth
	*initClass {
		StartUp.add({
		2.do({arg i;

		SynthDef("cutmod1chan"++((i+1).asSymbol),{ arg inbus=0, outbus=0, modamount=0,modfreq=0;
		var input, fx;

		input= In.ar(inbus,i+1);

		fx=SinOsc.ar(modfreq, 0, input);

		ReplaceOut.ar( outbus, (1-modamount)*input +(modamount*fx)
		); //Lag.kr(cutoff, 0.05),Lag.kr(q,0.05)

		}).add;

		});
	});
	}

	*new{arg modamount=1.0,modfreq=261.626,mamult=1,mfmult=1.0594630943593;

	^super.new.modamount_(modamount).modfreq_(modfreq).mamult_(mamult).mfmult_(mfmult);
	}

	setup {
	//tail of cutgroup
	synthid= cutgroup.server.nextNodeID;

	cutgroup.server.sendMsg(\s_new, \cutmod1chan++(cutgroup.numChannels.asSymbol), synthid, 1,cutgroup.fxgroup.nodeID,\inbus,cutgroup.index,\outbus,cutgroup.index, \modamount,0,\modfreq,0);

	}


//can't assume, individual free required for cut fx
//synth should be freed automatically by group free
	free {
		cutgroup.server.sendMsg(\n_free,synthid);
	}

	renderBlock {arg block,clock;
		var freq, amount,freqarray,amountarray, s;

		s= cutgroup.server;

		amount= modamount.value(block);
		freq= modfreq.value(block);

		amountarray= (Array.geom(block.cuts.size,amount,mamult.value(block))).clip2(1);
		freqarray= Array.geom(block.cuts.size,freq,mfmult.value(block));

		block.cuts.do({arg cut,i;

		block.msgs[i].add([\n_set, synthid,\modamount,amountarray[i],\modfreq,freqarray[i]]);

		});

		//don't need to return block, updated by reference
	}


}