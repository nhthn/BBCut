// This file is part of The BBCut Library.
// Copyright (C) 2016 Nathan Ho distributed under the terms of the GNU General Public License

CutVerb : Stream {
    var <>choicefunc, <>mixfunc;

    *initClass {
        StartUp.add({
            (1..2).do({ |n|
                SynthDef("BBCutVerb" ++ n.asSymbol, {
                    |inbus = 0, outbus = 0, dur, mix = 0.5|
                    var snd, wet;
                    snd = In.ar(inbus, n);
                    wet = GVerb.ar(if(n == 1, { snd }, { Mix(snd) }), 80);
                    wet = if(n == 1, { Mix(wet) }, { wet });
                    wet = wet * 0.4;
                    snd = (wet * mix) + (snd * (1 - mix));
                    snd = snd * EnvGen.kr(Env([1, 1], [dur]), doneAction: 2);
                    ReplaceOut.ar(outbus, snd);
                }).add;
            });

            CutFX.register(\verb, "BBCutVerb");
        });
    }

    *new { |choicefunc, mixfunc|
        ^super.new.initCutVerb(choicefunc, mixfunc);
    }

    initCutVerb { |argChoicefunc, argMixfunc|
        choicefunc = argChoicefunc ? { 0.1.coin };
        mixfunc = argMixfunc ? { 1.0 };
    }

    next { |block|
        var msg;
        choicefunc.value(block).if {
            block.fx.add((
                name: \verb,
                args: [\mix, mixfunc.value]
            ));
        };
        ^block;
    }

}