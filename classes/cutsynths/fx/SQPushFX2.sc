//This file is part of The BBCut Library. Copyright (C) 2001  Nick M.Collins distributed under the terms of the GNU General Public License full notice in file BBCutLibrary.help

//SQPushFX N.M.Collins 3/4/03, updated for bbcut2 6/8/05

//assumes 4 beat bar
//ampzerochance at the moment, could be stresschance


SQPushFXAmp2 {	
	var ampchance, amptemplate;
	var barpos; 
	
	*new {	
		arg ampchance=0.1, amptemplate;
		
		^super.new.initSQPushFXAmp2(ampchance, amptemplate);
	}
	
	initSQPushFXAmp2 {	
		arg ac=0.1, ampt=nil;
		
		ampchance=ac; 
		amptemplate=ampt ? [1.0,  0, 0.09, 0.06, 0.24, 0.03, 0.15, 0.06, 0.21, 0.03, 0.12, 0.09, 0.24, 0.21, 0.18, 0.21 ]; 
		
	}
	
	value {	
		arg repeat,block;
		var here, amp;
		
		here= (barpos*4).round(1.0).min(15.0).asInteger;	//corrected- used to be max! 
		
		amp= if (((amptemplate.at(here))*(ampchance.value)).coin,0,1); 
		
		//add ioi to next cut
		barpos= barpos+(block.iois[repeat]); 
		
		^amp
	}
	
	updateblock {	
		arg block;
		
		barpos= 4.0*(block.phraseprop); //BBCutBlock
	}
	
}





//assumes 4 beat bar
//subtle change of CutPBS1
SQPushFXPitch2 : CutPBS1 {	
	var pbchance;
	
	*new {
		arg pbm=0.97,directfunc, pbchance=0.2;
		
		^super.new(pbm,directfunc).initSQPushFXPitch2(pbchance);
	}
	
	initSQPushFXPitch2 {
		arg pbc=0.2;
		
		pbchance=pbc; 
	}
	
	updateblock {
		arg block;
		
		pbdirect=directfunc.value;
		
		pbmult= if(pbchance.value.coin, {pbmfunc.value},{1.0});
		pbmultrecip=1.0/pbmult;
		
		currpbmult=1;
	}
	
}




