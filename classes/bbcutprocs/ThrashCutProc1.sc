//This file is part of The BBCut Library. Copyright (C) 2001  Nick M.Collins distributed under the terms of the GNU General Public License full notice in file BBCutLibrary.help

//ThrashCutProc1 N.M.Collins 1/9/03
//With no apologies

//on fills invert role of kick and snare
//biased to 4/4 but can cope with other time signatures

//first of block amp boost- could become a parameter of the routine

ThrashCutProc1 : BBCutProc
{
    var beatsleft, cutsequence;
    var blocklen, left, source, siz, done, next, seq;	//temp variables
    var kickoffset, snareoffset, blockdiv, chooseriff, shuffles, filltest, stopchance;
    var <>database;
    var <>whichriff, currriff, fillflag, didstop;

    *new
    {
        arg kickoffset, snareoffset, phraselength=4.0, blockdiv, chooseriff, shuffles=1, filltest, stopchance=0.125;

        ^super.new(0.5,phraselength).initThrashCutProc1(kickoffset, snareoffset, blockdiv, chooseriff, shuffles, filltest, stopchance);
    }

    initThrashCutProc1
    {
        arg ko, so, bd, cr, sh, ft, sc;

        kickoffset= ko ? 0.0;
        snareoffset= so ? 0.125;

        //default of triplets
        blockdiv= bd ? [0.34,0.33,0.33];

        chooseriff= cr ? 0;
        shuffles= sh ? 1;
        filltest= ft ? {0.125.coin};

        stopchance= sc? 0.125;


        //blocksize, type (0=single kick, 1= single snare, 3=roll kick, 4=roll snare, 5= prescribe block)
        //then optional duty, amp

        //if need to explicitly define a block, done as blocklength, type 5, then list
        //subdiv,type (kick or snare), dutycycle, amp where last two are optional

        //can access from outside and add your own riffs so an instance variable not classvar

        //SHOULD EXTEND THIS, but fine for now
        database=
        [
            [[1.0,2],[2.0,1,1.0],[1.0,2],[4.0,5,4,1,1.0,1.0]],
            [[0.5,2],[1.0,1,0.25],[0.5,2],[0.5,2],[0.5,2],[0.25,1,1.0,0.0],[0.75,1]],
            [[0.5,2],[0.5,2],[0.5,2],[0.5,2],[0.5,1],[0.5,2],[0.5,1],[0.5,2]],
            [[0.5,2],[1.0,1],[0.5,2],[0.5,1],[0.5,2],[1.0,1]]
        ];

        //if next taken element will break the bank, just do a silent end to that bar
        whichriff=database.size.rand;
    }

    //unravelling the shortcut notation above with the thrash rolls feature
    calcblock
    {
        arg next;
        var bl, block,offset, amp, duty, durs;

        bl= next.at(0);	//block length

        //invert kick and snare block meanings for a fill
        if(fillflag,{

            if(next.at(1)==0, {next.put(1,1)},
                {
                    if(next.at(1)==1, {next.put(1,0)});
            });

            if(next.at(1)==2, {next.put(1,3)},
                {
                    if(next.at(1)==3, {next.put(1,2)});
            });

            if(next.at(1)==5, {
                if(next.at(3)==0, {next.put(3,1)},{
                    if(next.at(3)==1, {next.put(3,0)});
                });

            });

        });

        amp=0.8;		//NOTE- default amp is slightly quieter to avoid overload on accenting
        duty=1.0;

        if(next.at(1)<5,
            {
                if(next.size==4,{amp=next.at(3)});
                if(next.size>2,{duty=next.at(2)});
                offset= (next.at(1))%2;
            },
            {
                if(next.size==6,{amp=next.at(5)});
                if(next.size>4,{duty=next.at(4)});
                offset= next.at(3);
        });

        if((didstop==0) && (stopchance.value.coin), {didstop=1; amp=0.0;});

        offset= if(offset<0.5,{kickoffset.value},{snareoffset.value});

        durs=[bl];	//default for single play

        if(next.at(1)>1.5,{

            if(next.at(1)==5,{
                //user defined block
                durs= (Array.fill(next.at(2),{bl}))/(next.at(2));
            },
            {
                //roll play
                durs= bl*(blockdiv.value);
            });

        });

        block=Array.fill(durs.size,{arg i; [durs.at(i),durs.at(i)*duty, offset, amp]});

        //accent first of any group
        block.at(0).put(3,block.at(0).at(3)*1.1);

        ^[bl, block]
    }


    chooseblock
    {

        //new phrase to calculate?
        if(phrasepos>=(currphraselength-0.001),
            {
                this.newPhraseAccounting;

                fillflag=filltest.value;

                didstop=0;

                whichriff=chooseriff.value(whichriff, database.size);

                source= database.at(whichriff%(database.size));
                siz=source.size;

                //Post <<[siz,source,whichriff,fillflag]<<nl;

                //will do for now, but is trouble if have very nested arrays!
                currriff= Array.fill(siz,{arg i; source.at(i).copy});

                //involute randomly a number of times
                shuffles.value.do({arg i;
                    currriff=currriff.swap(siz.rand,siz.rand);
                });

                seq= Pseq(currriff,inf).asStream;

                done=0.0;

                cutsequence= LinkedList.new;

                while({done<(currphraselength-0.001)},{

                    left= currphraselength-done;

                    next= seq.next;

                    next= this.calcblock(next.copy);

                    blocklen=next.at(0);

                    if(blocklen>left, {blocklen=left; next=[left, [left, left, 0, 0.0]]});

                    cutsequence= cutsequence++[next];
                    done=blocklen+done;

                });


        });

        beatsleft= currphraselength- phrasepos;

        //just overlap in scheduling if a problem
        cuts=cutsequence.at(block).at(1);

        //always new slice/roll to calculate
        blocklength=cutsequence.at(block).at(0);

        //safety to force conformity to phrases
        if(blocklength>beatsleft,{blocklength=beatsleft;});

        //offsets are part of this routine so already done everything

        //is roll if block has more than 2 elemnts
        roll = if(cuts.size>1, 1, 0);

        this.updateblock;
        
        this.endBlockAccounting;
    }


}