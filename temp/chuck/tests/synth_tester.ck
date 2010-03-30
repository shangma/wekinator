SynthClass c;
c.setup();
c.sound();

float p[3];
[0.0, 0.0, 0.0] @=> p;

c.setParams(p);

1::second => now;

[0.0,0.1, 1.0] @=> p;
c.setParams(p);
1::second => now;

[.5,.5,0.0] @=> p;
c.setParams(p);
1::second => now;

c.silent();
1::second => now;
