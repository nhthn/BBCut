//This file is part of The BBCut Library. Copyright (C) 2016 Nathan Ho distributed under the terms of the GNU General Public License

CutTape : CutSynth {
    var synthdef;
    var <>choicefunc, <>dutyfunc;

    *initClass {
        StartUp.add({
            (1..2).do({ |n|
                SynthDef("cuttapechan" ++ n.asSymbol, {
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
        });
    }

    *new { |choicefunc, dutyfunc|
        ^super.new.initCutTape(choicefunc, dutyfunc);
    }

    initCutTape { |argChoicefunc, argDutyfunc|
        choicefunc = argChoicefunc ? { 0.1.coin };
        dutyfunc = argDutyfunc ? 0.7;
    }

    setup {
        synthdef = \cuttapechan ++ cutgroup.numChannels.asSymbol;
    }

    renderBlock { |block, clock|
        var msg;
        choicefunc.value(block).if {
            msg = [
                \s_new,
                synthdef,
                -1, // Node ID
                1, // Add to tail
                cutgroup.fxgroup.nodeID, // Group to add to
                // Synth args
                \inbus, cutgroup.index,
                \outbus, cutgroup.index,
                \dur, block.length / clock.tempo,
                \duty, dutyfunc.value(block)
            ];
            block.timedmsgs.add([0.0, 0.0, msg]);
        };
    }

}