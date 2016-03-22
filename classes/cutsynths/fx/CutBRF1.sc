//This file is part of The BBCut Library. Copyright (C) 2001  Nick M.Collins distributed under the terms of the GNU General Public License full notice in file BBCutLibrary.help

//CutBRF1 N.M.Collins 13/1/05

CutBRF1 : CutBPF1 {

	//makes SynthDef for filter FX Synth
	*initClass {

		StartUp.add({

		2.do({arg i;

		SynthDef("cutbrf1chan"++((i+1).asSymbol),{ arg inbus=0, outbus=0, cfreq=1000,rq=1;
		ReplaceOut.ar( outbus,
		BRF.ar(In.ar(inbus,i+1), cfreq, rq)); //Lag.kr(cutoff, 0.05),Lag.kr(q,0.05)

		}).add;

		});

		});
	}

	*new{arg cfreqfunc=1000,rqfunc=1,drqfunc=0.8;

	^super.new.cfreqfunc_(cfreqfunc).rqfunc_(rqfunc).drqfunc_(drqfunc);
	}

	setup {
	//tail of cutgroup
	synthid= cutgroup.server.nextNodeID;

	cutgroup.server.sendMsg(\s_new, \cutbrf1chan++(cutgroup.numChannels.asSymbol), synthid, 1,cutgroup.fxgroup.nodeID,\inbus,cutgroup.index,\outbus,cutgroup.index, \cfreq,1000,\rq,10);

	}

}