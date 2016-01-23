//This file is part of The BBCut Library. Copyright (C) 2001  Nick M.Collins distributed under the terms of the GNU General Public License full notice in file BBCutLibrary.help

//CutTrace 7/8/05  by N.M.Collins 

//experimental, no guarantees

//base class for trace, will support Post, OSC and File Out

//compensate can be s.latency for enaction at same time as Server event. If ping time> s.latency, must allow for this

//CutBuf3, CutStream1 et al- include a cutsynthtype field? 

CutTrace {
	var instance, compensate, data;
	var func;
	
	*new {arg instance=1, compensate=0.0, data;
	
	^super.newCopyArgs(instance,compensate, data).init}
	
	
	//override this in subclasses
	init {
		func={Post <<([\instance,~instance]++~msg) << nl; };
	}
	
	//args list of property, value tags
	msg {arg block, i, beatdelay=0.0, timedelay=0.0...args;
		var event;
		//[\instance,instance]++
		event= (instance: instance, msg:args++[\repeat,i,\totalrepeats,block.cuts.size],play:func,target:data);
		
		block.functions.add([block.cumul[i]+beatdelay,compensate+timedelay, event]);
		
	}


}