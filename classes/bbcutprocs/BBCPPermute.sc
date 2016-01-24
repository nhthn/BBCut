//This file is part of The BBCut Library. Copyright (C) 2001  Nick M.Collins distributed under the terms of the GNU General Public License full notice in file BBCutLibrary.help
//bbcutproc permute N.M.Collins 12/10/02

BBCPPermute : BBCutProc
{
    var subdivfunc,permutefunc,stutterfunc;

    //variables persisting between spawns
    var beatsleft;
    var subdiv, index, prop;

    *new
    {
        arg phraselength=4.0,subdivfunc=8,permutefunc,stutterfunc=1;

        ^super.new(0.5,phraselength).initBBCPPermute(subdivfunc,permutefunc,stutterfunc);
    }

    initBBCPPermute
    {
        arg sdf=8,pf,sf=1;

        subdivfunc=sdf;

        //default do nothing permutation
        permutefunc=pf ?
        {
            arg index,n;
            index%n;
        };

        stutterfunc= sf;
    }


    chooseblock
    {
        var repeats,temp;

        //new phrase to calculate?
        if(phrasepos>=(currphraselength-0.001),
            {
                this.newPhraseAccounting;

                //new cutsize?
                subdiv= subdivfunc.value(currphraselength).round(1.0).asInteger;	//integers only

                beatspersubdiv=currphraselength/subdiv;

                prop= 1.0/subdiv;

                //blocksize=beatspersubdiv;

                index=0;
            }
        );

        beatsleft= currphraselength- phrasepos;

        //always new slice/roll to calculate
        blocklength=beatspersubdiv;

        //safety to force conformity to phrases
        if(blocklength>(beatsleft-0.001),{blocklength= beatsleft;});

        repeats=stutterfunc.value(index, subdiv, phrasepos).round(1.0).asInteger.max(1);

        temp=blocklength/repeats;
        cuts=Array.fill(repeats,{temp});

        //correction for arithmetic errors
        cuts.put(repeats-1,temp+(blocklength-(temp*repeats)));

        temp= permutefunc.value(index, subdiv).round(1.0).asInteger;

        index=index+1;

        temp= (temp%subdiv)*prop;

        bbcutsynth.setoffset(temp);

        //flags if a stutter is happening
        this.updateblock(if((repeats-1)>0, 1, 0));

        this.endBlockAccounting;
    }


}