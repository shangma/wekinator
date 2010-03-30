SynthClass c;
c.setup();

OscSend xmit;
ScorePlayer sp;
sp.setup(c, xmit);

sp.startScore();

20::second =>now;

sp.stopScore();
/*
c.sound();
float p[1];
3 => p[0];

c.setParams(p);


3::second => now;

1 => p[0];
c.setParams(p);
5::second => now; */