//This file is part of The BBCut Library. Copyright (C) 2001  Nick M.Collins distributed under the terms of the GNU General Public License full notice in file BBCutLibrary.help

//N.M.Collins 24/10/01

//currently no way of deciding which states for cuts etc, too complex
//do yourself by making own classes or write own cutfuncs

//possibly rewriting BBCutProc11 so then have automatic available cuts- just can't change subdiv as go along,
//so pretty equivalent to choosecutproc (just a couple of subtle diffs in algos)

//final advanced- write a new synth that has a statbalstream for offset positions rather than lehmer random number generator
//DONE- again. not so noticeable an effect on breakbeats, better on extremely varied sources

StatBalProc : ChooseCutProc
{
    var cutsbs,repeatsbs;

    *new
    {
        arg cutsbs=nil,repeatsbs=nil,phraselength=16.0,rollchance=0.1,rollallowed=2.0,bpsd=0.5;

        ^super.new(nil,nil,nil,phraselength,rollchance,rollallowed,bpsd).initStatBalProc(cutsbs,repeatsbs);
    }

    initStatBalProc
    {
        arg csbs=nil,rsbs=nil;
        /*	may need if derived defaults aren't set- but OK, works automatically
        cutsizefunc={ arg ...extraArgs; this.cutsizedefault(extraArgs.at(0),extraArgs.at(1))};
        repeatfunc={ arg ...extraArgs; this.repeatfuncdefault(extraArgs.at(0),extraArgs.at(1),extraArgs.at(2))};
        */

        if(csbs.isNil,{csbs=StatBalStream.new([0.5,1.5,2.5],[0.35,0.6,0.05],0.5);});
        if(rsbs.isNil,{rsbs=StatBalStream.new([1,2,3],[0.6,0.3,0.1],0.5);});

        cutsbs=csbs;
        repeatsbs=rsbs;
    }

    //main code will reduce cutsize if off the end, but can check yourself if desired
    cutsizedefault
    {
        arg pos,currlength;	//prop is phrasepos/currphraselength

        var prop;

        prop=pos/currlength;

        //if set differing weights for different parts do here
        //if set cutsize weight to zero will need StatBalNormStream rather than StatBalStream

        ^cutsbs.next
    }

    //main code will reduce num repeats if necessary, but can check yourself if desired
    repeatfuncdefault
    {
        arg cutsize,pos,currlength;

        ^repeatsbs.next;
    }

}



//this version changes weights over the phrase
//do simple changer using Lehmer for comparison

StatBalProc2 : ChooseCutProc
{
    var cutarray,repeatarray;
    var cutsbs,repeatsbs;

    *new
    {
        arg cutarray=nil,repeatarray=nil,chet=0.5,rhet=0.5,phraselength=16.0,rollchance=0.0,rollallowed=2.0,bpsd=0.5;

        ^super.new(nil,nil,nil,phraselength,rollchance,rollallowed,bpsd).initStatBalProc2(cutarray,repeatarray,chet,rhet);
    }

    initStatBalProc2
    {
        arg carray=nil,rarray=nil,chet=0.5,rhet=0.5;

        if(carray.isNil,{carray= [[0.5,1.5,2.5],[0.0,0.6,0.4],[0.5,0.5,0.0]]});
        if(rarray.isNil,{rarray= [[1,2,3],[0.4,0.4,0.2],[0.7,0.3,0.0]]});

        cutarray=carray;
        repeatarray=rarray;

        cutsbs=StatBalNormStream.new(cutarray.at(0),cutarray.at(1),chet);
        repeatsbs=StatBalNormStream.new(repeatarray.at(0),repeatarray.at(1),rhet);
    }

    //main code will reduce cutsize if off the end, but can check yourself if desired
    cutsizedefault
    {
        arg pos,currlength;	//prop is phrasepos/currphraselength

        var prop,weights,start,end;

        //reduce effective currlength so reach weights end sooner (at point rolls become permissible)
        currlength= currlength-rollallowed.value;
        //assumes currlength at least 1.0- but no danger if wrong, just wrong weights for position in phrase
        if(currlength<1.0,{currlength=1.0});

        prop= pos/currlength;

        if(prop>1.0,{prop=1.0});

        //linear interp of starting and ending distributions
        start= cutarray.at(1);
        end=cutarray.at(2);

        cutsbs.weights_(((1.0-prop)*start) + (prop*end));

        //no danger updating repeats weights same time since know will be called next anyway
        start= repeatarray.at(1);
        end=repeatarray.at(2);

        repeatsbs.weights=((1.0-prop)*start) + (prop*end);

        //if set differing weights for different parts do here
        //if set cutsize weight to zero will need StatBalNormStream rather than StatBalStream

        ^cutsbs.next
    }

    //main code will reduce num repeats if necessary, but can check yourself if desired
    repeatfuncdefault
    {
        arg cutsize,pos,currlength;

        ^repeatsbs.next;
    }

}





