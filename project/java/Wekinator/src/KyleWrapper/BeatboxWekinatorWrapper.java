/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package KyleWrapper;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.swing.event.EventListenerList;

/**
 *
 * @author fiebrink
 */
public class BeatboxWekinatorWrapper {

    protected int numFeatures = 0;
    protected int numClasses = 0;
    protected int trainingClassValue = 0;
    protected String[] featureNames = {};
    protected RecordingState recordingState = RecordingState.NOT_RECORDING;
    protected RunningState runningState = RunningState.NOT_RUNNING;
    protected TrainingState trainingState = TrainingState.NOT_TRAINING;
    protected ClassifierState classifierState = ClassifierState.NOT_TRAINED;
    protected EventListenerList classificationListenerList = new EventListenerList();
    protected EventListenerList trainingListenerList = new EventListenerList();
    public static String PROP_RECORDINGSTATE = "recordingState";
    public static String PROP_RUNNINGSTATE = "runningState";
    public static String PROP_TRAININGSTATE = "trainingState";
    public static String PROP_CLASSIFIERSTATE = "classifierState";
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    //Temp for this skeleton:
    int maxId = 0;
    protected HashMap<Integer, double[]> examples = new HashMap<Integer, double[]>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    ScheduledFuture<?> trainSimulationHandler = null;
    final Runnable trainSimulator = new Runnable() {
        public void run() {
            setTrainingState(TrainingState.NOT_TRAINING);
        }
    };

    /**
     * Add PropertyChangeListener.
     *
     * @param listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Remove PropertyChangeListener.
     *
     * @param listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public void addClassificationListener(ClassificationListener l) {
        classificationListenerList.add(ClassificationListener.class, l);
    }

    public void removeClassificationListener(ClassificationListener l) {
        classificationListenerList.remove(ClassificationListener.class, l);
    }

    public void addTrainingExampleListener(TrainingExampleListener l) {
        trainingListenerList.add(TrainingExampleListener.class, l);
    }

    public void removeTrainingExampleListener(TrainingExampleListener l) {
        trainingListenerList.remove(TrainingExampleListener.class, l);
    }



    //Whether Wekinator should add incoming feature vectors to the training set
    public enum RecordingState {

        RECORDING,
        NOT_RECORDING
    };

    //Whether Wekinator should be classifying incoming feature vectors
    public enum RunningState {

        RUNNING,
        NOT_RUNNING
    };

    //Whether the classifier is currently training
    public enum TrainingState {

        TRAINING,
        NOT_TRAINING
    };

    //State of the most recent classifier
    //Note: We might want it to be possible to use a trained classifier to classify incoming examples even while
    //a newer classifier is training, if these actions are happening on different threads.
    //That is why ClassifierState and TrainingState are separate things.
    public enum ClassifierState {

        NOT_TRAINED,
        TRAINED,
        ERROR
    };

    public BeatboxWekinatorWrapper(int numFeatures, int numClasses, String[] featureNames) {
        this.numFeatures = numFeatures;
        this.numClasses = numClasses;
        this.featureNames = new String[featureNames.length];
        System.arraycopy(featureNames, 0, this.featureNames, 0, featureNames.length);
        //TODO: check that all these values/lengths are valid
    }

    public int getNumFeatures() {
        return numFeatures;
    }

    public String[] getFeatureNames() {
        return featureNames;
    }

    //Don't use this method unless you're simulating Chuck FE sending this info to Wekinator directly.
    public void addTrainingExample(int id, double[] features) {
        double[] featuresCopy = new double[features.length];
        System.arraycopy(features, 0, featuresCopy, 0, features.length);
        examples.put(id, featuresCopy);
        newTrainingExampleRecorded(id);
    }

    public int getTrainingClassValue() {
        return trainingClassValue;
    }

    public void setTrainingClassValue(int trainingClassValue) {
        this.trainingClassValue = trainingClassValue;
    }

    /*public double[] getTrainingExample(int id) {
        if (examples.containsKey(id)) {
        return examples.get(id);
        } else {
        return null;
        }
    } */



    public boolean exampleIdExists(int id) {
        return examples.containsKey(id);
    }

    public int numTrainingExamples() {
        return examples.size();
    }

