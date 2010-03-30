/* Does a random walk through continuous parameter space.
 If you want to edit this for yourself, see "TODO" in code below.
 Usable with icmc_bowed_physmod, for example.

 Wekinator version 0.2
 Copyright 2009 Rebecca Fiebrink
 http://wekinator.cs.princeton.edu
*/

public class ScorePlayer {
	//Required:
	SynthClass mySynth;
	OscSend xmit; //For transmitting messages to GUI
	0 => int isPlaying;
	1 => int hasFinished;
	3 => int numParams; //Can change this for different # of params.
	float p[numParams]; // my param array

	1::second => dur waitTime;

	//TODO: Add any other setup here
	fun void setup(SynthClass s, OscSend x) {
		s @=> mySynth;
		x @=> xmit;
	}

	//TODO: Fill in while-loop and advance time within it
	fun void playScore() {
		//start at a random place.
		for (0 => int i; i < numParams; i++) {
			Std.rand2f(0.0, 1.0) => p[i];
		}
		mySynth.sound();
		1 => isPlaying; 
		0 => hasFinished;
		int i, j;
		1 => int sign;
		0.0 => float val;
		while (isPlaying) {
			for (0 => i; (i < numParams && isPlaying); i++) {
			//  for (0 => j; j < 5; j++) { //take 5 steps for each param
				1 => sign;
				if (maybe) { -1 => sign; }
				Std.rand2f(0, .3) * sign + p[i] => val;
				fix(val) => p[i];
				mySynth.setParams(p);
				sendMessage();
				1.5::second => now;
			//  } 
			}
		}
		1 => hasFinished;
		//Don't silence it: if play-along, we want sound to keep happening.
	}

	fun float fix(float f) {
		if (f < 0)
			0.0 => f;
		if (f > 1) 
			1.0 => f;
		return f;
	}

	//TODO: For play-along learning, you may want to periodically send a message
    //explaining how the parameters are currently being set. You might do this from your
    //playScore() code (it's never called automatically) 
	fun void sendMessage() {
		xmit.startMsg("/playAlongMessage s");
		"Random walk of " + numParams + " parameters; current params are " => string s;
		for (0 => int i; i < numParams; i++) {
			s + " " + p[i] => s;
		}
		xmit.addString(s);
		<<< s >>>;
	}


	//Shouldn't need to edit this
	fun void startScore() {
		if (!isPlaying && hasFinished) {
			spork ~playScore();
		} else {
			//The playScore() while loop is still executing.
			1 => isPlaying;
			mySynth.sound();
		}
	}

	//Shouldn't need to edit this
	fun void stopScore() {
		//mySynth.silent();
		0 => isPlaying;
	}
}