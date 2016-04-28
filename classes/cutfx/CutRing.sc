// This file is part of The BBCut Library.
// Copyright (C) 2016 Nathan Ho distributed under the terms of the GNU General Public License

CutRing : Stream {
    var <>choicefunc, <>freqfunc, <>mixfunc;

    *initClass {
        StartUp.add({
            (1..2).do({ |n|
                SynthDef("BBCutRing" ++ n.asSymbol, {
                    |inbus = 0, outbus = 0, dur, freq = 440, mix = 0.5|
                    var snd;
                    snd = In.ar(inbus, n);
                    snd = (snd * SinOsc.ar(freq) * mix) + (snd * (1 - mix));
                    snd = snd * EnvGen.kr(Env([1, 1], [dur]), doneAction: 2);
                    ReplaceOut.ar(outbus, snd);
                }).add;
            });

            CutFX.register(\ring, "BBCutRing");
        });
    }

    *new { |choicefunc, freqfunc, mixfunc|
        ^super.new.initCutRing(choicefunc, freqfunc, mixfunc);
    }

    initCutRing { |argChoicefunc, argFreqfunc, argMixfunc|
        choicefunc = argChoicefunc ? { 0.1.coin };
        freqfunc = argFreqfunc ? { exprand(400, 3000) };
        mixfunc = argMixfunc ? { 1.0 };
    }

    next { |block|
        var msg;
        choicefunc.value(block).if {
            block.fx.add((
                name: \ring,
                args: [\freq, freqfunc.value, \mix, mixfunc.value]
            ));
        };
        ^block;
    }

}