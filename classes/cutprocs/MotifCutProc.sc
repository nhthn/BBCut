//This file is part of The BBCut Library. Copyright (C) 2001  Nick M.Collins distributed under the terms of the GNU General Public License full notice in file BBCutLibrary.help
//MotifCutProc- N.M.Collins 2/03/02

//accompanying Motif class? No, complicates easy creation of arrays
//can add convert string to array format function to this class as class method!

MotifCutProc : BBCutProc
{
    var motiflist,indexfunc,phraselength;
    var motifpos,motifindex,currmotif,motifsize;

    //variables persisting between spawns
    var beatsleft;

    *new
    {
        arg motiflist,indexfunc,phraselength=8.0,bpsd=0.5;

        ^super.new(bpsd,phraselength).initMotifCutProc(motiflist,indexfunc);
    }

    initMotifCutProc
    {
        arg ml,if;

        motiflist=ml ? List.newUsing([[[1.5,1.5],[1.0]]]);

        //pass motiflist as argument to choice
        indexfunc= if ? {motiflist.size.rand};

        //adding additional level of hierachy myself
        motifpos=0;
        motifsize=0;
        currmotif=0;
        motifindex=0;
    }


    chooseblock
    {
        var temp,temp2,temp3;

        //new phrase to calculate?
        if(phrasepos>=currphraselength,
            {
                this.newPhraseAccounting;

                //force move onto new motif!
                motifpos=motifsize;
            }
        );

        beatsleft= currphraselength- phrasepos;

        //new motif?
        if(motifpos>=motifsize,
            {
                motifindex=indexfunc.value(motiflist);
                currmotif=motiflist.at(motifindex);

                //ie number of blocks in this motif
                motifsize=currmotif.size;
                motifpos=0;
            }
        );

        //get next block from motif

        cuts=currmotif.at(motifpos);
        motifpos=motifpos+1;

        //have to calculate blocklength
        temp=0.0;
        cuts.do({arg val; temp= temp+val;});
        blocklength=temp;

        //if it doesn't fit!
        if(blocklength>beatsleft,
            {
                //take as much as possible
                temp=0.0;
                temp3=nil;

                cuts.do({arg val,i; temp= temp+val;

                    if((temp>=beatsleft) && (temp3.isNil),{temp2=i;temp3= (temp-beatsleft);});

                });

                //can take up to ith and must curtail ith
                cuts= Array.fill(temp2+1,{arg i; cuts.at(i)});
                cuts.put(temp2,(cuts.at(temp2))-temp3);
                blocklength=beatsleft;

                //"corrected  ".post; blocklength.post; " ".post; cuts.postln;

        });

        

        this.endBlockAccounting;
    }


    *stringtomotif
    {
        arg string;
        var output;
        var pos,temp,first,repeat,asc;

        //r's are repeats, others are numbers, else ignore

        output= Array.new;

        //go through parsing a block at a time

        pos=0;

        while({pos<(string.size)},
            {//try to sort a block
                //code is number, then possible repeats

                temp=string.at(pos);
                asc=temp.ascii;

                first=1;
                repeat=1;

                if((asc<58) && (asc>48),{
                    first=temp.digit;

                    pos=pos+1;

                    if(pos<string.size,
                        {
                            temp=string.at(pos);
                            asc=temp.ascii;

                            //now check for repeat
                            if((asc==114),
                                {
                                    pos=pos+1;

                                    if(pos<string.size,
                                        {
                                            temp=string.at(pos);
                                            asc=temp.ascii;

                                            if((asc<58) && (asc>48),{
                                                repeat=temp.digit;
                                            });

                                            //always move on because if not number, no need to test again later!
                                            pos=pos+1;

                                    });

                            });

                    });

                    output=output.add(Array.fill(repeat,first));

                },{pos=pos+1;});

            }
        );


        ^output
    }


}

