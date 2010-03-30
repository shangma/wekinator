/* Feature extractor for receiving custom OSC features
 from any source

  Don't need to edit this file.

 OSC process will need to send message "/oscCustomFeatures" 
 with string of floats equal to value of n provided to 
 setup()

 Wekinator version 0.2
 Copyright 2009 Rebecca Fiebrink
 http://wekinator.cs.princeton.edu
*/

public class CustomOSCFeatureExtractor {
	Event received;
	0 => int isExtracting;
	1 => int isOK;
	0 => int numFeats; 
	float features[]; //store computed features in this array
	OscEvent oscCustomFeatures;
	OscRecv recv;

	//Setup and specify # features
	fun void setup(int n, OscRecv r) {
		0 => isExtracting;
		1 => isOK;
		r @=> recv;
		n => numFeats;
		new float[numFeats] @=> features;
		for (0 => int i ; i < features.size(); i++) {
			0.0 => features[i];
		}
		if (numFeats >0) {
			"f" => string s;
			for (1 => int i; i < numFeats; i++) {
				s + " f" => s;
			}	
			recv.event("/oscCustomFeatures", s) @=> oscCustomFeatures;
		}
	}

	fun void extract() {
		//<<< "ee">>>;
		if (! isExtracting) {
			//<<< "Here">>>;
		  	1 => isExtracting;
			oscCustomFeaturesWait();
		 }
	   }

	fun void stop() {
		0 => isExtracting;
	}

	fun float[] getFeatures() {
		return features;
	}

	fun int numFeatures() {
		return numFeats;
	}		

	//TODO: implement ability to communicate start/stop to OSC

	//Get features as they come in
	fun void oscCustomFeaturesWait() {
		while (isExtracting) {
			//<<< "Waiting for osc feature">>>;
			oscCustomFeatures => now;
			while (oscCustomFeatures.nextMsg() != 0) {
				for (0 => int i; i < numFeats; i++) {
					oscCustomFeatures.getFloat() => features[i];
				}
			}
			//<<< "Got OSC features">>>;
			received.broadcast();
		}
	}

}