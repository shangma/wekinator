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

//***Don't change this part: Adding essential files.***/
Machine.add("core_chuck/TrackpadFeatureExtractor.ck");
Machine.add("core_chuck/MotionFeatureExtractor.ck");
Machine.add("core_chuck/AudioFeatureExtractor.ck");
Machine.add("core_chuck/HidDiscoverer.ck");
Machine.add("core_chuck/CustomOSCFeatureExtractor.ck");
Machine.add("core_chuck/ProcessingFeatureExtractor.ck");

//***The custom feature extractor you want to use***//
Machine.add("feature_extractors/keyboard_rowcol.ck");

//***The synthesis class you want to use***//
Machine.add("synths/icmc_melody_continuous.ck");

//** The play-along learner you want to use ***//
Machine.add("score_players/icmc_melody.ck");

//***The main chuck code (don't change)***//
//Always add this as the last ChucK file.
Machine.add("core_chuck/main_chuck.ck");	

//Finally run the Java component
Std.system("java -jar ../java/Wekinator/dist/Wekinator.jar &");