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
    |     logo, soundfile       |
    |                           |
    +---------+-----------------+
    |         |                 |
    |         |                 |
    |  BBCut  |    procedure-   |
    | globals |     specific    |
    |         |     settings    |
    |         |                 |
    |         |                 |
    +---------+-----------------+

    */

    var margin = 10;
    var globalPanelWidth = 200;
    var procPanelWidth = 400;
    var logoHeight = 30;
    var fileHeight = 20;
    var sfviewHeight = 100;
    var playButtonHeight = 30;

    var fileFieldWidth = 200;
    var fileBrowseButtonWidth = 100;
    var beatLengthFieldWidth = 150;
    var fileLoadButtonWidth = 100;

    var knobWidth = 50;
    var knobHeight = 100;

    // GUI elements
    var window;
    var topPanel;
    var globalPanel;
    var procPanel;

    var logoText;

    var fileField;
    var fileBrowseButton;
    var beatLengthField;
    var fileLoadButton;

    var sfview;
    var playButton;
    var procMenu;
    var tempoKnob;
    var jumpKnob;

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
        var topPanelWidth;
        var bottomPanelY;
        var windowHeight;

        // sound file view variables
        cursorPos = 0.0;
        grainStartPos = 0.0;
        grainDur = 0.0;

        // Messy dimension computations
        // Can't use Layout, it's Qt only and doesn't work with EZGui
        topPanelWidth = globalPanelWidth + margin + procPanelWidth;
        bottomPanelY = margin + logoHeight + margin + fileHeight + margin + sfviewHeight + margin;
        windowHeight = bottomPanelY + playButtonHeight + margin + knobHeight + margin;

        // Creating the GUI elements

        window = Window("CutLab", Rect(0, 0, margin + topPanelWidth + margin, windowHeight));

        logoText = StaticText(window, Rect(margin, margin, topPanelWidth, logoHeight))
            .string_("✂ CutLab ✂")
            .align_(\center)
            .font_(Font(nil, 30));

        // this is SO awful!!!
        fileField = TextField(window, Rect(margin, margin + logoHeight + margin, fileFieldWidth, fileHeight))
            .value_(if(File.exists(Platform.resourceDir +/+ "sounds/break.aiff"), Platform.resourceDir +/+ "sounds/break.aiff", Platform.resourceDir +/+ "sounds/a11wlk01.wav"));
        fileBrowseButton = Button(window, Rect(margin + fileFieldWidth + margin, margin + logoHeight + margin, fileBrowseButtonWidth, fileHeight))
            .states_([["Browse..."]])
            .action_ {
                Dialog.openPanel { |filename|
                    fileField.value = filename;
                };
            };
        beatLengthField = EZNumber(
            window, Rect(margin + fileFieldWidth + margin + fileBrowseButtonWidth + margin, margin + logoHeight + margin, beatLengthFieldWidth, fileHeight),
            label: "# of beats",
            numberWidth: 60,
            controlSpec: ControlSpec(4, 64, \lin, 1, default: 8)
        );
        fileLoadButtonWidth = Button(window, Rect(margin + fileFieldWidth + margin + fileBrowseButtonWidth + margin + beatLengthFieldWidth + margin, margin + logoHeight + margin, fileLoadButtonWidth, fileHeight))
            .states_([["Load"]])
            .action_ {
                this.loadBuf(fileField.value, beatLengthField.value);
            };

        sfview = SoundFileView(window, Rect(margin, margin + logoHeight + margin + fileHeight + margin, topPanelWidth, sfviewHeight))
            .gridOn_(false)
            .setSelectionColor(0, Color.gray(0.2))
            .timeCursorColor_(Color.white);

        sfviewRoutFreq = 30;

        playButton = Button(window, Rect(margin, bottomPanelY, globalPanelWidth, playButtonHeight))
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
            })
            .enabled_(false);

        tempoKnob = EZKnob(
            window, Rect(margin, bottomPanelY + playButtonHeight + margin, knobWidth, knobHeight),
            label: "Tempo",
            controlSpec: ControlSpec(40, 200, \lin, 2, default: 144),
            action: { |knob|
                clock.notNil.if {
                    clock.tempo_(knob.value / 60);
                };
                this.updateCode;
            }
        );
        tempoKnob.labelView.align_(\center);

        jumpKnob = EZKnob(
            window, Rect(margin + knobWidth + margin, bottomPanelY + playButtonHeight + margin, knobWidth, knobHeight),
            label: "Jump",
            controlSpec: \unipolar,
            initVal: 0.5,
            action: { |knob|
                cutsynth.notNil.if {
                    cutsynth.offset = knob.value;
                };
                this.updateCode;
            }
        );
        jumpKnob.labelView.align_(\center);

        procMenu = EZPopUpMenu(
            window, Rect(margin + globalPanelWidth + margin, bottomPanelY, procPanelWidth, playButtonHeight),
            items: cutprocs.collect(_.key),
            initVal: 0,
            globalAction: { |menu|
                bbcut.notNil.if {
                    bbcut.proc = cutprocs[menu.value].value.interpret;
                };
                this.updateCode;
            }
        );

        window.front;
        window.onClose_ {
            this.cleanUp;
        };

    }

    cleanUp {
        buf.notNil.if { buf.free };
        sf.notNil.if { sf.free };
        clock.notNil.if { clock.stop };
        bbcut.notNil.if { bbcut.free };
        sfviewRout.notNil.if { sfviewRout.stop; };
        buf = nil;
        sf = nil;
        clock = nil;
        bbcut = nil;
        sfviewRout = nil;
    }

    loadBuf { |filename, n|

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

    }

    updateCode {
        "BBCut2(CutBuf3(buf, %), %).play(%)".format(
            jumpKnob.value.asStringPrec(3),
            cutprocs[procMenu.value].value,
            (tempoKnob.value / 60).asStringPrec(3);
        ).postln;
    }

}