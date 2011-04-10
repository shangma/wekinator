/* Feature extractor for Jeff Snyder's Manta controller
	Use with MantaCocoa program for transmitting features here via OSC

 	Copyright 2010 Rebecca Fiebrink
 	http://wekinator.cs.princeton.edu
*/

public class CustomFeatureExtractor {
	0 => int isExtracting;
	1 => int isOK;
	53 => int numFeats;
	0 => int numKeysDown;
	1 => int isFirst;
	new float[numFeats] @=> float features[]; //store computed features in this array
	50::ms => dur defaultRate => dur rate; //optionally change

	OscRecv recv;
	57120 => recv.port;
	recv.listen();
	recv.event( "/manta/noteOff, iii" ) @=> OscEvent @ noteOff;
	recv.event( "/manta/value, iii" ) @=> OscEvent @ value;
	spork ~waitNoteOff();
	spork ~waitValue();
	
	fun void waitNoteOff() {
		int which, tmp;
		while (true) {
			noteOff => now;
			<<< "received note off">>>;
			while( noteOff.nextMsg() ) {
				noteOff.getInt() => which;
				noteOff.getInt() => tmp;
				noteOff.getInt() => tmp;
				if (which < numFeats) {
					0 => features[which];
				} else {
					<<< "feat too high: " + which>>>;
				}
			}
		}
	}

	fun void waitValue() {
		int which, area, tmp;
		while (true) {
			value => now;
			<<< "received value">>>;
			while( value.nextMsg() ) {
				value.getInt() => which;
				value.getInt() => area;
				value.getInt() => tmp;
				if (which < numFeats) {
					tmp => features[which];
				} else {
					<<< "feat too high: " + which>>>;
				}
			}
		}
	}

	fun void setup() {
		<<< "Manta setup listening">>>;
		0 => isExtracting;
		1 => isOK;
		new float[numFeats] @=> float features[];
		
		defaultRate => rate;
	}

	fun void setup(int n) {
		setup();
		if (n != numFeats) {
			0 => isOK;
			<<< "Error: Chuck & GUI don't agree on the number of features!">>>;
		}
	}
	
	fun float[] getFeatures() {
		return features;
	}

	fun int numFeatures() {
		return numFeats;
	}

	fun void extract() {
		1 => isExtracting;
		if (isFirst) {
			0 => isFirst;
		   	while (true) {
				rate => now;
			}
		}	
	}			

	fun void stop() {
		0 => isExtracting;
	}


	fun string[] getFeatureNamesArray() {
		string s[numFeats];
		for (0 => int i; i < numFeats; i++) {
			"mantaButton" + i => s[i];
		}
		return s;
	}

} //end class