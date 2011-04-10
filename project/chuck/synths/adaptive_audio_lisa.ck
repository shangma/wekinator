/* This changes LiSa params of live audio input
	(i.e., makes munchkin/monster choruses)

 LiSa code is from Dan Trueman

 Copyright 2009 Rebecca Fiebrink
 http://wekinator.cs.princeton.edu
*/

//The synth always lives in a SynthClass definition
public class SynthClass {
	//Necessary state objects and overall envelope
	OscSend xmit;
	0 => int isPlayingScore;
	0 => int isSendingParams;
	1 => int hasFinished;
	50::ms => dur rate;
	Envelope e => dac;
	.1::second => e.duration;
	0 => e.target => e.value;
	e.keyOn();

	//1 parameter, specifying pan location
	1 => int numParams;
	float myParams[numParams];

	//The synthesis patch: Based on code by Dan Trueman
	//use three buffers to avoid clicks
	0 => int isSounding;
	float p1;
	LiSa l[3];
	1::second 		=> dur bufferlen; //allocated buffer size -- remains static
	0.1::second 	=> dur reclooplen;//portion of the buffer size to use -- can vary
	0 => int recbuf;
	2 => int playbuf;

	[-12,0, 4, 7, 12] @=> int pp1[];
	[-12,-13,-14,-15] @=> int pp2[];
	[12,13,14,15] @=> int pp3[];
	pp1 @=> int pitchtable[];

	//LiSa params, set
	for(0=>int i; i<3; i++) {
    
    	l[i].duration(bufferlen);
	    l[i].loopEndRec(reclooplen);
    	l[i].maxVoices(10);
	    l[i].clear();
    	l[i].gain(0.2);
	    //if you want to retain earlier passes through the recording buff when loop recording:
    	//l[i].feedback(0.5); 
	    l[i].recRamp(20::ms); //ramp at extremes of record buffer while recording
    	l[i].record(0);
    
	    //adc => l[i].chan(0) => e;
		adc => l[i] => e;
	}

	//Do we want discrete or continuous parameters?
	//i.e., NN or classifier?
	fun int isDiscrete() {
		return 1;
	}
	
	//The number of classes-- max-- that we want to use
	//Necessary for structuring OSC messages
	fun int getNumClasses() {
		return 3;
	}

	//Do we want the labels for each parameter,
	//or a distribution over all possible labels? (classifier only)
	fun int useDistribution() {
		return 0;
	}

	//TODO: Any other setup code that should be called
	//This is called by the main code, only once after initialization, like a constructor
	fun void setup() {
		spork ~li();
	}

	//This is also sporked in the other code
	//to define what happens when we get learned
	//parameters back-- i.e., what do we do with them?
	fun void setParams(float params[]) {
		if (! isSounding) {
			sound();
		}
		if (params.size() > 0) {
			//Change the pitchtable used based on Wekinator's output
			params[0] => p1;
			if (p1 == 0) {
				pp1 @=> pitchtable;
			} else if (p1 == 1) {
				pp2 @=> pitchtable;
			} else {
				pp3 @=> pitchtable;
			}
		}
	}
	
	//LiSa-specific functions: From Dan
	fun void li() {
		//start recording in buffer 0
		l[recbuf].record(1);

		//create grains, rotate record and play bufs as needed
		//shouldn't click as long as the grainlen < bufferlen
		while(true) {
    
		    //will update record and playbufs to use every reclooplen
		    now + reclooplen => time later;
    
  			//toss some grains
		    while (now<later) {
        
        		Std.rand2f(0, pitchtable.size()) $ int => int newpitch; //choose a transposition from the table
		        Std.mtof(pitchtable[newpitch] + 60)/Std.mtof(60) => float newrate;
		        Std.rand2f(50, 100) * 1::ms => dur newdur; //create a random duration for the grain
        
        		//spork off the grain!
		        spork ~ getgrain(playbuf, newdur, 20::ms, 20::ms, newrate);
        
        		//wait a bit.... then do it again, until we reach reclooplen
		        5::ms => now;
    
			}

		//rotate the record and playbufs
		l[recbuf++].record(0);
		if(recbuf == 3) 0 => recbuf;
		l[recbuf].record(1);

		playbuf++;
		if(playbuf == 3) 0 => playbuf;

		} //end while tru
	} //end li
	
	//for sporking grains; can do lots of different stuff here -- just one example here
	fun void getgrain(int which, dur grainlen, dur rampup, dur rampdown, float rate)
	{
   	 	l[which].getVoice() => int newvoice;
    	//<<<newvoice>>>;
    
    	if(newvoice > -1) {
       		l[which].rate(newvoice, rate);
	        l[which].playPos(newvoice, Std.rand2f(0., 1.) * reclooplen);
    	    l[which].rampUp(newvoice, rampup);
        	(grainlen - (rampup + rampdown)) => now;
	        l[which].rampDown(newvoice, rampdown);
    	    rampdown => now;
	    }
    }


/* PROBABLY don't need to change anything below this line ----------------------------*/
/* See modification in icmc_twinkle_form_control.ck for implementing custom sound on/off 
behavior, beyond master envelope control.*/
	fun int getNumParams() {
		return numParams;
	}

	fun float[] getParams() {
		return myParams;
	}

	//Be quiet! If you want to improve efficiency here, you could also stop
	//other processing
	fun void silent() {
		0 => e.target;
		1 => isSounding;
	}

	//Make sound!
	fun void sound() {
		1 => e.target;
		1 => isSounding;
	}

	//Received when wekinator wants our params for playalong learning
	fun void startGettingParams(OscSend x, dur r) { //Q: Where does this go?? Here or in recording? Or neither-- put in main?
		x @=> xmit;
		r => rate;
		1 => isSendingParams;
		spork ~sendParamsLoop();
	}

	//Send those parameters on at a specified rate
	fun void sendParamsLoop() {
		while (isSendingParams) {
			sendParams();
			rate => now;
		}
	}

	//Received when wekinator wants us to stop sending those playalong params
	fun void stopGettingParams() {
		0 => isSendingParams;
	}

	//Send current parameters directly to Wekinator
	fun void sendParams() {
		"/realValue f" => string ss;
		1 => int i;
		for (1 => i; i < numParams; i++) {
			ss + " f" => ss;
		}
		xmit.startMsg(ss);
		for (0 => i; i < numParams; i++) {
			xmit.addFloat(myParams[i]); //Add all params, each in its own addFloat message.
		}
	}

	//If OSC synth, we need to instruct the synth how to get back to ChucK
	fun void setOscHostAndPort(string h, int p) {
		//no need to do anything, unless you're using an OSC synth like Processing or Max.
	}
/*** Copy & Paste below at end of your code to add new functions as of 12/6/09 ***/
	fun int[] useDistributionArray() {
		new int[numParams] @=> int a[];
		for (0 => int i; i < numParams; i++) {
			useDistribution() => a[i];
		}
		return a;
	}

	fun int[] isDiscreteArray() {
		new int[numParams] @=> int a[];
		for (0 => int i; i < numParams; i++) {
			isDiscrete() => a[i];
		}
		return a;
	}

	fun int[] getNumClassesArray() {
		new int[numParams] @=> int a[];
		for (0 => int i; i < numParams; i++) {
			getNumClasses() => a[i];
		}
		return a;
	}
	
	fun string[] getParamNamesArray() {
		new string[1] @=> string s[];
		"LisaPitchSet" => s[0];
		return s;
	}

}
