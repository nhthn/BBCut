//This file is part of The BBCut Library. Copyright (C) 2001  Nick M.Collins distributed under the terms of the GNU General Public License full notice in file BBCutLibrary.help

//OffsetCP N.M.Collins 8/12/02

//durationoffsetfunc, offsetscale, durationscale

//combined duration/offset function
//or independent

//one call at beginning of phrase to determine whole phrase?
//or call as you go

OffsetCP1 : BBCutProc
{
    var dofunc, offsetscale, durationscale;
    //variables persisting between spawns
    var beatsleft, nextblock;

    var temp,index;


    *new
    {
        arg dofunc,offsetscale=1.0, durationscale=1.0,phraselength=8.0,bpsd=0.5;

        ^super.new(bpsd,phraselength).initOffsetCP(dofunc,offsetscale, durationscale);
    }

    initOffsetCP
    {
        arg dof,os=1.0,ds=1.0;

        dofunc=dof ?
        {	//procedure will automatically correct if choice is longer than beatsleft
            arg left,length;

            [[1.0], 0.0]
        };


        offsetscale=os;
        durationscale=ds;
    }


    chooseblock
    {

        //new phrase to calculate?
        if(phrasepos>=currphraselength,
            {
                this.newPhraseAccounting;

            }
        );

        beatsleft= currphraselength- phrasepos;

        nextblock= dofunc.value(beatsleft, currphraselength, block);

        //just overlap in scheduling if a problem
        cuts=(nextblock.at(0))*(durationscale.value);

        //always new slice/roll to calculate
        blocklength=cuts.sum;

        //safety to force conformity to phrases
        if(blocklength>beatsleft,{

            temp=0.0; index=0;


            while({temp<(beatsleft-0.001)},{

                temp= temp+(cuts.at(index)); index=index+1;

            });

            index=index-1;

            cuts= cuts.copyRange(0,index);

            if((cuts.size)<1,
                {
                    cuts= [beatsleft];
                },
                {
                    cuts.put(index, cuts.at(index)- (temp-beatsleft));
            });

            blocklength= beatsleft;
        });


        offset=(nextblock.at(1))*(offsetscale.value);

        

        this.endBlockAccounting;
    }


}