//This file is part of The BBCut Library. Copyright (C) 2001  Nick M.Collins distributed under the terms of the GNU General Public License full notice in file BBCutLibrary.help

//warp cut proc N.M.Collins 17/10/01

//added accel param 24/10/01- valid values around 0.5 to 0.99- don't pass zero or one!

WarpCutProc1 : BBCutProc
{
    var blocksizefunc,rollfunc,probs,accel;

    //variables persisting between spawns
    var beatsleft;
    var rollon;

    *new
    {
        arg blocksizefunc=nil,rollfunc=nil,probs=nil,phraselength=12.0,accel=0.9,bpsd=0.5;

        ^super.new(bpsd,phraselength).initWarpCutProc1(blocksizefunc,rollfunc,probs,accel);
    }

    initWarpCutProc1
    {
        arg bsf=nil,rf=nil,p=nil,acc=0.9;

        blocksizefunc=bsf;
        rollfunc=rf;
        probs=p;
        accel=acc;

        rollon=0;

        if(probs.isNil,
            {
                probs=[0.5,0.7,0.6];
        });

        if(blocksizefunc.isNil,
            {
                blocksizefunc= {	//procedure will automatically correct if choice is longer than beatsleft
                    arg left,length;
                    [0.5,1,2].wchoose([0.5,0.4,0.1]);
                };
            }
        );

        if(rollfunc.isNil,
            {
                rollfunc= {
                    arg size;
                    if(size<1.0,
                        {[4,8,16].choose},
                        {
                            [8,16,32].choose
                        }
                    );
                }
            }
        );

    }



    chooseblock
    {
        var repeats,temp,acctemp;

        //new phrase to calculate?
        if(phrasepos>=(currphraselength-0.0001),
            {
                this.newPhraseAccounting;
            }
        );

        beatsleft= currphraselength- phrasepos;

        //always new slice/roll to calculate
        blocklength=blocksizefunc.value(beatsleft,currphraselength);

        //safety to force conformity to phrases
        if(blocklength>beatsleft,{blocklength= beatsleft;});

        if(probs.at(0).value.coin,
            {//straight section
                rollon=0;
                repeats=1;
                cuts=[blocklength];
            },
            {//roll
                rollon=1;
                repeats= rollfunc.value(blocklength);

                if(probs.at(1).value.coin,
                    {//straight roll

                        //likely to be arithmetic errors- so schedule all cuts with one spawn
                        cuts=Array.fill(repeats,{blocklength/repeats});

                    },
                    {//accel
                        //accel is a parameter controlling the degree of acceleration in a geometric series
                        acctemp=accel.value;
                        temp= blocklength*(1-acctemp)/(1-((acctemp)**repeats));
                        cuts=Array.fill(repeats,{arg i; temp*((acctemp)**i)});

                        if(probs.at(2).value.coin,
                            {//accel, not ritard
                                cuts=cuts.reverse;	//fast to slow
                            }
                        );

                    }
                )
        });

        roll = rollon;

        
        
        this.endBlockAccounting;
    }


}