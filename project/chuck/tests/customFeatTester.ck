CustomFeatureExtractor ex;

ex.setup();

spork ~ex.extract();

while (true) {
	ex.getFeatures() @=> float feats[];
	if (feats[0] > 0) {
	<<< "GOT FEATURES: ">>>;
	for (0 => int i; i < feats.size(); i++) {
		<<< "     feature ", i, " is ", feats[i]>>>;
	}
	}
	.1::second => now;
}