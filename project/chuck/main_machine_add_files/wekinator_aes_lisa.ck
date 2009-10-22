/* This starts the ChucK component of the Wekinator.
 This should be the only chuck file you have to run, as it
 automatically adds all the components.

 Do not change the order of these files.
 You may have to change your miniAudicle working
 directory or add absolute path names to these files (see
 instructions on the wiki).

 You may want to change the synthesis and feature extraction
 modules used, as specified below.

 Wekinator version 0.2
 Copyright 2009 Rebecca Fiebrink
 http://wekinator.cs.princeton.edu
*/

//***Don't change this part.***/
Machine.add("TrackpadFeatureExtractor.ck");
Machine.add("MotionFeatureExtractor.ck");
Machine.add("AudioFeatureExtractor.ck");
Machine.add("HidDiscoverer.ck");
Machine.add("CustomOSCFeatureExtractor.ck");
Machine.add("vowel_feature_extractor.ck");
Machine.add("ProcessingFeatureExtractor.ck");
Machine.add("vowel_feature_extractor.ck");


//***The custom feature extractor you want to use***//
//If you're not using custom features, just use CustomFeatureExtractor.ck
// (keep the line below) 
//and specify in the GUI that you're not using custom features.
//Machine.add("FeatureExtractorSingleKeyRowCol.ck");
//Alternatively, if you want to try out our example custom chuck
//feature extractor, comment out the machine.add line above
//and uncomment out this line:
//Machine.add("CustomFeatureExtractorCentroid.ck"); 



//***The synthesis class you want to use***//
//Uncomment ONE of the following lines to test in some
//very simple built-in synthesis methods
//Machine.add("synthesis_with_playalong_1.ck");
//Machine.add("synthesis_with_playalong_physical");
//Machine.add("synth_sin_simple_pitchonly_NN.ck");
//Machine.add("synth_sin_simple_form.ck");
//Machine.add("synth_physmod_NNb.ck");
//Machine.add("synth_OSC_example_playalong");
//Machine.add("synth_beats_playalong.ck");
Machine.add("synth_voice_lisa.ck");
//Machine.add("synth_twinkle_simple_form.ck");
//Machine.add("synth_playalong_melody_NN.ck");
//Machine.add("synth_playalong_melody_discrete.ck");

//Machine.add("synth_OSC_stub"); //If you're using an external synth and communiating via OSC

//Note: If you're using synth_OSC and want to try out a
//Chuck synthesis class as if it were an external synth,
//add synth_OSC_stub above and uncomment this line:
//Machine.add("synth_OSC_example.ck");


//***The main chuck code (don't change)***//
//Always add this LAST in your file.
Machine.add("main_chuck.ck");	


//Std.system("open ../processing_code/downsampled_webcam/application.macosx/downsampled_webcam.app/ &");
Std.system("java -jar ../dist/Wekinator.jar &");