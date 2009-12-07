/* Vowel feature extractor for Wekinator

 Wekinator version 0.2
 Copyright 2009 Rebecca Fiebrink
 http://wekinator.cs.princeton.edu
*/

public class CustomFeatureExtractor {
	0 => int isExtracting;
	1 => int isOK;
	5 => int numFeats; //change this to your # of features
	new float[numFeats] @=> float features[]; //store computed features in this array
	50::ms => dur defaultRate => dur rate; //optionally change

	//Custom objects
	adc => FFT f => blackhole;
	UAnaBlob b;

	//Set up bin stuff
	1024 => int FFT_SIZE;
	FFT_SIZE => f.size;
	Windowing.hamming(512) => f.window;

	1::second / 1::samp => float SR;
	SR/FFT_SIZE => float bin_width;

	(200/bin_width)$int => int bin200;
	(400/bin_width)$int + 1 => int bin400;
	float totalEnergy200to400;

	(600/bin_width)$int + 1 => int bin600;
	float totalEnergy400to600;

	(800/bin_width)$int => int bin800;
	(1200/bin_width)$int + 1 => int bin1200;
	float totalEnergy800to1200;

	(2200/bin_width)$int => int bin2200;
	(2600/bin_width)$int + 1 => int bin2600;
	float totalEnergy2200to2600;

	(3000/bin_width)$int => int bin3000;
	(3500/bin_width)$int + 1 => int bin3500;
	float totalEnergy3000to3500;

	//TODO: Try to set up; set 0=>isOK if any problems happen
	fun void setup() {
		0 => isExtracting;
		1 => isOK;
		new float[numFeats] @=> float features[];
		defaultRate => rate;
	}

	//Setup and specify # features Java wants
	fun void setup(int n) {
		setup();
		if (n != numFeats) {
			0 => isOK;
			<<< "Error: we don't agree on the number of features!">>>;
		}
	}

	//TODO: Fill in function for computing features
	fun void computeFeatures() {
		f.upchuck() @=> b;
		0 => totalEnergy200to400;
		0 => totalEnergy800to1200;	
		0 => totalEnergy400to600;
		0 => totalEnergy2200to2600;
		0 => totalEnergy3000to3500;	

		for	 (bin200 => int i; i <= bin400; i++) {
			totalEnergy200to400 + b.fval(i) => totalEnergy200to400;
		}
		for (bin400 => int i; i <= bin600; i++) {
			totalEnergy400to600 + b.fval(i) => totalEnergy400to600;
		}
		for (bin800 => int i; i <= bin1200; i++) {
			totalEnergy800to1200 + b.fval(i) => totalEnergy800to1200;
		}
		for (bin2200 => int i; i <= bin2600; i++) {
			totalEnergy2200to2600 + b.fval(i) => totalEnergy2200to2600;
		}
		for (bin3000 => int i; i <= bin3500; i++) {
			totalEnergy3000to3500 + b.fval(i) => totalEnergy3000to3500;
		}	

		totalEnergy200to400 => features[0];
		totalEnergy400to600 => features[1];
		totalEnergy800to1200 => features[2];
		totalEnergy2200to2600 => features[3];
		totalEnergy3000to3500 => features[4];
	}
	
/*** Shouldn't have to edit anything beyond this point **/
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
	fun string[] getFeatureNamesArray() {
		string s[numFeats];
		for (0 => int i; i < numFeats; i++) {
			"Bin_" + i => s[i];
		}
		return s;
	}

} //end class