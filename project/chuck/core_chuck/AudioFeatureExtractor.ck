/* Extracts standard audio features

 No need to edit.

 Wekinator version 0.2
 Copyright 2009 Rebecca Fiebrink
 http://wekinator.cs.princeton.edu
*/

public class AudioFeatureExtractor {
	float features[0];
	0 => int isExtracting;
	1 => int isOK;
	0 => int useFFT => int useRMS => int useCentroid => int useFlux => int fftSize => int windowSize => int windowType;
	0 => int numFeats;	
	100::ms => dur rate;
	FeatureCollector fc;
	UAnaBlob b;

	FFT fft;
	RMS rms;
	Centroid centroid;
	Flux flux;
	RollOff rolloff;

	//Try to set up
	fun void setup() {
		adc => fft => blackhole;
		fft =^ rms;
		fft =^ centroid;
		fft =^ rolloff;
		fft =^ flux;
		
		fft =< fc;
		rms =< fc;
		centroid =< fc;
		rolloff =< fc;
		flux =< fc;

		0 => numFeats;
		100::ms => rate;
	}

	fun void setup(int useFFT, int useRMS, int useCentroid, int useRolloff, int useFlux, int fs, int ws, int wt, dur r) {
		setup();
		<<< "Use fft? ", useFFT >>>;
	
		if (useFFT) {
			fft =^ fc;
			numFeats + fs/2 => numFeats;
		}
		if (useRMS) {
			rms =^ fc;
			numFeats + 1 => numFeats;
		}
		if (useCentroid) {
			centroid =^ fc;
			1 +=> numFeats;
		}
		if (useRolloff) {
			rolloff =^ fc;
			1 +=> numFeats;
		}
		if (useFlux) {
			flux =^ fc;
			1 +=> numFeats;
		}
		fs => fft.size;
		if (wt == 0) {
			Windowing.rectangle(ws) => fft.window;
			<<< "Rectangle, ", ws >>>;
		} else if (wt == 1) {
			Windowing.triangle(ws) => fft.window;
			
		} else if (wt == 2) {
			Windowing.hann(ws) => fft.window;
			<<< "Hann, ", ws>>>;
		} else if (wt == 3) {
			Windowing.hamming(ws) => fft.window;
			<<< "Hamming ", ws >>>;
		} else {
			Windowing.blackmanHarris(ws) => fft.window;
		}
		r => rate;
		<<< "Num feats ", numFeats>>>;
	}

	fun float[] getFeatures() {
		return b.fvals();
	}

	fun int numFeatures() {
		if (isOK)
			return numFeats;
		else
			return 0;
	}

	//spork this
	fun void extract() {
		if (! isExtracting) {
		  1 => isExtracting;
			while (isExtracting) {
				fc.upchuck() @=> b;
				rate => now;
		 }
		  
	   }
	}			

	fun void stop() {
		0 => isExtracting;
	}

}