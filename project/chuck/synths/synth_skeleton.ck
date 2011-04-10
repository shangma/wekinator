/*  Simple synth skeleton with 1 continuous & 1 discrete parameter
 	To make your own synth, edit everywhere marked TODO below

 	Copyright 2011 Rebecca Fiebrink
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
	2 => int numParams;
	float myParams[numParams];

	//TODO: Add your own objects for making sound
	//and patch them together, output to the envelope
	float freq;
	//0.0 => float myNote;
	0.0 => myParams[0]; //we've got 1 param, let it be 0 for starters
    SinOsc s => e; //patch into the envelope
	440 => s.freq;	
	Envelope volumeSmoother => blackhole;
	1.0 => volumeSmoother.value;
	1.0 => volumeSmoother.target;
	200::ms => volumeSmoother.duration;

	//TODO: Are your parameters discrete (integers) or continuous (real numbers)?
	//This determines the learning algorithms available in the GUI
	fun int[] isDiscreteArray() {
		new int[2] @=> int a[]; //a is a temporary array whose length is the number of parameters (2 here)
		1 => a[0]; //1 means this parameter is discrete
		0 => a[1]; //0 means this parameter is continuous (not discrete)
		return a;
	}

	//TODO: If you are using a discrete model, you must specify
	//the number of classes of output (maximum) that you want to learn
	//For example, to learn a "vowel vs. consonant" classifier, you would have 2 classes
	//Or to output a class for each pitch chroma, you would have 12 classes
	//It doesn't matter what number you use for a continuous parameter, since it'll be ignored
	fun int[] getNumClassesArray() {
		new int[2] @=> int a[];
		12 => a[0]; //12 pitches for parameter 1
		32435 => a[1]; //don't care (parameter 2 is continuous)
		return a;
	}

	//TODO: For each discrete parameter, you must specify
    //whether you just want the integer class label output for each parameter, or whether
    //you want the output to consist of a probability distribution over all classes
	fun int[] useDistributionArray() {
		new int[2] @=> int a[]; 
		0 => a[0]; //Let's not use a distribution (would be 1 otherwise)
		0 => a[1]; //don't care (parameter 2 is continuous)
		return a;
	}

	//TODO: Give your parameters some names, which will be shown in the GUI
	fun string[] getParamNamesArray() {
		new string[2] @=> string s[];
		"note" => s[0];
	    "volume" => s[1];
		return s;
	}

	//TODO: Any other setup code that should be called
	//This is called by the main code, only once after initialization, like a constructor
	fun void setup() {
		//Spork things here if necessary, then return.
		spork ~smooth();
	}

	//TODO: This gets called when the model provides us with new parameter values
	//Specify how you want to use them!
    //Want to do error checking here (e.g., that parameters are within the expected range,
    // and that we have the expected number of parameters)
	// Make sure you both use the new params to make sound AND store the values in myParams[].
	fun void setParams(float params[]) {
		if (params.size() >= 2) {	//always check you have at least as many params as you're expecting	
			//Adjust the synthesis accordingly
			params[0] => myParams[0];
			//Always check the range of the parameter you receive, if it matters
			//For 1st param, we want an octave range of pitch
			if (myParams[0] < 0)
				0 => myParams[0];
			if (myParams[0] > 12)
				11 => myParams[0];
			calcFreqFromParam(myParams[0]) => s.freq;

			params[1] => myParams[1];
			//For 2nd param, we want volume between 0 and 1
			if (myParams[1] < 0) 
				0 => myParams[1];
			if (myParams[1] > 1) 
				1 => myParams[1];
			myParams[1] => volumeSmoother.target;
		}
	}

	//TODO (optional)
	//Add your own methods to be called from your synthesis, for example:
	fun float calcFreqFromParam(float p) {
		return Std.mtof(60 + p);
	}

	fun void smooth() {
		//Could pull this into separate methods if want to smooth 
		//different model parameters at different rates.
		while (true) {
			volumeSmoother.value() => s.gain;
			10::ms => now;
		}
	}

/* PROBABLY don't need to change anything below this line ----------------------------*/
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

}
