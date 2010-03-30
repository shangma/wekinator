/* FM synth by Jeff Snyder, wekinator mod by Rebecca Fiebrink
	//this is an FM synthesis class.
    //it is a sawtooth wave, which is frequency modulated by a sine wave
    //which then gets put through a low-pass filter
    //and has an amplitude envelope

	//This one is "always on" -- no note triggering with keyboard
    
    //parameters for this class are:
    //0 = midinote pitch of Sawtooth oscillator (carrier freq)
    //1 = lowpass filter cutoff frequency
    //2 = Master gain (carrier gain)
    //3 = fm oscillator midinote pitch (modulator freq)
    //4 = fm oscillator index (modulator index)
*/

//The synth always lives in a SynthClass definition
public class SynthClass {
	OscSend xmit;
	0 => int isSendingParams;	
	50::ms => dur rate;

 50 => float attack;
    500 => float decay;
    1. => float sustainlevel;
    50 => float release;
    SinOsc fmosc => SawOsc s => LPF lpf => Envelope vol => dac;
    2000 => lpf.freq;
   // d.set(attack::ms, decay::ms, sustainlevel, release::ms);
	2 => s.sync;

	5::ms => vol.duration;
    
    5 => int numParams;
	float myParams[numParams];


	50 => Std.mtof => s.freq;
    
	Envelope envs[numParams];
	for (0 => int i; i < numParams; i++) {
		envs[i] => blackhole;
		.5 => envs[i].value;
		10::ms => envs[i].duration;
	}	

	
	//TODO: Any other setup code that should be called
	//This is called by the main code, only once after initialization, like a constructor
	fun void setup() {
		spork ~smooth();
	}

	//TODO: Are your parameters discrete (integers) or continuous (real numbers)?
	//This determines the learning algorithms available in the GUI
	fun int isDiscrete() {
		return 1; //Return 1 for discrete, 0 for continuous
	}

	fun int useDistribution() {
		return 0;
	}

	fun int getNumClasses() {
		return 2;
	}

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

	fun void smooth() {
		while (true) {
            (envs[0].value() * 100 + 20) => Std.mtof => s.freq;
            (envs[1].value() * 10000) + 1 => lpf.freq;
            envs[2].value() => s.gain;
            (envs[3].value() * 100 + 20.)=> Std.mtof => fmosc.freq;
            (envs[4].value() * 400) => fmosc.gain;
           /* (envs[5].value() * 1000) => attack;
            (envs[6].value() * 1000) => decay;
            envs[7].value() => sustainlevel;
            (envs[8].value() * 1000) => release;
            d.set(attack::ms, decay::ms, sustainlevel, release::ms ); */
			10::ms => now;
		}
	}

	//Turn note off
	/*fun void noteOff() {
        d.keyOff();
	}

	//Turn note on
	fun void noteOn() {
        d.keyOn();
	} */

	//Be quiet!
	fun void silent() {
        vol.keyOff();
	}

	//Turn volume on!
	fun void sound() {
		//<<< "SOUUUUUUUND">>>;
        vol.keyOn();
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

	//Received when wekinator wants our params for playalong learning
	fun void startGettingParams(OscSend x, dur r) { 
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

/*** Copy & Paste below at end of your code to add new functions as of 12/6/09 ***/
	fun int[] useDistributionArray() {
		new int[numParams] @=> int a[];
		for (0 => int i; i < numParams; i++) {
			0 => a[i];
		}
		return a;
	}

	fun int[] isDiscreteArray() {
		new int[numParams] @=> int a[];
		for (0 => int i; i < numParams; i++) {
			0 => a[i];
		}

		return a;
	}

	fun int[] getNumClassesArray() {
		new int[numParams] @=> int a[];
		for (0 => int i; i < numParams; i++) {
			2 => a[i];
		}
		return a;
	}
	
	fun string[] getParamNamesArray() {
		new string[numParams] @=> string s[];
		"carrier_pitch" => s[0];
		"lpf_cutoff" => s[1];
		"carrier_gain" => s[2];
		"osc_pitch" => s[3];
		"osc_index" => s[4];
		
		return s;
	}
	
}