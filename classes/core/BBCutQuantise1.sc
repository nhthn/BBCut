//This file is part of The BBCut Library. Copyright (C) 2001  Nick M.Collins distributed under the terms of the GNU General Public License full notice in file BBCutLibrary.help

//BBCutQuantise1 07/1/05  by N.M.Collins

//master quantise for all blocks, 16th triplet

//swing test always based on proximity to 0.25


BBCutQuantise1 {
    var <>swing;
    var <>phrasetotal;

    *new{arg swing=0.25;

        ^super.new.swing_(swing).phrasetotal_(0.0);
    }

    value {arg block, proc;
        var copyiois,pos,lastpos, keptindices;

        //startpos= block.phrasepos;

        //work out a satisfactory cutlength from b.phrasepos and proc.phrasepos

        //block.length;

        copyiois= block.cuts.copy;
        keptindices=Array.fill(copyiois.size,{false});

        //add on to phrasetotal one at a time seeing where they fall on quantise grid

        pos= phrasetotal;

        lastpos=pos;

        copyiois.do({arg cut,i;
            var nextpos,quantpos,roundpos,swingadd, ioi;

            ioi=cut[0];

            nextpos= pos+ioi;

            quantpos= (nextpos%0.5);

            swingadd= if(abs(quantpos-0.25)<0.125,1,0);

            roundpos= nextpos.round(0.5);

            if(roundpos>nextpos, {roundpos= roundpos-(swingadd*(0.5-swing));},			 {roundpos= roundpos+(swingadd*swing);});

            if(roundpos>(lastpos+0.0001),{
                ioi=roundpos-lastpos;
                copyiois[i][0]= ioi;
                copyiois[i][1]= ioi;
                lastpos=roundpos;
                keptindices[i]=true;
            },{copyiois[i]=0;});

            pos=nextpos;
        });


        //remove any 0.0 length iois

        //lastpos gives revised length
        block.length= lastpos-phrasetotal;
        block.cuts= copyiois.select({arg val,i; keptindices[i]});


        if(block.length<0.001,{"this shouldn't be zero?".postln});

        //trust length works out, else no sweat
        //
        //end of phrase, must add up to correct amount here if possible
        if((proc.currphraselength-0.001)<(proc.phrasepos),{

            //reset for new phrase
            phrasetotal=0.0;
        });

    }



}