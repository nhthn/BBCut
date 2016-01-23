//This file is part of The BBCut Library. Copyright (C) 2001  Nick M.Collins distributed under the terms of the GNU General Public License full notice in file BBCutLibrary.help

//CutOSC 31/3/06  by N.M.Collins 

//klipp av messaging protocol 2 
		
CutOSC : CutTrace {
	
	//*new {arg instance=1, compensate=0.0, netaddr;
	//
	//^super.new(instance,compensate,netaddr)}
	
	
	//override this in subclasses
	init {
		
		func={
		
		~target.performList(\sendMsg,"sched", ~server.latency,  ~instance, ~msg);
		//		
//		~msg.clump(2).do({
//		arg item;
//		
//		~target.sendMsg("sched", ~server.latency, item[0], ~instance, item[1]);
//		});
//		
		}
	
	}
	

}