/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wekinator;

import wekinator.LearningAlgorithms.LearningAlgorithm;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import org.jdesktop.swingworker.SwingWorker;
import weka.core.Instance;
import wekinator.util.SerializedFileUtil;

/**
 *
 * @author rebecca
 */
public class LearningSystem implements Serializable {

    private transient ChangeEvent changeEvent = null;
    protected transient EventListenerList listenerList = new EventListenerList();
    protected boolean[] paramMask;
    protected double[] outputs;
    protected boolean[] paramUsingDistribution;
    protected int[] numMaxValsForParameter;
    protected transient SwingWorker trainingWorker = null;
    protected transient EvaluationWorker evaluationWorker = null;
    protected double[] cvResults;
    public static final String PROP_CVRESULTS = "cvResults";
    protected double[] trainResults;
    public static final String PROP_TRAINRESULTS = "trainResults";
    public static final String PROP_DATASET = "dataset";
    protected int learnerToEvaluate = -1;
    protected int learnerToTrain = -1;


    public static String getFileExtension() {
        return "wlsys";
    }

    public static String getFileTypeDescription() {
        return "learning system";
    }

    public static String getDefaultLocation() {
        return "learningSystems";
    }
    /**
     * Get the value of trainResults
     *
     * @return the value of trainResults
     */
    public double[] getTrainResults() {
        return trainResults;
    }

    /**
     * Set the value of trainResults
     *
     * @param trainResults new value of trainResults
     */
    public void setTrainResults(double[] trainResults) {
        double[] oldTrainResults = null;
        if (this.trainResults != null) {
            oldTrainResults = new double[this.trainResults.length];
            for (int i = 0; i < oldTrainResults.length; i++) {
                oldTrainResults[i] = this.trainResults[i];
            }
        }

        this.trainResults = trainResults;
        propertyChangeSupport.firePropertyChange(PROP_TRAINRESULTS, oldTrainResults, trainResults);
    }

    /**
     * Get the value of trainResults at specified index
     *
     * @param index
     * @return the value of trainResults at specified index
     */
    public double getTrainResults(int index) {
        return this.trainResults[index];
    }

    /**
     * Set the value of trainResults at specified index.
     *
     * @param index
     * @param newTrainResults new value of trainResults at specified index
     */
    public void setTrainResults(int index, double newTrainResults) {
        double oldTrainResults = this.trainResults[index];
        this.trainResults[index] = newTrainResults;
        propertyChangeSupport.fireIndexedPropertyChange(PROP_TRAINRESULTS, index, oldTrainResults, newTrainResults);
    }

    /**
     * Get the value of cvResults
     *
     * @return the value of cvResults
     */
    public double[] getCvResults() {
        return cvResults;
    }

    /**
     * Set the value of cvResults
     *
     * @param cvResults new value of cvResults
     */
    public void setCvResults(double[] cvResults) {
        double[] oldCvResults = null;
        if (this.cvResults != null) {
            oldCvResults = new double[this.cvResults.length];
            for (int i = 0; i < oldCvResults.length; i++) {
                oldCvResults[i] = this.cvResults[i];
            }
        }

        this.cvResults = cvResults;
        propertyChangeSupport.firePropertyChange(PROP_CVRESULTS, oldCvResults, cvResults);
    }

    /**
     * Get the value of cvResults at specified index
     *
     * @param index
     * @return the value of cvResults at specified index
     */
    public double getCvResults(int index) {
        return this.cvResults[index];
    }

    /**
     * Set the value of cvResults at specified index.
     *
     * @param index
     * @param newCvResults new value of cvResults at specified index
     */
    public void setCvResults(int index, double newCvResults) {
        double oldCvResults = this.cvResults[index];
        this.cvResults[index] = newCvResults;
        propertyChangeSupport.fireIndexedPropertyChange(PROP_CVRESULTS, index, oldCvResults, newCvResults);
    }

    public enum LearningAlgorithmsInitializationState {

        NOT_INITIALIZED,
        SOME_INITIALIZED,
        ALL_INITIALIZED
    };

    public enum LearningSystemTrainingState {

        NOT_TRAINED,
        TRAINING,
        TRAINED,
        ERROR
    };

    public enum EvaluationState {

        NOT_EVALUATED,
        EVALUTATING,
        DONE_EVALUATING
    };

    public enum DatasetState {

        NO_DATA,
        HAS_DATA
    };

    protected enum EvaluationType {

        CV,
        TRAINING
    };
    protected EvaluationState evaluationState = EvaluationState.NOT_EVALUATED;
    public static final String PROP_EVALUATIONSTATE = "evaluationState";

