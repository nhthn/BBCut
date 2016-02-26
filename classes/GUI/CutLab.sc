// This file is part of The BBCut Library. Copyright (C) 2016  Nathan Ho distributed under the terms of the GNU General Public License

CutLab {
    // Models
    var buf;
    var <bbcut;
    var cutsynth;
    var clock;

    // Index of cut procedures
    classvar <>cutprocs;

    // GUI elements
    var window;
    var logoText;
    var playButton;
    var procMenu;
    var tempoKnob;
    var jumpKnob;
    var sfview;

    // SoundFile
    var sf;
    var sfviewRout;
    var sfviewRoutFreq = 30.0;
    var cursorPos;
    var grainStartPos;
    var grainDur;

    *initClass {
        cutprocs = [
            'Jungle' -> "BBCutProc11()",
            'Warp' -> "WarpCutProc1()",
            'Thrash' -> "ThrashCutProc1()",
            'Squarepusher 1' -> "SQPusher1()",
            'Squarepusher 2' -> "SQPusher2()",
            'Cage' -> "CageCut()",
            'Timeline' -> "TimelineCut()"
        ];
    }

    *new { |buf|
        ^super.new.initCutLab(buf);
    }

    initCutLab { |argBuf|
        buf = argBuf;

        cutsynth = CutBuf3(buf, 0.3);
        cutsynth.grainfunc_(e { |whichcut, block, clock, startPos, dur|
            cursorPos = startPos;
            grainStartPos = startPos;
            grainDur = dur * buf.sampleRate;
        });

        clock = ExternalClock(TempoClock(2.4)).play;
        bbcut = BBCut2(CutGroup(cutsynth, numChannels: buf.numChannels), BBCutProc11());

        cursorPos = 0.0;
        grainStartPos = 0.0;
        grainDur = 0.0;

        window = Window("CutLab", Rect(100, 100, 600, 300));
        logoText = StaticText(window, Rect(10, 10, 580, 30))
            .string_("✂ CutLab ✂")
            .align_(\center)
            .font_(Font(nil, 30));

        sf = SoundFile();
        sf.openRead(~buf.path);
        sfview = SoundFileView(window, Rect(10, 50, 580, 40))
            .gridOn_(false)
            .setSelectionColor(0, Color.gray(0.2))
            .timeCursorColor_(Color.white)
            .soundfile_(sf)
            .read.refresh;

        sfviewRoutFreq = 30;
        sfviewRout = Routine({
            loop {
                {
                    sfview.setSelectionStart(0, grainStartPos);
                    sfview.setSelectionSize(0, grainDur);
                    sfview.timeCursorPosition_(cursorPos);
                    sfview.timeCursorOn_(cursorPos <= (grainStartPos + grainDur));
                }.defer;
                cursorPos = cursorPos + (~buf.sampleRate * sfviewRoutFreq.reciprocal);
                sfviewRoutFreq.reciprocal.yield;
            }
        });

        playButton = Button(window, Rect(10, 100, 100, 30))
            .states_([
                ["▶ Play", nil, Color.green],
                ["■ Stop", Color.white, Color.red]
            ])
            .action_({ |button|
                (button.value == 1).if {
                "play".postln;
                    bbcut.play(clock);
                } {
                    bbcut.stop;
                };
            });

        procMenu = EZPopUpMenu(
            window, Rect(120, 100, 300, 30),
            items: cutprocs.collect(_.key),
            initVal: 0,
            initAction: true,
            globalAction: { |menu|
                bbcut.proc = cutprocs[menu.value].value.interpret;
                this.updateCode;
            }
        );

        tempoKnob = EZKnob(
            window, Rect(10, 140, 50, 100),
            label: "Tempo",
            controlSpec: ControlSpec(40, 200, \lin, 2, default: 144),
            initAction: true,
            action: { |knob|
                clock.tempo_(knob.value / 60);
                this.updateCode;
            }
        );
        tempoKnob.labelView.align_(\center);

        jumpKnob = EZKnob(
            window, Rect(70, 140, 50, 100),
            label: "Jump",
            controlSpec: \unipolar,
            initVal: 0.5,
            initAction: true,
            action: { |knob|
                cutsynth.offset = knob.value;
                this.updateCode;
            }
        );
        jumpKnob.labelView.align_(\center);

        sfviewRout.play;

        window.front;
        window.onClose_ {
            sf.free;
            sfviewRout.stop;
            clock.stop;
            bbcut.free;
        };

    }

    updateCode {
        /*
        "BBCut2(CutBuf3(~buf, %), %).play(%)".format(
            jumpKnob.value.asStringPrec(3),
            cutprocs[procMenu.value].value,
            (tempoKnob.value / 60).asStringPrec(3);
        ).postln;*/
    }

}