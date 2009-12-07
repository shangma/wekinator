/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package wekinator;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Controls wekinator instance (state)
 *
 * @author rebecca
 */
public class WekinatorLearningManager {

    private static WekinatorLearningManager ref = null;
    protected double[] params;
    protected boolean[] mask;
    protected LearningSystem learningSystem = null;
    public static final String PROP_LEARNINGSYSTEM = "learningSystem";

    protected FeatureConfiguration featureConfiguration = null;
    public static final String PROP_FEATURECONFIGURATION = "featureConfiguration";

    protected double[] p2 = null;
    public static final String PROP_PARAMS = "params";



    /**
     * Set the value of p2
     *
     * @param p2 new value of p2
     */
    public void setParams(double[] params) {
        double[] oldparams = this.params;
        this.params = params;
        propertyChangeSupport.firePropertyChange(PROP_PARAMS, oldparams, p2);
    }

    /**
     * Get the value of p2 at specified index
     *
     * @param index
     * @return the value of p2 at specified index
     */
    public double getP2(int index) {
        return this.p2[index];
    }

  


    protected PropertyChangeListener learningSystemPropertyChange = new PropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent evt) {
            learningSystemPropertyChanged(evt);
        }
    };

    public enum InitializationState {
        NOT_INITIALIZED,
        INITIALIZED
    };
    protected InitializationState initState = InitializationState.NOT_INITIALIZED;
    public static final String PROP_INITSTATE = "initState";

    public enum Mode {
        DATASET_CREATION,
        TRAINING,
        RUNNING,
        NONE
    };

    protected Mode mode = Mode.NONE;
    public static final String PROP_MODE = "mode";
    protected double[] outputs = null;
    public static final String PROP_OUTPUTS = "outputs";


    /**
     * Get the value of params
     *
     * @return the value of params
     */
    public double[] getParams() {
        return params;
    }

    public void setParamsAndMask(double[] params, boolean[] mask) {
        setParams(params);
        this.mask = mask;
    }
    /**
     * Get the value of params at specified index
     *
     * @param index
     * @return the value of params at specified index
     */
    public double getParams(int index) {
        return this.params[index];
    }


    /**
     * Get the value of outputs
     *
     * @return the value of outputs
     */
    public double[] getOutputs() {
        return outputs;
    }

    /**
     * Set the value of outputs
     *
     * @param outputs new value of outputs
     */
    protected void setOutputs(double[] outputs) {
        double[] oldOutputs = this.outputs;
        this.outputs = outputs;
        propertyChangeSupport.firePropertyChange(PROP_OUTPUTS, oldOutputs, outputs);
    }

    /**
     * Get the value of outputs at specified index
     *
     * @param index
     * @return the value of outputs at specified index
     */
    public double getOutputs(int index) {
        return this.outputs[index];
    }

    /**
     * Set the value of outputs at specified index.
     *
     * @param index
     * @param newOutputs new value of outputs at specified index
     */
    protected void setOutputs(int index, double newOutputs) {
        double oldOutputs = this.outputs[index];
        this.outputs[index] = newOutputs;
        propertyChangeSupport.fireIndexedPropertyChange(PROP_OUTPUTS, index, oldOutputs, newOutputs);
    }

    /**
     * Get the value of mode
     *
     * @return the value of mode
     */
    public Mode getMode() {
        return mode;
    }

    /**
     * Set the value of mode
     *
     * @param mode new value of mode
     */
    protected void setMode(Mode mode) {
        Mode oldMode = this.mode;
        this.mode = mode;
        propertyChangeSupport.firePropertyChange(PROP_MODE, oldMode, mode);
    }

    public void startDatasetCreation() {
        if (mode == Mode.RUNNING) {
            stopRunning();
        } if (mode == Mode.TRAINING) {
            stopTraining();
        }
        setMode(Mode.DATASET_CREATION);
    }

    public void stopRunning() {
        setMode(Mode.NONE);
    }

    public void startRunning() {
        if (mode == Mode.TRAINING) {
            return;
        }
        if (mode == Mode.DATASET_CREATION) {
            stopDatasetCreation();
        }
        setMode(Mode.RUNNING);
    }

    public void stopDatasetCreation() {
        setMode(Mode.NONE);
    }

    public void stopTraining() {
        //TODO: cancel training here
        setMode(Mode.NONE);
    }

    public void updateFeatures(double[] features) {
        if (mode == Mode.DATASET_CREATION) {
            learningSystem.addToTraining(features, params);
        } else if (mode == Mode.RUNNING) {
            //classify these features
            setOutputs(learningSystem.classify(featureConfiguration.process(features)));
        }
    }

    /**
     * Get the value of initState
     *
     * @return the value of initState
     */
    public InitializationState getInitState() {
        return initState;
    }

    /**
     * Set the value of initState
     *
     * @param initState new value of initState
     */
    protected void setInitState(InitializationState initState) {
        InitializationState oldInitState = this.initState;
        this.initState = initState;
        propertyChangeSupport.firePropertyChange(PROP_INITSTATE, oldInitState, initState);
    }



    /**
     * Get the value of featureConfiguration
     *
     * @return the value of featureConfiguration
     */
    public FeatureConfiguration getFeatureConfiguration() {
        return featureConfiguration;
    }

    /**
     * Set the value of featureConfiguration
     *
     * @param featureConfiguration new value of featureConfiguration
     */
    public void setFeatureConfiguration(FeatureConfiguration featureConfiguration) {
        FeatureConfiguration oldFeatureConfiguration = this.featureConfiguration;
        this.featureConfiguration = featureConfiguration;
        propertyChangeSupport.firePropertyChange(PROP_FEATURECONFIGURATION, oldFeatureConfiguration, featureConfiguration);
        updateMyState();
    }

    /**
     * Get the value of learningSystem
     *
     * @return the value of learningSystem
     */
    public LearningSystem getLearningSystem() {
        return learningSystem;
    }

    /**
     * Set the value of learningSystem
     *
     * @param learningSystem new value of learningSystem
     */
    public void setLearningSystem(LearningSystem learningSystem) {

        LearningSystem oldLearningSystem = this.learningSystem;
        if (oldLearningSystem != null) {
            oldLearningSystem.removePropertyChangeListener(learningSystemPropertyChange);
        }
        this.learningSystem = learningSystem;
        if (learningSystem != null) {
            learningSystem.addPropertyChangeListener(learningSystemPropertyChange);
        }
        propertyChangeSupport.firePropertyChange(PROP_LEARNINGSYSTEM, oldLearningSystem, learningSystem);
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


    public static synchronized WekinatorLearningManager getInstance() {
        if (ref == null) {
            ref = new WekinatorLearningManager();
        }
        return ref;
    }

    private WekinatorLearningManager() {
        
    }

    static Object getWekinatorManager() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    private void learningSystemPropertyChanged(PropertyChangeEvent evt) {
        updateMyState();
    }

    protected void updateMyState() {
        if (learningSystem != null && featureConfiguration != null
                && learningSystem.getInitializationState() == LearningSystem.LearningAlgorithmsInitializationState.ALL_INITIALIZED) {
            setInitState(InitializationState.NOT_INITIALIZED);
        } else {
            setInitState(InitializationState.INITIALIZED);
        }
                
    }

}