    /**
     * Get the value of evaluationState
     *
     * @return the value of evaluationState
     */
    public EvaluationState getEvaluationState() {
        return evaluationState;
    }

    /**
     * Set the value of evaluationState
     *
     * @param evaluationState new value of evaluationState
     */
    public void setEvaluationState(EvaluationState evaluationState) {
        EvaluationState oldEvaluationState = this.evaluationState;
        this.evaluationState = evaluationState;
        propertyChangeSupport.firePropertyChange(PROP_EVALUATIONSTATE, oldEvaluationState, evaluationState);
    }
    protected int numFolds = 2;
    protected EvaluationType evaluationType = EvaluationType.CV;
    //TODO: why is this score here?
    protected PlayalongScore score = null;
    public static final String PROP_SCORE = "score";
    protected SimpleDataset dataset = null;
    protected FeatureLearnerConfiguration featureLearnerConfiguration = null;
    protected int numParams = 0;
    protected LearningAlgorithm[] learners;
    protected boolean[] learnerEnabled;
    protected transient PropertyChangeListener learnerChangeListener = new PropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent evt) {
            learnerPropertyChanged(evt);
        }
    };
    protected transient PropertyChangeListener datasetListener = new PropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent evt) {
            datasetChanged(evt);
        }
    };
    protected transient Logger logger = Logger.getLogger(LearningSystem.class.getName());
    protected LearningAlgorithmsInitializationState initializationState = LearningAlgorithmsInitializationState.NOT_INITIALIZED;
    public static final String PROP_INITIALIZATIONSTATE = "initializationState";
    // protected LearningAlgorithmsTrainingState algorithmsTrainingState = LearningAlgorithmsTrainingState.NOT_TRAINED;
    protected DatasetState datasetState = DatasetState.NO_DATA;
    public static final String PROP_DATASETSTATE = "datasetState";
    protected LearningSystemTrainingState systemTrainingState = LearningSystemTrainingState.NOT_TRAINED;
    public static final String PROP_SYSTEMTRAININGSTATE = "systemTrainingState";

    void addToTraining(double[] features, double[] params) {
        //Add to the training dataset.
        dataset.addInstance(features, params, paramMask, new Date());
    }

    double[] classify(double[] features) {
        Instance[] instances = dataset.convertToClassifiableInstances(features);
        int next = 0;
        for (int i = 0; i < numParams; i++) {
            if (learners[i] != null) { //classify whether or not learner enabled
                if (!paramUsingDistribution[i]) {
                    try {
                        outputs[next] = learners[i].classify(instances[i]);
                        next++;
                    } catch (Exception ex) {
                        next++;
                        Logger.getLogger(LearningSystem.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    double[] dist = new double[0];
                    try {
                        dist = learners[i].distributionForInstance(instances[i]);
                        for (int j = 0; j < dist.length; j++) {
                            outputs[next++] = dist[j];
                        }
                    } catch (Exception ex) {
                        next+= dist.length;
                        Logger.getLogger(LearningSystem.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                }
            } else { //learner not active : keep old values, but advance next
                if (!paramUsingDistribution[i]) {
                    next++;
                } else {
                    next += numMaxValsForParameter[i];
                }
            }
        }
        return outputs;
    }

    /* TODO: Training system is a mess.
     *
     * Why keep track of # trained?
     * - Care about updating GUI with progress
     * ---> Maybe more appropriate to have status / progress : n of N done w/ message
     *
     * - Care about whether all trained so that we can run in future
     *
     * */
    public void train() {
        setSystemTrainingState(LearningSystemTrainingState.TRAINING);

        for (int i = 0; i < numParams; i++) {
            if (learners[i] != null && learnerEnabled[i]) {
                try {
                    learners[i].train(dataset.getClassifiableInstances(i));
                } catch (Exception ex) {
                    Logger.getLogger(LearningSystem.class.getName()).log(Level.SEVERE, null, ex);
                //TODO lower priority: test this works when error actually occurs in learner
                }
            }

        }
    //s setSystemTrainingState(LearningSystemTrainingState.TRAINED);
    }

    public void train(int learnerNum) {
        setSystemTrainingState(LearningSystemTrainingState.TRAINING);
        if (learners[learnerNum] != null && learnerEnabled[learnerNum]) {
            try {
                learners[learnerNum].train(dataset.getClassifiableInstances(learnerNum));
            } catch (Exception ex) {
                Logger.getLogger(LearningSystem.class.getName()).log(Level.SEVERE, null, ex);
            //TODO lower priority: test this works when error actually occurs in learner
            }
        }
    }

    public void trainInBackground() {
        synchronized (this) {

            //See http://www.j2ee.me/javase/6/docs/api/javax/swing/SwingWorker.html
            if (trainingWorker != null && trainingWorker.getState() != SwingWorker.StateValue.DONE) {
                //TODO: Cancel?
                //trainingWorker.cancel(true);
                return;
            }
            trainingWorker = new SwingWorker<Integer, Void>() {

                @Override
                public Integer doInBackground() {
                    train(); //TODO: Add status updates
                    return new Integer(0);
                }


             public void done() {
               // setTrainingState(
            }
            };

            trainingWorker.execute();
        }
    }

    public void trainInBackground(int paramNum) {
        synchronized (this) {
            //See http://www.j2ee.me/javase/6/docs/api/javax/swing/SwingWorker.html
            if (trainingWorker != null && trainingWorker.getState() != SwingWorker.StateValue.DONE) {
                //TODO: Cancel?
                //trainingWorker.cancel(true);
                return;
            }
            learnerToTrain = paramNum;

            trainingWorker = new SwingWorker<Integer, Void>() {

                @Override
                public Integer doInBackground() {
                    train(learnerToTrain); //TODO: Add status updates
                    return new Integer(0);
                }
            };

            trainingWorker.execute();
        }
    }

    public void forget() {
        for (int i = 0; i < numParams; i++) {
            if (learners[i] != null && learnerEnabled[i]) {
                learners[i].forget();
            }
        }
        setSystemTrainingState(LearningSystemTrainingState.NOT_TRAINED);
    }

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

    public void setLearnersEnabled(boolean[] enabled) {
        learnerEnabled = enabled;
    }

    public void setLearnerEnabled(int learner, boolean enabled) {
        learnerEnabled[learner] = enabled;
    }

    //Update this when initialization
    ///TODO problem: What if learner swapped mid-training?!?
    protected void updateTrainingState() {
        if (learners == null || learners.length == 0) {
            setSystemTrainingState(LearningSystemTrainingState.NOT_TRAINED);
            return;
        }

        int numTrained = 0;
        for (int i = 0; i < learners.length; i++) {
            if (learners[i] != null) {
                if (learners[i].getTrainingState() == LearningAlgorithm.TrainingState.TRAINED) {
                    numTrained++;
                } else if (learners[i].getTrainingState() == LearningAlgorithm.TrainingState.TRAINING) {
                    setSystemTrainingState(LearningSystemTrainingState.TRAINING);
                    return;
                //If any one is training, we'return training overall, even if someone is in error
                } else if (learners[i].getTrainingState() == LearningAlgorithm.TrainingState.ERROR) {
                    setSystemTrainingState(LearningSystemTrainingState.ERROR);
                    return;
                }
            }
        }
        if (numTrained == 0) {
            setSystemTrainingState(LearningSystemTrainingState.NOT_TRAINED);
            return;
        } else {
            setSystemTrainingState(LearningSystemTrainingState.TRAINED); // could consider adding SOME_TRAINED state
        }
    }

    protected void updateInitializationState() {
        if (learners == null || learners.length == 0) {
            setInitializationState(LearningAlgorithmsInitializationState.NOT_INITIALIZED);
            return;
        }

        int numInit = 0;
        for (int i = 0; i < learners.length; i++) {
            if (learners[i] != null) {
                numInit++;
            }
        }
        if (numInit == 0) {
            setInitializationState(LearningAlgorithmsInitializationState.NOT_INITIALIZED);
        } else if (numInit < numParams) {
            setInitializationState(LearningAlgorithmsInitializationState.SOME_INITIALIZED);
        } else {
            setInitializationState(LearningAlgorithmsInitializationState.ALL_INITIALIZED);
        }
    }

    // protected LearningAlgorithmsInitializationState state = LearningAlgorithmsInitializationState.NOT_INITIALIZED;
    // public static final String PROP_STATE = "state";
    private void learnerPropertyChanged(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(LearningAlgorithm.PROP_TRAININGSTATE)) {
            LearningAlgorithm.TrainingState newState =
                    (LearningAlgorithm.TrainingState) evt.getNewValue();
            LearningAlgorithm.TrainingState oldState =
                    (LearningAlgorithm.TrainingState) evt.getOldValue();
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

        updateInitializationState();
        updateTrainingState();
        fireLearnerChanged();
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
        if (learners[index] != null) {
            learners[index].removePropertyChangeListener(learnerChangeListener);
        }

        this.learners[index] = newLearners;
        if (this.learners[index] != null) {
            this.learners[index].addPropertyChangeListener(learnerChangeListener);
        }
        updateInitializationState();
        updateTrainingState();
        fireLearnerChanged();
    }

    public LearningSystem(int numParams) {
        this.numParams = numParams;
        learners = new LearningAlgorithm[numParams];
        learnerEnabled = new boolean[numParams];
        paramUsingDistribution = new boolean[numParams];
        cvResults = new double[numParams];
        trainResults = new double[numParams];

        numMaxValsForParameter = ChuckSystem.getChuckSystem().getNumSynthMaxParamVals();
        for (int i = 0; i < numParams; i++) {
            if (ChuckSystem.getChuckSystem().isIsParamDiscrete(i)) {
                paramUsingDistribution[i] = ChuckSystem.getChuckSystem().isDoesParamUseDistribution(i);
            } else {
                paramUsingDistribution[i] = false;
            }
        }

        outputs = new double[numParams];
        setOutputsArray(paramUsingDistribution, numMaxValsForParameter);
        paramMask = new boolean[numParams];
        for (int i = 0; i < numParams; i++) {
            learnerEnabled[i] = true;
            paramMask[i] = true;
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
        SimpleDataset oldDataset = this.dataset;
        this.dataset = dataset;

        propertyChangeSupport.firePropertyChange(PROP_DATASET, oldDataset, dataset);
        updateDatasetState();
    }

    public void computeTrainingAccuracyInBackground() throws Exception {
        computeTrainingAccuracyInBackground(-1);
    }

    public void computeTrainingAccuracyInBackground(int paramNum) throws Exception {
        synchronized (this) {
            if (evaluationState != EvaluationState.EVALUTATING && systemTrainingState == LearningSystemTrainingState.TRAINED) {
                setEvaluationState(evaluationState.EVALUTATING);
                evaluationWorker = new EvaluationWorker();
                learnerToEvaluate = paramNum;
                evaluationType = EvaluationType.TRAINING;
                evaluationWorker.execute();
            } else {
                throw new Exception("Cannot evaluate; either already evaluating, or not trained");
            }
        }
    }

    public void computeCVAccuracyInBackground(int numFolds) throws Exception {
        computeCVAccuracyInBackground(-1, numFolds);
    }

    public void computeCVAccuracyInBackground(int paramNum, int numFolds) throws Exception {
        synchronized (this) {
            if (evaluationState != EvaluationState.EVALUTATING && systemTrainingState == LearningSystemTrainingState.TRAINED) {
                evaluationWorker = new EvaluationWorker();
                setEvaluationState(evaluationState.EVALUTATING);
                learnerToEvaluate = paramNum;
                this.numFolds = numFolds;
                evaluationType = EvaluationType.CV;
                evaluationWorker.execute();
            } else {
                throw new Exception("Cannot evaluate; either already evaluating, or not trained");
            }
        }
    }

    public void computeCVAccuracy(int numFolds) throws Exception {
        if (evaluationState != EvaluationState.EVALUTATING && systemTrainingState == LearningSystemTrainingState.TRAINED) {
            setEvaluationState(EvaluationState.EVALUTATING);
            double[] a = new double[numParams];
            for (int i = 0; i < numParams; i++) {
                if (learners[i] != null) {
                    a[i] = learners[i].computeCVAccuracy(numFolds, dataset.getClassifiableInstances(i));
                    setCvResults(i, a[i]);
                }
            }
            setEvaluationState(EvaluationState.EVALUTATING.DONE_EVALUATING);
        } else {
            throw new Exception("Cannot evaluate; either already evaluating, or not trained");
        }
    }

    protected void updateDatasetState() {
            if (dataset.isHasInstances()) {
                setDatasetState(DatasetState.HAS_DATA);
            } else {
                setDatasetState(DatasetState.NO_DATA);
            }

    }


    protected void datasetChanged(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(SimpleDataset.PROP_HASINSTANCES)) {
            updateDatasetState();
        }
    }

    public int getNumLearnersTrained() {
        int num = 0;
        for (int i = 0; i < learners.length; i++) {
            if (learners[i].getTrainingState() == LearningAlgorithm.TrainingState.TRAINED) {
                num++;
            }
        }
        return num;
    }

    public int[] getNumMaxValsForParameter() {
        return numMaxValsForParameter;
    }

    public void setNumMaxValsForParameter(int[] numMaxValsForParameter) {
        this.numMaxValsForParameter = numMaxValsForParameter;
        setOutputsArray(paramUsingDistribution, numMaxValsForParameter);
    }

    /**
     * Get the value of paramUsingDistribution
     *
     * @return the value of paramUsingDistribution
     */
    public boolean[] isParamUsingDistribution() {
        return paramUsingDistribution;
    }

    /**
     * Set the value of paramUsingDistribution
     *
     * @param paramUsingDistribution new value of paramUsingDistribution
     */
    public void setParamUsingDistribution(boolean[] paramUsingDistribution) {
        this.paramUsingDistribution = paramUsingDistribution;
        setOutputsArray(paramUsingDistribution, numMaxValsForParameter);
    }

    protected void setOutputsArray(boolean[] paramUsingDistribution, int[] maxParamVals) {
        int numOutputs = 0;
        for (int i = 0; i < numParams; i++) {
            if (paramUsingDistribution[i]) {
                numOutputs += maxParamVals[i];
            } else {
                numOutputs++;
            }
        }
        if (numOutputs != outputs.length) {
            outputs = new double[numOutputs];
        }
    }

    /**
     * Get the value of paramUsingDistribution at specified index
     *
     * @param index
     * @return the value of paramUsingDistribution at specified index
     */
    public boolean isParamUsingDistribution(int index) {
        return this.paramUsingDistribution[index];
    }

    /**
     * Set the value of paramUsingDistribution at specified index.
     *
     * @param index
     * @param newParamUsingDistribution new value of paramUsingDistribution at specified index
     */
    public void setParamUsingDistribution(int index, boolean newParamUsingDistribution) {
        this.paramUsingDistribution[index] = newParamUsingDistribution;
    }

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

    public void addLearnerChangeListener(ChangeListener l) {
        listenerList.add(ChangeListener.class, l);
    }

    public void removeLearnerChangeListener(ChangeListener l) {
        listenerList.remove(ChangeListener.class, l);
    }

    protected void fireLearnerChanged() {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ChangeListener.class) {
                if (changeEvent == null) {
                    changeEvent = new ChangeEvent(this);

                }
                ((ChangeListener) listeners[i + 1]).stateChanged(changeEvent);
            }
        }
    }

    protected class EvaluationWorker extends SwingWorker<Integer, Void> {

        @Override
        protected Integer doInBackground() throws Exception {
            if (dataset.getNumDatapoints() > 0) {
                setProgress(1);

                if (learnerToEvaluate == -1) {
                    for (int i = 0; i < numParams; i++) {
                        setProgress(2 + i);
                        double d;
                        if (evaluationType == EvaluationType.CV) {
                            d = learners[i].computeCVAccuracy(numFolds, dataset.getClassifiableInstances(i));
                        } else {
                            d = learners[i].computeAccuracy(dataset.getClassifiableInstances(i));
                        }
                        if (evaluationType == EvaluationType.CV) {
                            setCvResults(i, d);
                        } else {
                            setTrainResults(i, d);
                        }
                    }
                    setProgress(numParams + 2);
                    return new Integer(0);
                } else {
                    setProgress(2);
                    double d;
                    if (evaluationType == EvaluationType.CV) {
                        d = learners[learnerToEvaluate].computeCVAccuracy(numFolds, dataset.getClassifiableInstances(learnerToEvaluate));
                    } else {
                        d = learners[learnerToEvaluate].computeAccuracy(dataset.getClassifiableInstances(learnerToEvaluate));
                    }
                    if (evaluationType == EvaluationType.CV) {
                        setCvResults(learnerToEvaluate, d);
                    } else {
                        setTrainResults(learnerToEvaluate, d);
                    }

                    setProgress(3); //TODO: fix progress if using it
                    return new Integer(0);
                }
            } else {
                setProgress(0);
                System.out.println("Nothing to do: no instances yet"); //TODO log
                return new Integer(0);
            }
        }

        @Override
        protected void done() {
            setEvaluationState(EvaluationState.DONE_EVALUATING);
        }
    }

    public static LearningSystem readFromFile(File f) throws Exception {
        final LearningSystem ls = (LearningSystem)SerializedFileUtil.readFromFile(f);
        ls.listenerList = new EventListenerList();
                //System.out.println("Dataset state is " + ls.datasetState);

        LearningAlgorithm[] algs = ls.getLearners();

        ls.learnerChangeListener = new PropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent evt) {
            ls.learnerPropertyChanged(evt);
        }
    };
        ls.datasetListener = new PropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent evt) {
            ls.datasetChanged(evt);
        }
    };

    //TODO: what about score player?

        ls.setLearners(algs); //re-adds property change listeners!

        return ls;
    }

    public void writeToFile(File f) throws Exception {
        System.out.println("Dataset state is " + datasetState);
        SerializedFileUtil.writeToFile(f, this);
    }
}

