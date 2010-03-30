/* Controls form for the "twinkle-twinkle" ICMC playalong example.
 If you want to edit this for yourself, see "TODO" in code below.

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
	float p[2]; // my param array

	//TODO: Add any other setup here
	fun void setup(SynthClass s, OscSend x) {
		s @=> mySynth;
		x @=> xmit;
	}

	//TODO: Fill in while-loop and advance time within it
	fun void playScore() {
		mySynth.sound();
		1 => isPlaying; 
		0 => hasFinished;
		while (isPlaying) {
			Std.rand2(100,500) => p[0];
			Std.rand2(100,500) => p[1];
			mySynth.setParams(p);
			sendMessage();
			.5::second => now;
		}
		1 => hasFinished;
		//Don't silence it: if play-along, we want sound to keep happening.
	}

	//TODO: For play-along learning, you may want to periodically send a message
    //explaining how the parameters are currently being set. You might do this from your
    //playScore() code (it's never called automatically) 
	fun void sendMessage() {
		xmit.startMsg("/playAlongMessage s");
		"Playing simple random score" => string s;
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
		mySynth.silent();
		0 => isPlaying;
	}
}