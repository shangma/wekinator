/* Simple synth skeleton
 To make your own synth, edit everywhere marked TODO below!
 
 Wekinator version 0.2
 Copyright 2009 Rebecca Fiebrink
 http://wekinator.cs.princeton.edu
*/

//The synth always lives in a SynthClass definition
public class SynthClass {

	//Don't change this part: necessary state objects and overall envelope
	OscSend xmit;
	0 => int isSendingParams;
	50::ms => dur rate;
	Envelope e => dac;
	.1::second => e.duration;
	0 => e.target => e.value;
	e.keyOn();

	//TODO: set your # of parameters
	//For example, if you want to control pitch and volume, you might have 
	//2 parameters.
	1 => int numParams;
	float myParams[numParams];

	//TODO: Add your own objects for making sound
	//and patch them together, output to the envelope
	float freq;
	//0.0 => float myNote;
	0.0 => myParams[0]; //we've got 1 param, let it be 0 for starters
    SinOsc s => e; //patch into the envelope
	440 => s.freq;	

	//TODO: Are your parameters discrete (integers) or continuous (real numbers)?
	//This determines the learning algorithms available in the GUI
	fun int isDiscrete() {
		return 1; //Return 1 for discrete, 0 for continuous
	}

	//TODO: If you are using a discrete model, you must specify
	//the number of classes of output (maximum) that you want to learn
	//For example, to learn a "vowel vs. consonant" classifier, you would have 2 classes
	//Or to output a class for each pitch chroma, you would have 12 classes
	//For now, you have to have the same number of classes for each parameter (sorry, this should be fixed)
	fun int getNumClasses() {
		return 12;
	}

	//TODO: If you are using a discrete model, you must specify
    //whether you just want the integer class label output for each parameter, or whether
    //you want the output to consist of a probability distribution over all classes
	fun int useDistribution() {
		return 0; //Return 1 for distribution, 0 for simple integer label
	}

	//TODO: Any other setup code that should be called
	//This is called by the main code, only once after initialization, like a constructor
	fun void setup() {
		//Spork things here if necessary, then return.
	}

	//TODO: This gets called when the model provides us with new parameter values
	//Specify how you want to use them!
    //Want to do error checking here (e.g., that parameters are within the expected range,
    // and that we have the expected number of parameters)
	// Make sure you both use the new params to make sound AND store the values in myParams[].
	fun void setParams(float params[]) {
		if (params.size() >= 1) {		
			//Adjust the synthesis accordingly
			params[0] => myParams[0];
			if (myParams[0] < 0)
				0 => myParams[0];
			if (myParams[0] > 12)
				11 => myParams[0];
			calcFreqFromParam(myParams[0]) => s.freq;
		}
	}

	//TODO (optional)
	//Add your own methods to be called from your synthesis, for example:
	fun float calcFreqFromParam(float p) {
		return Std.mtof(72 + p);
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
