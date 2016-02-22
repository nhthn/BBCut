//This file is part of The BBCut Library. Copyright (C) 2001  Nick M.Collins distributed under the terms of the GNU General Public License full notice in file BBCutLibrary.help

//BBCutProc11- N.M.Collins 17/10/01, originally known as BreakBeatx

//24/10/01 added stutterarea param- prop of subdiv num units in which stutter allowed to occur (for all tail of phrase)

BBCutProc11 : BBCutProc
{
    //functional variables
    var sdiv,barlength,phrasebars,numrepeats,stutterchance,stutterspeed,stutterarea;
    //variables persisting
    var numbarsnow;
    var unitblock,unitsdone,totalunits;
    var subdiv,index;
    var sdivbeats;
    var stutteron;	//to tell when a stutter was selected

    *new
    {
        arg sdiv=8,barlength=4.0,phrasebars=3,numrepeats=nil,stutterchance=0.2,stutterspeed=2,stutterarea=0.5;

        ^super.new.initBBCutProc11(sdiv,barlength,phrasebars,numrepeats,stutterchance,stutterspeed,stutterarea);
    }

    initBBCutProc11
    {
        arg sd=8,bl=4.0,pb=3,nr=nil,sc=0.2,ss=2,sa=0.5;

        sdiv=sd;
        barlength=bl;
        phrasebars=pb;
        numrepeats=nr;
        stutterchance=sc;
        stutterspeed=ss;
        stutterarea=sa;

        if(numrepeats.isNil,
            {
                numrepeats= {(2.rand)+1};
            }
        );


        unitsdone=0;
        totalunits=0;
        numbarsnow=0;
        stutteron=0;
    }


    chooseblock
    {

        var unitsleft, unitproj,stutsp;
        var oddmax,repeats;

        //new phrase to calculate?
        if(unitsdone>=(totalunits-0.000001),	//account for very slight floating point errors
            {

                //new cutsize?
                subdiv= sdiv.value.round(1.0).asInteger;	//integers only

                //new number of beats to cut into subdiv units
                sdivbeats=barlength.value;

                //new numbarsnow- each bar is sdivbeats long
                numbarsnow= phrasebars.value.round(1.0).asInteger;	//must be an integer//could be a function that returns up to maxphrase
                totalunits= numbarsnow*subdiv;

                currphraselength= sdivbeats*numbarsnow;
                this.newPhraseAccounting(currphraselength);


                //beats per unit bpu=currphraselength/totalunits;
                //but this is beatspersubdiv, ie beats per unit for this algo
                beatspersubdiv=sdivbeats/subdiv;

                unitsdone= 0;
                repeats=0;
                //no longer any accounting for repeats, render scheme is a block of repeats at a time
            }
        );

        unitsleft= totalunits- unitsdone;


        //always create a new block

        //chance of stutter
        if( (stutterchance.value.coin) && (unitsleft<((stutterarea.value)*subdiv)), //only within a final bar of a phrase (could make half bar with subdiv/2)
            //STUTTER
            {
                //stutterspeed must be an integer greater than zero
                stutsp=stutterspeed.value.round(1.0).asInteger;

                repeats= unitsleft*stutsp;
                unitblock=1.0/(stutsp);

                blocklength=currphraselength-phrasepos;	//remainder
                unitsdone= totalunits;
                stutteron=1;
            },
            //NO STUTTER
            {
                stutteron=0;
                oddmax= subdiv.div(2);
                if((oddmax % 2)==0,{oddmax= ((oddmax-2).div(2));}, {oddmax= ((oddmax-1).div(2))});
                unitblock= rrand(0,oddmax);
                unitblock= (2*unitblock)+1;

                while( { unitblock> unitsleft }, {unitblock=unitblock-2;});	//this part makes a difference! only odd number unitblocks...

                if(unitblock<=0, {"this should never happen".postln});

                repeats= (numrepeats.value.round(1.0).asInteger);	//usually 1 or 2

                unitproj= (repeats*unitblock)+ unitsdone;

                //should be same logic as old algorithm for numrepeats=2
                while( {unitproj> totalunits},
                    {
                        repeats= repeats-1;

                        if(repeats<=1,
                            {
                                repeats=1;
                                unitblock= unitsleft;
                            }
                        );

                        unitproj=(repeats*unitblock)+ unitsdone;

                });

                blocklength=repeats*unitblock*beatspersubdiv;

                if(unitproj==totalunits,{blocklength=currphraselength-phrasepos;});	//remainder in this case

                unitsdone= unitsdone+ (repeats*unitblock);

        });

        //ACCOUNTING
        //for floating point errors use difference between phrasepos and currphraselength for final blocks- done above

        //calculate cuts and block size from this directly!
        //using unitproj as temp variable
        unitproj = unitblock*beatspersubdiv; //beat length of a repeat
        cuts = Array.fill(repeats,{unitproj});

        roll = stutteron;

        

        this.endBlockAccounting;
    }

}

