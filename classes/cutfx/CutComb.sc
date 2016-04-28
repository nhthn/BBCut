// This file is part of The BBCut Library.
// Copyright (C) 2016 Nathan Ho distributed under the terms of the GNU General Public License

CutComb : CutFX {
    var <>choicefunc, <>freqfunc, <>decayfunc;

    *initClass {
        StartUp.add({
            (1..2).do({ |n|
                SynthDef("BBCutComb" ++ n.asSymbol, {
                    |inbus = 0, outbus = 0, dur, freq = 440, decay = 100|
                    var snd, stopdur, t;
                    snd = In.ar(inbus, n);
                    snd = CombC.ar(snd, freq.reciprocal, freq.reciprocal, decay);
                    snd = snd * EnvGen.kr(Env([1, 1], [dur]), doneAction: 2);
                    ReplaceOut.ar(outbus, snd);
                }).add;
            });

            CutFX.register(\comb, "BBCutComb");
        });
    }

    *new { |choicefunc, freqfunc, decayfunc|
        ^super.new.initCutComb(choicefunc, freqfunc, decayfunc);
    }

    initCutComb { |argChoicefunc, argFreqfunc, argDecayfunc|
        choicefunc = argChoicefunc ? { 0.1.coin };
        freqfunc = argFreqfunc ? { 100 * rrand(1, 10) };
        decayfunc = argDecayfunc ? { 0.05 };
    }

    next { |block|
        var msg;
        choicefunc.value(block).if {
            block.fx.add((
                name: \comb,
                args: [\freq, freqfunc.value, \decay, decayfunc.value]
            ));
        };
        ^block;
    }

}