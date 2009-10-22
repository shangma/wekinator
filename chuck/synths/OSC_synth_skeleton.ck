/* Use this as your ChucK synth class whenever your
  synth is in another environment using OSC (e.g., Max, Processing)

  To customize this for use with your own synth, see the parts 
  below marked TODO.

 Wekinator version 0.2
 Copyright 2009 Rebecca Fiebrink
 http://wekinator.cs.princeton.edu
*/

// This synth class is a proxy for the real synth, and communicates
// via OSC. 
public class SynthClass {
	0 => int isDebug;

	//Don't change this part: necessary state objects and overall envelope
	0 => int isPlayingScore;
	0 => int isSendingParams;
	1 => int hasFinished;
	50::ms => dur rate;
	int expectedParamSize;

	//TODO: edit your # of parameters (must match your OSC synth)
	3 => int numParams;

	//TODO: edit whether it's a discrete problem (1=discrete, 0=continuous)
	0 => int amIDiscrete;

	//TODO: edit whether using a distribution (for discrete only) (1=yes, 0=no)
	0 => int amIUsingDistribution;

	//TODO: edit num of classes
	2 => int myNumClasses;

	OscSend xmitSynth; //xmit that points to synth
	//TODO: Set your OSC host & port for sending to your SYNTH
	//This is the port your synth listens on
	//This CANNOT be the same as either of the ports specified in the
	//1st pane of the wekinator gui! (those are reserved for chuck-java communication)
	"localhost" => string synthHostname; //TODO: edit this if not localhost
	12000 => int synthPort; //TODO: edit this (your synth listens here)
	xmitSynth.setHost( synthHostname, synthPort );

/*** NO need to edit below this line, BUT you will want to make sure your synth is listening for
	the OSC messages below! ***/

	//Other misc. setup for OSC communication:
	string wekinatorHostname;
	int wekinatorPort;
	"" => string paramString;
	for (0 => int i; i < numParams; i++) {
		"f " +=> paramString;
	}
	if (! amIDiscrete || !amIUsingDistribution) {
		numParams => expectedParamSize;
	} else {
		numParams * myNumClasses => expectedParamSize;
	}

	//TODO: Make sure your synth is listening for this /OSCSynth/setup message
	fun void setup() {
		//Will happen 1x at beginning
		xmitSynth.startMsg("/OSCSynth/setup", "i");
		1 => xmitSynth.addInt; //better to have a dummy int for chuck's reliability		
	}

	//TODO: Make sure your synth is listening for /OSCSynth/params message
	fun void setParams(float params[]) {
		if (params.size() == expectedParamSize) {
			xmitSynth.startMsg("/OSCSynth/params", paramString);
			for (0 => int i; i < expectedParamSize; i++) {
				params[i] => xmitSynth.addFloat;
			}
			debug("Sent params to OSC synth");
		} else {
			<<< "Error in OSC_synth: Unexpected size of params: Expected ", expectedParamSize, " received ", params.size()>>>;
		}
	}

	//TODO: Make sure your synth is listening for this message
	fun void silent() {
		xmitSynth.startMsg("/OSCSynth/silent", "i");
		1 => xmitSynth.addInt; //better to have a dummy int for chuck's reliability		
	}

	//TODO: Make sure your synth is listening for this message
	fun void sound() {
		xmitSynth.startMsg("/OSCSynth/sound", "i");
		1 => xmitSynth.addInt; //better to have a dummy int for chuck's reliability		
		debug("Sent /OSCSynth/sound to OSC synth");
	}

	//Received when wekinator wants our params for playalong learning
	//TODO: make sure your synth is listening for this message
	// (IF you want it to be capable of play-along learning)
	fun void startGettingParams(OscSend x, dur r) { 
		//We want the OSC synth to send params continuously on its own
		//(We don't want to prompt it every time)
		xmitSynth.startMsg("/OSCSynth/startSendingParams", "s i f");
		xmitSynth.addString(wekinatorHostname); //host to send to
		xmitSynth.addInt(wekinatorPort); //port to send to
		xmitSynth.addFloat(r / 1::ms); //rate, in ms, of sending param stream (your synth might ignore this)
		debug("Sent /OSCSynth/startSendingParams to OSC synth");
	}

	//Received when wekinator wants us to stop sending those playalong params
	//TODO: Make sure your synth is listening for this, if you
	// want to implement play-along learning.
	fun void stopGettingParams() {
		xmitSynth.startMsg("/OSCSynth/stopParams", "i");
		xmitSynth.addInt(0);
		debug("Sent /OSCSynth/stopParams to OSC synth");
	}

	//Send current parameters directly to Wekinator, just once
	//TODO: Make sure your synth is listening for this
	fun void sendParams() {
		xmitSynth.startMsg("/OSCSynth/sendParams", "s i");
		xmitSynth.addString(wekinatorHostname); //host to send to
		xmitSynth.addInt(wekinatorPort); //port to send to
		debug("Sent /OSCSynth/sendParams to OSC synth");
	}

/** We really don't care about the rest. ------------------- */
	fun void setOscHostAndPort(string h, int p) {
		h => wekinatorHostname;
		p => wekinatorPort;
	}

	fun int getNumParams() {
		return numParams;
	}

	fun int isDiscrete() {
		return amIDiscrete; 
	}

	fun int getNumClasses() {
		return myNumClasses;
	}

	fun int useDistribution() {
		return amIUsingDistribution;
	}

	fun void debug(string s) {
		if (isDebug) {
			<<< s >>>;
		}
	}
}
