SynthClass sc;
OscSend sender;
50::ms => dur rate;

sc.setup();

sc.sound();


[75.0, 325.0, 50.0] @=> float p[];

sc.setParams(p);
200.0 => float pixelsPerSecond;
1.0 / pixelsPerSecond * 1::second => dur secsPerPic;


325.0 => float p2;

while (true) {
while (p2 > 75.0) {
	1 -=> p2;
	p2 => p[1];
	sc.setParams(p);
	secsPerPic => now;
}

75.0 => float p1;

while (p1 < 325.0) {
	1 +=> p1;
	p1 => p[0];
	sc.setParams(p);
	secsPerPic=> now;
}

while (p2 < 325) {
	1 +=> p2;
	p2 => p[1];
	sc.setParams(p);
	secsPerPic => now;
}

while (p1 > 75.0) {
	1 -=> p1;
	p1 => p[0];
	sc.setParams(p);
	secsPerPic => now;
}

while (p1 < 200) {
	1 +=> p1;
	1 -=> p2;
	p1 => p[0];
	p2 => p[1];
	sc.setParams(p);
	secsPerPic => now;
}

p[2] => float p3;
while (p3 < 200) {
	1 +=> p3;
	p3 => p[2];
	sc.setParams(p);
	secsPerPic => now;	
}

while (p3 > 50) {
	1 -=> p3;
	p3 => p[2];
	sc.setParams(p);
	secsPerPic => now;	
}

while (p1 > 75) {
	1 -=> p1;
	1 +=> p2;
	p1 => p[0];
	p2 => p[1];
	sc.setParams(p);
	secsPerPic => now;
}

}


/*
sc.startGettingParams(sender, rate);
sc.stopGettingParams();
sc.sendParams();

1::second => now;

sc.silent();

1::second => now; */
