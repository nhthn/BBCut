//This file is part of The BBCut Library. Copyright (C) 2001  Nick M.Collins distributed under the terms of the GNU General Public License full notice in file BBCutLibrary.help

//choose cut proc N.M.Collins 24/10/01
//limited control of beats per subdiv from this class- but no value updating

//not done by choosing blocksize first (see warpcutproc) but by
//choosing minimal cut then a number of repeats (see bbcutproc11)
//special effect like roll constrained towards end of phrase
//but no use of units (can easily convert any subdiv,cutlist,barlength scheme to this paradigm)

//can derive own versions overriding pertinent functions for rollfunc etc

ChooseCutProc : BBCutProc
{
    var cutsizefunc,repeatfunc,rollfunc,rollchance,rollallowed;

    //variables persisting between spawns
    var beatsleft;
    var rollon;

    *new
    {
        arg cutsizefunc=nil,repeatfunc=nil,rollfunc=nil,phraselength=16.0,rollchance=0.1,rollallowed=2.0,bpsd=0.5;

        ^super.new(bpsd,phraselength).initChooseCutProc(cutsizefunc,repeatfunc,rollfunc,rollchance,rollallowed);
    }

    initChooseCutProc
    {
        arg csf=nil,repf=nil,rollf=nil,rc=0.1,ra=2.0;


        //convoluted because cannot write csf= cutsizedefault and csf=this.cutsizedefault calls the function!
        if(csf.isNil,
            {
                csf={ arg ...extraArgs; this.cutsizedefault(extraArgs.at(0),extraArgs.at(1))};
        });

        if(repf.isNil,
            {
                repf={ arg ...extraArgs; this.repeatfuncdefault(extraArgs.at(0),extraArgs.at(1),extraArgs.at(2))};
        });

        if(rollf.isNil,
            {
                rollf={ arg ...extraArgs; this.rollfuncdefault(extraArgs.at(0))};
        });


        cutsizefunc=csf;
        repeatfunc=repf;
        rollfunc=rollf;
        rollchance=rc;
        rollallowed=ra;

        rollon=0;
    }



    chooseblock
    {
        var repeats,proj,cutsize;

        //new phrase to calculate?
        if(phrasepos>=currphraselength,
            {
                this.newPhraseAccounting;
            }
        );

        beatsleft= currphraselength- phrasepos;

        rollon=if((rollchance.value.coin) &&(beatsleft<(rollallowed.value)),
            {	//roll
                blocklength=beatsleft;
                cuts=rollfunc.value(beatsleft);
                1
            },
            {	//normal
                cutsize= cutsizefunc.value(phrasepos,currphraselength);

                if(cutsize>beatsleft,{cutsize=beatsleft});

                repeats= repeatfunc.value(cutsize,phrasepos,currphraselength);

                proj= (repeats*cutsize)+ phrasepos;

                //should be same logic as old algorithm for numrepeats=2
                while( {proj>currphraselength},
                    {
                        repeats= repeats-1;

                        if(repeats<=1,
                            {
                                repeats=1;
                                cutsize= beatsleft;
                            }
                        );

                        proj= (repeats*cutsize)+ phrasepos;

                });

                cuts= Array.fill(repeats,{cutsize});

                blocklength=repeats*cutsize;
                0
            }
        );

        roll = rollon;

        

        this.endBlockAccounting;
    }




    //main code will reduce cutsize if off the end, but can check yourself if desired
    cutsizedefault
    {
        arg pos,currlength;	//prop is phrasepos/currphraselength

        var prop;

        prop=pos/currlength;

        ^[1.0,1.5].choose	//wchoose([0.1+(0.8*prop),0.1+0.8*(1-prop)])
    }

    //main code will reduce num repeats if necessary, but can check yourself if desired
    repeatfuncdefault
    {
        arg cutsize,pos,currlength;
        ^[1,2].choose	//wchoose([0.4,0.6])
    }

    //return a list of cuts
    rollfuncdefault
    {
        arg blocksize;

        var reps,cutsize;

        reps= (blocksize*4).round(1.0).asInteger;

        cutsize=blocksize/reps;

        ^Array.fill(reps,{cutsize})
    }


}