//This file is part of The BBCut Library. Copyright (C) 2001  Nick M.Collins distributed under the terms of the GNU General Public License full notice in file BBCutLibrary.help

//CutSynth 27/12/04  by N.M.Collins 

//base class, cutgrp passes itself in to this, then calls setup method which is overridden in 
//sub classes

//Could add trace functionality for sending OSC messages from CutBuf3 et al where additional to 
//standard algorithmic composer cuts
//actually, have this as the standard method, if(trace.notNil,{trace.msg(block,i,paramslist)});

CutSynth {
var <cutgroup, <>trace;
//var <oscout; 

	//must be called by cutgroup
	initCutSynth {arg cg;
	
	cutgroup=cg;
	
	this.setup;
	}

	//override in subclasses
	setup {
	
	}
	
	free {
	
	}

}