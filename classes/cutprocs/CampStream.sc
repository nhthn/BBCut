//This file is part of The BBCut Library. Copyright (C) 2001  Nick M.Collins distributed under the terms of the GNU General Public License full notice in file BBCutLibrary.help

//campcutproc N.M.Collins 17/10/01

//camp stream is fixed once initialised
//method is a stream itself
CampStream : Stream
{
    var <permdata, <method, <bells;

    *new { |permdata, method, bells|
        ^super.new.initCampStream(permdata, method, bells);
    }

    initCampStream {
        arg argPermdata, argMethod, argBells;

        permdata = argPermdata;
        method = argMethod;
        bells = argBells;
        this.reset;
    }

    *notation { |bells, string|
        var permdata;
        permdata = [[]] ++ string.split($.).collect {
            |row|
            (row == "x").if {
                (0,2..bells - 2)
            } {
                var changed, unchanged;
                changed = [];
                unchanged = row.as(Array).collect { |n| n.asString.asInteger - 1 };
                for(0, bells - 1, {
                    |i|
                    (
                        unchanged.includes(i).not and:
                        { unchanged.includes(i + 1).not } and:
                        { changed.includes(i - 1).not }
                    ).if {
                        changed = changed.add(i);
                    };
                });
                changed;
            };
        };
        ^CampStream(
            permdata,
            Pseq([0], 1).asStream ++ Pseq((1..permdata.size - 1), inf),
            bells
        );
    }

    next {
        ^permdata.at(method.next)
    }

    reset {
        method.reset;
    }

}
