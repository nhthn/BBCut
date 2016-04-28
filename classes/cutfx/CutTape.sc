// This file is part of The BBCut Library.
// Copyright (C) 2016 Nathan Ho distributed under the terms of the GNU General Public License

CutTape : CutFX {
    var <>choicefunc, <>dutyfunc;

    *initClass {
        StartUp.add({
            (1..2).do({ |n|
                SynthDef("BBCutTape" ++ n.asSymbol, {
                    |inbus = 0, outbus = 0, dur, duty = 0.7|
                    var snd, stopdur, t;
                    snd = In.ar(inbus, n);
                    stopdur = dur * duty;
                    t = EnvGen.ar(Env([0, 0, stopdur], [0, stopdur]));
                    snd = DelayC.ar(snd, 1.0, (t**2)/(2*stopdur));
                    snd = snd * EnvGen.kr(Env([1, 1, 0, 0], [stopdur, 0, dur - stopdur]), doneAction: 2);
                    ReplaceOut.ar(outbus, snd);
                }).add;
            });

            CutFX.register(\tape, "BBCutTape");
        });
    }

    *new { |choicefunc, dutyfunc|
        ^super.new.initCutTape(choicefunc, dutyfunc);
    }

    initCutTape { |argChoicefunc, argDutyfunc|
        choicefunc = argChoicefunc ? { 0.1.coin };
        dutyfunc = argDutyfunc ? 0.7;
    }

    next { |block|
        var msg;
        choicefunc.value(block).if {
            block.fx.add((
                name: \tape,
                args: [\duty, dutyfunc.value]
            ));
        };
        ^block;
    }

}