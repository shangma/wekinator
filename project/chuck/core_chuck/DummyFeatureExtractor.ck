/*  This is run when no chuck custom feature extractor is selected.

 	Copyright 2011 Rebecca Fiebrink
 	http://wekinator.cs.princeton.edu
*/

public class CustomFeatureExtractor {
	0 => int isExtracting;
	1 => int isOK;
	0 => int numFeats;
	1 => int isFirst;
	new float[numFeats] @=> float features[];

	fun void setup() {
		0 => isExtracting;
		1 => isOK;
		new float[numFeats] @=> float features[];
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
	}			

	fun void stop() {
		0 => isExtracting;
	}

	fun void check() {
	}

	fun string[] getFeatureNamesArray() {
		string s[0];
		//"dummy" => s[0];
		return s;
	}

} //end class