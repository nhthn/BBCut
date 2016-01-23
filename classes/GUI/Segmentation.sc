//This file is part of The BBCut Library. Copyright (C) 2001  Nick M.Collins distributed under the terms of the GNU General Public License full notice in file BBCutLibrary.help

//Segmentation- N.M.Collins 1/8/05

//updated 6/6/2006

//new GUI system showing segmented event locations and lengths
//overview, zoomview both SCSoundFileView objects, SCEnvelopeView for event markers, can't edit them?

//start with just overview, no zoom, event markers, load/analyse buttons

//will run the AnalyseEvents2 UGen to get information 

//SignalPlusGUI, see plot method

Segmentation {
	var overview, eventview;
	var w, loadb, analysisb, analysisNRTb, nametext, postb;
	var viewsize;
	
	var <onsets, <lengths, <pats, <timbre; 
	var filename, f;
	var buffer, analysisbuffer;
	var sflength;
	var s;
	var threshslid;
	var loadflag; 
	var keyoffset;
	
	*new {|server|
		^super.new.initSegmentation(server ? (Server.default));
		}
	
	initSegmentation {|server|
		var tmp;
		
		s= server ?? {Server.default};
		
		loadflag=false;
		
		//allow for up to 30 seconds at 15 events/sec 15*10*30+2
		analysisbuffer= Buffer.alloc(s,4502,1); //can't have more thean 255 events, expand later
		
		viewsize=800;
		
		w=  Window("onset detection GUI", Rect(50,500,viewsize+50,200));
		
		overview=  SoundFileView.new(w, Rect(5,5, viewsize, 90));

		f = SoundFile.new;

		//overview.elasticMode_(true);
		//overview.timeCursorOn_(true);
		//overview.timeCursorColor_(Color.red);
		//overview.timeCursorPosition_(2050);
		overview.gridOn_(false);
		//overview.gridResolution_(0.2);
		
		overview.drawsWaveForm_(true);
	
		eventview = EnvelopeView(w, Rect(5, 105, viewsize, 30));
		
		eventview.thumbWidth_(3.0); //setProperty(\thumbWidth, 3.0);
		eventview.thumbHeight_(30.0); //setProperty(\thumbHeight, 30.0);
		
		eventview.drawLines_(false);
		eventview.strokeColor_(Color.red);
		eventview.selectionColor_(Color.blue);
		
		nametext= StaticText(w, Rect(5, 155, 400,30)).font_(Font(\HelveticaNeue, 12)).string_(" ");
		
		loadb= Button(w, Rect(405, 155, 70,30));
		loadb.states= [["load",Color.blue]];
		
		loadb.action_({
		
		Dialog.openPanel({arg path; 
		
		//no checking safety, just tries to load as best it can
		this.load(path);
		
		});
		
		});
		
		analysisb= Button(w, Rect(475, 155, 70,30));
		analysisb.states= [["analyse",Color.blue]];
		
		analysisb.action_({ if(loadflag,{this.analyse;}); });
	//	
//		analysisNRTb= SCButton(w, Rect(555, 155, 70,30));
//		analysisNRTb.states= [["NRT",Color.blue]];
//		
//		analysisNRTb.action_({if(loadflag,{this.analyseNRT;});});
//		
		postb= Button(w, Rect(625, 155, 70,30));
		postb.states= [["post",Color.blue]];
		
		//output in format required by bbcut2 for CutBuf3
		postb.action_({
		var outputarray;
	
		outputarray=Array.fill(onsets.size,{arg i; [onsets[i],lengths[i]/(f.sampleRate),pats[i],timbre[i]]});
		
		Post << outputarray <<nl;  //? 1.0 
		});
		
		threshslid= DDSlider(w, Rect(700, 155, 100,50),"threshold",0.0,1.0,\lin,0.01,0.34);
			
		keyoffset=0;	
				
		w.view.keyDownAction_({arg ...args; 
		var last, start, end;
			var key;
		
			key=args[3];
		
			//key.postln;
			
			//capitals
			if((key<91) && (key>64),{
			
			keyoffset= (key-65)*26;
			
			});
		
			if((key<123) && (key>96),{
			if(((buffer.notNil) && (onsets.notNil)) ,{
				key=key-97;
				last=(key+keyoffset)%(onsets.size);
				
				start= onsets[last]; ///f.numFrames;  
				end= lengths[last]; ///f.numFrames;
				
				//CutBuf3
				Synth((\cb3playbuf++(f.numChannels)), [\bufnum, buffer.bufnum, \startPos,start,\dur, (end/f.sampleRate), \atk,0.0,\rel,0.0]);
				
				//f.numFrames
			
				overview.setSelectionStart(0,start);	
				overview.setSelectionSize(0,end);
				
				
			});
			});
		
		
		});
		
		
		w.front;
	}

	//must also load Buffer if turning on with a SoundFile there
	loadServer {
	
		s.boot;
		
		s.waitForBoot({"server loaded".postln; this.loadBuffer});
	}
	
	quitServer {
	
		s.quit;
	}
	
	
	loadBuffer {
	
		//also load corresponding Buffer on the Server 
		
		//if Server loaded
		
		Routine.run({
		
		if(s.serverRunning, {
		buffer.free; //free previous, safe for Nil
		buffer=Buffer.read(s,filename); //(,action:{arg buf; sflength= buf.numFrames/buf.sampleRate;}); 
		
		s.sync;
		
		"buffer loaded".postln;
		});
		 
		});
		
	}

	
	load {|file|
		
		loadflag=false;
		
		nametext.string_("loading");
		
		filename=file;
	
		f.openRead(filename);
		overview.soundfile_(f);
		overview.read(0, f.numFrames, 1); //block size of 1 for best details
		
		sflength= (f.numFrames)/(f.sampleRate);
		
		this.loadBuffer;
		
		nametext.string_(filename++"  loaded");
		
		w.refresh;
		
		loadflag=true;
	}
	
	
	//run as realtime Synth, then obtain events 
	analyse {	
		
		
		if (not((s.serverRunning) || (s.serverBooting)),{"server not booted".postln;	//this.loadServer
		
		this.analyseNRT;
		
		},{
		
		
		if(sflength>30.0,{"Too long for Realtime mode".postln; nametext.string_("Too long for realtime"); ^0.0});
		
		//"analyse".postln;
		
		//choose based on numChannels
		nametext.string_("analysing");
	
		//should do NRT if possible- do on internal server? 
		
		//THIS SHOULD BE s.sendBundle! No, in RT can just take buffer positions, relative time not important 
		Synth(\segmentation++((buffer.numChannels).asSymbol), [\playbufnum,buffer.bufnum,\analysisbufnum,analysisbuffer.bufnum, \length, sflength, \threshold, threshslid.value]);
		
		SystemClock.sched(sflength+1.0, {	
					
			//Post << "transfer" << nl;		
					
			analysisbuffer.loadToFloatArray(action: {	 arg array; 
				var num;
				
				//Post << array << nl;
				
				num= array[0];
				
				this.prepareData(num, array);
			
			});
		
		});
		});
		
	}
	
	prepareData {arg num, array;
	
		lengths=Array.fill(num,1);
		onsets=Array.fill(num,1);
		pats=Array.fill(num,{0.005});
		timbre=Array.fill(num,{1});
		
		num.do({arg i;
		var base, ending;
		
		base= i*10+2;
		
		onsets[i]=array[base];
		
		ending= array[base+1];
		
		if(ending>(f.numFrames),{ending=f.numFrames;});
		lengths[i]=(array[base+1])- onsets[i];
		
		//once add in analysis code
		//transients[i]=(array[base+2]);
		pats[i]=(array[base+3]);
		timbre[i]=(array[base+4]);
		//also pitch, loudness, type, beatdev
		
		});
		
		{nametext.string_(filename++"  analysed"); this.updateEvents;}.defer;
	
	}
	
	
	
	
	//needs some sort of notification when analysis done and output file available
	analyseNRT {
		var x,o;
		var analysisfilename;
		var datasf;
		var buffersize;
		
		analysisfilename= filename++".analysis";
	
		//allow for 15 events per second, else beyond rhythmic rate
		buffersize= 150*sflength+2; 
	
		if ((s.serverRunning) || (s.serverBooting),{
		
		"server booted, can't run in NRT mode".postln;
		//this.quitServer;
		
		}, {
		
		nametext.string_("analysing");
			
		Routine({	
			var array1, array2, num, a, limit;
			
		x = [
		[0.0, [\b_alloc, 1, buffersize, 1]],
		[0.0, [\b_allocRead, 0, filename, 0, 0]],
		[0.0, [ \s_new, \segmentation++((f.numChannels).asSymbol), 1000, 0, 0,  \playbufnum,0,\analysisbufnum,1, \length, sflength, \threshold, threshslid.value]],
		
		//after length of soundfile played, end
		[sflength,[\b_write,1,analysisfilename,"WAV", "float"]],
		[sflength, [\c_set, 0, 0]] 
		];
		
		o = ServerOptions.new.numOutputBusChannels = f.numChannels; // mono output
		
		//o = ServerOptions.new.numInputBusChannels = 1; // mono input
		//"sounds/SCsamp/breaks/drumtimemono.wav"
	
		//"NRTanalysis.wav"
		Score.recordNRT(x, "NRTanalysis", PathName.tmp +/+ "NRTAnalysis.wav", nil, 44100, "WAV", "int16", o); // synthesize
				
		//"afterwards?".postln 
		//may need fine tuning how long is waited, works on my machine
		//1/10 of realtime on my machine, faster on others I guess! 
		//SystemClock.sched((sflength*0.1)+1.0,{
		//});
		
		limit=5000;	//1000 seconds	

0.01.wait; //make sure scsynth running first (can be zero but better safe than sorry)
while({
a="ps -xc | grep 'scsynth'".systemCmd; //256 if not running, 0 if running

//a.postln;

	(a==0) and: {(limit = limit - 1) > 0}
},{
	0.2.wait;	
});
		
		if(limit==0,{"timeout!".postln});
		
		//load analysis file into SoundFile object and extract the array of data
		
		datasf= SoundFile.new; 
		datasf.openRead(analysisfilename);
		
		array1=FloatArray.newClear(2);
		datasf.readData(array1);
		
		//array1.postln;
		
		num=array1[0].round(1.0).asInteger;
		
		//num.postln;
		
		array2=FloatArray.newClear(10*num);
		
		datasf.readData(array2);
		
		//array2.postln;
		
		this.prepareData(num, array1++array2);
		
		datasf.close;
		
		
		}).play(SystemClock);
		
		
		
		});
				
		//datasf	
				
	}
	
	
	updateEvents {
	
		eventview.value_([onsets/f.numFrames,Array.fill(onsets.size,0.0)]);
		
		w.refresh;

	}
	
	
	//added 1/2 second silence to force end of in progress events?
	//SynthDefs for analysis, two versions for mono and stereo files
	*initClass {
	
		StartUp.add({
	
		2.do({arg i;

		SynthDef.writeOnce(\segmentation++((i+1).asSymbol),{arg playbufnum, analysisbufnum, length, threshold=0.34;  
		var env, input;

		env=EnvGen.ar(Env([1,1,0,0],[length,0.5]),doneAction:2);		
		//assumes input soundfile loaded into buffer 0 
		input= PlayBuf.ar(i+1, playbufnum, BufRateScale.kr(playbufnum), 1, 0, 0); 
		
		if (i==1,{input=Mix(input)}); //mono analysis only
		
		AnalyseEvents2.ar(input*env, analysisbufnum, threshold,0,0);
				
		});
		
		});
		
		});
				
	
	}
	

}