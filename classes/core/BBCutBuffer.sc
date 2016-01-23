//This file is part of The BBCut Library. Copyright (C) 2001  Nick M.Collins distributed under the terms of the GNU General Public License full notice in file BBCutLibrary.help

//BBCutBuffer 3/1/05  by N.M.Collins


//provides facilities for choosing num of beats or base tempo
//can have analysis data on transient locations, segmentations, managed on server by a secondary buffer mirrored here?
//offset choosing object as part of this- defaults to random positions based on imposed grid
//var <>grain=0.5, <>randomoffset=0.0;
//for continuous write, offset is relative to write head pos- else for fixed bufs, write head is at 0
//so can use one class for all offset decisions?
//can add convenience class methods for alloc and set beats etc


BBCutBuffer : Buffer {
	classvar <>defaultPAT=0.005;
	var <>length, <beatlength, <>bps;

	//event start positions
	var <eventstarts;	//in frames
	//maybe this data is mirrored on the server?
	//var transients, eventtype, pats; //data for each event
	var <eventlengths; //in seconds
	var <eventpats;
	var <onsetsinbeats;
	var <quantised, <groovepos; //quantised used as basic source, groovepos is adaption of quantised to a given groove template
	var <timedeviations;

	//shortcut function
	*new {arg filename, beatlength=8, eventlist, action;

		^super.read(Server.default,filename, action: {
			arg buf;
			buf.beatlength_(beatlength);
			buf.events_(eventlist);
			action.value(buf);
		});
	}

	*array {arg filenames, beatlengths, eventlists, action;
		var remaining, result;

		beatlengths= beatlengths ?? {Array.fill(filenames.size,{1})};

		remaining = filenames.size;

		result = filenames.collect({
			arg val,i;
			BBCutBuffer(
				val,
				beatlengths[i],
				if(eventlists.notNil, {eventlists[i]}, {nil}),
				action: {
					remaining = remaining - 1;
					if(remaining <= 0, { action.value(result); });
				}
			);
		});
		^result;
	}

	beatlength_ {arg b=4;

		length= numFrames/sampleRate;

		beatlength=b;
		bps= b/length;
	}

	events_ {arg eventlist;

		if(eventlist.isKindOf(Collection),{
			if(eventlist.isEmpty,{ eventlist=0.5;});
		});

		eventlist= eventlist ? 0.5;

		if(not(eventlist.isKindOf(Collection)),{

			eventlist= Array.fill((beatlength/eventlist).asInteger, {arg i; ((i*eventlist/beatlength)*numFrames).round(1).asInteger});
		});

		if(eventlist[0].isKindOf(Collection),{

			eventstarts=Array.fill(eventlist.size,{arg i; eventlist[i][0]});
			eventlengths=Array.fill(eventlist.size,{arg i; eventlist[i][1]});

			//can add pats etcs
			eventpats=Array.fill(eventlist.size,{arg i; eventlist[i][2]});

		},{
			eventstarts=eventlist;

			//distances between eventstarts are eventlengths, must convert to seconds
			eventlengths=eventstarts.rotate(-1)- eventstarts;
			eventlengths[eventlengths.size-1]=numFrames-eventstarts[eventlengths.size-1]-1;

			eventlengths=eventlengths/sampleRate;

			//Post << eventlengths << nl;

			//can add pats etcs, defaults to 5mS
			eventpats=Array.fill(eventlist.size,{arg i; BBCutBuffer.defaultPAT});

		});


		onsetsinbeats= (eventstarts/numFrames)*beatlength;

		//find nearest semiquaver for all onsets
		quantised=onsetsinbeats.round(0.25);

		groovepos= quantised.copy;

		//time in seconds error which will be preserved as you change speed
		//perhaps multiplied by some constant, could be zero, to use quantise positions
		//how will swing work? must delay second semiquavers
		timedeviations= (onsetsinbeats-quantised)/bps;

		//if less than a mS, set to zero
		timedeviations.do({arg val, j;   if(abs(val)<0.001,{timedeviations[j]=0.0}); });

		//Post << [onsetsinbeats, quantised, timedeviations] << nl;

	}

	//only really works for semiquaver swing, can't impose groove- perhaps could allow a setter for groovepos?
	setgroove {arg groovefunc;
		var t,prev,next;
		var groovepos;

		//corresponds to delay to second SQ of 0.07 beat= (0.32,0.18) swing= uk garage...
		//groovelevel can be a function, passed the argument phrasepos
		groovefunc= groovefunc ?  {arg beat; var tmp; tmp= beat%0.5; if(  ((tmp>=0.25) && (tmp<=0.49)) ,{beat-tmp+0.32},{beat})};

		//assume semiquaver resolution for a rough template of regions
		groovepos=quantised.copy;

		groovepos.do({arg val,i;

		t= groovefunc.value(val);

		//make sure between previous and next value
		prev = if(i>0, {groovepos[i-1]},{0.0});
		next = if(i<(groovepos.size-1), {groovepos[i+1]},{beatlength});

		if(t<prev,{t=prev;});
		if(t>next, {t=next;});

		groovepos[i]=t;
		});

		//groovepos= groovepos.sort; //in case put them well out of order?

	}






	//problem if wraparound 1.5 beats after 0.5 from end say - rewrite as as many wraps of these
	//as needed, with appropriate time delays? recursively call this

	//return list of indices
	findevents {arg start, dur;
		var output, end, teststart,q, cont, index;
		var list1, list2;
		end=start+dur;

		//need to cope with wraparound- but must delay scheduling time of events appropriately
		//will only support one wraparound, not going further than start
		if(end>beatlength,{

		if(end>(start+beatlength),{"cut longer than source".postln;  end=start+beatlength-0.005;});

		list1= this.findevents(start,beatlength-start-0.005);
		list2= this.findevents(0,end-beatlength);

		^(list1++list2);

		//^((list1++list2).asList);
		});


		teststart= start-0.01; //to account for floating point errors

		output= List.new;

		//find position in quantised such that >=start-0.001 and <start+dur
		//binarysearch

		index=0;

		cont=true;


		//groovepos here used to be quantised
		while({(index<(groovepos.size)) && cont},{

		q=groovepos[index];

		if(q>=end,{cont=false;});

		if((q>=teststart) && (q<end),{

		output.add(index);
		});

		index=index+1;
		});

		^output;

	}

	//return trimmed list of indices within bounds
	trimevents {arg start, dur, input, pre, post, tempo;
		var output, end, pos, pass;
		var list1, list2;
		//var whiletest;

		end=start+dur;

		//need to cope with wraparound- but must delay scheduling time of events appropriately
		//will only support one wraparound, not going further than start
		if(end>beatlength,{

		if(end>(start+beatlength),{"cut longer than source".postln;  end=start+beatlength-0.005;});

		//find index jump
		list1=input[0];
		list2=0;

		//must have only contribution from wrap around and nothing in original, special case
		if(list1==0,{^this.trimevents(0,end-beatlength,input[list2 .. (input.size-1)],false, post, tempo);});

		//occasionally crashes due to second clause being checked even if first fails
		while({(list2<input.size) && (list1<=input[list2])},{list2=list2+1;});

		//crashes
	//	whiletest=true;
//		while(whiletest, {
//
//		if((list2>=input.size),{whiletest=false;},{ if(list1<=input[list2],{list2=list2+1;},{whiletest=false;});
//
//		});
//
//		});
//

		//else normal routine sufficient
		if(list2!=input.size,{

		//fails if first [] was empty in []++[]? see findevents

		list1= this.trimevents(start,beatlength-start-0.005,input[0 .. list2-1], pre, false, tempo);
		list2= this.trimevents(0,end-beatlength,input[list2 .. (input.size-1)],false, post, tempo);

		^list1++list2;});

		});

		output= List.new;

		input.do({arg index, i;

		//used to be quantised
		pos= groovepos[index];

		pass=true;

		if(pos>=end,{pass=false;

		//Post << [\endfail, pos, end, index, groovepos] << nl;
		});

		//if(pre && ((timedeviations[index])<(-0.001)),)
		if(pre && ((pos+((timedeviations[index])*tempo))<(start-0.001)),
		{pass=false;

		//Post << [\prefail, pre, timedeviations[index]] << nl;
		});

		//use of original tempo here may mess up scheduling when have other playback tempi
		//but can't use current clock tempo since may be wrong!
		//actually, best estimate is current tempo, now passed in and used

		if(post && ((pos+((timedeviations[index])*tempo))>end),{pass=false;

		//Post << [\postfail, pos, ((timedeviations[index])*tempo), end] << nl;

		});

		if(pass,{output.add(index)});

		});


		^output;
	}


	chooseoffset {arg block, offsetparam=0.0, beatorframe=0;
		var phrasepos= block.phrasepos;
		var posbeats;
		var randomoffset;
		var grain=0.5;
		var numstarts;

		//var randomoffset=0.0;

		if (offsetparam.isKindOf(Function), {

		^offsetparam.value(this, block, beatorframe);
		});

		if(offsetparam.isKindOf(ArrayedCollection),{

		randomoffset=offsetparam[0].value;
		grain=offsetparam[1].value;
		},{randomoffset= offsetparam.value;});

		//quantised in quavers
		numstarts= (beatlength/grain).asInteger;

		//this calculation depends on the number of beats in the source sample
		posbeats= if(randomoffset.coin,{

			//possible num of cut positions within sample- get subdivs per beat and times by beats available
			//grain*(((1.0/grain)*beats).asInteger.rand)

			(numstarts.rand)*grain
			//(((quantised.choose)/beatlength)*numFrames).round(1).asInteger; //eventstarts.choose;


		},{

			//work out blocks sample params
			//where are we up to in the sample?
			(phrasepos%beatlength)
		});

		//in frames since about to synthesise with PlayBuf

		//.round(1).asInteger;
		^if(beatorframe==0, {((posbeats/beatlength)*numFrames)},{posbeats});

	}

	convertoffset {arg block, beatorframe=0;

	^if(beatorframe==0,{numFrames*(block.offset)},{beatlength*(block.offset)});
	}

}