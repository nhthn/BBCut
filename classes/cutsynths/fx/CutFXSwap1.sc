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
					
				newfx = [
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
					
						deltime = [
							[36, 50, 64, 65, 67, 69, 70, 72, 74, 73, 72, 70, 69, 70, 69, 67, 67, 60, 60].wrapAt(_).midicps.reciprocal,
							[36, 48, 48, 48.3, 47.7, 36, 36.5, 35.5, 36].wrapAt(_).midicps.reciprocal,
							(_.clip2(10) * 0.01) + 0.01
						].choose;

						dectime = [
							0.2,
							(_.clip2(10) * 0.2) + 0.1
						].choose;
						
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