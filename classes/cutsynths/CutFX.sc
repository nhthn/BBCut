//This file is part of The BBCut Library. Copyright (C) 2016 Nathan Ho distributed under the terms of the GNU General Public License

CutFXSynth : CutSynth {

    *new {
        ^super.new.initCutFX();
    }

    initCutFX {
    }

    renderBlock { |block, clock|
        var msg;
        block.fx.do { |fx|
            msg = [
                \s_new,
                CutFX.directory[fx.name] ++ cutgroup.numChannels.asSymbol,
                -1, // Node ID
                1, // Add to tail
                cutgroup.fxgroup.nodeID, // Group to add to
                // Synth args
                \inbus, cutgroup.index,
                \outbus, cutgroup.index,
                \dur, block.length / clock.tempo
            ] ++ (fx.args ?? { [] });
            block.timedmsgs.add([0.0, 0.0, msg]);
        };
    }

}

CutFX : Stream {
    classvar <directory;

    *initClass {
        directory = Dictionary[];
    }

    *register { |name, synthDefPrefix|
        CutFX.directory[name] = synthDefPrefix;
    }

    -> { |other|
        ^other <> this;
    }
}