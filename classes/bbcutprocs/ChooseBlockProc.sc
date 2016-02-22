//This file is part of The BBCut Library. Copyright (C) 2001  Nick M.Collins distributed under the terms of the GNU General Public License full notice in file BBCutLibrary.help

//choose block proc N.M.Collins 6/1/02

//like choosecutproc is a simplification of BBCutProc11
//this is a simplification of WarpCutProc1
//could have been made a more general base like ChooseCutProc but kept simpler
//for demo purposes
//no use of rolls (would add just like in ChooseCutProc)

ChooseBlockProc : BBCutProc
{
    var blocksizefunc,numcutfunc,probs,accel;

    //variables persisting between spawns
    var beatsleft;

    *new
    {
        arg blocksizefunc,numcutfunc,phraselength=12.0,bpsd=0.5;

        ^super.new(bpsd,phraselength).initChooseBlockProc(blocksizefunc,numcutfunc);
    }

    initChooseBlockProc
    {
        arg bsf,ncf;

        blocksizefunc=bsf ?
        {	//procedure will automatically correct if choice is longer than beatsleft
            arg left,length;
            [0.5,1,2].wchoose([0.5,0.4,0.1]);
        };

        numcutfunc=ncf ? {
            arg size;
            if(size<1.0,
                {[4,8,16].choose},
                {
                    [8,16,32].choose
                }
            );
        };

    }


    chooseblock
    {
        var repeats,temp;

        //new phrase to calculate?
        if(phrasepos>=currphraselength,
            {
                this.newPhraseAccounting;
            }
        );

        beatsleft= currphraselength- phrasepos;

        //always new slice/roll to calculate
        blocklength=blocksizefunc.value(beatsleft,currphraselength);

        //safety to force conformity to phrases
        if(blocklength>beatsleft,{blocklength= beatsleft;});

        repeats=numcutfunc.value(blocklength);

        temp=blocklength/repeats;
        cuts=Array.fill(repeats,{temp});

        //correction for arithmetic errors
        cuts.put(repeats-1,temp+(blocklength-(temp*repeats)));

        

        this.endBlockAccounting;
    }


}