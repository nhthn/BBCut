//This file is part of The BBCut Library. Copyright (C) 2001  Nick M.Collins distributed under the terms of the GNU General Public License full notice in file BBCutLibrary.help

//DataDisplaySlider N.M.Collins 6/1/02 reworked for SC3 23/8/03

//5
//10	1 gap 58 name 1 gap 29 data 1 gap
//10	1 gap 98 slider 1 gap	
//5

//needs some colouring in

DDSlider
{
classvar <>frontcol, <>backcol; //<>slidfcol, <>slidbcol, <>strfcol, <>strbcol, <>namefcol, <>namebcol; 
var <>slider, <>string, <>spec, <>lastval;

*initClass
{
backcol= Color.blue(0.5,0.5); //Color.blue(0.5,0.7);//Color.new255(135, 206, 250);
frontcol=Color.white(0.8,0.7); //Color.new255(310, 105, 30); //210 .blue(0.3).
}

*new
{
arg w, bounds, name, min=0.0, max=1.0, warp=\lin, step=0.0, start=0.0, xprop=0.6, yprop=0.6; 

^super.new.initDDSlider(w, bounds, name, min, max, warp, step,start, xprop, yprop);
}

initDDSlider {
arg w, bounds, name, min=0.0, max=1.0, warp=\lin, step=0.0, startval=0.0, xprop=0.6, yprop=0.6; 
var namerect,stringrect,slidrect;
var temp,temp2;
	
lastval= startval; 
spec= ControlSpec(min,max,warp, step, startval);

temp= ((bounds.width) * xprop).round(1.0).asInteger; //[1,temp-3,1,bounds.width-temp,1]
temp2= ((bounds.height) * yprop).round(1.0).asInteger; //[1,temp2-6,4,bounds.height-temp2,1]
		 	
//namerect= Rect(bounds.left+1,bounds.top+1,temp-3,temp2-6);
//stringrect= Rect(bounds.left+temp-1,bounds.top+1,(bounds.width)-temp,temp2-6);
//slidrect= Rect(bounds.left+1,bounds.top+(temp2-1),(bounds.width)-2,(bounds.height)-temp2);

namerect= Rect(bounds.left,bounds.top,temp-1,temp2-1);
stringrect= Rect(bounds.left+temp-1,bounds.top,(bounds.width)-temp,temp2-1);
slidrect= Rect(bounds.left,bounds.top+(temp2-1),(bounds.width),(bounds.height)-temp2);


SCStaticText( w, namerect).string_(name).stringColor_(frontcol).background_(backcol);
string=SCStaticText( w, stringrect).string_(lastval).stringColor_(frontcol).background_(backcol);

slider=SCSlider.new( w, slidrect).knobColor_(frontcol).background_(backcol);
//set slider to default value, else will default to 0.0
slider.value_(spec.unmap(lastval));

slider.action_({arg sl; 
var val; val = spec.map(sl.value); 

string.string_(val); 

lastval=val;  
//set associated variable to this value, client code will poll this rather than the slider directly
//so safe for TempoClock use etc
});

}


value
{
^lastval;
}


value_
{arg val;

lastval=val;
string.string_(lastval); 
slider.value_(spec.unmap(lastval));
}



action_
{
arg func;

slider.action_({arg sl; 
var val; val = spec.map(sl.value);  string.string_(val); 
lastval=val;  

func.value(lastval);
});
}

}
