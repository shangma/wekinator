SynthClass c;
c.setup();
c.sound();

float p[1];
[0.] @=> p;

c.setParams(p);

10::second => now;

