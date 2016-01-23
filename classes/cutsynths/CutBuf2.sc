//This file is part of The BBCut Library. Copyright (C) 2001  Nick M.Collins distributed under the terms of the GNU General Public License full notice in file BBCutLibrary.help

//CutBuf2 26/12/04  by N.M.Collins 

//class for offset playback from a buffer based on blocks
//grain Synth spawned for each cut, with pbsfunc, 
//BBCutSynthSF

CutBuf2 : CutSynth {	
var <>bbcutbuf, <>offset, <>pbsfunc, <>dutycycle, <>atkprop, <>relprop, <>curve;
var whichsynthdef;	
	
	*initClass {

	StartUp.add({
		
		2.do({arg i;

		SynthDef.writeOnce(\cb2playbuf++((i+1).asSymbol),{arg bufnum=0,outbus=0,rate=1,startPos=0,dur=0.1,atkprop=0.005, relprop=0.005, curve=0;  
		var playbuf, env;
		
		playbuf= PlayBuf.ar(i+1,bufnum,BufRateScale.kr(bufnum)*rate,1,startPos,1);
		
		env= EnvGen.ar(Env.linen(atkprop*dur,(1.0-atkprop-relprop)*dur,relprop*dur, 1.0, curve),doneAction:2);
		
		Out.ar(outbus,playbuf*env);
		});
		});
		
	});		
	}
	
	//will get Server from BBCutGroup later 
	*new{arg bbcutbuf, offset, pbsfunc, dutycycle, atkprop, relprop, curve; 
	
	^super.new.initCutBuf2(bbcutbuf, offset, pbsfunc, dutycycle, atkprop, relprop, curve);
	}
	
	initCutBuf2 {arg bcb, off, pf, dc,ap,rp,c; 
	
		bbcutbuf=bcb;
		offset= off ? 0.0;
		pbsfunc=pf ? 1.0;
		dutycycle= dc ? 1.0; //{arg dur; 1.0}; 
		atkprop= ap ? 0.001;
		relprop= rp ? 0.001;
		curve= c ? 0;
		
	
	}
	
		
	setup {
		whichsynthdef= \cb2playbuf++(cutgroup.numChannels.asSymbol);
	}
	
	//there should be a free function to release this, I guess group release will sort that out
	
	//could refine CPU use by rendering one playbuf for all later repeats (need to know block.length-cuts[0][0] and sending retrigger messages for each cut
	
	renderBlock {arg block,clock;
		var startpos,rate, tmp;
	
		bbcutbuf.tryPerform(\updateblock,  block);
		pbsfunc.tryPerform(\updateblock,  block);
		offset.tryPerform(\updateblock,  block);
		dutycycle.tryPerform(\updateblock,  block);
		atkprop.tryPerform(\updateblock,  block);
		relprop.tryPerform(\updateblock,  block);
		curve.tryPerform(\updateblock,  block);
	
		startpos= if(block.offset.isNil,{
		
		bbcutbuf.chooseoffset(block, offset);
		},{
		bbcutbuf.convertoffset(block);
		});
			
		//this is wrong at this point, tempo may change while cached! 	
		rate=(clock.tempo)/(bbcutbuf.bps); //bps/buffertempo
			
		//["rate check", rate, clock.tempo, bbcutbuf.bps, pbsfunc.value(0,block)].postln;	
	
		block.cuts.do({arg cut,i;
		var dur;
		
		dur=(cut[1])*(dutycycle.value(i,block));
		
		//synthid= s.nextNodeID;
	
		//grain so -1 for id
		block.msgs[i].add([\s_new,whichsynthdef, -1, 0,cutgroup.synthgroup.nodeID,\outbus,cutgroup.index,\bufnum,bbcutbuf.bufnum,\startPos, startpos, \rate,rate*(pbsfunc.value(i,block)),\dur, dur,\atkprop,atkprop.value(i,block),\relprop, relprop.value(i,block), \curve, curve.value(i,block)]);
		
		if(trace.notNil,{trace.msg(block, i,0.0,0.0, \offset,startpos/(bbcutbuf.numFrames),\repeatlength,dur)});
		

		});
		
		//don't need to return block, updated by reference
	}
	
	
}