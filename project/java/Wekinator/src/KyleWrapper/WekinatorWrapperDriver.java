/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package KyleWrapper;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author fiebrink
 */
public class WekinatorWrapperDriver {

    BeatboxWekinatorWrapper w = null;

    //For this driver:
    protected int currentSetToRecord = 0; // a set ID #; when wekinator receives a new feature vector in RECORDING mode, it stores it with this set
    //Note: Using synchronized version here -- matters if it's possible that 2 threads could be modifying example sets, ids, or selection simultaneously
    protected Map<Integer, Set<Integer>> exampleSets = Collections.synchronizedMap(new HashMap<Integer, Set<Integer>>()); //map from set ID to example IDs
    protected Map<Integer, Integer> exampleIDs = Collections.synchronizedMap(new HashMap<Integer, Integer>()); //map from example ID to set ID
    protected Map<Integer, Integer> selectedSetsAndClasses = Collections.synchronizedMap(new HashMap<Integer, Integer>()); //IDs of sets that are selected, and the class ID to be used for training examples of that set

    public static void main(String[] args) {
        WekinatorWrapperDriver d = new WekinatorWrapperDriver();
        try {
            d.test();
        } catch (InterruptedException ex) {
            Logger.getLogger(WekinatorWrapperDriver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void test() throws InterruptedException {

        //Kyle: Change # features
        int numFeats = 1;

        try {
            // max # classes
            w = new BeatboxWekinatorWrapper(numFeats, 100);
            //w = BeatboxWekinatorWrapper.loadFromFile(new File("/Users/rebecca/tmp.wek"));
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
            public void fireTrainingExampleRecorded(int id) {
                wekinatorTrainingExampleRecorded(id);
            }
        });

        //PropertychangeListener has to do some extra work to detect which property has changed.
        w.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent pce) {
                if (pce.getPropertyName().equals(BeatboxWekinatorWrapper.PROP_RECORDINGSTATE)) {
                    wekinatorRecordingStateChanged(pce);
                } else if (pce.getPropertyName().equals(BeatboxWekinatorWrapper.PROP_RUNNINGSTATE)) {
                    wekinatorRunningStateChanged(pce);
                }
            }
        });

