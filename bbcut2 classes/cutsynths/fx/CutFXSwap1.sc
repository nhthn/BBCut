//This file is part of The BBCut Library. Copyright (C) 2001  Nick M.Collins distributed under the terms of the GNU General Public License full notice in file BBCutLibrary.help 

//CutFXSwap1 N.M.Collins 1/2/05

CutFXSwap1 : CutSynth { 
	var basenum, numfx, <>maxnumfx, <>addchance, <>removechance;
	
	*new{arg maxnumfx=3, addchance=0.5, removechance=0.5;
	
		^super.new.initCutFXSwap1(maxnumfx, addchance, removechance);
	}

	initCutFXSwap1 {arg mnfx, ac, rc;
		
		maxnumfx= mnfx;
		addchance= ac;
		removechance= rc;
		
		numfx=0;
	
	}
	
	setup {
	
		basenum= cutgroup.cutsynths.size; //includes this one? 
		
		//"basenum".postln;
		basenum.postln;	
	}

	
	//can swap at a phrase beginning, block.blocknum==0
	renderBlock {arg block,clock;
		var newfx;
		
		//may be consequences if outer call already iterating through the list! 
		if(block.blocknum==1, {	
			
			//remove
			if((removechance.coin) && (numfx>0),{
				cutgroup.removeAt(basenum+(numfx.rand));
				numfx=numfx-1;
			});
			
			
			
			if((addchance.coin) && (numfx<maxnumfx),{	
					
				newfx= [
				{var cfreq, rq,drq;
				
				cfreq= [exprand(500,7500),{arg block; (block.phraseprop)*1000+5000}, {exprand(500,7500)}].choose;
				rq= [0.1,exprand(0.05,0.5),{exprand(0.05,0.5)}].choose;
				drq= [rrand(1.01,1.1),{rrand(1.01,1.05)},{rrand(1.01,1.1)}].choose;
				
				CutBRF1(cfreq,rq,drq)
				}
				,{var cfreq, rq,drq;
				
				cfreq= [exprand(500,7500),{arg block; (block.phraseprop)*1000+5000}, {exprand(500,7500)}].choose;
				rq= [1,exprand(0.5,2),{exprand(0.5,2)}].choose;
				drq= [rrand(0.9,0.99),{rrand(0.9,0.99)},{rrand(0.8,0.9)}].choose;
				
				CutBPF1(cfreq,rq,drq)
				}
				,{var modfreq,mfmult,modamount,mamult;
				
				modfreq=[exprand(100,1000),{exprand(100,1000)},{exprand(500,5000)}].choose;
				mfmult=[rrand(0.8,1.2),{rrand(0.9,1.1)},{rrand(0.8,1.2)}].choose;
				modamount=[1.0,rrand(0.5,1.0),{rrand(0.0,1.0)}].choose;
				mamult=[1,{rrand(0.9,0.99)},{rrand(0.8,0.95)}].choose;
				
				CutMod1(modamount,modfreq,mamult,mfmult)
				}
				,{var bits,bitadd,sr,srmult;
				
				bits=[16,rrand(3,16),{rrand(2,16)},{rrand(3,10)},{rrand(3,6)}].choose;
				bitadd=[-0.5,-1,rrand(-4,-1),{rrand(-0.5,-0.1)},{rrand(-2,-0.1)},{rrand(-4,-1)}].choose;
				sr=[22050,exprand(10000,20000),{exprand(5000,22050)},{rrand(1000,5000)}].choose;
				srmult=[rrand(0.5,0.9),rrand(0.8,0.99),{rrand(0.8,0.99)},{rrand(0.5,0.99)}].choose;
				
				CutBit1(bits,sr,bitadd,srmult);
				}
				,{var deltime, dectime;
				
				//Post << [#[36,50,64,65,67,69,70,72,74,73,72,70,69,70,69,67,67,60,60],
//				#[36,48,48.3,47.7,47.1,36,36.5,35.5,36]
//				].midicps.reciprocal << nl; 
//				
				deltime= [{arg i; #[ 0.015289025731769, 0.0068104867130459, 0.003033726941292, 0.0028634569315766, 0.0025510501097929, 0.0022727272727273, 0.0021451688924589, 0.001911128216487, 0.0017026216782709, 0.0018038648317837, 0.001911128216487, 0.0021451688924589, 0.0022727272727273, 0.0021451688924589, 0.0022727272727273, 0.0025510501097929, 0.0025510501097929, 0.0038222564329635, 0.0038222564329635 ].wrapAt(i)},{arg i; #[ 0.015289025731769, 0.0076445128659058, 0.0075131847104569, 0.0077781365970763, 0.0080524319917989, 0.015289025731769, 0.014853776847535, 0.015737028381808, 0.015289025731769 ].wrapAt(i)},{arg i,block; (i.clip2(10)*0.01)+0.01}].choose;
				dectime= [0.2, {arg i; (i.clip2(10)*0.2)+0.1}].choose;
				
				CutComb1(deltime,dectime);
				}
				,{var amount,send;
				
				amount=[0.1,0.3,exprand(0.01,0.3)].choose;
				send= [1,{arg i,block; if(i==0,1,0)}, {arg i,block; (i.clip2(10))*0.1}, {arg i,block; if(i.even,1,0)}].choose;
				
				CutRev1(amount,send);
				}
				].choose.value; //wchoose([0.3,0.3,0.2,0.1,0.05,0.05]).value;
				
				cutgroup.add(newfx);
				numfx=numfx+1;
			});
			
			//break swapping in a separate BBCSFunc
			//if(0.8.coin,{~cutbuf2.bbcutbuf= ~breaks[rrand(0,7)];});
			
		});
	}


}