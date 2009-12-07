/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wekinator;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.core.Instance;

/**
 *
 * @author rebecca
 */
public class LearningSystem implements Serializable {
    //Take over from Weka operator:
    //Manage training, testing in general
    //Not specific to the GUI

    protected boolean[] paramMask;
    protected double[] defaultValues;
    protected PlayalongScore score = null;
    public static final String PROP_SCORE = "score";

    /**
     * Get the value of score
     *
     * @return the value of score
     */
    public PlayalongScore getScore() {
        return score;
    }

    /**
     * Set the value of score
     *
     * @param score new value of score
     */
    public void setScore(PlayalongScore score) {
        PlayalongScore oldScore = this.score;
        this.score = score;
        propertyChangeSupport.firePropertyChange(PROP_SCORE, oldScore, score);
    }


    //Has a bunch of classifiers, most recent evaluation results
    // protected SimpleDataset dataset = null;
    protected SimpleDataset dataset = null;
  //  protected FeatureConfiguration featureConfiguration = null;
    protected FeatureLearnerConfiguration featureLearnerConfiguration = null;
    protected int numParams = 0;
    protected LearningAlgorithm[] learners;
    protected boolean[] learnerEnabled;
    protected transient PropertyChangeListener learnerChangeListener = new PropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent evt) {
            learnerChanged(evt);
        }
    };

    protected transient PropertyChangeListener datasetListener = new PropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent evt) {
            datasetChanged(evt);
        }
    };
    protected int numLearnersInitialized = 0;
    protected int numLearnersTrained = 0;
    protected transient Logger logger = Logger.getLogger(LearningSystem.class.getName());

    void addToTraining(double[] features, double[] params) {
        //Add to the training dataset.
        dataset.addInstance(features, params, paramMask, new Date());
    }

    double[] classify(double[] features) {
        double[] outputs = new double[numParams];
        Instance[] instances = dataset.convertToClassifiableInstances(features);
        for (int i = 0; i < numParams; i++) {
            if (learners[i] != null && learnerEnabled[i]) {
                outputs[i] = learners[i].classify(instances[i]);
            }
        }
        return outputs;
    }

    public void train() {
        setSystemTrainingState(LearningSystemTrainingState.TRAINING);

        for (int i = 0; i < numParams; i++) {
            if (learners[i] != null && learnerEnabled[i]) {
                learners[i].train(dataset.getClassifiableInstances(i));
            }

        }
        setSystemTrainingState(LearningSystemTrainingState.TRAINED);
    }

    public void forget() {
        for (int i= 0; i < numParams; i++) {
            if (learners[i] != null && learnerEnabled[i]) {
                learners[i].forget();
            }
        }
        setSystemTrainingState(LearningSystemTrainingState.NOT_TRAINED);
    }

    public enum LearningAlgorithmsInitializationState {

        NOT_INITIALIZED,
        SOME_INITIALIZED,
        ALL_INITIALIZED
    };

    public enum LearningAlgorithmsTrainingState {
        NOT_TRAINED,
        SOME_TRAINED,
        ALL_TRAINED
    };

    public enum LearningSystemTrainingState {
        NOT_TRAINED,
        TRAINING,
        TRAINED
    };

    public enum DatasetState {
        NO_DATA,
        HAS_DATA
    };
    protected LearningAlgorithmsInitializationState initializationState = LearningAlgorithmsInitializationState.NOT_INITIALIZED;
    public static final String PROP_INITIALIZATIONSTATE = "initializationState";
    protected LearningAlgorithmsTrainingState algorithmsTrainingState = LearningAlgorithmsTrainingState.NOT_TRAINED;
    public static final String PROP_TRAININGSTATE = "trainingState";
    protected DatasetState datasetState = DatasetState.NO_DATA;
    public static final String PROP_DATASETSTATE = "datasetState";
    protected LearningSystemTrainingState systemTrainingState = LearningSystemTrainingState.NOT_TRAINED;
    public static final String PROP_SYSTEMTRAININGSTATE = "systemTrainingState";

    /**
     * Get the value of systemTrainingState
     *
     * @return the value of systemTrainingState
     */
    public LearningSystemTrainingState getSystemTrainingState() {
        return systemTrainingState;
    }

    /**
     * Set the value of systemTrainingState
     *
     * @param systemTrainingState new value of systemTrainingState
     */
    public void setSystemTrainingState(LearningSystemTrainingState systemTrainingState) {
        LearningSystemTrainingState oldSystemTrainingState = this.systemTrainingState;
        this.systemTrainingState = systemTrainingState;
        propertyChangeSupport.firePropertyChange(PROP_SYSTEMTRAININGSTATE, oldSystemTrainingState, systemTrainingState);
    }


        /**
     * Get the value of paramMask
     *
     * @return the value of paramMask
     */
    public boolean[] isParamMask() {
        return paramMask;
    }

    /**
     * Set the value of paramMask
     *
     * @param paramMask new value of paramMask
     */
    public void setParamMask(boolean[] paramMask) {
        this.paramMask = paramMask;
    }

    /**
     * Get the value of paramMask at specified index
     *
     * @param index
     * @return the value of paramMask at specified index
     */
    public boolean isParamMask(int index) {
        return this.paramMask[index];
    }

    /**
     * Set the value of paramMask at specified index.
     *
     * @param index
     * @param newParamMask new value of paramMask at specified index
     */
    public void setParamMask(int index, boolean newParamMask) {
        this.paramMask[index] = newParamMask;
    }


    public boolean getLearnerEnabled(int learner) {
        return learnerEnabled[learner];
    }

    public void setLearnerEnabled(int learner, boolean enabled) {
        learnerEnabled[learner] = enabled;
    }

    protected void updateInitializationState() {
        if (numLearnersInitialized == 0) {
            setInitializationState(LearningAlgorithmsInitializationState.NOT_INITIALIZED);
        } else if (numLearnersInitialized < numParams) {
            setInitializationState(LearningAlgorithmsInitializationState.SOME_INITIALIZED);
        } else if (numLearnersInitialized == numParams) {
            setInitializationState(LearningAlgorithmsInitializationState.ALL_INITIALIZED);
        } else {
            logger.log(Level.SEVERE, "Invalid number of learners initialized: " + numLearnersTrained);
        }
    }

    protected void updateTrainingState() {
        if (numLearnersTrained == 0) {
            setTrainingState(LearningAlgorithmsTrainingState.NOT_TRAINED);
        } else if (numLearnersTrained < numParams) {
            setTrainingState(LearningAlgorithmsTrainingState.SOME_TRAINED);
        } else if (numLearnersTrained == numParams) {
            setTrainingState(LearningAlgorithmsTrainingState.ALL_TRAINED);
        } else {
            logger.log(Level.SEVERE, "Invalid number of learners trained: " + numLearnersTrained);
        }
    }

    // protected LearningAlgorithmsInitializationState state = LearningAlgorithmsInitializationState.NOT_INITIALIZED;
    // public static final String PROP_STATE = "state";
    private void learnerChanged(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(LearningAlgorithm.PROP_TRAININGSTATE)) {
            LearningAlgorithm.TrainingState newState =
                    (LearningAlgorithm.TrainingState) evt.getNewValue();
            LearningAlgorithm.TrainingState oldState =
                    (LearningAlgorithm.TrainingState) evt.getOldValue();

            if (oldState == LearningAlgorithm.TrainingState.TRAINED) {
                numLearnersTrained--;
            }
            if (newState == LearningAlgorithm.TrainingState.TRAINED) {
                numLearnersTrained++;
            }
            updateTrainingState();
        }
    }

    /**
     * Get the value of learners
     *
     * @return the value of learners
     */
    public LearningAlgorithm[] getLearners() {
        return learners;
    }

    /**
     * Set the value of learners
     *
     * @param learners new value of learners
     */
    public void setLearners(LearningAlgorithm[] learners) {
        //TODO: check that length is valid (= numParams)
        
        if (this.learners != null) {
            for (int i = 0; i < this.learners.length; i++) {
                if (this.learners[i] != null) {
                    this.learners[i].removePropertyChangeListener(learnerChangeListener);
                }
            }
        }
        this.learners = learners;
        if (this.learners != null) {
            for (int i = 0; i < this.learners.length; i++) {
                if (this.learners[i] != null) {
                    this.learners[i].addPropertyChangeListener(learnerChangeListener);
                }
            }
        }
        updateLearnerStats();
    }

    /**
     * Get the value of learners at specified index
     *
     * @param index
     * @return the value of learners at specified index
     */
    public LearningAlgorithm getLearners(int index) {
        return this.learners[index];
    }

    /**
     * Set the value of learners at specified index.
     *
     * @param index
     * @param newLearners new value of learners at specified index
     */
    public void setLearners(int index, LearningAlgorithm newLearners) {
        //TODO: update learner stats
        if (this.learners[index] == null && newLearners != null) {
            numLearnersInitialized++;
        } else if (this.learners[index] != null && newLearners == null) {
            numLearnersInitialized--;
        }
        if (this.learners[index] != null && this.learners[index].getTrainingState() == LearningAlgorithm.TrainingState.TRAINED) {
            numLearnersTrained--;
        }
        if (learners[index] != null && learners[index].getTrainingState() == LearningAlgorithm.TrainingState.TRAINED) {
            numLearnersTrained++;
        }

        this.learners[index] = newLearners;
        if (this.learners[index] != null) {
            this.learners[index].addPropertyChangeListener(learnerChangeListener);
        }
        updateInitializationState();
        updateTrainingState();
    }

    public LearningSystem(int numParams) {
        this.numParams = numParams;
        learners = new LearningAlgorithm[numParams];
        learnerEnabled = new boolean[numParams];
        for (int i = 0; i < numParams; i++) {
            learnerEnabled[i] = true;
        }
        paramMask = new boolean[numParams];
        defaultValues = new double[numParams];
        for (int i = 0; i < numParams; i++){
            paramMask[i] = true;
            defaultValues[i] = 0.0;
        }
        setScore(new PlayalongScore(numParams));
    }

    /**
     * Get the value of numParams
     *
     * @return the value of numParams
     */
    public int getNumParams() {
        return numParams;
    }
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

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
    //Properties: Num params, type of params, feature/param mask,
    //Other methods: train, compute accuracy (summary), compute individual accuracy, get learner types

    /**
     * Get the value of initializationState
     *
     * @return the value of initializationState
     */
    public LearningAlgorithmsInitializationState getInitializationState() {
        return initializationState;
    }

    /**
     * Set the value of initializationState
     *
     * @param initializationState new value of initializationState
     */
    protected void setInitializationState(LearningAlgorithmsInitializationState initializationState) {
        LearningAlgorithmsInitializationState oldInitializationState = this.initializationState;
        this.initializationState = initializationState;
        propertyChangeSupport.firePropertyChange(PROP_INITIALIZATIONSTATE, oldInitializationState, initializationState);
    }

    /**
     * Get the value of datasetState
     *
     * @return the value of datasetState
     */
    public DatasetState getDatasetState() {
        return datasetState;
    }

    /**
     * Set the value of datasetState
     *
     * @param datasetState new value of datasetState
     */
    public void setDatasetState(DatasetState datasetState) {
        DatasetState oldDatasetState = this.datasetState;
        this.datasetState = datasetState;
        propertyChangeSupport.firePropertyChange(PROP_DATASETSTATE, oldDatasetState, datasetState);
    }

    /**
     * Get the value of algorithmsTrainingState
     *
     * @return the value of algorithmsTrainingState
     */
    public LearningAlgorithmsTrainingState getTrainingState() {
        return algorithmsTrainingState;
    }

    /**
     * Set the value of algorithmsTrainingState
     *
     * @param algorithmsTrainingState new value of algorithmsTrainingState
     */
    public void setTrainingState(LearningAlgorithmsTrainingState trainingState) {
        LearningAlgorithmsTrainingState oldTrainingState = this.algorithmsTrainingState;
        this.algorithmsTrainingState = trainingState;
        propertyChangeSupport.firePropertyChange(PROP_TRAININGSTATE, oldTrainingState, trainingState);
    }

    private void updateLearnerStats() {
        numLearnersInitialized = 0;
        numLearnersTrained = 0;
        for (int i = 0; i < learners.length; i++) {
            if (learners[i] != null) {
                numLearnersInitialized++;
                if (learners[i].getTrainingState() == LearningAlgorithm.TrainingState.TRAINED) {
                    numLearnersTrained++;
                }
            }
        }
    }

    /**
     * Get the value of dataset
     *
     * @return the value of dataset
     */
    public SimpleDataset getDataset() {
        return dataset;
    }

    /**
     * Set the value of dataset
     *
     * @param dataset new value of dataset
     */
    public void setDataset(SimpleDataset dataset) {
        if (this.dataset != null) {
            this.dataset.removePropertyChangeListener(datasetListener);
        }
        if (dataset != null) {
            dataset.addPropertyChangeListener(datasetListener);
        }
        this.dataset = dataset;
    }

    public double[] getLastTrainingAccuracy() {
        double[] a = new double[numParams];
        for (int i= 0; i < numParams; i++) {
            if (learners[i] != null) {
                a[i] = learners[i].getLastTrainingAccuracy();
            }
        }
        return a;
    }

    public double[] computeCVAccuracy(int numFolds) {
        double[] a = new double[numParams];
        for (int i= 0; i < numParams; i++) {
            if (learners[i] != null) {
                a[i] = learners[i].computeCVAccuracy(numFolds);
            }
        }
        return a;
    }

    protected void datasetChanged(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(SimpleDataset.PROP_HASINSTANCES)) {
            if (dataset.isHasInstances()) {
                setDatasetState(DatasetState.HAS_DATA);
            } else {
                setDatasetState(DatasetState.NO_DATA);
            }
        }
    }
}
