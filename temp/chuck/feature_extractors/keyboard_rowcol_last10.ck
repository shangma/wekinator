/* last 10 characters typed

 Wekinator version 0.2
 Copyright 2009 Rebecca Fiebrink
 http://wekinator.cs.princeton.edu
*/

public class CustomFeatureExtractor {
	40 => int returnKey;
	0 => int isExtracting;
	1 => int isOK;
	20 => int numFeats;
	0 => int numKeysDown;
	1 => int isFirst;
	new float[numFeats] @=> float features[]; //store computed features in this array
	new float[numFeats] @=> float currentBuffer[];
	50::ms => dur defaultRate => dur rate; //optionally change

	fun void setup() {
		0 => isExtracting;
		1 => isOK;
		new float[numFeats] @=> float features[];
		setFeatures(0);
		defaultRate => rate;

		spork ~getInput();
			0 => isFirst;
		   	while (true) {
				rate => now;
			}

	}

	fun void setup(int n) {
		setup();
		if (n != numFeats) {
			0 => isOK;
			<<< "Error: Chuck & GUI don't agree on the number of features!">>>;
		}
	}
	
	fun float[] getFeatures() {
		return features;
	}

	fun int numFeatures() {
		return numFeats;
	}

	fun void extract() {
		1 => isExtracting;
		//Clear buffer
		//	for (0 => int i; i < features.size(); i++) {
		//		0 => features[i];
		//	}
	/*	if (isFirst) {
			spork ~getInput();
			0 => isFirst;
		   	while (true) {
				rate => now;
			}
		}	*/
	}			

	fun void stop() {
		0 => isExtracting;
		0 => numKeysDown;
	}

	fun void check() {
		while (true) {
			<<< "Feature is: ", features[0] , ", " , features[1]>>>;
			.1::second => now;
		}
	}

	fun void getInput() {
	  // the device number to open
	   0 => int deviceNum;

	  HidIn hi;
	  HidMsg msg;
	  // open keyboard
	  if( !hi.openKeyboard( 0 ) ) {
	    if (!hi.openKeyboard(1)) {
			<<< "ERROR OPENING KEYBOARD">>>;
		  me.exit();
	    }
 	  } 
	  // successful! print name of device
	  <<< "keyboard '", hi.name(), "' ready" >>>;

	 while( true )
	 {
	    hi => now; 
		
      while( hi.recv( msg ) )
      {
   	     if( msg.isButtonDown() )
    	    {
				setFeatures(msg.which);
				numKeysDown++;
	        }
    	    else
        	{
				numKeysDown--;
				if (numKeysDown == 0) {
					setFeatures(0);
				} else if (numKeysDown < 0) {
					0 => numKeysDown;
					setFeatures(0);
				}
    	    } //end button up
	} //while receive message

  } //while true

 } //end function

	int rows[58];
	4 => rows[29];
	4=> rows[27];
	4 => rows[6];
	4 => rows[25];
	4 => rows[5];
	4 => rows[17];
	4 => rows[17];
	4 => rows[54];
	4 => rows[55];
	4 => rows[56];

	3 => rows[4];
	3=> rows[22];
	3 => rows[7];
	3 => rows[9];
	3 => rows[10];
	3 => rows[11];
	3 => rows[13];
	3 => rows[14];
	3 => rows[15];
	3 => rows[51];
	3 => rows[52];

	2 => rows[20];
	2 => rows[26];
	2 => rows[8];
	2 => rows[21];
	2 => rows[23];
	2 => rows[28];
	2 => rows[24];
	2 => rows[12];
	2 => rows[18];
	2 => rows[19];
	2 => rows[47];
	2 => rows[48];
	2 => rows[49];

	1 => rows[30];
	1 => rows[31];
	1 => rows[32];
	1 => rows[33];
	1 => rows[34];
	1 => rows[35];
	1 => rows[36];
	1 => rows[37];
	1 => rows[38];
	1 => rows[39];
	1 => rows[45];
	1 => rows[46];
	1 => rows[42];

	int cols[58];
	1 => cols[29];
	2=> cols[27];
	3 => cols[6];
	4 => cols[25];
	5 => cols[5];
	6 => cols[17];
	7 => cols[17];
	8 => cols[54];
	9 => cols[55];
	10 => cols[56];

	1=> cols[4];
	2=> cols[22];
	3 => cols[7];
	4 => cols[9];
	5 => cols[10];
	6 => cols[11];
	7 => cols[13];
	8 => cols[14];
	9 => cols[15];
	10 => cols[51];
	11=> cols[52];

	1=> cols[20];
	2 => cols[26];
	3 => cols[8];
	4 => cols[21];
	5 => cols[23];
	6 => cols[28];
	7 => cols[24];
	8 => cols[12];
	9 => cols[18];
	10 => cols[19];
	11=> cols[47];
	12=> cols[48];
	13=> cols[49];

	1=> cols[30];
	2 => cols[31];
	3=> cols[32];
	4 => cols[33];
	5 => cols[34];
	6 => cols[35];
	7 => cols[36];
	8 => cols[37];
	9 => cols[38];
	10 => cols[39];
	11=> cols[45];
	12=> cols[46];
	13=> cols[42];

	fun void setFeaturesOld(int which) {
		if (which == 0 || which >= rows.size() || which >= cols.size()) {
			0 => features[0] => features[1];
		} else {
			rows[which] => features[0];
			cols[which] => features[1];
		}

	}

	fun void setFeatures(int which) {
		0 => int thisrow;
		0 => int thiscol;
	//	<<< "Which: ", which >>>;
		if (which == returnKey) {
			<<< "Current buffer pushed to features">>>;
			for (0 => int i; i < features.size(); i++) {
				currentBuffer[i] => features[i];
				0 => currentBuffer[i];
			}
		}
		else if (which == 0 || which >= rows.size() || which >= cols.size()) {
			//0 => features[0] => features[1];
		} else {
			rows[which] => thisrow;
			cols[which] => thiscol;

			//Shift feats back
			for (features.size() => int i; i >= 4; i--) {
				currentBuffer[i-3] => currentBuffer[i-1];
				currentBuffer[i-4] => currentBuffer[i-2];
			}
			thisrow => currentBuffer[0];
			thiscol => currentBuffer[1];

	/*			for (0 => int i; i < features.size()-2; i+2 => i) {
				<<<"f", i, "- ", i+1, ": ", features[i] + "," + features[i+1] >>>;
			} */
		}
		
		//test
		//

	}

	fun string[] getFeatureNamesArray() {
		string s[numFeats];
		for (0 => int i; i < numFeats-1; i+2 => i) {
			"Char_" + i*.5 + "_row" => s[i];
			"Char+" + i*.5 + "_col" => s[i+1];
		}
		return s;
	}

} //end class