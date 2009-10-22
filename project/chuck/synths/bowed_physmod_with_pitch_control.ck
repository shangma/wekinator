/* This controls a bowed string instrument physical model
	Use the keyboard to control pitch (learning only affects timbral parameters, not pitch)

 Wekinator version 0.2
 Copyright 2009 Rebecca Fiebrink
 http://wekinator.cs.princeton.edu
*/

//The synth always lives in a SynthClass definition
public class SynthClass {
	//Necessary state objects and overall envelope
	OscSend xmit;
	0 => int isPlayingScore;
	0 => int isSendingParams;
	1 => int hasFinished;
	50::ms => dur rate;
	Envelope e => dac;
	.1::second => e.duration;
	0 => e.target => e.value;
	e.keyOn();

	//3 parameters here, though could change this easily
	3 => int numParams;
	float myParams[numParams];

	//base and register
	12 => int base;
	3 => int register;
	0 => int reg_change;

	// keyboard
	HidIn kb;
	// hid message
	HidMsg msg;

	// open
	if( !kb.openKeyboard( 0 ) ) me.exit();
	<<< "Ready?", "" >>>;



	// key map
	int key[256];
	// key and pitch
	0 => key[29];
	1 => key[27];
	2 => key[6];
	3 => key[25];
	4 => key[5];
	5 => key[4] => key[17];
	6 => key[22] => key[16];
	7 => key[7] => key[54];
	8 => key[9] => key[55];
	9 => key[10] => key[56];
	10 => key[20] => key[11];
	11 => key[26] => key[13];
	12 => key[8] => key[14];
	13 => key[21] => key[15];
	14 => key[23] => key[51];
	15 => key[28] => key[52];
	16 => key[24];
	17 => key[12];
	18 => key[18];
	19 => key[19];
	20 => key[47];
	21 => key[48];
	22 => key[49];
	// which is current
	0 => int current;

	//The synthesis patch
	Bowed b => e;
	440 => b.freq;
	Envelope envs[numParams];
	for (0 => int i; i < numParams; i++) {
	// new Envelope @=> envs[i];
		envs[i] => blackhole;
		.5 => envs[i].value;
		100::ms => envs[i].duration;
	}

	//Do we want discrete or continuous parameters?
	//i.e., NN or classifier?
	fun int isDiscrete() {
		return 0;
	}
	
	//The number of classes-- max-- that we want to use
	//Necessary for structuring OSC messages
	fun int getNumClasses() {
		return 4;
	}

	//Do we want the labels for each parameter,
	//or a distribution over all possible labels? (classifier only)
	fun int useDistribution() {
		return 0;
	}

	//TODO: Any other setup code that should be called
	//This is called by the main code, only once after initialization, like a constructor
	fun void setup() {
		1.0 => b.noteOn;
		spork ~smooth();
		spork ~getKeys();
	}

	//This is also sporked in the other code
	//to define what happens when we get learned
	//parameters back-- i.e., what do we do with them?
	fun void setParams(float params[]) {
		if (params.size() >= numParams) {		
			//Adjust the synthesis accordingly
			0.0 => float x;
			for (0 => int i; i < numParams; i++) {
				params[i] => x;
				if (x < 0)
					0 => x;
				if (x > 1)
					1 => x;
				x => envs[i].target;
				x => myParams[i];
			}
		}
		//NOTE: we rely on smooth() method to actually interpret these parameters musically.
	}

	//Functions particular to this synth:
	fun void smooth() {
		//Could pull this into separate methods if want to smooth 
		//different model parameters at different rates.
		while (true) {
			envs[0].value() => b.bowPosition;
			envs[0].value() => b.vibratoGain;
			envs[1].value() * 10 => b.vibratoFreq;
			envs[2].value() => b.bowPressure;		
			10::ms => now;
		}
	}

	fun void registerUp()
	{
    	if( register < 6 ) { register++; 1 => reg_change; }
	    <<< "register:", register >>>;
	}

	fun void registerDown()
	{
	    if( register > 0 ) { register--; 1 => reg_change; }
    	<<< "register:", register >>>;
	}

	fun void getKeys() {
		while( true )
		{
    		// wait for event
		    kb => now;

		    // get message
		    while( kb.recv( msg ) )
		    {
		        // which
        		if( msg.which > 256 ) continue;
		        if( key[msg.which] == 0 && msg.which != 29 )
        		{
		            // register
        		    if( msg.which == 80 && msg.isButtonDown() )
                		registerDown();
		            else if( msg.which == 79 && msg.isButtonDown() )
        		        registerUp();
		        }
        		// set
		        else if( msg.isButtonDown() )
        		{
        		    base + register * 12 + key[msg.which] => Std.mtof => b.freq;
		        }
    		}
		}
	}


/* PROBABLY don't need to change anything below this line ----------------------------*/
/* See modification in icmc_twinkle_form_control.ck for implementing custom sound on/off 
behavior, beyond master envelope control.*/
	fun int getNumParams() {
		return numParams;
	}

	fun float[] getParams() {
		return myParams;
	}

	//Be quiet! If you want to improve efficiency here, you could also stop
	//other processing
	fun void silent() {
		0 => e.target;
	}

	//Make sound!
	fun void sound() {
		1 => e.target;
	}

	//Received when wekinator wants our params for playalong learning
	fun void startGettingParams(OscSend x, dur r) { //Q: Where does this go?? Here or in recording? Or neither-- put in main?
		x @=> xmit;
		r => rate;
		1 => isSendingParams;
		spork ~sendParamsLoop();
	}

	//Send those parameters on at a specified rate
	fun void sendParamsLoop() {
		while (isSendingParams) {
			sendParams();
			rate => now;
		}
	}

	//Received when wekinator wants us to stop sending those playalong params
	fun void stopGettingParams() {
		0 => isSendingParams;
	}

	//Send current parameters directly to Wekinator
	fun void sendParams() {
		"/realValue f" => string ss;
		1 => int i;
		for (1 => i; i < numParams; i++) {
			ss + " f" => ss;
		}
		xmit.startMsg(ss);
		for (0 => i; i < numParams; i++) {
			xmit.addFloat(myParams[i]); //Add all params, each in its own addFloat message.
		}
	}

	//If OSC synth, we need to instruct the synth how to get back to ChucK
	fun void setOscHostAndPort(string h, int p) {
		//no need to do anything, unless you're using an OSC synth like Processing or Max.
	}
}
