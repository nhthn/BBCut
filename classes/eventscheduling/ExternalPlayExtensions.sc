//This file is part of The BBCut Library. Copyright (C) 2001  Nick M.Collins distributed under the terms of the GNU General Public License full notice in file BBCutLibrary.help

//Extensions to support playing on an ExternalClock

+ Pattern {

    //quant will be ignored
    playExt { arg clock, protoEvent, quant=1.0;

        //"not here".postln;
        ^this.asEventStreamPlayer2(protoEvent).play(clock, false, quant)
    }

    asEventStreamPlayer2 { arg protoEvent;
        ^EventStreamPlayer2(this.asStream, protoEvent);
    }

}