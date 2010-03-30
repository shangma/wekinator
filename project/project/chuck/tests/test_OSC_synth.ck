/* An example of implementing a synth that communicates
  via OSC instead of using ChucK's infrastructure

 To run, add this code to the VM along with the regular wekinator stuff.

 If you make a synth in Max/MSP, Processing, etc., your synth
	there should listen for the same messages as this one.

  Make sure you actually run this file if you're trying out
  the example (but not normally)

 Wekinator version 0.2
 Copyright 2009 Rebecca Fiebrink
 http://wekinator.cs.princeton.edu
*/

12000 => int recvPort; 
2 => int numParams;

OscRecv recv;
recvPort => recv.port;

OscSend xmit;
0 => int isSendingParams;
50::ms => dur rate;
Envelope e => dac;
.1::second => e.duration;
0 => e.target => e.value;
e.keyOn();


//Set up receiver
recv.listen();
"f f" => string paramString;
recv.event("/OSCSynth/params", paramString) @=> OscEvent oscParams;
recv.event("/OSCSynth/setup", "i") @=> OscEvent oscSetup;
recv.event("/OSCSynth/silent", "i") @=> OscEvent oscSilent;
recv.event("/OSCSynth/sound", "i") @=> OscEvent oscSound;
recv.event("/OSCSynth/startSendingParams", "s i f") @=> OscEvent oscStartSendingParams;
recv.event("/OSCSynth/stopParams", "i") @=> OscEvent oscStopParams;
recv.event("/OSCSynth/sendParams", "s i") @=> OscEvent oscSendParams;


spork ~oscParamsWait();
spork ~oscSetupWait();
spork ~oscSilentWait();
spork ~oscSoundWait();
spork ~oscStartSendingParamsWait();
spork ~oscStopParamsWait();
spork ~oscSendParamsWait();

SinOsc s1 => e;
SinOsc s2 => e;

while (true) {
	1::hour => now;
}

fun void oscParamsWait() {
	//<<< "Waiting for params">>>;
	while (true) {
		oscParams => now;
		while (oscParams.nextMsg() != 0) {
			oscParams.getFloat() => s1.freq;
			oscParams.getFloat() => s2.freq;
		}
		<<< "Got OSC params">>>;
	}
}

fun void oscSetupWait() {
	while (true) {
		oscSetup => now;
		while (oscSetup.nextMsg() != 0) {
			oscSetup.getInt();
		}
		<<< "Got OSC setup" >>>;
	}
}

fun void oscSilentWait() {
	while (true) {
		oscSilent => now;
		while (oscSilent.nextMsg() != 0) {
			oscSilent.getInt();
		}
		<<< "Got OSC silent" >>>;
		0 => e.target;
	}
}

fun void oscSoundWait() {
	while (true) {
		oscSound => now;
		while (oscSound.nextMsg() != 0) {
			oscSound.getInt();
		}
		<<< "Got OSC sound">>>;
		1 => e.target;
	}
}
fun void oscStartSendingParamsWait() {
	string s;
	int i;
	float f;
	while (true) {
		oscStartSendingParams => now;
		<<< "Received osc start sending params">>>;
		while (oscStartSendingParams.nextMsg() != 0) {
			oscStartSendingParams.getString() => s;
			oscStartSendingParams.getInt() => i;
			oscStartSendingParams.getFloat() => f;
		}
		//TODO: start sending
		xmit.setHost(s, i);
		f * 1::ms => rate;
		1 => isSendingParams;
		spork ~sendParamsLoop();
	}
}

	//Send those parameters on at a specified rate
fun void sendParamsLoop() {
	while (isSendingParams) {
		sendParams();
		rate => now;
	}
}

fun void oscStopParamsWait() {
	while (true) {
		oscStopParams => now;
		while (oscStopParams.nextMsg() != 0) {
			oscStopParams.getInt();
		}
		//TODO: stop sending
		<<< "Got OSC stop params">>>;
		0 => isSendingParams;
	}
}

fun void sendParams() {
	"/realValue f" => string ss;
	/*1 => int i;
	for (1 => i; i < numParams; i++) {
		ss + " f" => ss;
	}
	xmit.startMsg(ss);
	//for (0 => i; i < 2; i++) {
		xmit.addFloat(myParams[i]); //Add all params, each in its own addFloat message.
	}*/
	xmit.startMsg("/realValue f f");
	xmit.addFloat(s1.freq());
	xmit.addFloat(s2.freq());
}

fun void oscSendParamsWait() {
	string s;
	int i;
	while (true) {
		oscSendParams => now;
		while (oscSendParams.nextMsg() != 0) {
			oscSendParams.getString() => s;
			oscSendParams.getInt() => i;
		}
		<<< "Got OSC send params">>>;
		xmit.setHost(s, i);
		sendParams();
	}
}