    public void startTraining() {
        if (trainingState != TrainingState.TRAINING) {
            setTrainingState(TrainingState.TRAINING);
            //Just for this version: Simulates a 2-second training time
            trainSimulationHandler =
                    scheduler.schedule(trainSimulator, 2l, TimeUnit.SECONDS);
        }
    }

    public void cancelTraining() {
        if (trainingState == TrainingState.TRAINING) {
            setTrainingState(TrainingState.NOT_TRAINING);
        }
    }

    public void startRecordingExamples() {
        if (recordingState == RecordingState.NOT_RECORDING) {
            setRecordingState(RecordingState.RECORDING);
        }
    }

    public void stopRecordingExamples() {
        if (recordingState == RecordingState.RECORDING) {
            setRecordingState(RecordingState.NOT_RECORDING);
        }
    }

    public void startRunning() {
        if (runningState != RunningState.RUNNING) {
            setRunningState(RunningState.RUNNING);
        }
    }

    public void stopRunning() {
        if (runningState == RunningState.RUNNING) {
            setRunningState(RunningState.NOT_RUNNING);
        }
    }

    public void deleteTrainingExample(int id) {
        if (examples.containsKey(id)) {
            examples.remove(id);
        }
    }

    public void deleteAllTrainingExamples() {
        examples.clear();
    }

    public int classifyExample(double[] features) {
        //TODO: For now, this will always return 0.
        return 0;
    }

    public RecordingState getRecordingState() {
        return recordingState;
    }

    protected void setRecordingState(RecordingState recordingState) {
        RecordingState oldState = this.recordingState;
        this.recordingState = recordingState;
       // System.out.println("Wekinator recording state set to " + recordingState);
        propertyChangeSupport.firePropertyChange(PROP_RECORDINGSTATE, oldState, recordingState);
    }

    public RunningState getRunningState() {
        return runningState;
    }

    protected void setRunningState(RunningState runningState) {
        RunningState oldState = this.runningState;
        this.runningState = runningState;
       // System.out.println("Wekinator running state set to " + runningState);

        propertyChangeSupport.firePropertyChange(PROP_RUNNINGSTATE, oldState, runningState);
    }

    public TrainingState getTrainingState() {
        return trainingState;
    }

    protected void setTrainingState(TrainingState trainingState) {
        TrainingState oldState = this.trainingState;
        this.trainingState = trainingState;
       // System.out.println("Wekinator training state set to " + trainingState);

        propertyChangeSupport.firePropertyChange(PROP_TRAININGSTATE, oldState, trainingState);
    }

    public ClassifierState getClassifierState() {
        return classifierState;
    }

    protected void setClassifierState(ClassifierState classifierState) {
        ClassifierState oldState = this.classifierState;
        this.classifierState = classifierState;
      //  System.out.println("Wekinator classifier state set to " + classifierState);
        propertyChangeSupport.firePropertyChange(PROP_CLASSIFIERSTATE, oldState, classifierState);
    }

    public void saveWekinatorToFile(File f) {
        //TODO
    }

    public static BeatboxWekinatorWrapper loadFromFile(File f) {
        //return new BeatboxWekinatorWrapper();
        System.out.println("This method loadFromFile is not implemented yet.");
        return null; //TODO
    }

    public int[] getExampleIds() {
        Set<Integer> set = examples.keySet();
        int[] ints = new int[set.size()];
        int j = 0;
        for (Integer i : set) {
            ints[j++] = i;
        }
        return ints;
    }

    //This method will be initiated by wekinator receiving a new feature vector while in "run" mode
    protected void newClassificationResult(int id, int classValue) {
        // Guaranteed to return a non-null array
        Object[] listeners = classificationListenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ClassificationListener.class) {
                ((ClassificationListener) listeners[i + 1]).fireClassificationResult(id, classValue);
            }
        }
    }


    //This method will be initiated by Wekinator receiving a new feature vector while in "record" mode
    protected void newTrainingExampleRecorded(int id) {
        Object[] listeners = trainingListenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TrainingExampleListener.class) {
                ((TrainingExampleListener) listeners[i + 1]).fireTrainingExampleRecorded(id, trainingClassValue);
            }
        }
    }

    //TODO: Ensure add, remove operations are atomic if using Collection underneath
}
