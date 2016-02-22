//This file is part of The BBCut Library. Copyright (C) 2001  Nick M.Collins distributed under the terms of the GNU General Public License full notice in file BBCutLibrary.help

//TimelineCut N.M.Collins 31/8/05

TimelineCut : BBCutProc {
    var <>timelinefunc,<>blockfunc,<>freeze;

    //variables persisting between spawns
    var beatsleft;
    var currform;
    var blockarray;

    *new {
        arg phraselength=4.0,timelinefunc,blockfunc,freeze;

        ^super.new(0.5,phraselength).initCageCut(timelinefunc,blockfunc,freeze);
    }

    initCageCut {
        arg tlf,bf,f;

        timelinefunc= tlf ?? {[3,3,2]}; //archetypal default

        blockfunc=bf ? {arg dur, blocknum; [dur]};

        freeze=f ? false;
    }


    chooseblock {
        var temp, index;

        //new phrase to calculate?
        if(phrasepos>=(currphraselength-0.001), {

            //may choose new phraselength
            this.newPhraseAccounting;

            if(not(freeze.value) || (currform.isNil),{

                currform= currphraselength*(timelinefunc.value(currphraselength).normalizeSum); //always normalize to make safe
                blockarray= Array.fill(currform.size,{arg i; blockfunc.value(currform[i],i)});

                //Post << [currform, blockarray] << nl;

            });

            //beatspersubdiv=currphraselength/subdiv;

            index=0;
        });

        beatsleft= currphraselength- phrasepos;

        //could call permutefunc on each block
        cuts= blockarray[block];

        //cuts.postln;

        //always new slice/roll to calculate
        //blocklength=currform[block];
        blocklength=cuts.sum;

        //in case permutefunc changes lengths!
        if(blocklength>beatsleft,{

            temp=0.0; index=0;

            while({temp<(beatsleft-0.001)},{

                temp= temp+(cuts.at(index)); index=index+1;

            });

            index=index-1;

            cuts= cuts.copyRange(0,index);

            if((cuts.size)<1,{cuts= [beatsleft];},{
                cuts.put(index, cuts.at(index)- (temp-beatsleft));
            });

            blocklength= beatsleft;
        });

        

        this.endBlockAccounting;
    }


}