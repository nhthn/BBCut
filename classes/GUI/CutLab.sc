// This file is part of The BBCut Library. Copyright (C) 2016  Nathan Ho distributed under the terms of the GNU General Public License

CutLab {
    // Models
    var buf;
    var <bbcut;
    var cutsynth;
    var clock;

    // Index of cut procedures
    classvar <>cutprocs;

    /*

    +---------------------------+
    |                           |
    |          soundfile        |
    |                           |
    +---------+-----------------+
    |         |                 |
    |         |                 |
    |         |                 |
    |  synth  |      proc       |
    |         |                 |
    |         |                 |
    |         |                 |
    +---------+-----------------+

    */

    // GUI elements
    var window;
    var synthPanel;
    var procPanel;

    var fileField;
    var fileBrowseButton;
    var fileLoadButton;

    var sfview;
    var playButton;
    var procMenu;
    var segmentsMenu;
    var tempoKnob;
    var jumpKnob;

    var knobSize;

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

    *new {
        ^super.new.initCutLab;
    }

    initCutLab {
        var flow;
        var synthPanel;
        var synthPanelKnob;

        // sound file view variables
        cursorPos = 0.0;
        grainStartPos = 0.0;
        grainDur = 0.0;

        knobSize = 50@100;

        //////////////////// WINDOW ////////////////////

        window = Window("CutLab", 500@350);
        window.onClose_ {
            this.cleanUp;
        };
        window.addFlowLayout;
        window.view.palette_(QPalette.dark);

        //////////////////// FILE CONTROLS ////////////////////

        fileField = TextField(window, 150@20)
            .value_(
                File.exists(Platform.resourceDir +/+ "sounds/break.aiff").if(
                    Platform.resourceDir +/+ "sounds/break.aiff",
                    Platform.resourceDir +/+ "sounds/a11wlk01.wav"
                )
            );

        fileBrowseButton = Button(window, 80@20)
            .states_([["Browse..."]])
            .action_ {
                Dialog.openPanel { |filename|
                    fileField.value = filename;
                };
            };

        fileLoadButton = Button(window, 80@20)
            .states_([["Load"]])
            .action_({
                this.loadBuf(fileField.value, segmentsMenu.items[segmentsMenu.value]);
            })
            .focus;

        StaticText(window, 90@20).string_("Segments:");

        segmentsMenu = PopUpMenu(window, window.view.decorator.indentedRemaining.width@20)
            .items_([4, 5, 6, 7, 8, 10, 12, 16, 32, 48, 64])
            .action_({ |menu|
                buf.postln;
                buf.notNil.if {
                    var segments = menu.items[menu.value];
                    buf.beatlength_(segments).events_();
                };
            });
        segmentsMenu.value_(segmentsMenu.items.indexOf(8));

        window.view.decorator.nextLine;

        //////////////////// SOUND FILE ////////////////////

        sfview = SoundFileView(window, window.view.decorator.indentedRemaining.width@100)
            .gridOn_(false)
            .setSelectionColor(0, Color.gray(0.2))
            .timeCursorColor_(Color.white);

        sfviewRoutFreq = 30;

        window.view.decorator.nextLine;

        //////////////////// SYNTH PANEL ////////////////////

        synthPanel = CompositeView(window, 200@300);
        // Parent already has a margin, so margin is 0
        synthPanel.addFlowLayout(margin: 0@0);

        //////////////////// PLAY BUTTON ////////////////////

        playButton = Button(synthPanel, synthPanel.decorator.indentedRemaining.width@30)
            .states_([
                ["▶ Play", nil, Color.fromHexString("155814")],
                ["■ Stop", nil, Color.fromHexString("591c1b")]
            ])
            .action_({ |button|
                (button.value == 1).if {
                "play".postln;
                    bbcut.play(clock);
                } {
                    bbcut.stop;
                };
            })
            .enabled_(false);

        synthPanel.decorator.nextLine;

        //////////////////// GLOBAL SETTINGS ////////////////////

        synthPanelKnob = { |label, controlSpec, initVal, action|
            var knob;
            knob = EZKnob(
                synthPanel, knobSize,
                layout: \vert2,
                label: label,
                initVal: initVal,
                controlSpec: controlSpec,
                action: action
            );
            knob.labelView
                .font_(Font(nil, 10.5));
            knob.numberView
                .font_(Font(nil, 10.5))
                .align_(\center);
            knob;
        };

        tempoKnob = synthPanelKnob.(
            "Tempo", ControlSpec(40, 200, \lin, 2, default: 144), nil,
            { |knob|
                clock.notNil.if {
                    clock.tempo = knob.value / 60;
                };
                this.updateCode;
            }
        );

        jumpKnob = synthPanelKnob.(
            "Jump", \unipolar, 0.5,
            { |knob|
                cutsynth.notNil.if {
                    cutsynth.offset = knob.value;
                };
                this.updateCode;
            }
        );

        //////////////////// PROCEDURE PANEL ////////////////////

        procPanel = CompositeView(window, window.view.decorator.indentedRemaining.width@300);
        // Parent already has a margin, so margin is 0
        procPanel.addFlowLayout(margin: 0@0);

        //////////////////// PROCEDURE MENU ////////////////////

        procMenu = EZPopUpMenu(
            procPanel, procPanel.decorator.indentedRemaining.width@30,
            items: cutprocs.collect(_.key),
            initVal: 0,
            globalAction: { |menu|
                bbcut.notNil.if {
                    bbcut.proc = cutprocs[menu.value].value.interpret;
                };
                this.updateCode;
            }
        );

        procPanel.decorator.nextLine;

        //////////////////// DISPLAY ////////////////////

        window.front;

    }

    cleanUp { |buffer|

        buf.notNil.if { buf.free };
        buf = nil;

        sf.notNil.if { sf.free };
        sf = nil;

        bbcut.notNil.if { bbcut.free };
        bbcut = nil;

        sfviewRout.notNil.if { sfviewRout.stop; };
        sfviewRout = nil;

        clock.notNil.if { clock.stop };
        clock = nil;

    }

    loadBuf { |filename, n|

        Server.default.waitForBoot {

            BBCutBuffer(filename, n, action: { |argBuf|
                this.cleanUp;

                buf = argBuf;

                // Synth initialization
                cutsynth = CutBuf3(buf, 0.3);
                cutsynth.grainfunc_(e { |whichcut, block, clock, startPos, dur|
                    cursorPos = startPos;
                    grainStartPos = startPos;
                    grainDur = dur * buf.sampleRate;
                });

                clock = ExternalClock(TempoClock(2.4)).play;
                bbcut = BBCut2(CutGroup(cutsynth, numChannels: buf.numChannels), BBCutProc11());

                { playButton.enabled = true }.defer;

                sf = SoundFile();
                sf.openRead(buf.path);

                { sfview.soundfile_(sf).read.refresh; }.defer;

                sfviewRout = Routine({
                    loop {
                        {
                            sfview.setSelectionStart(0, grainStartPos);
                            sfview.setSelectionSize(0, grainDur);
                            sfview.timeCursorPosition_(cursorPos);
                            sfview.timeCursorOn_(cursorPos <= (grainStartPos + grainDur));
                        }.defer;
                        cursorPos = cursorPos + (buf.sampleRate * sfviewRoutFreq.reciprocal);
                        sfviewRoutFreq.reciprocal.yield;
                    }
                });

                sfviewRout.play;

                {
                    procMenu.doAction;
                    tempoKnob.doAction;
                    jumpKnob.doAction;
                }.defer;

            });

        };

    }

    updateCode {
        "~buf = BBCutBuffer(%, %);\nBBCut2(CutBuf3(~buf, %), %).play(%);".format(
            buf.path.asCompileString,
            segmentsMenu.items[segmentsMenu.value],
            jumpKnob.value.asStringPrec(3),
            cutprocs[procMenu.value].value,
            (tempoKnob.value / 60).asStringPrec(3);
        ).postln;
    }

}