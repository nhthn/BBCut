//This file is part of The BBCut Library. Copyright (C) 2001  Nick M.Collins distributed under the terms of the GNU General Public License full notice in file BBCutLibrary.help

//SQPusher2 N.M.Collins 2/3/06

//Thanks to Tom Jenkinson for sort of giving permission

SQPusher2 : BBCutProc {
    classvar <seqdata;
    var barfunc,repetitionfunc,scramble, quant;
    var beatsleft, cutsequence;
    var temp,index, done, temp2;
    var iois, addzero;

    *new {arg barfunc,repetitionfunc,scramble=0.0,quant=0.0,bpsd=0.5;
        var phraselength= 4.0;

        ^super.new(bpsd,phraselength).initSQPusher2(barfunc,repetitionfunc,scramble,quant);
    }

    initSQPusher2{arg bf, rf, scr=0.0, q=0.0;

        barfunc=bf ? {arg data; data.choose;};
        repetitionfunc=rf ? {arg bar; bar.curdle(0.5)}; //see separate, clump, curdle under SequenceableCollection
        scramble=scr ? 0.0;

        quant=q ? 0.0;

    }


    chooseblock{

        //new phrase to calculate?
        if(phrasepos>=(currphraselength-0.001),{
            this.newPhraseAccounting;

            addzero=false;
            temp=barfunc.value(seqdata).round(quant.value);

            //temp.postln;

            iois= temp.rotate(-1)-temp;
            iois[temp.size-1]= 4.0- (temp[temp.size-1]);


            //if any zero iois, remove them (will remove any stray events rounded to end of bars)
            temp2=List[];

            iois.do({arg val; if(val>0.00001,{temp2.add(val)}); });

            iois=temp2.asArray;

            if(iois.size<1,{"this shouldn't happen".postln;});

            //iois.postln;

            if(temp[0]>0.025) {addzero=true;};

            iois=iois.collect({arg val;  [val,val, nil,1.0]});

            cutsequence=repetitionfunc.value(iois);

            //cutsequence.postln;

            //now have repetition groups, may need to add a zero rest group,
            if(addzero) {cutsequence= [[[temp[0],temp[0],nil,0.0]]]++cutsequence;};

            //cutsequence.postln;


            //could do some later alterations and refinements here, but leave for now

            if(scramble.value.coin,{cutsequence= cutsequence.scramble;});

            //cutsequence.postln;


        });

        beatsleft= currphraselength- phrasepos;

        //just overlap in scheduling if a problem
        cuts=cutsequence.at(block);

        if((cuts.isNil)) {cuts= [[beatsleft,beatsleft,nil,1.0]];};
        if((cuts.isEmpty)) {cuts= [[beatsleft,beatsleft,nil,1.0]];};

        //cuts.postln;

        //always new slice/roll to calculate
        blocklength=(cuts.collect({arg val; val[0]})).sum;

        //safety to force conformity to phrases
        if(blocklength>beatsleft,{

            temp=0.0; index=0;

            while({temp<(beatsleft-0.001)},{

                temp= temp+(cuts[index][0]); index=index+1;

            });

            index=index-1;

            cuts= cuts.copyRange(0,index);

            if((cuts.size)<1,{
                cuts= [[beatsleft,beatsleft,nil,1.0]];
            },
            {

                temp2= (cuts[index][0])- (temp-beatsleft);
                cuts.put(index, [temp2,temp2,nil,1.0]);
            });

            blocklength= beatsleft;
        });

        this.updateblock;

        this.endBlockAccounting;
    }



    //data extracted automatically from all bars of Come On My Selector
    //note that I didn't differentiate event timbre in the extraction, this is just raw 4/4 rhythm data
    *initClass {

        //source quantised to 0.01, shoudl be no perceptual difference

        seqdata= #[ [ 0.47, 1.02, 1.5, 1.75, 1.94, 2.27, 2.49, 3, 3.52, 3.77 ], [ 0, 0.47, 0.99, 1.5, 1.76, 1.94, 2.24, 2.49, 3.01, 3.49, 3.78, 3.96 ], [ 0.48, 0.99, 1.51, 1.76, 1.98, 2.24, 2.46, 2.79, 3.01, 3.53, 3.78 ], [ 0, 0.26, 0.48, 0.78, 1, 1.51, 1.77, 1.99, 2.24, 2.46, 2.8, 3.02, 3.53, 3.79 ], [ 0.01, 0.26, 0.45, 1, 1.51, 1.73, 1.99, 2.25, 2.47, 2.73, 3.02, 3.53, 3.98 ], [ 0.27, 0.45, 1, 1.48, 1.74, 1.92, 2.25, 2.47, 2.73, 2.99, 3.54, 3.76, 3.98 ], [ 0.46, 1.01, 1.49, 1.74, 2, 2.26, 2.48, 2.99, 3.51, 3.76, 3.98 ], [ 0.46, 0.98, 1.49, 1.75, 1.93, 2.22, 2.48, 3, 3.47, 3.99 ], [ 0.8, 1.02, 1.24, 1.46, 2.49, 2.63, 3, 3.55, 3.99 ], [ 0.58, 0.98, 1.46, 1.61, 2.27, 2.49, 2.93, 3.33, 3.7 ], [ 0, 0.29, 0.73, 1.06, 1.25, 1.5, 1.72, 2.24, 2.49, 2.97, 3.27, 3.41, 3.74 ], [ 0, 0.29, 0.51, 0.74, 0.99, 1.25, 1.51, 1.76, 1.91, 2.13, 2.46, 2.94, 3.27, 3.49, 3.75 ], [ 0, 1, 1.47, 2.47, 2.98, 3.49, 3.64 ], [ 0.49, 1.15, 1.29, 1.51, 1.77, 1.99, 2.47, 2.98, 3.28, 3.5, 3.76, 3.98 ], [ 0.23, 0.56, 0.97, 1.52, 1.78, 1.92, 2.4, 2.58, 3.03, 3.47, 3.8, 3.94 ], [ 0.46, 0.72, 0.94, 1.49, 1.67, 2, 2.48, 2.99, 3.43, 3.76, 3.98 ], [ 0.65, 0.9, 1.45, 2.12, 2.48, 2.74, 2.96, 3.73, 3.99 ], [ 0.58, 0.98, 1.38, 1.61, 1.94, 2.19, 2.45, 2.74, 3, 3.22, 3.48, 3.74, 3.99 ], [ 0.91, 1.17, 1.5, 1.98, 2.34, 2.49, 2.71, 2.93, 3.3, 3.48, 3.96 ], [ 0.51, 1.1, 1.5, 1.76, 1.98, 2.46, 2.97, 3.19, 3.45, 3.71, 3.96 ], [ 0.48, 0.63, 1.1, 1.32, 1.47, 2.46, 2.98, 3.49, 3.67, 3.97 ], [ 0.23, 0.48, 0.92, 1.51, 1.99, 2.47, 2.72, 2.98, 3.46, 3.75, 3.97 ], [ 0.49, 0.96, 1.48, 1.74, 1.99, 2.47, 3.46, 3.76, 3.94 ], [ 0.12, 0.42, 0.6, 0.79, 1.19, 1.45, 1.96, 2.44, 2.7, 2.95, 3.25, 3.43, 3.72, 3.98 ], [ 0.24, 0.5, 0.75, 0.97, 2.48, 3.51, 3.77, 3.99 ], [ 1.6, 1.97, 2.23, 2.45, 2.67, 2.92, 3.44, 3.7, 3.95 ], [ 0.25, 0.43, 0.72, 0.98, 1.42, 1.68, 1.94, 2.52, 2.71, 3, 3.22, 3.44, 3.77 ], [ 0.03, 0.47, 0.69, 0.84, 1.06, 1.21, 1.43, 1.98, 2.16, 2.42, 3.19, 3.48, 3.7, 3.96 ], [ 0.22, 0.48, 0.73, 0.88, 1.47, 2.46, 2.94, 3.45, 3.64 ], [ 0, 0.44, 0.96, 1.47, 1.69, 1.95, 2.17, 2.43, 2.87, 3.2, 3.46, 3.71, 3.97 ], [ 0.48, 0.7, 0.93, 1.11, 1.7, 1.95, 2.43, 3.39, 3.75, 3.9 ], [ 0.19, 0.45, 0.64, 0.93, 1.44, 1.96, 2.44, 2.95, 3.24, 3.46, 3.94 ], [ 0.2, 0.46, 0.68, 0.93, 1.45, 2, 2.15, 2.44, 2.7, 2.92, 3.17, 3.43, 3.65, 3.95 ], [ 0.72, 0.94, 1.45, 1.93, 2.92, 3.91 ], [ 0.28, 0.65, 0.91, 1.16, 1.42, 1.6, 1.93, 2.19, 2.67, 3.22, 3.44, 3.7 ], [ 0.03, 0.28, 0.47, 0.69, 0.95, 1.2, 1.39, 1.86, 2.08, 2.42, 2.89, 3.19, 3.48, 3.66, 3.92 ], [ 0.47, 0.69, 0.95, 1.17, 1.43, 1.91, 2.42, 2.93, 3.45, 3.96 ], [ 0.22, 0.44, 0.95, 1.43, 1.73, 1.95, 2.17, 2.42, 2.72, 2.94, 3.2, 3.45, 3.71, 3.93 ], [ 0.19, 0.66, 0.92, 1.47, 1.73, 1.88, 2.24, 2.94, 3.42, 3.71, 3.93 ], [ 0.67, 0.93, 1.11, 1.44, 1.88, 2.14, 2.43, 2.95, 3.2, 3.42, 3.72, 3.94 ], [ 0.2, 0.42, 0.93, 1.44, 1.7, 1.92, 2.18, 2.4, 2.91, 3.43, 3.72, 3.94 ], [ 0.42, 0.93, 1.41, 1.67, 2.15, 2.37, 2.7, 2.92, 3.18, 3.95 ], [ 0.46, 0.94, 1.45, 1.93, 2.44, 2.92, 3.44, 3.91 ], [ 0.43, 0.91, 1.42, 1.93, 2.41, 3.92 ], [ 0.18, 0.43, 0.69, 0.95, 1.2, 1.42, 1.72, 1.94, 2.2, 2.45, 2.93, 3.19, 3.45, 3.78, 3.92 ], [ 0.14, 0.4, 0.66, 0.91, 1.17, 1.43, 1.69, 1.91, 2.42, 2.71, 2.9, 3.16, 3.38, 3.63, 3.93 ], [ 0.44, 0.66, 1.4, 1.91, 2.65, 2.9, 3.42, 3.64, 3.89 ], [ 0.41, 0.63, 0.92, 1.18, 1.4, 1.66, 1.91, 2.17, 2.43, 2.69, 2.91, 3.16, 3.42, 3.64, 3.94 ], [ 0.45, 0.93, 1.44, 2.4, 2.65, 2.95, 3.43, 3.65, 3.94 ], [ 0.42, 0.64, 0.93, 1.15, 1.37, 1.89, 2.18, 2.36, 2.92, 3.17, 3.43, 3.69 ], [ 0.2, 0.42, 0.68, 0.94, 1.41, 1.93, 2.44, 2.92, 3.43, 3.69, 3.91 ], [ 0.43, 0.9, 1.38, 1.89, 2.15, 2.41, 3.4, 3.88 ], [ 0.39, 1.02, 1.16, 1.42, 1.97, 2.41, 2.89, 3.41, 3.92 ], [ 0.18, 0.4, 0.91, 1.43, 1.9, 2.16, 2.42, 2.67, 2.9, 3.37, 3.63, 3.78, 3.92 ], [ 0.22, 0.58, 0.88, 1.25, 1.58, 1.83, 2.24, 2.57, 2.86, 3.23, 3.6, 3.89 ], [ 0.22, 0.55, 0.88, 1.25, 1.58, 1.91, 2.54, 3.23, 3.9 ], [ 0.23, 0.56, 0.81, 1.22, 1.92, 2.25, 2.54, 2.91, 3.57, 3.9 ], [ 0.56, 0.89, 1.22, 1.55, 1.88, 2.54, 2.91, 3.13, 3.39, 3.54, 3.9 ], [ 0.38, 0.93, 1.41, 1.59, 1.89, 2.88, 3.36, 3.91 ], [ 1.08, 1.23, 1.45, 1.67, 1.89, 2.19, 2.44, 2.92, 3.4, 3.62 ], [ 0.1, 0.35, 0.65, 0.87, 1.38, 2.12, 2.85, 3.14, 3.62, 3.88 ], [ 0.39, 0.69, 0.87, 1.17, 1.39, 1.61, 2.12, 2.38, 2.56, 2.89, 3.15, 3.33, 3.63, 3.88 ], [ 0.4, 1.43, 1.9, 2.38, 3.23, 3.37, 3.92 ], [ 0.11, 0.48, 0.66, 0.92, 1.39, 1.91, 2.39, 2.64, 2.86, 3.16, 3.41, 3.64, 3.89 ], [ 0.37, 0.63, 0.88, 1.1, 1.4, 2.13, 2.35, 2.87, 3.2, 3.6, 3.82 ], [ 0.37, 0.67, 0.89, 1.62, 1.84, 2.14, 2.36, 2.87, 3.39, 3.64, 3.86 ], [ 0.89, 1.15, 1.63, 1.85, 2.4, 2.84, 3.13, 3.39, 3.79 ], [ 0.13, 0.38, 0.71, 0.9, 1.37, 3.62, 3.91 ], [ 0.64, 1.38, 2.11, 2.33, 2.52, 2.88, 3.44, 3.66 ], [ 0.39, 0.87, 1.13, 1.6, 1.86, 2.19, 2.56, 2.93, 3.18, 3.88 ], [ 0.03, 0.17, 0.36, 0.62, 0.91, 1.09, 1.35, 1.5, 1.86, 2.16, 2.38, 2.64, 3.04, 3.37, 3.85 ], [ 0.18, 0.58, 0.84, 1.21, 1.61, 1.87, 2.13, 2.38, 2.86, 3.19, 3.52, 3.85 ], [ 0.26, 0.62, 0.88, 1.14, 1.32, 1.69, 1.87, 2.24, 2.86, 3.31, 3.6 ], [ 0.11, 0.37, 0.81, 1.11, 1.55, 1.88, 2.17, 2.39, 3.24, 3.46, 3.86 ], [ 0.6, 0.93, 1.88, 2.84, 3.2, 3.42, 3.64, 3.86 ], [ 0.34, 0.64, 0.89, 1.33, 1.48, 2.03, 3.83 ], [ 0.31, 0.64, 0.9, 1.38, 1.56, 1.89, 2.11, 2.37, 2.84, 3.21, 3.62, 3.8 ], [ 0.35, 0.61, 1.12, 1.31, 1.56, 2.55, 2.81, 3.14, 3.29, 3.51, 3.84, 3.99 ], [ 0.35, 0.87, 1.38, 1.64, 1.9, 2.12, 2.34, 2.63, 3.33, 3.84 ], [ 0.14, 0.36, 0.84, 1.24, 1.61, 1.87, 2.12, 2.56, 3.85 ], [ 0.14, 0.58, 0.8, 1.02, 1.36, 1.58, 2.13, 2.86, 3.34, 3.6, 3.85 ], [ 0.37, 0.85, 1.07, 1.87, 2.61, 2.87, 3.31, 3.86 ], [ 0.81, 1.36, 1.62, 1.88, 2.1, 2.36, 2.87, 3.09, 3.38 ], [ 0.3, 0.6, 0.85, 1.33, 1.85, 2.1, 2.36, 2.84, 3.35, 3.83 ], [ 0.82, 1.37, 1.63, 2.07, 2.33, 2.69, 2.88, 3.21, 3.39, 3.83 ], [ 0.35, 0.6, 0.86, 1.38, 2.07, 2.33, 2.63, 2.85, 3.14, 3.32, 3.58, 3.84 ], [ 0.09, 0.5, 0.83, 1.6, 1.86, 2.3, 2.59, 3.11, 3.29, 3.84 ], [ 0.61, 0.87, 1.35, 2.08, 2.34, 2.85, 3.33, 3.59, 3.85 ], [ 0.1, 0.32, 1.17, 1.32, 1.83, 2.05, 2.86, 3.34, 3.56 ], [ 0.11, 1.1, 1.83, 2.09, 2.46, 2.72, 2.97, 3.3, 3.85 ], [ 0.85, 1.1, 1.36, 1.51, 1.65, 1.84, 2.35, 2.83, 3.12, 3.34, 3.6, 3.86 ], [ 0.34, 0.59, 0.85, 1.59, 1.84, 2.1, 2.28, 2.94, 3.31, 3.86 ], [ 0.63, 0.89, 1.07, 1.55, 2.36, 2.84, 3.1, 3.57, 3.83 ], [ 0.12, 0.34, 0.56, 0.82, 1.34, 2.33, 3.36, 3.5, 3.83 ], [ 0.05, 0.35, 0.79, 1.05, 1.34, 1.78, 2.3, 2.81, 3.07, 3.32, 3.51 ], [ 0.28, 0.79, 1.01, 1.34, 1.82, 2.12, 2.37, 2.92, 3.44, 3.99 ], [ 1.31, 1.83, 2.3, 2.74, 3.04, 3.55, 3.81 ], [ 0.32, 0.77, 1.06, 1.28, 1.79, 2.46, 2.82, 3.08, 3.23, 3.56, 3.78 ], [ 0.81, 1.1, 1.32, 1.8, 2.83, 3.05, 3.3, 3.56, 3.82 ], [ 0.08, 0.33, 0.81, 1.07, 1.55, 1.8, 2.13, 2.32, 2.87, 3.31, 3.53, 3.82 ], [ 0.08, 0.59, 0.85, 1.29, 1.59, 1.81, 2.32, 2.8, 3.06, 3.31, 3.53, 3.83 ], [ 0.16, 0.3, 0.75, 1.3, 1.81, 2.33, 3.02, 3.28, 3.83 ], [ 0.57, 0.79, 1.04, 1.26, 1.78, 2.29, 2.81, 3.03, 3.28, 3.54, 3.84 ], [ 0.24, 0.79, 1.05, 1.3, 1.75, 2.04, 2.33, 2.88, 3.03, 3.33, 3.55 ], [ 0.83, 1.6, 1.75, 2.3, 2.82, 3.07, 3.29, 3.55 ], [ 0.03, 0.54, 0.91, 1.06, 1.31, 1.46, 1.83, 2.05, 2.27, 2.49, 2.82, 3.15, 3.41, 3.81 ], [ 0.51, 0.77, 1.28, 1.79, 1.94, 2.27, 2.53, 2.79, 3.04, 3.3, 3.56, 3.78 ], [ 0.04, 0.33, 0.77, 1.32, 1.76, 2.31, 3.31, 3.82 ], [ 1.11, 1.25, 1.8, 2.28, 2.8, 3.02, 3.53 ], [ 0.26, 0.82, 1.29, 1.81, 2.8, 3.06, 3.31, 3.79 ], [ 0.09, 0.34, 0.53, 0.75, 1.3, 1.48, 1.77, 2, 2.29, 2.77, 3.02, 3.28, 3.54, 3.8 ], [ 0.05, 0.24, 0.93, 1.52, 1.82, 2.04, 2.29, 2.84, 3.32, 3.8 ], [ 0.83, 1.01, 1.16, 1.42, 1.78, 2.26, 2.78, 3, 3.29, 3.51, 3.77 ], [ 0.8, 1.27, 2.23, 2.78, 3.29, 3.73 ], [ 0.1, 0.36, 0.51, 0.76, 1.28, 1.76, 2.27, 2.75, 3, 3.26, 3.48, 3.78 ], [ 0.25, 0.4, 0.73, 1.06, 1.28, 1.43, 1.69, 1.87, 2.31, 3.27, 3.78 ], [ 0.07, 0.29, 0.55, 1.29, 1.47, 1.76, 2.06, 2.28, 2.76, 3.01, 3.49, 3.75, 3.97 ], [ 0.7, 1.03, 1.25, 1.51, 1.77, 2.06, 2.28, 2.76, 2.91, 3.24, 3.57, 3.79 ], [ 0.01, 0.52, 0.78, 1.77, 2.25, 2.73, 3.02, 3.24, 3.46, 3.79 ], [ 0.27, 0.53, 0.75, 1.59, 2.29, 2.88, 3.03, 3.25, 3.43, 3.91 ], [ 0.05, 0.27, 0.79, 2.77, 2.99, 3.51, 3.76 ], [ 0.17, 0.54, 1.05, 1.2, 1.42, 1.75, 1.89, 2.78, 3.69 ], [ 0.06, 0.39, 0.61, 0.91, 1.24, 2.45, 2.6, 2.96, 3.22, 3.48, 3.77 ], [ 0.18, 0.8, 0.98, 1.21, 1.79, 2.27, 2.78, 3.04, 3.26, 3.52, 3.78 ], [ 0.25, 0.77, 1.91, 2.31, 2.79 ], [ 0.3, 0.44, 0.77, 1.03, 1.47, 1.8, 1.98, 2.28, 2.79 ], [ 0.01, 1.25, 1.51, 1.77, 2.28, 2.47, 2.76, 3.24, 3.39, 3.79 ], [ 0.3, 0.78, 1, 1.26, 1.77, 2.25, 2.76, 3.02, 3.24, 3.5, 3.76 ], [ 0.27, 0.75, 1.48, 1.78, 2.07, 2.25, 2.99, 3.25, 3.76 ], [ 0.16, 0.53, 0.79, 1.12, 1.27, 1.49, 1.78, 2.26, 2.74, 2.99, 3.25, 3.47, 3.76 ], [ 0.72, 1.23, 1.53, 1.75, 2.26, 2.78, 3.22, 3.77 ], [ 0.5, 0.76, 0.98, 1.24, 1.75, 2.23, 2.74, 3, 3.22, 3.48, 3.77 ], [ 0.73, 0.95, 1.21, 1.5, 1.72, 1.98, 2.27, 2.75, 3.04, 3.26, 3.52, 3.74 ], [ 0.77, 1.28, 1.76, 2.24, 2.72, 2.97, 3.23, 3.49, 3.75 ], [ 0.44, 0.77, 0.99, 1.21, 1.43, 2.06, 2.28, 2.72, 2.94, 3.53, 3.75 ], [ 0.04, 0.74, 1.26, 1.51, 1.77, 2.54, 2.76, 3.02, 3.28, 3.5 ], [ 0.01, 0.27, 0.56, 0.75, 1.48, 2.03, 2.25, 2.8, 3.02, 3.72 ], [ 0.27, 0.75, 1.26, 2.22, 2.59, 2.73, 2.99, 3.25, 3.47, 3.73, 3.91 ], [ 0.24, 0.46, 0.75, 0.97, 1.38, 2.77, 3.8 ], [ 0.17, 0.61, 0.76, 0.9, 1.49, 1.64, 1.97, 2.23, 2.48, 2.7, 3.04, 3.22, 3.51, 3.7 ], [ 0.17, 0.73, 1.02, 1.5, 1.64, 1.97, 2.19, 2.49, 2.75, 3.04, 3.52, 3.74, 3.99 ], [ 0.22, 0.73, 1.02, 1.24, 1.46, 1.65, 1.98, 2.2, 2.46, 2.75, 3.04, 3.3, 3.52, 3.67 ], [ 0, 0.18, 0.73, 1.03, 1.25, 1.47, 1.65, 1.98, 2.2, 2.46, 2.72, 3.27, 3.49, 3.71, 3.86 ], [ 0, 0.19, 0.74, 1.25, 1.47, 1.66, 1.99, 2.21, 2.46, 2.72, 3.27, 3.49, 3.71 ], [ 0.19, 0.71, 1.22, 1.48, 1.66, 1.95, 2.21, 2.73, 3.2, 3.5, 3.68 ], [ 0.2, 0.71, 1.22, 1.48, 1.7, 1.96, 2.18, 2.51, 2.73, 3.24, 3.5, 3.72, 3.98 ], [ 0.2, 0.71, 1.23, 1.49, 1.71, 1.96, 2.18, 2.51, 2.73, 3.21, 3.73 ], [ 0.02, 0.2, 0.75, 1.05, 1.45, 1.71, 2.19, 2.48, 2.74, 3.18, 3.73 ], [ 0.72, 1.24, 1.75, 2.19, 2.49, 2.74, 3.51, 3.73, 3.99 ], [ 0.21, 0.95, 1.42, 1.68, 2.2, 2.49, 3.19, 3.7 ], [ 0, 0.4, 0.73, 0.99, 1.17, 1.5, 1.65, 1.94, 2.16, 2.71, 3.23, 3.49, 3.74 ], [ 0, 0.22, 0.73, 0.96, 1.43, 1.69, 1.95, 2.2, 2.72, 3.2, 3.71 ], [ 0.74, 0.96, 1.66, 1.95, 2.17, 2.72, 3.24, 3.49, 3.75, 3.97 ], [ 0.41, 0.74, 1.04, 1.44, 1.7, 1.99, 2.21, 2.73, 3.17, 3.72, 3.98 ], [ 0.38, 0.71, 1.48, 1.74, 2.18, 2.73, 3.21, 3.69 ], [ 0.24, 0.71, 1.96, 2.22, 2.7, 3.51, 3.69 ], [ 0.24, 0.72, 0.9, 2.19, 2.7, 3.22, 3.47, 3.69 ], [ 0.76, 1.35, 1.64, 2.6, 3.44, 3.81 ], [ 0.07, 0.58, 1.2, 1.76, 2.05, 2.2, 2.71, 3.19, 3.48, 3.78 ], [ 1.14, 1.32, 1.69, 2.61, 3.19, 3.52 ] ];

    }

}