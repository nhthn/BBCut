//This file is part of The BBCut Library. Copyright (C) 2001  Nick M.Collins distributed under the terms of the GNU General Public License full notice in file BBCutLibrary.help 

//CutBPF1 N.M.Collins 13/1/05

CutBPF1 : CutSynth { 
	var <>cfreqfunc,<>rqfunc,<>drqfunc;	
	var synthid;
	
	
	//makes SynthDef for filter FX Synth 
	*initClass { 
	
		StartUp.add({
	
		2.do({arg i;

		SynthDef.writeOnce("cutbpf1chan"++((i+1).asSymbol),{ arg inbus=0, outbus=0, cfreq=1000,rq=1; 
		ReplaceOut.ar( outbus, 
		BPF.ar(In.ar(inbus,i+1), cfreq, rq)); //Lag.kr(cutoff, 0.05),Lag.kr(q,0.05) 
		
		}); 
		
		});
		
		});
		
	} 
	
	*new{arg cfreqfunc=1000,rqfunc=1,drqfunc=0.8;
	
	^super.new.cfreqfunc_(cfreqfunc).rqfunc_(rqfunc).drqfunc_(drqfunc);
	}
	
	setup { 
	//tail of cutgroup
	synthid= cutgroup.server.nextNodeID;
		
	cutgroup.server.sendMsg(\s_new, \cutbpf1chan++(cutgroup.numChannels.asSymbol), synthid, 1,cutgroup.fxgroup.nodeID,\inbus,cutgroup.index,\outbus,cutgroup.index, \cfreq,1000,\rq,10);
		 
	} 


//can't assume, individual free required for cut fx
//synth should be freed automatically by group free
	free {
		cutgroup.server.sendMsg(\n_free,synthid);	
	}

	renderBlock {arg block,clock;
		var freq, rqarray, s;
		
		s= cutgroup.server;

		freq= cfreqfunc.value(block);
		
		rqarray= Array.geom(block.cuts.size,rqfunc.value(block),drqfunc.value(block));

		if(block.cuts.size>0,{block.msgs[0].add([\n_set, synthid,\cfreq,freq]);});

		block.cuts.do({arg cut,i;
		
		block.msgs[i].add([\n_set, synthid,\rq,rqarray[i]]);
		
		});
		
		//don't need to return block, updated by reference
	}
	

}