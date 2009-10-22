/* Simple 1-voice melodic synth
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

	//Create our objects for making sound, patch to envelope
    VoicForm s => e;
	"ohh" => s.phoneme;
	440 => s.freq;	
	1 => s.noteOn;

	//Do we want discrete or continuous parameters?
	//i.e., NN or classifier?
	fun int isDiscrete() {
		return 1;
	}
	
	//The number of classes-- max-- that we want to use
	//Necessary for structuring OSC messages
	fun int getNumClasses() {
		return 24;
	}

	//Do we want the labels for each parameter,
	//or a distribution over all possible labels? (classifier only)
	fun int useDistribution() {
		return 0;
	}

	//This is called by the main code, only once after initialization, like a constructor
	fun void setup() {
		//Nothing to do
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
			if (myParams[0] > 23)
				23 => myParams[0];
			calcFreqFromParam(myParams[0]) => s.freq;
		}
	}

	//Helper method
	fun float calcFreqFromParam(float p) {
		return Std.mtof(72 + p);
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
}