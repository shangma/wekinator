/* Skeleton feature extractor
  Will show up as "custom" feature(s) in GUI
  If you're not using custom features, include this file as-is
  Or to create your own feature extractor, start by editing
  this file. You will need to edit numFeats and all 
  functions with "TODO"

 Wekinator version 0.2
 Copyright 2009 Rebecca Fiebrink
 http://wekinator.cs.princeton.edu
*/

public class CustomFeatureExtractor {
	0 => int isExtracting;
	1 => int isOK;
	0 => int numFeats; //TODO: change this to your # of features
	new float[numFeats] @=> float features[]; //store computed features in this array
	100::ms => dur defaultRate => dur rate; //optionally change: rate at which features are extracted & sent

	//TODO: Try to set up; set 0=>isOK if any problems happen
	fun void setup() {
		0 => isExtracting;
		1 => isOK;
		new float[numFeats] @=> float features[];
		defaultRate => rate;
	}

	//TODO: Fill in function for computing features
	fun void computeFeatures() {
		//put results in features array
	}
	
/*** Shouldn't have to edit anything beyond this point **/
	//Setup and specify # features Java wants
	fun void setup(int n) {
		setup();
		if (n != numFeats) {
			0 => isOK;
			<<< "Error: we don't agree on the number of features!">>>;
		}
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
			while (isExtracting) {
				computeFeatures();			
				rate => now;
		 }
	   }
	}			

	fun void stop() {
		0 => isExtracting;
	}

	//TODO: return an array of your feature names
	fun string[] getFeatureNamesArray() {
		string s[numFeats];
		for (0 => int i; i < numFeats; i++) {
			"Feature_" + i => s[i];
		}
		return s;
	}

} //end class