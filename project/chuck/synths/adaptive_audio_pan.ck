/* This adaptively pans the audio based on the input class

 Wekinator version 0.2
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

	//The synthesis patch
	adc => Envelope e1 => dac.left;
	adc => Envelope e2 => dac.right;
	0 => e1.value => e2.value;
	0 => e1.target => e2.target;
	100::ms => e1.duration => e2.duration;
	e1.keyOn();
	e2.keyOn();

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

	//TODO: Any other setup code that should be called
	//This is called by the main code, only once after initialization, like a constructor
	fun void setup() {
	}

	//This is also sporked in the other code
	//to define what happens when we get learned
	//parameters back-- i.e., what do we do with them?
	fun void setParams(float params[]) {
		if (params.size() >= numParams) {		
			//Adjust the synthesis accordingly
			params[0] => float p1;
			if (p1 == 0) { //pan left
				1 => e1.target;
				0 => e2.target;
			} else if (p1 == 1) { //pan right
				0 => e1.target; 
				1 => e2.target;
			} else if (p1 == 2) { //no audio
				0 => e1.target;
				0 => e2.target;
			} else if (p1 == 3) { //no panning
				1 => e1.target;
				1 => e2.target;
			}
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
}
