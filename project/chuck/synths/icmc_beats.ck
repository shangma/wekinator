/* This is a simple additive layered drum machine
   Sounds adapted from standard ChucK demos by Ge Wang, Perry Cook
   Part of ICMC 2009 demo
 
 Wekinator version 0.2
 Copyright 2009 Rebecca Fiebrink
 http://wekinator.cs.princeton.edu
*/

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

	//Only 1 parameter here
	1 => int numParams;
	float myParams[numParams];

	//Beats-specific stuff:
	.5::second => dur T;

	//The synthesis patch: envelope each of 4 parts individually
    Envelope e1 => e;
	Envelope e2 => e;
	Envelope e3 => e;
	Envelope e4 => e;
	0 => e1.value => e2.value => e3.value => e4.value;
	0 => e1.target => e2.target => e3.target => e4.target;
	100::ms => e1.duration => e2.duration => e3.duration => dur defaultDuration;

	//Do we want discrete or continuous parameters?
	//i.e., NN or classifier?
	fun int isDiscrete() {
		return 1;
	}
	
	//The number of classes-- max-- that we want to use
	//Necessary for structuring OSC messages
	fun int getNumClasses() {
		return 4;
	}

	//Do we want the labels for each parameter,
	//or a distribution over all possible labels? (classifier only)
	fun int useDistribution() {
		return 0;
	}

	//This is called by the main code, only once after initialization, like a constructor
	fun void setup() {
		spork ~part1();
		spork ~part2();
		spork ~part3();
		spork ~part4();
		e1.keyOn();
		e2.keyOn();
		e3.keyOn();
		e4.keyOn();
		turnOnUpTo(0);		
	}

	//This is also sporked in the other code
	//to define what happens when we get learned
	//parameters back-- i.e., what do we do with them?
	//Change both code if you want something different.
	fun void setParams(float params[]) {
		if (params.size() >= 1) {		
			//Adjust the synthesis accordingly
			params[0] => myParams[0];
			if (myParams[0] < 0)
				0 => myParams[0];
			if (myParams[0] > 4)
				4 => myParams[0];
			turnOnUpTo(myParams[0]);
			turnOffAbove(myParams[0]);
		}
	}

	//drum parts
	fun void part1() {
		 drum1();
	}
	
	fun void part2() {
		spork ~drum2();
		drum3();
	}

	fun void part3() {
		spork ~melody1();
		melody2();
	}

	fun void part4() {
		funtimes();
	}

	fun void drum1() {
		// construct the patch
		SndBuf buf => Gain g => e1;
		"synths/data/kick.wav" => buf.read;
		.5 => g.gain;

		// time loop
		while( true )
		{
    		0 => buf.pos;
		    Std.rand2f(.8,.9) => buf.gain;
		    1::T => now;
		}
	}

	fun void drum2() {
		// construct the patch
		SndBuf buf => Gain g => e2;
		"synths/data/hihat.wav" => buf.read;
		.25 => g.gain;

		// time loop
		while( true )
		{
		    Std.rand2f(.4,.9) => buf.gain;
    		if( Std.randf() > 0.75 )
		    {
    	    	0 => buf.pos;
    		    .5::T => now;
	   	 	}
    		else
	    	{
    	    	0 => buf.pos;
		        .25::T => now;
    	    	0 => buf.pos;
        		.25::T => now;
	    	}
		}
	}
	
	fun void drum3() {
		// construct the patch
		SndBuf buf => Gain g => e2;
		"synths/data/hihat-open.wav" => buf.read;
		.5 => g.gain;

		// time loop
		while( true )
		{
		    0 => buf.pos;
		    Std.rand2f(.8,.9) => buf.gain;
		    1::T => now;
		}
	}

	fun void melody1() {
		// connect patch
		SinOsc s => e3;
		.25 => s.gain;

		// scale (in semitones)
		[ 0, 2, 4, 7, 9 ] @=> int scale[];

		// infinite time loop
		while( true )
		{
		    // get note class
    		scale[ Math.rand2(0,4) ] => float freq;
		    // get the final freq    
    		Std.mtof( 21.0 + (Std.rand2(0,3)*12 + freq) ) => s.freq;

	    	// advance time
	    	.25::T => now;
		}
	}

	fun void melody2() {
		SinOsc s => JCRev r => e3;
		.05 => s.gain;
		.25 => r.mix;

		// scale (in semitones)
		[ 0, 2, 4, 7, 9 ] @=> int scale[];

		// infinite time loop
		while( true )
		{
    		// get note class
		    scale[ Math.rand2(0,4) ] => float freq;
    		// get the final freq
		    Std.mtof( 69 + (Std.rand2(0,3)*12 + freq) ) => s.freq;
    		// reset phase for extra bandwidth
	    	0 => s.phase;

	    	// advance time
		    if( Std.randf() > -.5 ) .25::T => now;
    		else .5::T => now;
		}
	}

	fun void funtimes() {
		// construct the patch
		SndBuf buf => Gain g => JCRev r => e4;
		"synths/data/snare.wav" => buf.read;
		.5 => g.gain;
		.05 => r.mix;

		// where we actually want to start
		25 => int where;

		// time loop
		while( true )
		{
    		Std.rand2f(.8,.9) => buf.gain;

		    if( Std.randf() > .5 )
		    {
		        0 => int i;
        		while( i < 8 )
		        {
        		    i / 8.0 => buf.gain;
		            where => buf.pos;
        		    (1.0/8.0)::T => now;
		            i++;
        		}

		        while( i > 0 )
        		{
            		i / 8.0 => buf.gain;
	            	where => buf.pos;
	    	        (1.0/8.0)::T => now;
    	    	    i--;
	    	    }
    		} else {
	        	.9 => buf.gain;
		        where => buf.pos;
    		    .25::T => now;
        		.3 => buf.gain;
	       		where => buf.pos;
		        .25::T => now;
    		    .3 => buf.gain;
        		where => buf.pos;
		        .25::T => now;
	
    		    .9 => buf.gain;
	    	    where => buf.pos;
    	    	.25::T => now;
	        	.3 => buf.gain;
		        where => buf.pos;
    		    .25::T => now;
        		.3 => buf.gain;
		        where => buf.pos;
    		    .25::T => now;
	
    	    	.9 => buf.gain;
		        where => buf.pos;
    		    .25::T => now;
        		.3 => buf.gain;
	        	where => buf.pos;
	    	    .25::T => now;
    		}
		}
	}

	//Control layering via envelopes:
	fun void turnOnUpTo(float p) {
			1 => e1.target;
		if (p > 0) 
			1 => e2.target;
		if (p > 1)
			1 => e3.target;
		if (p > 2) 
			1 => e4.target;
	}

	fun void turnOffAbove(float p) {
		if (p < 3) 
			0 => e4.target;
		if (p < 2)
			0 => e3.target;
		if (p < 1) 
			0 => e2.target;
	}

/* Don't need to change anything below this line ----------------------------*/
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
	}

	//Make sound!
	fun void sound() {
		1 => e.target;
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
		"Beat#" => s[0];
		return s;
	}

}
