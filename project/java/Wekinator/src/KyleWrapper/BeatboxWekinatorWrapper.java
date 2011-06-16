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
import wekinator.OscSynthConfiguration;
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
    protected HashMap<Integer, double[]> examples = new HashMap<Integer, double[]>();
    private final String chuckConfigFilename;
    private final boolean runChuckOnStart;
    private final String chuckExecutableLocation;

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

    //X Fired when Chuck & Java components talking to each other as required
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

    //Constructor
    //Error will happen later (during recording possibly) if features mismatch those being extracted,
    // or # classes does not match synth
    public BeatboxWekinatorWrapper(int numFeatures, int numClasses, String[] featureNames, String chuckConfigFilename, boolean runChuckOnStart, String chuckExecutableLocation) throws Exception {
        this.numFeatures = numFeatures;
        this.numClasses = numClasses;
        this.featureNames = new String[featureNames.length];
        this.chuckConfigFilename = chuckConfigFilename;
        this.runChuckOnStart = runChuckOnStart;
        this.chuckExecutableLocation = chuckExecutableLocation;
        
        System.arraycopy(featureNames, 0, this.featureNames, 0, featureNames.length);
        //TODO: check that all these values/lengths are valid

        WekinatorRunner.run(false);

        setupChuckConfiguration();
        try {
            WekinatorInstance.getWekinatorInstance().addFeatureParameterSetupDoneListener(new ChangeListener() {

                public void stateChanged(ChangeEvent ce) {
                    System.out.println("********* FEAT PARAM SETUP DONE");
                    buildLearningSystem();
                }
            });

            WekinatorLearningManager.getInstance().addPropertyChangeListener(new PropertyChangeListener() {

                public void propertyChange(PropertyChangeEvent pce) {
                    learningManagerPropertyChanged(pce);
                }
            });

            if (runChuckOnStart) {
                ChuckRunner.runConfigFile(chuckConfigFilename);
                Thread.sleep(5000); //TODO: Replace with callback, handle errors if possible
            }

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


        s.addExampleAddedListener(new ChangeListener() {
            public void stateChanged(ChangeEvent ce) {
                newTrainingExampleRecorded(((SimpleDataset.ExampleAddEvent)ce).id);
            }
        });

        ls.setDataset(s);

        LearningAlgorithm a = new IbkLearningAlgorithm();

        ls.setLearners(0, a);

        ls.addClassificationResultListener(new ChangeListener() {

            public void stateChanged(ChangeEvent ce) {
                LearningSystem.ClassificationEvent e = (LearningSystem.ClassificationEvent)ce;
                newClassificationResult(e.id, (int)e.results[0]);
              //  (LearningSystem.ClassificationEvent; ce)
            }
        });


        /*  ls.addPropertyChangeListener(new PropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent pce) {
        learningSystemPropertyChanged(pce);
        }
        }); */

        WekinatorInstance.getWekinatorInstance().setLearningSystem(ls);
        fireSetupComplete();
        System.out.println("done setting up");
    }

    private void setupChuckConfiguration() {
        ChuckConfiguration c = ChuckRunner.getConfiguration();
        c.setChuckExecutable(chuckExecutableLocation);
        c.setUseChuckSynthClass(false);
        c.setUseOscSynth(true);

        boolean[] isDiscreteArray = {true};
        boolean[] isDistArray = {false};
        int[] maxValueArray = {numClasses-1};
        String[] paramNamesArray = {"HitClass"};
        OscSynthConfiguration synthConfig = new OscSynthConfiguration(1, paramNamesArray, isDiscreteArray, isDistArray, maxValueArray);
        c.setOscSynthConfiguration(synthConfig);
    }

    //X
    private void setupFeatureConfiguration() throws Exception {
        FeatureConfiguration fc = new FeatureConfiguration();
        fc.setUseCustomOscFeatures(true);
        fc.setNumCustomOscFeatures(numFeatures);
        fc.validate();
        WekinatorInstance.getWekinatorInstance().setFeatureConfiguration(fc);
    }

    //X
    public int getNumFeatures() {
        return numFeatures;
    }

    //X
    public String[] getFeatureNames() {
        return featureNames;
    }

    //TODO
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

    //X
    public boolean exampleIdExists(int id) {
        LearningSystem ls = WekinatorInstance.getWekinatorInstance().getLearningSystem();
        if (ls != null) {
            SimpleDataset s = ls.getDataset();
            if (s != null) {
                return s.hasID(id);
            }
        }
        return false;
    }

    //X
    public int numTrainingExamples() {
         LearningSystem ls = WekinatorInstance.getWekinatorInstance().getLearningSystem();
        if (ls != null) {
            SimpleDataset s = ls.getDataset();
            if (s != null) {
                return s.getNumDatapoints();
            }
        }
        return 0;
    }

    //X
    public void startTraining() {
        if (trainingState != TrainingState.TRAINING) {
            setTrainingState(TrainingState.TRAINING);
            WekinatorLearningManager.getInstance().startTraining();
        }
    }

    //X (untested)
    public void cancelTraining() {
        if (trainingState == TrainingState.TRAINING) {
            setTrainingState(TrainingState.NOT_TRAINING);
            WekinatorLearningManager.getInstance().stopTraining();
        }
    }

    //X
    public void startRecordingExamples() throws Exception {
        if (recordingState == RecordingState.NOT_RECORDING) {
            WekinatorLearningManager.getInstance().startDatasetCreation();
            setRecordingState(RecordingState.RECORDING);
        }
    }

    //X
    public void stopRecordingExamples() {
        if (recordingState == RecordingState.RECORDING) {
            WekinatorLearningManager.getInstance().stopDatasetCreation();
            setRecordingState(RecordingState.NOT_RECORDING);
        }
    }

    //X
    public void startRunning() {
        if (runningState != RunningState.RUNNING) {
            setRunningState(RunningState.RUNNING);
            WekinatorLearningManager.getInstance().startRunning();
       //     Works for both chuck & osc synths: TODO might remove?
            OscHandler.getOscHandler().startSound();
        }
    }

    //X
    public void stopRunning() {
        if (runningState == RunningState.RUNNING) {
            WekinatorLearningManager.getInstance().stopRunning();
            setRunningState(RunningState.NOT_RUNNING);
        }
    }

    //X
    public void deleteTrainingExample(int id) {
         LearningSystem ls = WekinatorInstance.getWekinatorInstance().getLearningSystem();
        if (ls != null) {
            SimpleDataset s = ls.getDataset();
            if (s != null) {
                s.deleteInstanceWithID(id);
            }
        }
    }

    //X
    public void deleteAllTrainingExamples() {
         LearningSystem ls = WekinatorInstance.getWekinatorInstance().getLearningSystem();
        if (ls != null) {
            SimpleDataset s = ls.getDataset();
            if (s != null) {
                s.deleteAll();
            }
        }
    }

    //TODO
    public int classifyExample(double[] features) {
        //TODO: For now, this will always return 0.
        return 0;
    }

    //X
    public RecordingState getRecordingState() {
        return recordingState;
    }

    //X
    protected void setRecordingState(RecordingState recordingState) {
        RecordingState oldState = this.recordingState;
        this.recordingState = recordingState;
        propertyChangeSupport.firePropertyChange(PROP_RECORDINGSTATE, oldState, recordingState);
    }

    public RunningState getRunningState() {
        return runningState;
    }

    protected void setRunningState(RunningState runningState) {
        RunningState oldState = this.runningState;
        this.runningState = runningState;
        propertyChangeSupport.firePropertyChange(PROP_RUNNINGSTATE, oldState, runningState);
    }

    public TrainingState getTrainingState() {
        return trainingState;
    }

    protected void setTrainingState(TrainingState trainingState) {
        TrainingState oldState = this.trainingState;
        this.trainingState = trainingState;
        propertyChangeSupport.firePropertyChange(PROP_TRAININGSTATE, oldState, trainingState);
    }

    public ClassifierState getClassifierState() {
        return classifierState;
    }

    protected void setClassifierState(ClassifierState classifierState) {
        ClassifierState oldState = this.classifierState;
        this.classifierState = classifierState;
        propertyChangeSupport.firePropertyChange(PROP_CLASSIFIERSTATE, oldState, classifierState);
    }

    //TODO
    public void saveWekinatorToFile(File f) {
    }

    //TODO
    public static BeatboxWekinatorWrapper loadFromFile(File f) {
        //return new BeatboxWekinatorWrapper();
        System.out.println("This method loadFromFile is not implemented yet.");
        return null; //TODO
    }

    //X
    public double[] getExampleIds() {
        LearningSystem ls = WekinatorInstance.getWekinatorInstance().getLearningSystem();
        if (ls != null) {
            SimpleDataset d = ls.getDataset();
            if (d != null) {
                return d.getIDs();
            }
        }
        return new double[0];
    }

    //X
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

    //X
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

    private void learningManagerPropertyChanged(PropertyChangeEvent pce) {
        if (pce.getPropertyName().equals(WekinatorLearningManager.PROP_MODE)) {
            WekinatorLearningManager.Mode oldM = (WekinatorLearningManager.Mode) pce.getOldValue();
            WekinatorLearningManager.Mode newM = (WekinatorLearningManager.Mode) pce.getNewValue();

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
