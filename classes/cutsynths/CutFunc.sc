//This file is part of The BBCut Library. Copyright (C) 2001  Nick M.Collins distributed under the terms of the GNU General Public License full notice in file BBCutLibrary.help

//CutFunc 11/2/05  by N.M.Collins
//arbitrary function called at Cut, Block or Phrase, function is placed in an Event for later rendering, via BBCutBlock and clock

CutFunc : CutSynth {
    var <>cutfunc, <>blockfunc, <>phrasefunc, <>compensate;

    *new
    {
        arg cutfunc, blockfunc, phrasefunc, compensate=0.0;

        ^super.new.cutfunc_(cutfunc).blockfunc_(blockfunc).phrasefunc_(phrasefunc).compensate_(compensate);
    }

    //like CutBuf3
    renderBlock {arg block,clock;


        //BBCutBlock

        if(block.blocknum==1, {

            //phrasefunc.value(block,clock);

            if (phrasefunc.notNil,{

                block.functions.add([0.0,compensate,(block:block,clock:clock,func:phrasefunc, play:{~func.value(~block, ~clock);})]);

            });



        });

        //blockfunc.value(block,clock);

        if (blockfunc.notNil,{

            block.functions.add([0.0,compensate,(block:block,clock:clock,func:blockfunc, play:{~func.value(~block, ~clock);})]);

        });


        block.cuts.do({arg cut,i;
            //cutfunc.value(block,clock);

            if (cutfunc.notNil,{

                block.functions.add([block.cumul[i],compensate,(block:block,clock:clock,whichcut:i,func:cutfunc, play:{~func.value(~whichcut, ~block, ~clock);})]);

            });

        });

    }

}