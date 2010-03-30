/* K-bow feature extractor

 Wekinator version 0.1
 Copyright 2009 Rebecca Fiebrink
 http://wekinator.cs.princeton.edu
*/

public class CustomFeatureExtractor {
	0 => int isExtracting;
	1 => int isOK;

	string s[8];
	"x" => s[0];
	"y" => s[1];
	"z" => s[2];
	"hair" => s[3];
	"grip" => s[4];
	"length" => s[5];
	"bridge" => s[6];
	"tilt" => s[7];

	s.size() => int numFeats; //change this to your # of features
	
	
	new float[numFeats] @=> float features[]; //store computed features in this array

	OscEvent oscEvents[s.size()];
   
    100::ms => dur defaultRate => dur rate; //optionally change

	OscRecv recv;
	9001 => recv.port; //optionally change

	fun void setup() {
		0 => isExtracting;
		1 => isOK;

		new float[numFeats] @=> float features[];
		defaultRate => rate;
		for (0 => int i ; i < features.size(); i++) {
			0.0 => features[i];
		}

        recv.listen();
		for (0 => int i; i < s.size(); i++) {
			recv.event(s[i], "i") @=> oscEvents[i];
		}

        while (true) {
            1::hour => now;
        }
        
	}

	fun string[] getFeatureNamesArray() {
		return s;
	}

	//Setup and specify # features Java wants
	fun void setup(int n) {
		setup();
	}

	fun void computeFeatures() {
		//put results in features array: nothing to do here
	}
	
	fun float[] getFeatures() {
		return features;
	}

	fun int numFeatures() {
		return numFeats;
	}

	fun void extract() {
		if (! isExtracting) {
		  	1 => isExtracting;
			oscCustomFeaturesWait();
		 }
	}			

	fun void stop() {
		0 => isExtracting;
	}

	fun void oscCustomFeaturesWait() {
		for (0 => int i; i < s.size(); i++) {
			spork ~waitFeatureI(i);
		}

		while (isExtracting) {
			.1::second => now;				
		}
	}

	fun void waitFeatureI(int i) {
        float f;
		while (isExtracting) { // while (isExtracting)
			oscEvents[i] => now;
			while (oscEvents[i].nextMsg() != 0) {
					<<< "Got feature " , i , "; OSC is working">>>;
					oscEvents[i].getInt() => features[i];
			}
		}
	}
		
}