/* Controls play-along for icmc processing animation example.

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
	[75.0, 325.0, 50.0] @=> float p[]; // my param array

	200.0 => float pixelsPerSecond;
	1.0 / pixelsPerSecond * 1::second => dur secsPerPic;
	


	//TODO: Add any other setup here
	fun void setup(SynthClass s, OscSend x) {
		s @=> mySynth;
		x @=> xmit;
	}

	//TODO: Fill in while-loop and advance time within it
	fun void playScore() {
		mySynth.sound(); //RAF: need to fix score so it stops in the middle if necessary.
		1 => isPlaying; 
		0 => hasFinished;
		75.0 => float p1 => p[0];
		325.0 => float p2 => p[1];
		50.0 => float p3 => p[2];
		while (isPlaying) {
			while (p2 > 75.0) {
				1 -=> p2;
				p2 => p[1];
				mySynth.setParams(p);
				secsPerPic => now;
			}

			while (p1 < 325.0) {
				1 +=> p1;
				p1 => p[0];
				mySynth.setParams(p);
				secsPerPic=> now;
			}

			while (p2 < 325) {
				1 +=> p2;
				p2 => p[1];
				mySynth.setParams(p);
				secsPerPic => now;
			}

			while (p1 > 75.0) {
				1 -=> p1;
				p1 => p[0];
				mySynth.setParams(p);
				secsPerPic => now;
			}

			while (p1 < 200) {
				1 +=> p1;
				1 -=> p2;
				p1 => p[0];
				p2 => p[1];
				mySynth.setParams(p);
				secsPerPic => now;
			}

			while (p3 < 200) {
				1 +=> p3;
				p3 => p[2];
				mySynth.setParams(p);
				secsPerPic => now;	
			}

			while (p3 > 50) {
				1 -=> p3;
				p3 => p[2];
				mySynth.setParams(p);
				secsPerPic => now;	
			}

			while (p1 > 75) {
				1 -=> p1;
				1 +=> p2;
				p1 => p[0];
				p2 => p[1];
				mySynth.setParams(p);
				secsPerPic => now;
			}

		}
		1 => hasFinished;
		//Don't silence it: if play-along, we want sound to keep happening.
	}

	//TODO: For play-along learning, you may want to periodically send a message
    //explaining how the parameters are currently being set. You might do this from your
    //playScore() code (it's never called automatically) 
	fun void sendMessage() {
		xmit.startMsg("/playAlongMessage s");
		"Moving ball around. Watch it!" => string s;
		xmit.addString(s);
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