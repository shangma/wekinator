/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package KyleWrapper;

import KyleWrapper.BeatboxWekinatorWrapper.ClassifierState;
import KyleWrapper.BeatboxWekinatorWrapper.TrainingState;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author fiebrink
 */
public class WekinatorWrapperDriver {

    BeatboxWekinatorWrapper w = null;

    public static void main(String[] args) {
        WekinatorWrapperDriver d = new WekinatorWrapperDriver();
        try {
            d.test();
        } catch (InterruptedException ex) {
            Logger.getLogger(WekinatorWrapperDriver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void test() throws InterruptedException {

        int numFeats = 1;
        String[] featureNames = new String[numFeats];
        for (int i = 0; i < numFeats; i++) {
            featureNames[i] = "f" + i;
        }

        //  final BeatboxWekinatorWrapper w;
        try {
            w = new BeatboxWekinatorWrapper(numFeats, 24, featureNames);
        } catch (Exception ex) {
            Logger.getLogger(WekinatorWrapperDriver.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        //One way to implement a listener:
        w.addClassificationListener(new ClassificationListener() {

            public void fireClassificationResult(int id, int classValue) {
                System.out.println("I just received a classification result of " + classValue + " for id=" + id);
            }
        });

        //Another way to implement a listener: use a method implemented elsewhere in the class
        //Better if you're doing a lot of stuff within the method
        w.addTrainingExampleListener(new TrainingExampleListener() {

            public void fireTrainingExampleRecorded(int id, int classValue) {
                wekinatorTrainingExampleRecorded(id, classValue);
            }
        });

        //PropertychangeListener has to do some extra work to detect which property has changed.
        w.addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent pce) {
                if (pce.getPropertyName().equals(BeatboxWekinatorWrapper.PROP_CLASSIFIERSTATE)) {
                    wekinatorClassifierStateChanged(pce);
                } else if (pce.getPropertyName().equals(BeatboxWekinatorWrapper.PROP_RECORDINGSTATE)) {
                    wekinatorRecordingStateChanged(pce);
                } else if (pce.getPropertyName().equals(BeatboxWekinatorWrapper.PROP_RUNNINGSTATE)) {
                    wekinatorRunningStateChanged(pce);
                } else if (pce.getPropertyName().equals(BeatboxWekinatorWrapper.PROP_TRAININGSTATE)) {
                    wekinatorTrainingStateChanged(pce);
                }
            }
        });

        w.addSetupCompleteListener(new ChangeListener() {

            public void stateChanged(ChangeEvent ce) {
                wrapperSetupComplete();
            }
        });
    }

    private void testControlStuff() {
        try {
            w.setTrainingClassValue(0);
        
            Thread.sleep(1000);
            w.startRecordingExamples();
            System.out.println("*******RECORDING STARTED");

            Thread.sleep(5000);
            w.stopRecordingExamples();
            System.out.println("*******RECORDING STOPPED");
           /* double ids[] = w.getExampleIds();
            for (int i = 0; i < ids.length; i++) {
                System.out.print(ids[i] + ", ");
            }
            System.out.println(""); */

             
            Thread.sleep(5000);
            w.setTrainingClassValue(10);
            System.out.println("*******RECORDING STARTED");

            w.startRecordingExamples();
            Thread.sleep(5000);
            System.out.println("*******RECORDING Stopped");

            w.stopRecordingExamples();

            System.out.println("***HAVE " + w.getExampleIds().length + " EXAMPLES"); 


            w.startTraining();

            //***** BEGIN SIMULATED CHUCK FE
            //Simulate adding some examples
            //Normally this would be done by Wekinator receiving feature vectors from ChucK FE
            //while Wekinator is in the recording state
            //"Record" some examples for class 0
            /*        w.setTrainingClassValue(0);
            for (int i = 0; i < 3; i++) {
            w.newTrainingExampleRecorded(i);
            }
            //"Record" some examples for class 1
            w.setTrainingClassValue(1);
            for (int i = 0; i < 2; i++) {
            w.newTrainingExampleRecorded(i);
            }
            //***** END SIMULATED CHUCK FE
            w.stopRecordingExamples();
            w.startTraining(); //the skeleton simulates a wait of 2 seconds between train start and end
            Thread.sleep(4000);
            w.startRunning();
            //***** BEGIN SIMULATED CHUCK FE
            //Simulate the receipt of new examples to classify
            //Normally this would be done by ChucK FE sending feature vectors to Wekinator
            for (int i = 0; i < 3; i++) {
            w.newClassificationResult(i, 2);
            } */
            //***** END SIMULATED CHUCK FE
        }  catch (Exception ex) {
            System.out.println("Exception encountered in driver");
            Logger.getLogger(WekinatorWrapperDriver.class.getName()).log(Level.SEVERE, null, ex);
        }

    }


    private void wrapperSetupComplete() {
       
        System.out.println("In wrapper complete driver");


         java.awt.EventQueue.invokeLater(new Runnable() {

                    public void run() {
                       testControlStuff();
                    }
                });

       
           
        

    }

    private void wekinatorTrainingExampleRecorded(int id, int classValue) {
        System.out.println("***** Wekinator recorded new training example with classValue=" + classValue + " id=" + id);
    }

    private void wekinatorClassifierStateChanged(PropertyChangeEvent pce) {
        System.out.println("Wekinator classifier state changed from " + pce.getOldValue() + " to " + pce.getNewValue());

        ClassifierState oldState = (ClassifierState)pce.getOldValue();
        ClassifierState newState = (ClassifierState)pce.getNewValue();
        if (newState == ClassifierState.TRAINED) {
            //Start running
            w.startRunning();
        }
    }

    private void wekinatorRecordingStateChanged(PropertyChangeEvent pce) {
        System.out.println("Wekinator recording state changed from " + pce.getOldValue() + " to " + pce.getNewValue());
    }

    private void wekinatorRunningStateChanged(PropertyChangeEvent pce) {
        System.out.println("Wekinator running state changed from " + pce.getOldValue() + " to " + pce.getNewValue());
    }

    private void wekinatorTrainingStateChanged(PropertyChangeEvent pce) {
        System.out.println("****** Wekinator training state changed from " + pce.getOldValue() + " to " + pce.getNewValue());
    }
}
