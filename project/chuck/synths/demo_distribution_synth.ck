/*  Uses distribution over parameters to mix sounds
	Simplest possible version
	Would be much better to smooth gains!
*/

public class SynthClass {

	//Don't change this part: necessary state objects and overall envelope
	OscSend xmit;
	0 => int isSendingParams;
	50::ms => dur rate;
	Envelope e => dac;
	SinOsc s1 => e;
	440 => s1.freq;
	SinOsc s2 => e;
	500 => s2.freq;

	
	.1::second => e.duration;
	0 => e.target => e.value;
	e.keyOn();

	//Num params = 1; store entire distribution
	1 => int numParams;
	float myParams[numParams*2];

	float freq;
	1.0 => myParams[0]; //we've got 1 param, let it be 0 for starters	
	0.0 => myParams[1];

	//This determines the learning algorithms available in the GUI
	fun int isDiscrete() {
		return 1; //Return 1 for discrete, 0 for continuous
	}
	
	fun int getNumClasses() {
		return 2;
	}

	fun int useDistribution() {
		return 1; //Return 1 for distribution, 0 for simple integer label
	}

	fun void setup() {
	}
	
	fun void setParams(float params[]) {
		if (params.size() >= 2) {
			params[0] => myParams[0];
			if (myParams[0] < 0)
				0 => myParams[0];
			if (myParams[0] > 1)
				1 => myParams[0];
			params[1] => myParams[1];
			if (myParams[1] < 0)
				0 => myParams[1];
			if (myParams[1] > 1)
				1 => myParams[1]; 
			
			myParams[0] => s1.gain;
			myParams[1] => s2.gain;

		} else {
			<<< "Error: params are wrong size ">>>;
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
	}

	//Make sound!
	fun void sound() {
		1 => e.target;
	}

	//Received when wekinator wants our params for playalong learning
	fun void startGettingParams(OscSend x, dur r) { 
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
		new string[numParams] @=> string s[];
		for (0 => int i; i < numParams; i++) {
			"Param_" + i => s[i];
		}
		return s;
	}

}
