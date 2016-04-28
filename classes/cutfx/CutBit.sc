// This file is part of The BBCut Library.
// Copyright (C) 2016 Nathan Ho distributed under the terms of the GNU General Public License

CutBit : Stream {
    var <>choicefunc, <>ratefunc, <>bitfunc;

    *initClass {
        StartUp.add({
            (1..2).do({ |n|
                SynthDef("BBCutBit" ++ n.asSymbol, {
                    |inbus = 0, outbus = 0, dur, samplerate = 8000, bits = 8|
                    var snd, stopdur, t;
                    snd = In.ar(inbus, n);
                    snd = Decimator.ar(snd, samplerate, bits);
                    snd = snd * EnvGen.kr(Env([1, 1], [dur]), doneAction: 2);
                    ReplaceOut.ar(outbus, snd);
                }).add;
            });

            CutFX.register(\bit, "BBCutBit");
        });
    }

    *new { |choicefunc, ratefunc, bitfunc|
        ^super.new.initCutBit(choicefunc, ratefunc, bitfunc);
    }

    initCutBit { |argChoicefunc, argRatefunc, argBitfunc|
        choicefunc = argChoicefunc ? { 0.1.coin };
        ratefunc = argRatefunc ? { [4000, 8000, 16000].choose };
        bitfunc = argBitfunc ? { rrand(3, 16) };
    }

    next { |block|
        var msg;
        choicefunc.value(block).if {
            block.fx.add((
                name: \bit,
                args: [\samplerate, ratefunc.value, \bits, bitfunc.value]
            ));
        };
        ^block;
    }

}