/* This controls a brass physical model

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

	//6 parameters here, though could change this easily
	6 => int numParams;
	float myParams[numParams];

	//The synthesis patch
	Brass b => e;
	440 => b.freq;
	Envelope envs[numParams];
	for (0 => int i; i < numParams-1; i++) {
	// new Envelope @=> envs[i];
		envs[i] => blackhole;
		.5 => envs[i].value => envs[i].target;;
		100::ms => envs[i].duration;
	}
	envs[numParams-1] => blackhole;
	440 => envs[numParams-1].value => envs[numParams-1].target;
	100::ms => envs[numParams-1].duration;

	//Do we want discrete or continuous parameters?
	//i.e., NN or classifier?
	fun int isDiscrete() {
		return 0;
	}
	
	//The number of classes-- max-- that we want to use
	//Only relevant for discrete parameters (we have none of those)
	//Necessary for structuring OSC messages
	fun int getNumClasses() {
		return 4;
	}

	//Do we want the labels for each parameter,
	//or a distribution over all possible labels? (only relevant for discrete parameters)
	fun int useDistribution() {
		return 0;
	}

	//This is called by the main code, only once after initialization, like a constructor
	fun void setup() {
		1.0 => b.noteOn;
		spork ~smooth();
	}

	//This is also sporked in the other code
	//to define what happens when we get learned
	//parameters back-- i.e., what do we do with them?
	fun void setParams(float params[]) {
		if (params.size() >= numParams) {
			//Adjust the synthesis accordingly
			0.0 => float x;
			for (0 => int i; i < numParams; i++) {
				params[i] => x;
				if (x < 0)
					0 => x;
				if (x > 1)
					1 => x;
				x => envs[i].target;
				x => myParams[i];
			}
		}
		//NOTE: we rely on smooth() method to actually interpret these parameters musically.
	}

	//Functions particular to this synth:
	fun void smooth() {
		//Could pull this into separate methods if want to smooth 
		//different model parameters at different rates.
		while (true) {
			envs[0].value() => b.lip;
			envs[1].value() => b.slide;
			envs[2].value() * 10 => b.vibratoFreq;
			envs[3].value() => b.vibratoGain;		
			envs[4].value() => b.volume;
			envs[5].value() => b.rate;
			440 => b.freq;
		//	envs[6].value() => b.freq;
			10::ms => now;
		}
	}

	fun int getNumParams() {
		return numParams;
	}

	fun float[] getParams() {
		return myParams;
	}

	//Be quiet!
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
		new string[7] @=> string s[];
		"lip" => s[0];
		"slide" => s[1];
		"vibratoFreq" => s[2];
		"vibratoGain" => s[3];
		"volume" => s[4];
		"attackRate" => s[5];
		return s;
	}
}

