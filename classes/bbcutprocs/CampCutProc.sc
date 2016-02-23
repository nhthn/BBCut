//This file is part of The BBCut Library. Copyright (C) 2001  Nick M.Collins distributed under the terms of the GNU General Public License full notice in file BBCutLibrary.help

//campcutproc N.M.Collins 17/10/01

//only really makes sense for BBCutSynth which responds to setoffset

CampCutProc : BBCutProc
{
    var offsetlist,bells;
    var campstream;
    var bellbeat;

    *new
    {
        arg campstream,phraselength=4.0;

        ^super.new(0.5,phraselength).initCampCutProc(campstream);
    }

    initCampCutProc
    {
        arg cs,pf=4.0;

        campstream = cs ?? {
            // Default: Gainsborough Little Bob Major
            CampStream.notation(8, "x.18.x.18.x.16.x.18.x.18.x.12")
        };
        campstream.isKindOf(CampStream).not.if {
            campstream = CampStream.notation(campstream[0], campstream[1]);
        };

        bells= campstream.bells;
        offsetlist= List.series(bells,0,1);
        block=bells;
    }

    chooseblock
    {
        var temp,perm;

        if(bells==block,
            {	//new phrase
                this.newPhraseAccounting;

                //bellbeat=currphraselength/bells;
                //fixed block length, bells blocks per phrase
                blocklength=currphraselength/bells;

                //find next offsetlist
                //calculate offset sequence
                perm= campstream.next; //permdata.at(method.next);

                //calls to swap, permute
                perm.do(
                    {
                        arg val,i;
                        //involutions always swap val and one above val
                        offsetlist.swap(val,val+1);
                });
                offsetlist.postln;

        });

        //render block

        //always new slice/roll to calculate
        cuts=[blocklength];

        this.updateblock;

        //to minimise floating point errors?
        //if(block==(bells-1),{blocklength=currphraselength- phrasepos;});

        //proportionate- will be taken as percentage through sample
        bbcutsynth.setoffset((offsetlist.at(block))/bells);

        this.endBlockAccounting;
    }


}


