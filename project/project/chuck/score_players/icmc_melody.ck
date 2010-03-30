/* Simple example score player for "Do, a deer"
 Works with icmc_melody_disrete and icmc_melody_continuous examples
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
	float p[1]; // my param array

	//Construct my score:
	[0,2,4,0,4,0,4,2,4,5,4,2,5,4,5,7,4,7,4,7,5,7,9,7,5,9,7,0,2,4,5,7,9,9,2,4,5,7,9,11,11,4,5,7,9,11,12,11,9,7,2,11,7,12] @=> int melodyNotes[];
	[3,1,3,1,2,2,4,3,1,2,1,1,8,3,1,3,1,2,2,4,3,1,2,1,1,8,3,1,1,1,1,1,8,3,1,1,1,1,1,8,3,1,1,1,1,1,6,1,1,2,2,2,2,8] @=> int melodyBeats[];
	.3::second => dur qtr;

	//TODO: Add any other setup here
	fun void setup(SynthClass s, OscSend x) {
		s @=> mySynth;
		x @=> xmit;
	}

	//TODO: Fill in while-loop and advance time within it
	fun void playScore() {
		0 => int note;
		mySynth.sound();
		1 => isPlaying; 
		0 => hasFinished;
		while (isPlaying) {
			melodyNotes[note]=>p[0];
			mySynth.setParams(p);			
			sendMessage();
			melodyBeats[note] * qtr => now;
			(note + 1) % melodyNotes.size() => note;
		}
		1 => hasFinished;
			//Don't silence it: if play-along, we want sound to keep happening.
	}

	//TODO: For play-along learning, you may want to periodically send a message
    //explaining how the parameters are currently being set. You might do this from your
    //playScore() code (it's never called automatically) 
	fun void sendMessage() {
		xmit.startMsg("/playAlongMessage s");
		"Do... a deer... learning notes! current note # is " + p[0] => string s;
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