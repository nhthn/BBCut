//This file is part of The BBCut Library. Copyright (C) 2001  Nick M.Collins distributed under the terms of the GNU General Public License full notice in file BBCutLibrary.help

//CutStream1 24/12/04  by N.M.Collins 

//envelope applied in record saves CPU on playback! 

//could have a single playback synth running with retrigger, though would have to free whenever
//recording a new block- can add a free msg though if keep track


CutStream1 : CutSynth {	

var <>inbus,<>bbcutbuf, deallocate;
var dutycycle, atkprop, relprop, curve;
var playdef, recdef;

	*new{arg inbus, bbcutbuf, dutycycle, atkprop, relprop, curve; 
	^super.new.initCutStream1(inbus, bbcutbuf,dutycycle, atkprop, relprop, curve);
	}
	
	initCutStream1 {arg ib, bcb, dc,ap,rp,c; 
	
		deallocate=false;
		inbus=ib ?? {Server.default.options.numOutputBusChannels};
		bbcutbuf = bcb; //could be nil, dealt with in setup
		dutycycle= dc ? 1.0;  
		atkprop= ap ? 0.001;
		relprop= rp ? 0.001;
		curve= c ? 0;
		
	}

	
			
	setup {
		playdef= \cs1playbuf++(cutgroup.numChannels.asSymbol);
		recdef= \cs1recordbuf++(cutgroup.numChannels.asSymbol);
		
		//have to wait until cutgroup known and have numChannels to create a Buffer
		bbcutbuf=bbcutbuf ?? {deallocate=true; BBCutBuffer.alloc(Server.default,44100,cutgroup.numChannels)};
		
		playdef.postln;
		recdef.postln;
	}
	
		
	free {
		if (deallocate,{bbcutbuf.free;});
	}
	
	//could refine CPU use by rendering one playbuf for all later repeats (need to know block.length-cuts[0][0] and sending retrigger messages for each cut
	
	renderBlock {arg block,clock;
		var s, grpnum, dc, atk, rel, crv;
		
		s=cutgroup.server;
		
		grpnum=cutgroup.synthgroup.nodeID;
		
		//pbsfunc.tryPerform(\updateblock,  block);
		dutycycle.tryPerform(\updateblock,  block);
		atkprop.tryPerform(\updateblock,  block);
		relprop.tryPerform(\updateblock,  block);
		curve.tryPerform(\updateblock,  block);
		
		block.cuts.do({arg cut,i;
		var dur;
		
		dc= dutycycle.value(i,block);
		atk= atkprop.value(i,block);
		rel= relprop.value(i,block);
		crv= curve.value(i,block);
		
		dur=cut[1]*dc; //adjusted to 
		
		if(i==0,{
		//block.msgs[i].add([\s_new, \br1playthrough, s.nextNodeID, 0,1,\dur,dur]);
		//hard coded for beat induction from bus 16
		block.msgs[i].add([\s_new, recdef, s.nextNodeID, 1,grpnum,\bufnum,bbcutbuf.bufnum,\dur,dur,\inbus,inbus.value,\outbus,cutgroup.index, \atkprop, atk, \relprop, rel, \curve, crv]);
		},{ //this was missing dur*dc before, rationalised now
		block.msgs[i].add([\s_new, playdef, s.nextNodeID, 0,grpnum,\bufnum,bbcutbuf.bufnum,\dur,dur,\outbus,cutgroup.index]);
		});
		
		if(trace.notNil,{trace.msg(block, i,0.0,0.0,\repeatlength,dur)});
		
		});
		
		//don't need to return block, updated by reference
	}
	
	
	

	
	*initClass {
		
		StartUp.add({
		
		2.do({arg i;
		
		SynthDef.writeOnce(\cs1playbuf++((i+1).asSymbol),{arg bufnum=0,outbus=0,dur=0.1;  
		var tmp;
		
		tmp= PlayBuf.ar(i+1,bufnum,1,1,0,1);
		
		Out.ar(outbus,tmp*EnvGen.kr(Env([1,1],[dur]),doneAction:2));
		
		});
		
		//run record buf for duration of block- or just make one and keep retriggering? 
		SynthDef.writeOnce(\cs1recordbuf++((i+1).asSymbol),{arg bufnum=0,inbus=8,outbus=0,dur=0.1, atkprop=0.0,relprop=0.0,curve=0; 
		var in,tmp;
		
		//used to be InFeedback for safety- must worry about execution order?
		//or make two versions, an InFeedback one as well 
		in=In.ar(inbus,i+1)*EnvGen.kr(Env([0,1,1,0],[atkprop,1.0-atkprop-relprop,relprop]*dur,curve),doneAction:2);
		
		tmp= RecordBuf.ar(in,bufnum,0,1,0,1,1,1);
		
		Out.ar(outbus,in);
		
		});
		
		});
		
		});
		
	}
	
}