/* Dummy synth - does nothing
   No need to edit this -- all settings are specified via the GUI.

   //TODO: Fix so that host & port are taken from GUI-driven settings, not hardcoded
*/

// This synth class is a proxy for the real synth, and communicates
// via OSC. 
public class SynthClass {
	0 => int isDebug;
	OscSend xmitSynth;
	"localhost" => string synthHostname; //TODO: edit this if not localhost
	12000 => int synthPort; //TODO: edit this (your synth listens here)
	xmitSynth.setHost( synthHostname, synthPort );

	//Don't change this part: necessary state objects and overall envelope
	0 => int isPlayingScore;
	0 => int isSendingParams;
	1 => int hasFinished;
	50::ms => dur rate;
	int expectedParamSize;

	1 => int numParams;

	0 => int paramStringSet;
	"" => string paramString;

	//TODO: edit whether it's a discrete problem (1=discrete, 0=continuous)
	1 => int amIDiscrete;

	//TODO: edit whether using a distribution (for discrete only) (1=yes, 0=no)
	0 => int amIUsingDistribution;

	//TODO: edit num of classes
	2 => int myNumClasses;

//Stuff to implement:
/*
	/OSCSynth/setup i : what does this do?  when mc.setup() called.
	/OSCSynth/params f+ : sets params -- when setParams( ) called. *** used. // called a lot
	/OSCSynth/silent i : sets to silent (i always 1) : when silent() called
	/OSCSynth/sound i : turns sound on (i always 1) : when sound() called
	/OSCSynth/startSendingParams s i f : s is wekinator hostname, i is wekinator port, f is rate of sending. (Used with playalong score)
	/OSCSynth/stopParams i : stops getting params (i always 0) - when stopGettingParams() called - w/ playalong score.
*/

	fun void setup() {
		//<<< "OSC SYNTH PROXY SETUP CALLED">>>;
	}

	fun void setParams(float params[]) {
		if (!paramStringSet || params.size() != numParams) {
			params.size() => numParams;
			for (0 => int i; i < params.size(); i++) {
				"f " +=> paramString;
			}
			1 => paramStringSet;
		}
		
			<<< "Send to synth:", params[0], paramString >>>;
			xmitSynth.startMsg("/OSCSynth/params", paramString);
			for (0 => int i; i < params.size(); i++) {
				params[i] => xmitSynth.addFloat;
				//<<< params[i]>>>;
			}
			
			debug("Sent params to OSC synth");
		
	}

	fun void silent() {
	xmitSynth.startMsg("/OSCSynth/silent", "i");
		1 => xmitSynth.addInt; //better to have a dummy int for chuck's reliability		
	
	}

	fun void sound() {
		xmitSynth.startMsg("/OSCSynth/sound", "i");
		1 => xmitSynth.addInt; //better to have a dummy int for chuck's reliability		
		debug("Sent /OSCSynth/sound to OSC synth");
	
	}

	fun void startGettingParams(OscSend x, dur r) { 
		<<< "OSC SYNTH PROXY START GETTING PARAMS CALLED">>>;

	}

	fun void stopGettingParams() {
		<<< "OSC SYNTH PROXY STOP GETTING PARAMS CALLED">>>;

	}

//TODO: Can get rid of sendParams!

/** We really don't care about the rest. ------------------- */

	//TODO: get rid of this
	fun void setOscHostAndPort(string h, int p) {
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
