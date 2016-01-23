//This file is part of The BBCut Library. Copyright (C) 2001  Nick M.Collins distributed under the terms of the GNU General Public License full notice in file BBCutLibrary.help

//represents an External Clock ticking away, base class, wrapper for a TempoClock

//each provider has an associated Reference with an on flag, which is set to false by the provider 
//to leave the clock, addprovider returns this Reference

ExternalClock {
//classvar <>default;
var providers;
var <tempoclock;
var lastref;
var <lastBeatTime=0.0, <lastTempo=2, <lastBeat;

//	*initClass {
//	
//	Class.initClassTree(TempoClock);
//	ExternalClock.default= ExternalClock.new(TempoClock.default).play;
//	}
	
	//should pass in tempoclock, server
	*new {arg tempoclock;
	
	^super.new.initExternalClock(tempoclock);
	}
	
	initExternalClock {arg tc;
		
		tempoclock=tc ?? {TempoClock.default}; //use TempoClock as necessary
		
		if (tc.isKindOf(SimpleNumber),{
		tempoclock=TempoClock(tc);
		});
		
		//will initialise to default TempoClock beat position for ServerClock for instance
		lastBeat=tempoclock.beats; //1.neg; //keeps its own beat count for convenience, then not affected by
		//global beat positions of clock etc
		
		providers=List.new;
		
		lastref=Ref(true);
	}
	
	
	//can override this in subclasses
	//start immediately, could add quantise etc, passing in tempoclock etc
	play {arg quant=0;
	
		//tempoclock.schedAbs(tempoclock.elapsedBeats,{})
		tempoclock.play({
		
			//arg ...args; args.postln; [logical time, elapsed time, clock]
			
			//use logical beat time so can match up with other tasks running on the tempoclock
			lastBeat=tempoclock.beats; //lastBeat+1;
			lastBeatTime= Main.elapsedTime;
			lastTempo= tempoclock.tempo;
			
			this.tick;	
			//"beat!".postln;
			
			//[\bbcutclock, tempoclock.beats].postln;
			1.0
	
		},quant);
	
	}
	
	//requires rescheduling the list, no guarantees of safety
	//tempo_ {
//	
//	
//	}
//	
	
	tempo {
		^tempoclock.tempo;
	}
	
	tempo_ {arg val;
		tempoclock.tempo_(val);
	}
	
	tick {
			//this allows you to cancel schedules
			lastref.value=false;
			lastref=Ref(true);
			
			//call bbcutter to get the list of events within the next beat, passing in amount needed into following beat too
			
			//remove any dead providers? 
			providers.reverseDo({arg array;
			
			if (not(array[1].value),{this.removeprovider(array)});
			});
			
				
			this.getMaterial;
	
	}
	
	getMaterial {
	
		providers.do({arg array;
				var list, provider;
				
				provider=array[0];
				
				//provider.postln;
				
				//need next beat, allowing for latency, returns a SortedList 
				//first arg passing in TempoClock logical time
				list=provider.provideMaterial(tempoclock.beats,1.0, tempoclock.tempo);
				
				//Post << "schedule me  "<<nl << list <<nl<<nl ;
				
				this.sched(list, lastref, provider, array[1]);			
			});
	
	
	}
	
	//time based scheduling within a beat to account for PAT, beat deviation, server latency prescheduling
	sched { arg list, lastref, provider, alive;
	var listcount=0;
	var event;
		
		//Post << "schedstart" << Main.elapsedTime << nl;
		
		if(list.notEmpty,{	
			
			SystemClock.sched(0.0,{	
				var item;
		
				if((lastref.value) && (alive.value), {	
					item=list[listcount];
					
					if(	item.notNil,{
						
						//Post << ["sched ", listcount,item, Main.elapsedTime] << nl;
						
						listcount=listcount+1;
						
						//ie not nil
					//	if(item[1].isKindOf(List),{
//							//should check list not too large, also that cutsynth still exists? 
//							//should be fine, Server just complains if nodes for messaging not there
//							s.listSendBundle(s.latency,item[1]);
//						},{
//							
//							if(item[1].notNil,{
//							s.sendBundle(s.latency,item[1]);							//for timed msgs which aren't in Lists
//						});
//						
//						});
						
						
						//item.postln;
						
						event=item[1];
						
						//in general all events are now Events and can schedule the required action via play
						if(event.notNil,{
						
						//"pre ".postln; item[1].postln; item[1].play;
					
						event.use {
							~play.value(provider);	
							
							//event.postln;
						};
										
						});
			
						//["wait ",item[0]].postln;
						
						item[0];},nil);
				},nil)
				
			});
			
		});

	
	}


	addprovider {arg provider;
		var ref;
		
		ref= Ref.new.value_(true);
	
		providers.add([provider,ref]);	
	
		^ref;
		//Post << providers << nl;
	}
	
	
	removeprovider {arg array;
	
		providers.remove(array);		
		//Post << providers << nl;
	}
	
	
	stop {
		tempoclock.stop;
		
		providers=List.new;
		
		lastref.value=false;
		lastref=Ref(true);
	
		//providers.do({arg provider; provider.free;});
	}
	
	
	//clear {
//	
//	}
}





//Reference passing test code
//a={arg ref; Task({inf.do({ref.postln; 1.0.wait}); }).play}
//b=Ref.new;
//b.value_(2)
//a.value(b)


