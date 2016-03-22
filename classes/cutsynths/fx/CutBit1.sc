//This file is part of The BBCut Library. Copyright (C) 2001  Nick M.Collins distributed under the terms of the GNU General Public License full notice in file BBCutLibrary.help

//CutBit1 N.M.Collins 13/1/05

//bit reduction based on cut position

CutBit1 : CutSynth {
	var <>bits,<>sr,<>bitadd,<>srmult;
	var synthid;

	//makes SynthDef for filter FX Synth
	*initClass {

			StartUp.add({

		2.do({arg i;

		SynthDef("cutbit1chan"++((i+1).asSymbol),{ arg inbus=0, outbus=0, rnd, sr=44100;
		var input, fx;

		input= In.ar(inbus,i+1);

		fx=Latch.ar(input.round(rnd),Impulse.ar(sr));

		ReplaceOut.ar(outbus,fx);

		}).add;
		});

		});

	}

	*new{arg bits=16,sr,bitadd=1,srmult=1;

	^super.new.bits_(bits).bitadd_(bitadd).sr_(sr ?? {Server.default.sampleRate/2}).srmult_(srmult);
	}

	setup {
	//tail of cutgroup
	synthid= cutgroup.server.nextNodeID;

	cutgroup.server.sendMsg(\s_new, \cutbit1chan++(cutgroup.numChannels.asSymbol), synthid, 1,cutgroup.fxgroup.nodeID,\inbus,cutgroup.index,\outbus,cutgroup.index, \rnd,0.0,\sr,cutgroup.server.sampleRate);

	}


//can't assume, individual free required for cut fx
//synth should be freed automatically by group free
	free {
		cutgroup.server.sendMsg(\n_free,synthid);
	}

	renderBlock {arg block,clock;
		var samprate,bitstart,bitarray,srarray, s;

		s= cutgroup.server;

		bitstart= bits.value(block);
		samprate= sr.value(block);

		srarray= Array.geom(block.cuts.size,samprate,srmult.value(block));
		bitarray= Array.series(block.cuts.size,bitstart,bitadd.value(block));

		bitarray= 0.5**((bitarray).max(2)-1);

		block.cuts.do({arg cut,i;

		block.msgs[i].add([\n_set, synthid,\rnd,bitarray[i],\sr,srarray[i]]);

		});

		//don't need to return block, updated by reference
	}


}