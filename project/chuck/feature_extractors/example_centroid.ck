/* This is an example of how you might edit
   CustomFeatureExtractor.ck to make your own
   feature extractor in ChucK.

  For demo purposes only. (This isn't really useful, 
  since we have a centroid feature extractor built-in already.)

 Wekinator version 0.2
 Copyright 2009 Rebecca Fiebrink
 http://wekinator.cs.princeton.edu
*/

public class CustomFeatureExtractor {
	//Keep this part: Keeps track of setup and run state, also
	//stores features in a nicely named features[] array
	0 => int isExtracting;
	1 => int isOK;
	1 => int numFeats; //change this to your # of features
	new float[numFeats] @=> float features[]; //store computed features in this array
	
	//TODO: Optionally change the rate at which features are extracted
	//This may not correspond to rate at which they are polled.
	100::ms => dur defaultRate => dur rate;

	//TODO: create any custom objects here:
	adc => FFT fft => blackhole;
	fft =^ Centroid c;
	UAnaBlob b;

	//TODO: Fill in function for computing features
	fun void computeFeatures() {
		c.upchuck() @=> b;
		b.fval(0) => features[0];
	}

	//TODO: Any necessary setup work here
	//would set 0 => isOK if any problems happen.
	fun void setup() {
		0 => isExtracting;
		1 => isOK;
		new float[numFeats] @=> float features[];
		defaultRate => rate;
	}
	
/*** Shouldn't have to edit anything beyond this point **/

	//Calls setup, also checks that we agree on # featuress
	fun void setup(int n) {
		setup();
		if (n != numFeats) {
			0 => isOK;
			<<< "Error: we don't agree on the number of features!">>>;
		}
	}

	//Return the features
	fun float[] getFeatures() {
		return features;
	}

	fun int numFeatures() {
		return numFeats;
	}

	//Extraction loop, given user-specified functions above
	fun void extract() {
		if (! isExtracting) {
		  1 => isExtracting;
			while (isExtracting) {
				computeFeatures();			
				rate => now;
		 }
	   }
	}			

	//Stop extracting
	fun void stop() {
		0 => isExtracting;
	}
}