/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package KyleWrapper;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import wekinator.ChuckConfiguration;
import wekinator.ChuckRunner;
import wekinator.FeatureConfiguration;
import wekinator.LearningAlgorithms.IbkLearningAlgorithm;
import wekinator.LearningAlgorithms.LearningAlgorithm;
import wekinator.LearningSystem;
import wekinator.LearningSystem.TrainingStatus;
import wekinator.OscHandler;
import wekinator.SimpleDataset;
import wekinator.WekinatorInstance;
import wekinator.WekinatorLearningManager;
import wekinator.WekinatorRunner;

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
    protected EventListenerList setupCompleteListenerList = new EventListenerList();
    protected ChangeEvent setupCompleteEvent = null;
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
    // final Runnable trainSimulator = new Runnable() {
    //     public void run() {
    //         setTrainingState(TrainingState.NOT_TRAINING);
    //     }
    // };

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

    public void addSetupCompleteListener(ChangeListener l) {
        setupCompleteListenerList.add(ChangeListener.class, l);
    }

    public void removeSetupCompleteExampleListener(ChangeListener l) {
        setupCompleteListenerList.remove(ChangeListener.class, l);
    }

    protected void fireSetupComplete() {
        Object[] listeners = setupCompleteListenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ChangeListener.class) {
                if (setupCompleteEvent == null) {
                    setupCompleteEvent = new ChangeEvent(this);
                }
                ((ChangeListener) listeners[i + 1]).stateChanged(setupCompleteEvent);
            }
        }
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

    public BeatboxWekinatorWrapper(int numFeatures, int numClasses, String[] featureNames) throws Exception {
        this.numFeatures = numFeatures;
        this.numClasses = numClasses;
        this.featureNames = new String[featureNames.length];
        System.arraycopy(featureNames, 0, this.featureNames, 0, featureNames.length);
        //TODO: check that all these values/lengths are valid

        WekinatorRunner.run(false);

        setupChuckConfiguration();
        try {
            ChuckRunner.run(); //In future might want this to not run, only to communicate with other running chuck code.
            WekinatorInstance.getWekinatorInstance().addFeatureParameterSetupDoneListener(new ChangeListener() {

                public void stateChanged(ChangeEvent ce) {
                    buildLearningSystem();
                }
            });

            WekinatorLearningManager.getInstance().addPropertyChangeListener(new PropertyChangeListener() {

                public void propertyChange(PropertyChangeEvent pce) {
                    learningManagerPropertyChanged(pce);
                }
            });

            setupFeatureConfiguration();



        } catch (IOException ex) {
            System.out.println("Chuck runner failed");
            Logger.getLogger(BeatboxWekinatorWrapper.class.getName()).log(Level.SEVERE, null, ex);
            throw new Exception(ex);
        } catch (Exception ex) {
            System.out.println("some other exception happened");
            ex.printStackTrace();
            throw new Exception(ex);
        }

    }

    private void buildLearningSystem() {
        LearningSystem ls = new LearningSystem(1); //TODO: CHange for non-beatbox problem

        boolean[] isDiscreteArray = {true};
        int[] numClassesArray = {numClasses};
        String[] paramNamesArray = {"HitClass"};


        SimpleDataset s = new SimpleDataset(
                WekinatorInstance.getWekinatorInstance().getFeatureConfiguration().getNumFeaturesEnabled(),
                1,
                isDiscreteArray,
                numClassesArray,
                featureNames,
                paramNamesArray);


        ls.setDataset(s);

        LearningAlgorithm a = new IbkLearningAlgorithm();

        ls.setLearners(0, a);

        ls.addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent pce) {
                learningSystemPropertyChanged(pce);
            }
        });

        WekinatorInstance.getWekinatorInstance().setLearningSystem(ls);
        fireSetupComplete();
        System.out.println("done setting up");
    }

    private void setupChuckConfiguration() {
        ChuckConfiguration c = ChuckRunner.getConfiguration();
        //TODO: Make sure config is set properly
        //TODO: Change from my system defaults.

        // c.setChuckExecutable(A);
        c.setChuckSynthFilename("/Users/rebecca/work/projects/wekinator/project/chuck/synths/simple_melody_discrete.ck");
        c.setCustomChuckFeatureExtractorEnabled(false);
        c.setCustomChuckFeatureExtractorFilename("");
        c.setIsPlayalongLearningEnabled(false);
        //  c.setLocationToSaveMyself(PROP_RECORDINGSTATE);
        //  c.setNumCustomChuckFeaturesExtracted(numFeatures);
        //  c.setNumOSCFeaturesExtracted(0);
        c.setOscFeatureExtractorEnabled(false);
        //c.setOscFeatureExtractorSendPort();
        //c.setOscSynthConfiguration(null);
        //c.setOscSynthReceivePort(numFeatures);
        //c.setOscSynthSendPort(numFeatures);
        //c.setPlayalongLearningFile(PROP_RECORDINGSTATE);
        //c.setSaveLocation(PROP_RECORDINGSTATE);
        c.setUseChuckSynthClass(true);
        c.setUseOscSynth(false);
        //c.setWekDir(PROP_RUNNINGSTATE);

    }

    private void setupFeatureConfiguration() throws Exception {
        FeatureConfiguration fc = new FeatureConfiguration();
        fc.setUseMotionSensor(true);
        fc.validate();
        WekinatorInstance.getWekinatorInstance().setFeatureConfiguration(fc);
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

    //X
    public int getTrainingClassValue() {
        return trainingClassValue;
    }

    //X
    public void setTrainingClassValue(int trainingClassValue) {
        this.trainingClassValue = trainingClassValue;
        double[] d = new double[1];
        d[0] = (double) trainingClassValue;
        WekinatorLearningManager.getInstance().setParams(d);
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
            // trainSimulationHandler =
            //         scheduler.schedule(trainSimulator, 2l, TimeUnit.SECONDS);
            WekinatorLearningManager.getInstance().startTraining();

        }
    }

    public void cancelTraining() {
        if (trainingState == TrainingState.TRAINING) {
            setTrainingState(TrainingState.NOT_TRAINING);

        }
    }

    public void startRecordingExamples() throws Exception {
        if (recordingState == RecordingState.NOT_RECORDING) {
            WekinatorLearningManager.getInstance().startDatasetCreation();

            setRecordingState(RecordingState.RECORDING);
        }
    }

    public void stopRecordingExamples() {
        if (recordingState == RecordingState.RECORDING) {
            WekinatorLearningManager.getInstance().stopDatasetCreation();
            setRecordingState(RecordingState.NOT_RECORDING);
        }
    }

    public void startRunning() {
        if (runningState != RunningState.RUNNING) {
            setRunningState(RunningState.RUNNING);
            WekinatorLearningManager.getInstance().startRunning();
            OscHandler.getOscHandler().startSound(); //TODO: perhaps remove depending on synth
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

    public double[] getExampleIds() {
        /*  Set<Integer> set = examples.keySet();
        int[] ints = new int[set.size()];
        int j = 0;
        for (Integer i : set) {
        ints[j++] = i;
        }
        return ints; */
        LearningSystem ls = WekinatorInstance.getWekinatorInstance().getLearningSystem();
        if (ls != null) {
            SimpleDataset d = ls.getDataset();
            if (d != null) {
                return d.getIDs();
            }
        }
        return new double[0];
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

    private void learningSystemPropertyChanged(PropertyChangeEvent pce) {
        /*  if (pce.getPropertyName().equals(LearningSystem.PROP_TRAININGPROGRESS)) {
        TrainingStatus s = WekinatorInstance.getWekinatorInstance().getLearningSystem().getTrainingProgress();

        int i = s.getNumTrained();
        int j = s.getNumErrorsEncountered();
        int k = s.getNumToTrain();


        if ((s.getNumTrained() + s.getNumErrorsEncountered()) == s.getNumToTrain()) {
        setTrainingState(TrainingState.NOT_TRAINING);
        if (s.getNumErrorsEncountered() == 0) {
        setClassifierState(ClassifierState.TRAINED);
        } else {
        setClassifierState(ClassifierState.ERROR);
        }
        }

        } */
        /*if (pce.getPropertyName().equals(LearningSystem.PROP_ISTRAINING)) {
            boolean wasTraining = (Boolean) pce.getOldValue();
            boolean isTraining = (Boolean) pce.getNewValue();
            if (wasTraining && !isTraining) {
                setTrainingState(TrainingState.NOT_TRAINING);
                TrainingStatus s = WekinatorInstance.getWekinatorInstance().getLearningSystem().getTrainingProgress();

                if (s.getNumErrorsEncountered() == 0) {
                    setClassifierState(ClassifierState.TRAINED);
                } else {
                    setClassifierState(ClassifierState.ERROR);
                }


            }

        } */

    }

    private void learningManagerPropertyChanged(PropertyChangeEvent pce) {
        if (pce.getPropertyName().equals(WekinatorLearningManager.PROP_MODE)) {
            WekinatorLearningManager.Mode oldM = (WekinatorLearningManager.Mode)pce.getOldValue();

                        WekinatorLearningManager.Mode newM = (WekinatorLearningManager.Mode)pce.getNewValue();

                        if (oldM == WekinatorLearningManager.Mode.TRAINING && newM == WekinatorLearningManager.Mode.NONE) {
                            setTrainingState(TrainingState.NOT_TRAINING);
                TrainingStatus s = WekinatorInstance.getWekinatorInstance().getLearningSystem().getTrainingProgress();

                if (s.getNumErrorsEncountered() == 0) {
                    setClassifierState(ClassifierState.TRAINED);
                } else {
                    setClassifierState(ClassifierState.ERROR);
                }

                        }

        }

    }

    //TODO: Ensure add, remove operations are atomic if using Collection underneath
}
