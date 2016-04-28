// This file is part of The BBCut Library.
// Copyright (C) 2016 Nathan Ho distributed under the terms of the GNU General Public License

CutDist : Stream {
    var <>choicefunc, <>drivefunc;

    *initClass {
        StartUp.add({
            (1..2).do({ |n|
                SynthDef("BBCutDist" ++ n.asSymbol, {
                    |inbus = 0, outbus = 0, dur, drive = 1.0|
                    var snd, a, e;
                    snd = In.ar(inbus, n);
                    snd = snd * drive;
                    snd = snd * 0.686306;
                    a = 1 + exp(sqrt(abs(snd)) * -0.75);
                    e = exp(snd);
                    snd = (e - exp(snd.neg * a)) / (e + e.reciprocal);
                    snd = snd * EnvGen.kr(Env([1, 1], [dur]), doneAction: 2);
                    ReplaceOut.ar(outbus, snd);
                }).add;
            });

            CutFX.register(\dist, "BBCutDist");
        });
    }

    *new { |choicefunc, drivefunc|
        ^super.new.initCutDist(choicefunc, drivefunc);
    }

    initCutDist { |argChoicefunc, argDrivefunc|
        choicefunc = argChoicefunc ? { 0.1.coin };
        drivefunc = argDrivefunc ? { 1.0 };
    }

    next { |block|
        var msg;
        choicefunc.value(block).if {
            block.fx.add((
                name: \dist,
                args: [\drive, drivefunc.value]
            ));
        };
        ^block;
    }

}