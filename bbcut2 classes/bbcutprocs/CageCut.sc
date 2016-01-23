//This file is part of The BBCut Library. Copyright (C) 2001  Nick M.Collins distributed under the terms of the GNU General Public License full notice in file BBCutLibrary.help

//CageCut N.M.Collins 11/8/05

//following John Cage's square root form in miniature- really a recursive or fractal construction like Cantor set

//sets up uneven blocks but with a common offset

//necessarily calculates a phrase at a time

//perhaps don't want bpsd quantise

CageCut : BBCutProc {	
	var <>subdivfunc,<>permutefunc;
	
	//variables persisting between spawns
	var beatsleft;
	var currform; //subdiv
	var blockarray;
	
	*new {
		arg phraselength=8.0,subdivfunc,permutefunc;
		
		^super.new(0.5,phraselength).initCageCut(subdivfunc,permutefunc);
	}
	
	initCageCut {
		arg sdf,pf,of;
		
		subdivfunc=sdf ?? {[0.5,0.25,0.25]};
		
		//default do nothing permutation
		permutefunc=pf ? {arg array; array};
		
	}
	
	
	chooseblock {
		var temp, index;
		
		//new phrase to calculate?
		if(phrasepos>=(currphraselength-0.001), {
				
			//may choose new phraselength
			this.newPhraseAccounting;	
	
			//new cutsize?
			//subdiv= subdivfunc.value(currphraselength).round(1.0).asInteger;	//integers only
			
			currform= subdivfunc.value(currphraselength).normalizeSum; //always normalize to make safe
			
			blockarray= Array.fill(currform.size,{arg i; (currform[i])*currphraselength*currform});
			
			//beatspersubdiv=currphraselength/subdiv;
		
			index=0;
		});
		
		beatsleft= currphraselength- phrasepos;
				
		//could call permutefunc on each block
		cuts= permutefunc.value(blockarray[block]);
		
		//cuts.postln;
		
		//always new slice/roll to calculate
		//blocklength=currform[block];
		blocklength=cuts.sum;
		
		//in case permutefunc changes lengths!
		if(blocklength>beatsleft,{
			
			temp=0.0; index=0;
			
			while({temp<(beatsleft-0.001)},{
			
			temp= temp+(cuts.at(index)); index=index+1;
			
			});
			
			index=index-1;
			
			cuts= cuts.copyRange(0,index);
			
			if((cuts.size)<1,{cuts= [beatsleft];},{
			cuts.put(index, cuts.at(index)- (temp-beatsleft));
			});
			
			blocklength= beatsleft;
		});		
		
		//if(offsetflag,{});		
		//bbcutsynth.setoffset(currform[block]);
		
		//offsets are now decided by cut renderer		
		bbcutsynth.chooseoffset(phrasepos,beatspersubdiv,currphraselength);
		
		this.updateblock; 
		
		this.endBlockAccounting;
	}
	
	
}