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
	float p[1]; // my param array

	//Construct my score:
	[0,1,2,2,0,3,4] @=> int sectionOrders[];
	[8,8,8,8,4] @=> int sectionDurations[]; //cheating: computed from the SynthClass!
	.25::second => dur qtr; //TODO: may want to check that this is same as synth
	0 => int currentSection;

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
		0 => int i;
		sectionOrders[i] => currentSection;
		while (isPlaying) {
			currentSection => p[0];
			mySynth.setParams(p);
			sendMessage();
			//Wait:	
			sectionDurations[currentSection] * qtr => now;
			i++;
			if (i >= sectionOrders.size()) {
				0 => i;
			}
			sectionOrders[i] => currentSection;
		}
		1 => hasFinished;
		//Don't silence it: if play-along, we want sound to keep happening.
	}

	//TODO: For play-along learning, you may want to periodically send a message
    //explaining how the parameters are currently being set. You might do this from your
    //playScore() code (it's never called automatically) 
	fun void sendMessage() {
		xmit.startMsg("/playAlongMessage s");
		"Playing twinkle twinkle by section, current section is " + p[0] => string s;
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