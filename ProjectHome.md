# Welcome! #
Wekinator is a system for enabling real-time interaction with machine learning. It piggybacks on the Weka machine learning library and uses a hodgepodge of tools written in Java, ChucK, and Processing, glued together via Open Sound Control. The initial intent is for use in music performance and composition, though Wekinator can be used in many other realtime domains, such as gaming and interactive video.

This project is a product of academic research and therefore guaranteed to be a bit buggy. Contact Rebecca Fiebrink (fiebrink@princeton.edu) for more information.

# Instructions #
Instructions for downloading and running: http://wiki.cs.princeton.edu/index.php/ChucK/Wekinator/Instructions

Project homepage: http://wekinator.cs.princeton.edu/ (Includes description, publications, etc.)

For help, please email the Wekinator users mailing list (see link to the left).

# What is the Wekinator? #
## A system for patching input "features" to output "control parameters" ##

The Wekinator is, at heart, a system for patching input features to output parameters. Typically, your inputs will be gestural control signals (like an accelerometer indicating laptop tilt, or a joystick) or audio signals (like the audio captured from an acoustic flute player playing into a mic). The "features" are numbers describing the state of your inputs at a particular point in time. For example, for a joystick, we'd use a feature vector indicating whether each button is pressed and the current position of each axis. For audio, the raw audio samples are typically too complex a representation for use as a control input; instead we try to choose features that describe relevant aspects of the audio signal, such as the spectral centroid (which describes timbre).

At the other end of the system, you've got some parameters that you can plug into some piece of code that probably makes sound (or video, or something else, if you want), which we'll call your "synth." These parameters control your synth over time. For example, if you want your synth to play a melody, one parameter might control the pitch over time. Or if your synth is a drum machine, one parameter might control tempo, and another might control the number of loops currently playing.

![http://wiki.cs.princeton.edu/images/5/5b/Wekinatorsystem.jpg](http://wiki.cs.princeton.edu/images/5/5b/Wekinatorsystem.jpg)


## Example-based learning ##
You, the user, specify the relationship from input features to output parameters by supplying the Wekinator with a bunch of example input/output pairs. For example, if you want to use the Wiimote accelerometer to control the pitch of a sound, you'd provide training  examples of Wiimote positions/gestures along with the corresponding synth parameters. The Wekinator's job is to learn a model of the relationship between features and parameters, which will produce parameter outputs for new inputs (even those that may be different from the training examples).

The picture below illustrates how a supervised learning algorithm can create a webcam-based hand gesture labeler, using a training set consisting of example gestures (inputs) and labels (outputs). The trained model is capable of applying appropriate labels to new hand gestures.

![http://wiki.cs.princeton.edu/images/2/2a/SupervisedLearning.jpg](http://wiki.cs.princeton.edu/images/2/2a/SupervisedLearning.jpg)