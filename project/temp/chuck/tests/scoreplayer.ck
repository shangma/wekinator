public class ScorePlayer {
	SynthClass mySynth;
	[0,2,4,0,4,0,4,2,4,5,4,2,5,4,5,7,4,7,4,7,5,7,9,7,5,9,7,0,2,4,5,7,9,9,2,4,5,7,9,11,11,4,5,7,9,11,12,11,9,7,2,11,7,12] @=> int melodyNotes[];
	[3,1,3,1,2,2,4,3,1,2,1,1,8,3,1,3,1,2,2,4,3,1,2,1,1,8,3,1,1,1,1,1,8,3,1,1,1,1,1,8,3,1,1,1,1,1,6,1,1,2,2,2,2,8] @=> int melodyBeats[];
	.3::second => dur qtr;
	0 => int isPlaying;
	1 => int hasFinished;
	float p[1];

	fun void setup(SynthClass s) {
		s @=> mySynth;
	}

	fun void startScore() {
		if (!isPlaying && hasFinished) {
		spork ~playScore();
		} else {
			//The playScore() while loop is still executing.
			1 => isPlaying;
			mySynth.sound();
		}
	}

	fun void playScore() {
		0 => int note;
			mySynth.sound();
			1 => isPlaying; 
			0 => hasFinished;
			while (isPlaying) {
				<<< "here">>>;
			melodyNotes[note]=>p[0];
			mySynth.setParams(p);			
			mySynth.sendMessage();
			melodyBeats[note] * qtr => now;
			(note + 1) % melodyNotes.size() => note;
			}
			1 => hasFinished;
			//Don't silence it: if play-along, we want sound to keep happening.
	}

	fun void stopScore() {
		mySynth.silent();
		0 => isPlaying;
	}

}