/* Feature extractor for processing
  
 No need to edit this file.

 Wekinator version 0.2
 Copyright 2009 Rebecca Fiebrink
 http://wekinator.cs.princeton.edu
*/

public class ProcessingFeatureExtractor {
	0 => int isExtracting;
	1 => int isOK;
	0 => int numFeats; 
	float features[]; //store computed features in this array
	OscEvent oscProcessingFeatures;
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
			"i" => string s;
			for (1 => int i; i < numFeats; i++) {
				s + " i" => s;
			}	
			//<<< "My string is ", s >>>;
			recv.event("/processingFeatures", s) @=> oscProcessingFeatures;
			//spork ~oscProcessingFeaturesWait();
		}
	}

	/*fun void go() {
		<<< "Going!">>>;
		1 => isExtracting;
		spork ~oscProcessingFeaturesWait();
		
	} */

	fun void extract() {
		if (! isExtracting) {
		  	1 => isExtracting;
			oscProcessingFeaturesWait();
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

	//TODO: implement ability to communicate start/stop to processing

	//Get features as they come in
	fun void oscProcessingFeaturesWait() {
		//<<< "WAITING FOR PROCESSING FEATURE">>>;

		while (isExtracting) {
			//<<< "Wait here">>>;
			oscProcessingFeatures => now;
			//<<< "Received processing feats!">>>;
			while (oscProcessingFeatures.nextMsg() != 0) {
				for (0 => int i; i < numFeats; i++) {
					oscProcessingFeatures.getInt() => int nextVal; //TODO: change to float
					nextVal$float => features[i];
				}
			}
		}
	}

}