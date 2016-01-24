//This file is part of The BBCut Library. Copyright (C) 2001  Nick M.Collins distributed under the terms of the GNU General Public License full notice in file BBCutLibrary.help

//MultiProc N.M.Collins 18/10/01

//only change at phrase rate normally unless provide blockchangefunc
//don't leave both nil

//multiproc currently not nestable- but shouldn't need since funcs can be arbitrary


//multiproc knows nothing of phrase
MultiProc : BBCutProc
{
    var procs, phrasefunc,blockfunc,currproc;

    *new
    {
        arg procs,phrasefunc=nil,blockfunc=nil;

        ^super.new.initMultiProc(procs,phrasefunc,blockfunc);
    }

    initMultiProc
    {
        arg p,pf=nil,bf=nil;

        procs=p;

        //if no choosing func at all is provided, make the obvious one
        if(pf.isNil && bf.isNil,{pf= {procs.size.rand}});

        phrasefunc=pf;
        blockfunc=bf;

        //random, just need seomthing which is automatically going to finish straight away
        currproc=procs.choose;//if(blockfunc.isNil,{procs.at(phrasefunc.value)},{procs.at(blockfunc.value)});
    }

    //no knowledge of where this proc is- no update of phraselength
    chooseblock	//must know when a phrase is done- use phraseover function in base BBCutProc
    {

        if(blockfunc.isNil,
            {
                if((currproc.phraseover)==1,{
                    currproc=procs.at(phrasefunc.value);

                    //do calcs yourself since don't want updatephrase called twice
                    phrasepos=0.0;
                    phrase=phrase+1;
                    block=0;
                });
            },
            {	//don't care about completing phrases
                currproc=procs.at(blockfunc.value);
            }
        );

        //get current data
        currproc.chooseblock;

        cuts=currproc.cuts;
        blocklength=currproc.blocklength;

        //this.updateblock; //won't be called twice- let cutprocs independentally call updateblock

        this.endBlockAccounting;
    }

    attachsynth
    {
        arg bbcs;
        procs.do({arg val; val.attachsynth(bbcs)});
    }

    phraseover
    {
        ^currproc.phraseover
    }

}

