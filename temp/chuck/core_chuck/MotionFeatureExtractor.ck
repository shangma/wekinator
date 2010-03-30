/** Extracts built-in motion sensor features (Mac only)

 Probably no need to edit.

 Wekinator version 0.2
 Copyright 2009 Rebecca Fiebrink
 http://wekinator.cs.princeton.edu
*/

public class MotionFeatureExtractor {
	[0.0, 0.0, 0.0] @=> float features[];
	0 => int isExtracting;
	1 => int isOK;
	0 => int mouseDevice; //may need to be 1 for some computers
	100::ms => dur rate;

	Hid hiMotion;
	HidMsg msgMotion;

	//Try to set up
	fun void setup() {
		if (! hiMotion.openTiltSensor()) {
			<<< "Error: Couldn' use tilt sensor!" >>>;
			0 => isOK;
	   }
	}

	fun void setup(dur r) {
		r => rate;
		setup();
	}

	fun float[] getFeatures() {
		return features;
	}

	fun int numFeatures() {
		if (isOK)
			return 3;
		else
			return 0;
	}

	//spork this
	fun void extract() {
		if (! isExtracting) {
		  1 => isExtracting;
			while (isExtracting) {
				rate => now;
			hiMotion.read( 9, 0, msgMotion );
			msgMotion.x => features[0];
			msgMotion.y => features[1];
			msgMotion.z => features[2];
		 }
		  
	   }
	}			

	fun void stop() {
		0 => isExtracting;
	}

}