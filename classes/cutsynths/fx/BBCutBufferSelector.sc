//This file is part of The BBCut Library. Copyright (C) 2001  Nick M.Collins distributed under the terms of the GNU General Public License full notice in file BBCutLibrary.help

//BBCutBufferSelector N.M.Collins 30/8/05

//acts as if it is a BBCutBuffer object to the CutBuf2 and CutBuf3 classes

BufSelector {	
	var <array, <indexfunc, <>index;
	
	//needed by CutBuf3
	var <eventstarts, <eventlengths, <eventpats, <groovepos, <timedeviations; 
	
	*new {arg array, indexfunc;
		^super.new.initBufSelector(array, indexfunc);
	}
	
	initBufSelector{
		arg arr, ifunc;
		
		array= if(arr.isKindOf(Array),
		{arr}, 
		{[arr]}	//must be a single BBCutBuffer object at this juncture
		);
		
		indexfunc= ifunc ? 0;
		index=0;
	}
	
	
	newindex {
	index= (indexfunc.value(index).round.asInteger)%(array.size);
	}
	
	setindex {
	arg ind=0;
	
	index= ind%(array.size);
	}
	
	//wrappers to fetch the correct info
	bps {
	^array[index].bps;
	}
	
	beatlength {
	^array[index].beatlength;
	}
	
	numFrames {
	^array[index].numFrames;
	}

	bufnum {
	^array[index].bufnum;
	}
	
	chooseoffset { arg block, offset, type;
	
	^array[index].chooseoffset(block, offset,type);
	}
	
	convertoffset { arg block, type;
	
	^array[index].convertoffset(block,type);
	}	

	findevents {arg startpos, len;

	^array[index].findevents(startpos, len);
	}
	
	trimevents {arg start, dur, input, pre, post, tempo;

	^array[index].trimevents(start, dur, input, pre, post, tempo);
	}
	
		
	updateblock
	{
	arg block;
	
	index= (indexfunc.value(block, index).round(1.0).asInteger)%(array.size);
	
	//should just copy refs, shouldn't be overly expensive (price of this class use especially for CutBuf3
	
	 eventstarts=array[index].eventstarts;
	 eventlengths=array[index].eventlengths;
	 eventpats=array[index].eventpats;
	 groovepos=array[index].groovepos;
	 timedeviations=array[index].timedeviations;

	}


}
