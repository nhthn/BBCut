//This file is part of The BBCut Library. Copyright (C) 2001  Nick M.Collins distributed under the terms of the GNU General Public License full notice in file BBCutLibrary.help
//SQPusher1 N.M.Collins 3/4/03
//With apologies to Tom Jenkinson

//could have a further fill deviancy argument, have high n-tuplet fills less rare based on deviancy


SQPusher1 : BBCutProc
{
    var activity, fillfreq, fillscramble;
    var beatsleft, cutsequence;
    var temp,index, done, temp2;
    var sqweights1;
    var beatpos,barpos,quaverpos,barprop, sqchance, quaver;


    *new
    {
        //phraselength is always 4.0
        arg activity=0.1,fillfreq=4,fillscramble=0.0, sqweights, bpsd=0.5;

        var phraselength= 4.0;

        ^super.new(bpsd,phraselength).initSQPusher1(activity, fillfreq, fillscramble, sqweights);
    }

    initSQPusher1
    {
        arg ac=0.1, ff=4, fs=0.0, sw;

        activity=ac;
        fillfreq=ff;
        fillscramble=fs;

        sqweights1= sw ? [0.0,0.3, 0.0, 0.5, 0.7, 0.8, 0.9, 0.6];
    }


    chooseblock
    {

        //new phrase to calculate?
        if(phrasepos>=(currphraselength-0.001),
            {
                this.newPhraseAccounting;

                //structure for phrase is A1A2A3fill

                //fill if phrase%4=0

                //make the 4 here a routine variable
                if(phrase%(fillfreq.value)==0,
                    {

                        //choice of fills- these are all authentic Squarepusher transcripted rhythms adapted to my phrase/block/cuts representation
                        //a generative routine could be constructed here, with quintuplets, sextuplets and other rhythmic tricks
                        cutsequence= [
                            [[0.75,0.75,0.75,0.75],[1.0]],
                            [[0.5,1.0],[1.0],[1.0,0.5]],
                            [[0.5],[1.0,1.0,1.0],[0.5]],
                            [ [ 0.571429 ], [ 0.571429, 0.571429 ], [ 0.571429, 0.571429 ], [ 0.571429 ], [ 0.285714, 0.285716 ] ],
                            [[1.0,0.5],[1.0,0.5],[0.5,0.5]],//[[0.5,0.25],[0.5,0.25],[0.25,2.25]],
                            [[0.5,0.5],[0.66,0.67,0.67],[1.01]],
                            [[0.34],[0.33,0.33],[0.34,0.33],[2.33]],
                            [[1.4],[0.4,0.4],[0.6,0.2],[1.0]],
                            [[ 0.167, 0.167,0.166 ],[1.0,1.0,1.0],[0.5]],
                            [[1.5,0.5,1.0],[0.25,0.25,0.25,0.25]],
                            [[0.2,0.2],[0.4,0.4],[0.4,0.4],[2.0]],
                            [[0.75,0.75,1.0],[0.25,0.25,0.25,0.25,0.25,0.25]],
                            [[0.5, 1.0],[0.5],[0.125,0.125,0.125,0.125],[1.0],[0.167, 0.167,0.166]]
                        ].choose; //([0.5,0.5]);

                        if(fillscramble.value.coin,{cutsequence.scramble});

                    },
                    //else standard run, chance of semiquavers from the activity parameter
                    {

                        done=0.0;

                        cutsequence= LinkedList.new;

                        while({done<4.0},{

                            beatpos= (done.round(0.25))%1.0;

                            barpos= done.asInteger;

                            quaver= (((done.round(0.5))*2).asInteger)%8;

                            barprop= barpos/4.0;

                            //more chance of SQ towards the back of the bar? Use barprop*activity
                            //need a weighting function really
                            sqchance= (sqweights1.at(quaver))*(activity.value);

                            temp=(2.rand)+1;

                            if(beatpos== 0.5, {temp=1});

                            temp2= if(sqchance.coin, {Array.fill(temp*2,{0.25})},{Array.fill(temp,{0.5})});

                            cutsequence.add(temp2);
                            done=(temp2.sum)+done;

                        });

                        //could do some later alterations and refinements here, but leave for now

                });



            }
        );

        beatsleft= currphraselength- phrasepos;

        //just overlap in scheduling if a problem
        cuts=cutsequence.at(block);

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

        this.updateblock;

        this.endBlockAccounting;
    }


}