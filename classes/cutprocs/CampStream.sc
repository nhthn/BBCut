//This file is part of The BBCut Library. Copyright (C) 2001  Nick M.Collins distributed under the terms of the GNU General Public License full notice in file BBCutLibrary.help

//campcutproc N.M.Collins 17/10/01

//camp stream is fixed once initialised
//method is a stream itself
CampStream : Stream
{
    var <permdata,<method,<bells;
    *new {arg permdata,method,bells;

        ^super.new.init(permdata,method,bells).reset;
    }

    init
    {
        arg permd,meth,b;

        permdata=permd;
        method=meth;
        bells=b;
    }

    next
    {
        ^permdata.at(method.next)
    }

    reset
    {
        method.reset;
    }

}
