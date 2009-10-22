/* Extracts trackpad features.

 Probably no need to edit.

 Wekinator version 0.2
 Copyright 2009 Rebecca Fiebrink
 http://wekinator.cs.princeton.edu
*/
public class TrackpadFeatureExtractor {
	[0.0, 0.0] @=> float features[];
	0 => int isExtracting;
	1 => int isOK;
	0 => int mouseDevice; //may need to be 1 for some computers

	Hid hiMouse;
	HidMsg msgMouse;

	//Try to set up
	fun void setup() {
		if (! hiMouse.openMouse(mouseDevice)) {
		<<< "Error: couldn't use mouse!" >>>;
		0 => isOK;
		}
	}

	fun float[] getFeatures() {
		return features;
	}

	fun int numFeatures() {
		if (isOK)
			return 2;
		else
			return 0;
	}

	//spork this
	fun void extract() {
		if (! isExtracting) {
		  1 => isExtracting;
			while (isExtracting) {
			  hiMouse => now;
			  while (hiMouse.recv(msgMouse)) {
		 	  if (msgMouse.isMouseMotion()) {
				msgMouse.scaledCursorX => features[0];
				msgMouse.scaledCursorY => features[1];
			  }	
			  }  
		  }
	  }
	}			

	fun void stop() {
		0 => isExtracting;
	}

}