/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package KyleWrapper;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
public class WekinatorWrapperDriverNew {

    BeatboxWekinatorWrapperNew w = null;

    //Temp for this skeleton:
    protected int currentSetToRecord = 0; // a set ID #

    protected Map<Integer, Set<Integer>> exampleSets = Collections.synchronizedMap(new HashMap<Integer, Set<Integer>>()); //map from set ID to example IDs
    protected Map<Integer, Integer> exampleIDs = Collections.synchronizedMap(new HashMap<Integer, Integer>()); //map from example ID to set ID
    protected Map<Integer, Integer> selectedSetsAndClasses = Collections.synchronizedMap(new HashMap<Integer, Integer>()); //IDs of sets that are selected, and the class ID to be used for training examples of that set
   // protected HashMap<Integer, Set<Integer>> classifierIDs = new HashMap<Integer, Set<Integer>>(); //map from classifier ID to set IDs

    public static void main(String[] args) {
        WekinatorWrapperDriverNew d = new WekinatorWrapperDriverNew();
        try {
            d.test();
        } catch (InterruptedException ex) {
            Logger.getLogger(WekinatorWrapperDriverNew.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void test() throws InterruptedException {

        int numFeats = 1;
       
        try {
            //Kyle: Change # features, max # classes
            w = new BeatboxWekinatorWrapperNew(numFeats, 100);
        } catch (Exception ex) {
            Logger.getLogger(WekinatorWrapperDriverNew.class.getName()).log(Level.SEVERE, null, ex);
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
        w.addTrainingExampleListener(new TrainingExampleListenerNew() {
            public void fireTrainingExampleRecorded(int id) {
                wekinatorTrainingExampleRecorded(id);
            }
        });

        //PropertychangeListener has to do some extra work to detect which property has changed.
        w.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent pce) {
                if (pce.getPropertyName().equals(BeatboxWekinatorWrapperNew.PROP_RECORDINGSTATE)) {
                    wekinatorRecordingStateChanged(pce);
                } else if (pce.getPropertyName().equals(BeatboxWekinatorWrapperNew.PROP_RUNNINGSTATE)) {
                    wekinatorRunningStateChanged(pce);
                } 
            }
        });
    }

    private void testControlStuff() {
        try {

            currentSetToRecord = 0;
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
            currentSetToRecord = 2;

            System.out.println("*******RECORDING STARTED");

            w.startRecordingExamples();
            Thread.sleep(5000);
            System.out.println("*******RECORDING Stopped");

            w.stopRecordingExamples();

            System.out.println("***HAVE " + w.getExampleIds().length + " EXAMPLES"); 


            int[] setIDs = {0, 2};
            int[] classIDs = {0, 2};
            setSelectedExampleSetsAndClasses(setIDs, classIDs);
            w.startRunning();

            Thread.sleep(5000);
            
            w.stopRunning();
        }  catch (Exception ex) {
            System.out.println("Exception encountered in driver");
            Logger.getLogger(WekinatorWrapperDriverNew.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    // Choose the example sets to use for classification, and specify the class value that should correspond to each
    // requires setIDs and classIDs same length and ordering
    private void setSelectedExampleSetsAndClasses(int[] setIDs, int[] classes) {
        selectedSetsAndClasses = new HashMap<Integer, Integer>();
        if (setIDs == null || classes == null || setIDs.length != classes.length) {
            return; //error boo
        }

        for (int i = 0; i < setIDs.length; i++) {
           selectedSetsAndClasses.put(setIDs[i], classes[i]);
        }

        //Could do this in fixed length array but must be synchronized w/ lock on sets


    }
    
    private void updateWekinatorSelectedSetsAndClasses() {
        List<Integer> exampleList = new LinkedList<Integer>();
        List<Integer> classList = new LinkedList<Integer>();

        //for (int i= 0; i < setIDs.length; i++) {
        for (Integer setID : selectedSetsAndClasses.keySet()) {
            for (Integer ex : exampleSets.get(setID)) {
                exampleList.add(ex);
                classList.add(selectedSetsAndClasses.get(setID));
            }
        }
       //TODO w.setSelectedExamplesAndClasses(exampleList, classList);
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

        //Wekinator:
        //TODO: inform Wekinator - might be part of active classifier!
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

        //Wekinator:
        w.deleteTrainingExample(id); //FOR NOW, this will by default look through current training set and delete and retrain, if necessary
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
        w.deleteTrainingExampleIds(ids); //Currently will look through active training set and delete and retrain, if necessary
        //TODO: Rebecca: Wekinator should keep list of IDs in current training set to facilitate this process?
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
