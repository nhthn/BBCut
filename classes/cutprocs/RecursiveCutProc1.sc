//This file is part of The BBCut Library. Copyright (C) 2001  Nick M.Collins distributed under the terms of the GNU General Public License full notice in file BBCutLibrary.help

//RecursiveCutProc1 for BBCut Library 6/5/02 N.M.Collins
//moving on from the RecCutProc proof of concept to a fully capable analytic recursive cutter

//only really makes sense for BBCutSynth which responds to setoffset
//like a derived/alternative version of BBCutSynthSF

//RCP2 next- make choices of cut up recursion level specific- so smaller cuts for higher levels?
//TO ADD- initial offsetlist can be specified? No need really...

//name change- used to be called RecursiveCutProc1

RecursiveCutProc1 : BBCutProc
{
    var cutfunc,repeatfunc,offsetfunc,reclevel;
    var offsetlist;
    var quantise;

    *new
    {
        arg cutfunc,repeatfunc,offsetfunc,reclevel=2,phraselength=4.0,bpsd=0.5;

        ^super.new(bpsd,phraselength).initRecursiveCutProc1(cutfunc,repeatfunc,offsetfunc,reclevel);
    }

    initRecursiveCutProc1
    {
        arg cf,rf,of,rl;

        cutfunc= cf ? {[1.5,1].wchoose([0.666,0.334])};

        repeatfunc= rf ? {(2.rand)+1};

        offsetfunc= of ? {arg q,bpsubdiv; rrand(0,q - 1)*bpsubdiv};

        reclevel= rl ? 2;

        offsetlist= Array.new(0);

    }

    chooseblock
    {
        var next;

        if((offsetlist.size)==block,
            {	//new phrase
                this.newPhraseAccounting;

                //currphraselength.postln;

                //number of points available for offsetting
                quantise=(currphraselength/beatspersubdiv).round(1.0).asInteger;

                //calculate blocks list for this phrase, through reclevel iterations

                offsetlist=[[currphraselength,0.0]];

                reclevel.value.do(
                    {
                        offsetlist= this.calculatecuts(offsetlist);

                        //Post << offsetlist << nl;

                    }
                );

                //Post << offsetlist << nl;

        });

        //render block
        next=offsetlist.at(block);
        blocklength=next.at(0);
        cuts=[blocklength];

        //proportionate- will be taken as percentage through sample
        offset = next.at(1)/currphraselength;

        

        this.endBlockAccounting;
    }


    calculatecuts
    {
        arg array;
        var out,done,cutsize,repeats,offset,offend;

        done=0.0;
        out=Array.new(0);

        //////////////////////////////
        //HACK for debug, and investigations
        //currphraselength=8.0;
        //number of points available for offsetting
        //quantise=(currphraselength/beatspersubdiv).round(1.0).asInteger;
        /////////////////////////////////


        //this forces a wraparound and makes the code below safe for looping
        //(assuming cutsize <= phraselength and phraselength= sourcelength)
        array=array++array;

        //convert array to format [start,end,dur,offset] for convenience
        array= this.prepare(array);

        //start making cuts

        while({done<(currphraselength-0.00001)},	//to account for any floating point error
            {
                cutsize=cutfunc.value(done,currphraselength);

                //reduce cutsize if it won't fit in the phrase
                if((done+cutsize)>currphraselength,{cutsize=currphraselength-done;});

                repeats=repeatfunc.value(done,currphraselength);

                while({((repeats*cutsize)+done)>currphraselength},{repeats=repeats-1;});

                offset=offsetfunc.value(quantise,beatspersubdiv, done, currphraselength);
                offend=offset+cutsize; //what about wrapping?- hopefully taken care of by trick above

                //cutsize.post; "  cutsize".postln;
                //repeats.post; "  repeats".postln;
                //offset.post; "  offset".postln;
                //offend.post; "  offend".postln;

                repeats.do(
                    {
                        arg i;

                        //add all pertinent cuts to out

                        array.do
                        (
                            {
                                arg val,j;
                                var start,end,istart,iend;

                                start=val.at(0);
                                end=val.at(1);


                                if((start<=offset) && (end>offset),
                                    {//then intersection
                                        //"i1".postln;
                                        istart=offset;
                                        iend= end.min(offend);

                                        out=out.add([iend-istart,val.at(3)+(istart-start)]);
                                });

                                //these two options of intersection are mutually exclusive

                                if((start>offset) && (start<offend),	//end<=offend
                                    {//other type of intersection
                                        //"i2".postln;
                                        istart=start;
                                        iend= end.min(offend); //end

                                        out=out.add([iend-istart,val.at(3)]);
                                    }
                                );

                        });


                        done=done+cutsize;

                    }
                );

        });

        ^out;
    }

    prepare
    {
        arg array;
        var out,sum;

        sum=0.0;

        array.do
        (
            {
                arg val,i;

                array.put(i,[sum,sum+(val.at(0)),val.at(0),val.at(1)]);

                sum=sum+val.at(0);
            }
        );

        ^array;
    }


}







