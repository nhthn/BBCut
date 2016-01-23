//This file is part of The BBCut Library. Copyright (C) 2001  Nick M.Collins distributed under the terms of the GNU General Public License full notice in file BBCutLibrary.help

//BBCutBlock 24/12/04  by N.M.Collins 

//The unit of cut sequence creation and rendering

//all beat based durations

//msgs get attached by renderers

//can add variables for phraseprop etc

BBCutBlock {
	var <>blocknum; //number within a phrase, would indicate a phrase start by 0
	var <>length;
	var <>cuts; 
	var <>iois;
	var <>cumul;
	var <>msgs; //msgs exactly on cuts
	var <>timedmsgs; //msgs timed from start of block, may be within cuts
	//use this for functions which must be scheduled...
	var <>functions;
	var <>offset; 
	var <>phrasepos;
	var <>isroll;
	var <>phraseprop;
	var <>startbeat;
	
	
	//timedelay is groovedeviation- perceptual attack time
	addtimedmsgtocut {arg whichcut,beatdelay, timedelay, msg;
	
	//or need to adapt scheduler to send single arrays as single Msgs  List[msg]
	timedmsgs.add([cumul[whichcut]+beatdelay, timedelay, msg]);
	
	}
	
	
	//create [delta, bundle] list, will be sorted already
	cacheMsgs {arg cache, server;
	//var ioisum;
	var sortedorder;
	var event;
	
	sortedorder= List.new; //can sort Lists
	
	//ioisum=0.0;
	
	msgs.do({arg list,j; 
		//var ioi;
		
		//ioi= iois[j];
		
		if(list.notEmpty,{
		
		event= (server:server, msg:list, play:{arg slave; var s; s=~server; s.listSendBundle(s.latency,~msg)});
		
		sortedorder.add([startbeat+(cumul[j]),0.0,event]);
		});
		
		//ioisum=ioisum+ioi;
	});
	
	//could simplify this if cutsynths added timedmsgs with knowledge of adding startbeat themselves
	timedmsgs.do({arg msg,j; 
			
		event= (server: server, msg:msg[2], play:{arg slave; var s; s=~server; s.sendBundle(s.latency,~msg)});
			
		sortedorder.add([startbeat+(msg[0]),msg[1],event]);

	});
	
	//msg holds beatdev, time dev, function
	functions.do({arg msg;

		event= msg[2]; //(server:server, play:msg[2]);
			
		event.put(\server, server);	
	
		sortedorder.add([startbeat+(msg[0]),msg[1],event]);	
	});
	
	sortedorder= sortedorder.sort({arg a,b; (a[0])<(b[0])});
	
	//Post << "added"<< nl<< sortedorder << nl; 
	
	cache= cache++sortedorder; //will become a LinkedList
	
	^cache;
	}
	

}