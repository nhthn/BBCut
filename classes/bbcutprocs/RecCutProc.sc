//This file is part of The BBCut Library. Copyright (C) 2001  Nick M.Collins distributed under the terms of the GNU General Public License full notice in file BBCutLibrary.help

//RecursiveCutProc for BBCut Library 17/10/01 N.M.Collins
//auxilliary class RCutData

//subdivisions of phrase can be gained as sum of cutlengths

//potential improvements direction, but losing rhythmic quantisation-
//all durs and offsets are in proportion ie original cut= [3,3,2]/8 original offset= [0,0,6]/8
//reccut= [2,4,2]/8 all code then works the same with addition of searchforoffset(f) for any 0.0<=f<=1.0

//to work really recursively have everything take a source cutsize/offset seq and produce another cutsize/offset seq

//only really makes sense for BBCutSynth which responds to setoffset
//like a derived/alternative version of BBCutSynthSF

RecCutProc : BBCutProc
{
    var offsetlist;
    var rcd;
    var primitive;

    *new
    {
        arg rcd,phraselength=4.0;

        ^super.new(0.5,phraselength).initRecCutProc(rcd);
    }

    initRecCutProc
    {
        arg r;

        rcd=r;

        offsetlist= Array.new(0);

        block=0;
    }

    chooseblock
    {
        var next;

        if((offsetlist.size)==block,
            {	//new phrase
                this.newPhraseAccounting;

                //this is pretty much beatspersubdiv?
                primitive=currphraselength/rcd.subdiv;

                //calculate blocks list for this phrase
                offsetlist= rcd.offsetseq;
        });

        //render block
        next=offsetlist.at(block);
        blocklength=next.at(0)*primitive;
        cuts=[blocklength];

        //proportionate- will be taken as percentage through sample
        offset = (next.at(1).asFloat)/(rcd.subdiv);

        //to minimise floating point errors?
        //if(block==(offsetlist.size-1),{blocklength=currphraselength- phrasepos;});

        this.updateblock;

        this.endBlockAccounting;
    }


}







