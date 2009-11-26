/* Basic code for game controller / joystick using HID

 Probably no need to edit.

 Wekinator version 0.2
 Copyright 2009 Rebecca Fiebrink
 http://wekinator.cs.princeton.edu
*/

public class HidDiscoverer
{
	Hid js;
	HidMsg msg;
	int deviceID;

	public int init(int i) {
	<<< "In hd init ", i>>>;
		i => deviceID;
		return js.openJoystick(i);
	}
	new float[0] @=> float axes[];
	new int[0] @=> int buttons[];
	new int[0] @=> int hats[];
	new int[0] @=> int axesMask[]; //1 for axis used
	new int[0] @=> int buttonsMask[];
	new int[0] @=> int hatsMask[];


	//int numAxes, numButtons, numHats;
	1 => int isSetup;   
	1 => int isGo;

fun string name() {
	return js.name();
}

fun void print() {
	<<< axes.size(), "axes">>>;
	<<< buttons.size(), "buttons">>>;
	<<< hats.size(), "hats">>>;
	for (0 => int i; i < axesMask.size(); i++) {
		<<< "am",i,axesMask[i]>>>;
	}

	for (0 => int i; i < hatsMask.size(); i++) {
		<<< "hm",i,hatsMask[i]>>>;
	}
	for (0 => int i; i < buttonsMask.size(); i++) {
		<<< "bm",i,buttonsMask[i]>>>;
	}
}


fun void setup() {
<<< "STARTING SETUP!!">>>;
	new float[0] @=>  axes;
	new int[0] @=>  buttons;
	new int[0] @=>  hats;
	new int[0] @=>  axesMask; //1 for axis used
	new int[0] @=>  buttonsMask;
	new int[0] @=>  hatsMask;


	//int numAxes, numButtons, numHats;
	1 => isSetup;   
	0 => isGo;

while (isSetup) {
	js => now;
	while (js.recv(msg)) {
		//<<< "I see you">>>;
		msg.which => int id;
		if (msg.isAxisMotion()) {
			if (id >= axes.size()) {
				axes.size() => int oldSize;
				id + 1 => axes.size => axesMask.size;
				for (oldSize => int i; i < axes.size(); i++) {
					-1 => axes[i];
				}
			}
			if (axes[id] == -1.0) {
				<<< "Found new axis", id, msg.fdata>>>;
			}
			msg.fdata => axes[id];
			1 => axesMask[id];
		} else if (msg.isButtonDown() || msg.isButtonUp()) {
			<<< "button ", msg.which, msg.idata >>>;
			if (id >= buttons.size()) {
				buttons.size() => int oldSize;
				id + 1 => buttons.size => buttonsMask.size;
				<<< "old size ", oldSize, "new ", buttons.size()>>>;
				for (oldSize => int i; i < buttons.size(); i++) {
					-1 => buttons[i];
				}
			}
			if (buttons[id] == -1) {
				<<<"Found new button">>>;
			}
			msg.idata => buttons[id];
			1 => buttonsMask[id];
			<<< "buttons is " >>>;
			for (0 => int i; i < buttons.size(); i++) {
				<<< buttons[i]>>>;
			}
		} else if (msg.isHatMotion()) {
			//<<< "HAT">>>;
			if (id >= hats.size()) {
				hats.size() => int oldSize;
				id + 1 => hats.size => hatsMask.size;
				for (oldSize => int i; i < hats.size(); i++) {
					-1 => hats[i];
				}
			}
			if (hats[id] == -1) {
				<<<"Found new hat">>>;
				<<< hats[id]>>>;
			}
			msg.idata => hats[id];
			1 => hatsMask[id];
		}
	}
}
<<< "DONE WITH SETUP-- REALLY">>>;
}


//Skip the setup round and load a particular controller
//configuration.
fun void initialize(int nAxes, int nButtons, int nHats, float initAxes[], int initButtons[], int initHats[], int masks[]) {
	<<< "init: ", nAxes, nButtons, nHats, "last mask", masks[masks.size()-1]>>>;
	new float[nAxes] @=> axes;
	new int[nButtons] @=> buttons;
	new int[nHats] @=> hats;
	new int[nAxes] @=> axesMask;
	new int[nButtons] @=> buttonsMask;
	new int[nHats] @=> hatsMask;
	
	
	for (0 => int i; (i < nAxes && i < initAxes.size()); i++) {
		initAxes[i] => axes[i];
	}
	for (0 => int i; (i < nButtons && i < initButtons.size()); i++) {
		initButtons[i] => buttons[i];
	}
	for (0 => int i; (i < nHats && i < initHats.size()); i++) {
		initHats[i] => hats[i];
	}
	0 => int j;
	for (0 => int i; i < nAxes; i++) {
		masks[j] => axesMask[i];
		j++;
	}
	for (0 => int i; i < nHats; i++) {
		masks[j] => hatsMask[i];
		j++;
	}
	for (0 => int i; i < nButtons; i++) {
		masks[j] => buttonsMask[i];
		j++;
	}
}

fun int getNumAxes() {
	return axes.size();
}

fun int getNumButtons() {
	return buttons.size();
}

fun int getNumHats() {
	return hats.size();
}

//not counting masked
fun int getNumRealAxes() {
	0 => int s;
	for (0 => int i ; i < axesMask.size(); i++) {
		axesMask[i] +=> s;
	}
	return s;
}

fun int getNumRealHats() {
	0 => int s;
	for (0 => int i ; i < hatsMask.size(); i++) {
		hatsMask[i] +=> s;
	}
	return s;
}

fun int getNumRealButtons() {
	0 => int s;
	for (0 => int i ; i < buttonsMask.size(); i++) {
		buttonsMask[i] +=> s;
	}
	return s;
}

fun float[] getAxesVals() {
	return axes;
}

fun int[] getHatsVals() {
	return hats;
}

fun int[] getButtonsVals() {
	return buttons;
}

fun int[] getMask() {
	new int[axesMask.size() + hatsMask.size() + buttonsMask.size()] @=> int m[];
	0 => int j;
	for (0 => int i; i < axesMask.size(); i++) {
		axesMask[i] => m[j];
		j++;
	}
	for (0 => int i; i < hatsMask.size(); i++) {
		hatsMask[i] => m[j];
		j++;
	}
	for (0 => int i; i < buttonsMask.size(); i++) {
		buttonsMask[i] => m[j];
		j++;
	}
	return m;
}

fun void stopSetupAndStartRun() {
	0 => isSetup;
	<<< "in Stop setup and start run">>>;
	print();
	<<< "Setup results: ", axes.size(), buttons.size(), hats.size()>>>;
	for (0 => int i; i < axes.size(); i++) {
		if (axes[i] != -1.0) {
			<<< "Axis for which=" + i>>>;
		}
	}

	for (0 => int i; i < buttons.size(); i++) {
		if (buttons[i] != -1) {
			<<< "Button for which=" + i>>>;
		}
	}
	for (0 => int i; i < hats.size(); i++) {
		if (hats[i] != -1) {
			<<< "Hat for which=" + i>>>;
		}
	}
	

	run();
}

fun int numFeatures() {
	//return axes.size() + buttons.size() + hats.size();
	return getNumRealAxes() + getNumRealButtons() + getNumRealHats();
}

//must run in order to have features!
fun void run() {
	print();
	<<< "Hid running!">>>;
	1 => isGo;
  while (isGo) {
	//<<< "STILL RUNNING">>>;
	js => now;
	//<<< "GOT JS MESSAGE">>>;
	while (js.recv(msg)) {
	//	<<< "Got one">>>;
		msg.which => int id;
		if (msg.isAxisMotion()) {
			if (id < axes.size()) {
				msg.fdata => axes[id];
			//	<<<"Axis " + id + " " + axes[id]>>>;
			}
		} else if (msg.isButtonDown() || msg.isButtonUp()) {
			if (id < buttons.size()) {
				 msg.idata  => buttons[id];
			//	<<< "Button " + id + " " + buttons[id] >>>;
			}
		} else if (msg.isHatMotion()) {
			if (id <  hats.size()) {
				msg.idata => hats[id] ;
			//	<<< "Hat " + id + " " + hats[id] >>>;
			}
		}
	}
  }
}

fun void extractFeatureLoop() {
	while (true) {
		extractFeatures();
		.25::second => now;
	}	
}

fun float[] extractFeatures() {
		new  float[0] @=> float feats[];
	//	<<< "FEATURES">>>;
	 	for (0 => int i; i < axes.size(); i++) {
	//		<<< i >>>;
	//		<<< axes[i] >>>;
			if (axesMask[i]) 
				feats << axes[i];
		}
		for (0 => int i; i < buttons.size(); i++) {
	//		<<< "Button ", i>>>;
	//		<<< buttons[i] >>>;
			if (buttonsMask[i])
				feats << buttons[i]$float;
		}
		for (0 => int i; i < hats.size(); i++) {
		//	<<< "hat ", i>>>;
		//	<<< hats[i] >>>;
			if (hatsMask[i]) 
				feats << hats[i]$float;
	}
//	<<< "Done extracting">>>;
	return feats;
}

fun void stopSetup() {
	<<< "Setup stop request">>>;
	0 => isSetup;
}

fun void stop() {
	<<< "Go stopped">>>;
	0 => isGo;
}

//end of class
}

