//This file is part of The BBCut Library. Copyright (C) 2001  Nick M.Collins distributed under the terms of the GNU General Public License full notice in file BBCutLibrary.help

//CutComb1 N.M.Collins 28/12/04

//tune=(#[36,50,64,65,67,69,70,72,74,73,72,70,69,70,69,67,67,60,60]).midicps.reciprocal;
//deltime=tune.wrapAt(i);


CutComb1 : CutSynth {
	var <>deltime,<>dectime;
	var synthid;

	*initClass {
	StartUp.add({
		2.do({arg i;

		SynthDef("cutcomb1chan"++((i+1).asSymbol),{ arg inbus=0, outbus=0, deltime=0.5, dectime=0.5;
		ReplaceOut.ar( outbus,
		CombN.ar(In.ar(inbus,i+1), 0.5,deltime,dectime)); //Lag.kr(cutoff, 0.05),Lag.kr(q,0.05)
		}).add;
		});
		});
	}

	*new{arg deltime=0.01,dectime=0.5;

	^super.new.deltime_(deltime).dectime_(dectime);
	}

	setup {
	//tail of cutgroup
	synthid= cutgroup.server.nextNodeID;

	cutgroup.server.sendMsg(\s_new, \cutcomb1chan++(cutgroup.numChannels.asSymbol), synthid, 1,cutgroup.fxgroup.nodeID,\inbus,cutgroup.index,\outbus,cutgroup.index, \dectime,0.5);

	}


	free {

		cutgroup.server.sendMsg(\n_free,synthid);
	}

	renderBlock {arg block,clock;

		//s= cutgroup.server;

		block.cuts.do({arg cut,i;

		block.msgs[i].add([\n_set, synthid,\deltime,deltime.value(i,block),\dectime,dectime.value(i,block)]);

		});

		//don't need to return block, updated by reference
	}


}