/* 	This synth plays twinkle, twinkle
   	Its single parameter indicates the section to play
   	This was part of ICMC 2009 demo
 
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

	//Twinkle-specific stuff:
	//Encode sections of piece
	//Relative pitches for each section:
	[0,0,7,7,9,9,7] @=> int notes1[];
	[5,5,4,4,2,2,0] @=> int notes2[];
	[7,7,5,5,4,4,2] @=> int notes3[];
	[5,5,4,4,2,7,12] @=> int notes4[];
	[0] @=> int notes5[];
	[0,1,2,2,0,3,4] @=> int sectionOrders[];
	//Relative beat durations for each section:
	[1,1,1,1,1,1,2] @=> int beats1[];
	beats1 @=> int beats2[];
	beats1 @=> int beats3[];
	beats1 @=> int beats4[];
	[4] @=> int beats5[];
	.25::second => dur qtr;
	//Keep track of where we are:
	1 => int currentSection;
	0 => int isSilent;
	Event startPlaySection;

	//Finally, the synthesis patch:
    ModalBar s => Envelope ee => e;
	440 => s.freq;	
	.01::second => ee.duration;
	1 => ee.target => ee.value;
	ee.keyOn();
	1 => s.noteOn;

	//Do we want discrete or continuous parameters?
	//i.e., NN or classifier?
	fun int isDiscrete() {
		return 1;
	}
	
	//The number of classes-- max-- that we want to use
	//Necessary for structuring OSC messages
	fun int getNumClasses() {
		return 5;
	}

	//Do we want the labels for each parameter,
	//or a distribution over all possible labels? (classifier only)
	fun int useDistribution() {
		return 0;
	}

	//This is called by the main code, only once after initialization, like a constructor
	fun void setup() {
		spork ~playSection();
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
		}
	}

	fun float calcFreqFromParam(float p) {
		return Std.mtof(60 + p);
	}
	
	fun void playSection() {
  	  	while (true) {
			if (isSilent) {
				startPlaySection => now;
			}
			myParams[0]$int => currentSection;
			int thesenotes[];
			int thesebeats[];
			0 => int thisnote => int thisbeat;

			if (currentSection == 4) {
				0 => ee.target; //rest!
				1 => s.noteOff;
				beats5[0] * qtr => now;
			} else {
				1 => ee.target;
				if (currentSection == 0) {
					notes1 @=> thesenotes;
					beats1 @=> thesebeats;
				} else if (currentSection == 1) {
					notes2 @=> thesenotes;
					beats2 @=> thesebeats;
				} else if (currentSection == 2) {
					notes3 @=> thesenotes;
					beats3 @=> thesebeats;
				} else if (currentSection == 3) {
					notes4 @=> thesenotes;
					beats4 @=> thesebeats;
				} 	

				for (0 => int noteIndex; noteIndex < thesenotes.size(); noteIndex++) {
					thesebeats[noteIndex] => thisbeat;
					thesenotes[noteIndex] => thisnote;
					Std.mtof(thisnote + 72) => s.freq;			
					1 => s.noteOn;
					thisbeat*qtr => now;
				}
			} //end check for note or rest
	  	} //end while true
	}

	fun int getNumParams() {
		return numParams;
	}

	fun float[] getParams() {
		return myParams;
	}

	//Be quiet!
	//Note the use of "isSilent" here, in order to stop other code during silence
	fun void silent() {
		0 => e.target;
		1 => isSilent;
	}

	//Make sound!
	//NOTE: This uses an event broadcast when we've stopped being silent,
	//so that the appropriate next section can start playing from the beginning.
	fun void sound() {
		1 => e.target;
		0 => isSilent;
		startPlaySection.broadcast();
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
		"Section#" => s[0];
		return s;
	}

}