        testControlStuff();
    }

    private void testControlStuff() {
        try {

            currentSetToRecord = 0; //Put new examples in set 0 from now on
            System.out.println("RECORD IN 2 SECONDS");
            Thread.sleep(2000);
            w.startRecordingExamples();
           
            System.out.println("*******RECORDING STARTED");

            Thread.sleep(500);
            w.stopRecordingExamples();
            System.out.println("*******RECORDING STOPPED");

            /*Set<Integer> keys = exampleIDs.keySet();
            int id1 = 0;
            int id2 = 0;
            for (Integer k : keys) {
                if (id1 == 0)
                    id1 = k;
                else if (id2 == 0) {
                    id2 = k;
                    break;
                }
            }
            System.out.println("Using ids " + id1 + "," + id2); */

            for (int id : exampleIDs.keySet()) {
                w.addTrainingExampleToActiveClassifier(id, 0);
            }
           

            //Now delete:
             /* System.out.println("Deleting id" + id1);
            w.deleteTrainingExample(id1);
            System.out.println("Deleting id " + id2);
            w.deleteTrainingExample(id2);
            System.out.println("ANything?"); */

            exampleIDs.clear(); //TAKE THIS OUT LATER

            Thread.sleep(2000);
            currentSetToRecord = 1; //Put new examples in set 1
            System.out.println("*******RECORDING STARTED");
            w.startRecordingExamples();
            Thread.sleep(500);
            System.out.println("*******RECORDING Stopped");
            w.stopRecordingExamples();

            for (int id : exampleIDs.keySet()) {
                w.addTrainingExampleToActiveClassifier(id, 0);
            }

            /*System.out.println("Adding ids " + id1 + "," + id2);
            w.addTrainingExampleToActiveClassifier(id1, 2);
            w.addTrainingExampleToActiveClassifier(id2, 2); */



          /*  Thread.sleep(2000);
            currentSetToRecord = 2; //Put new examples in set 2
            System.out.println("*******RECORDING STARTED");
            w.startRecordingExamples();
            Thread.sleep(500);
            System.out.println("*******RECORDING Stopped");
            w.stopRecordingExamples(); */

            System.out.println("***HAVE " + w.getExampleIds().length + " EXAMPLES");


          //  int[] setIDs = {0, 1, 2}; //Which training examples will be used?
          //  int[] classIDs = {0, 1, 2}; //What class values will be associated with them?

            //Comment if only adding incrementally:
         //   setSelectedExampleSetsAndClasses(setIDs, classIDs);



            System.out.println("Classify without holdout: ");
          /*  System.out.println(id1 + ": " + w.classifyExampleWithoutHoldout(id1));
            System.out.println(id2 + ": " + w.classifyExampleWithoutHoldout(id2)); */

           /* System.out.println("Neighbor list:");
            int[] tmp1 = w.getNearestNeighborClassesForInstance(id1);
            System.out.print(id1 + ": ");
            for (int i = 0; i < tmp1.length; i++) {
                System.out.print(tmp1[i] + " ");
            }
            System.out.println("");

            int[] tmp2 = w.getNearestNeighborClassesForInstance(id2);
            System.out.print(id2 + ": ");
            for (int i = 0; i < tmp2.length; i++) {
                System.out.print(tmp2[i] + " ");
            }
            System.out.println("");  */

            //Run and classify with current configuration
            w.startRunning();
            Thread.sleep(8000);
            w.stopRunning();
          //  w.saveWekinatorToFile(new File("/Users/rebecca/tmp.wek"));
           // System.out.println("Saved");
           // 
            w.disconnectOSC();


        }  catch (Exception ex) {
            System.out.println("Exception encountered in driver");
            Logger.getLogger(WekinatorWrapperDriver.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    // Choose the example sets to use for classification, and specify the class value that should correspond to each example ID
    // requires setIDs and class values same length and ordering
    private void setSelectedExampleSetsAndClasses(int[] setIDs, int[] classes) {
        selectedSetsAndClasses = new HashMap<Integer, Integer>();
        if (setIDs == null || classes == null || setIDs.length != classes.length) {
            return; //error boo
        }

        for (int i = 0; i < setIDs.length; i++) {
           selectedSetsAndClasses.put(setIDs[i], classes[i]);
        }

        //Could do this in fixed length array but must be synchronized w/ lock on sets
        updateWekinatorSelectedSetsAndClasses();
    }

    private void updateWekinatorSelectedSetsAndClasses() {
        if (selectedSetsAndClasses == null || exampleSets == null) {
            return;
        }
        List<Integer> exampleList = new LinkedList<Integer>();
        List<Integer> classList = new LinkedList<Integer>();

        for (Integer setID : selectedSetsAndClasses.keySet()) {
            if (exampleSets.containsKey(setID)) {
            for (Integer ex : exampleSets.get(setID)) {
                exampleList.add(ex);
                classList.add(selectedSetsAndClasses.get(setID));
            }
            }
        }
        int[] exampleArray = new int[exampleList.size()];
        int[] classArray = new int[classList.size()];
        int i = 0;
        for (Integer ex : exampleList) {
            exampleArray[i] = ex;
            i++;
        }
        i = 0;
        for (Integer c : classList) {
            classArray[i] = c;
            i++;
        }
        try {
            w.setSelectedExamplesAndClasses(exampleArray, classArray); //This is what triggers new classifier to be built
        } catch (Exception ex) {
            System.out.println("Doh! Could not build a classifier for some reason.");
            ex.printStackTrace();
            return;
        }
    }


    private boolean isExampleSetSelected(int setID) {
        return selectedSetsAndClasses.containsKey(setID);
    }

    private void deleteExampleSet(int setId) {
        //Manage my objects:
        Set<Integer> deletedIDs = exampleSets.get(setId);
        //Delete from examples
        for (Integer id : deletedIDs) {
            exampleIDs.remove(id);
        }
        //Delete from sets
        exampleSets.remove(setId);

        //Must inform Wekinator - might be part of active classifier!
        if (isExampleSetSelected(setId)) {
            selectedSetsAndClasses.remove(setId);
            updateWekinatorSelectedSetsAndClasses();
        }
    }

    private void deleteExampleID(int id) {
        //Manage my objects:
        Integer setID = exampleIDs.get(id);
        if (setID != null) {
            exampleIDs.remove(id); //remove from list of example ids
            exampleSets.get(setID).remove(id); //get set containing this example, and remove reference to it
        }

        //Inform wekinator
        w.deleteTrainingExample(id); //FOR NOW, wekinator will by default look through current training set and delete and always retrain, if necessary
        //Alternatively, could explicitly trigger retraining here...
    }

    private void deleteExampleIDs(int[] ids) {
        //This is more efficient than deleting 1 at a time; e.g. wekinator will only have to retrain 1x at end
        for (int i = 0; i < ids.length; i++) {
            Integer setID = exampleIDs.get(ids[i]);
            if (setID != null) {
                exampleIDs.remove(ids[i]); //remove from list of example ids
                exampleSets.get(setID).remove(ids[i]); //get set containing this example, and remove reference to it
            }
        }
        w.deleteTrainingExamples(ids); //Currently will look through active training set and delete and retrain, if necessary
    }

    private void wekinatorTrainingExampleRecorded(int id) {
        if (currentSetToRecord == -1) {
            System.out.println("Wekinator recorded new training example with id=" + id + ", throwing it out because no set is currently selected for recording");
            return;
        }

        else if (! exampleSets.containsKey(currentSetToRecord)) {
            exampleSets.put(currentSetToRecord, new HashSet<Integer>()); //Create a new example set with the current ID
        }

        exampleSets.get(currentSetToRecord).add(id); //add the ID to the appropriate set
        exampleIDs.put(id, currentSetToRecord); //also enable set lookup by example ID

        System.out.println("***** Wekinator recorded new training example with id=" + id + ", put in Example Set " + currentSetToRecord);
    }

    private void wekinatorRecordingStateChanged(PropertyChangeEvent pce) {
        System.out.println("Wekinator recording state changed from " + pce.getOldValue() + " to " + pce.getNewValue());
    }

    private void wekinatorRunningStateChanged(PropertyChangeEvent pce) {
        System.out.println("Wekinator running state changed from " + pce.getOldValue() + " to " + pce.getNewValue());
    }
}
