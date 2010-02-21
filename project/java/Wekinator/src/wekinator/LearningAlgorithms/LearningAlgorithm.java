/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package wekinator.LearningAlgorithms;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Same trained model should be able to be used
 * when more features are added to the extracted set (ignoring new features).
 *
 * Store feature identities here??
 *
 * @author rebecca
 */
public abstract class LearningAlgorithm implements Serializable {
    public static String PROP_TRAININGSTATE = "trainingState";
    protected TrainingState trainingState = TrainingState.NOT_TRAINED;


    public enum TrainingState {
        NOT_TRAINED, TRAINING, TRAINED
    };

        /**
     * Get the value of trainingState
     *
     * @return the value of trainingState
     */
    public TrainingState getTrainingState() {
        return trainingState;
    }

    /**
     * Set the value of trainingState
     *
     * @param trainingState new value of trainingState
     */
    protected void setTrainingState(TrainingState trainingState) {
        TrainingState oldTrainingState = this.trainingState;
        this.trainingState = trainingState;
        propertyChangeSupport.firePropertyChange(PROP_TRAININGSTATE, oldTrainingState, trainingState);
    }
    protected PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);


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

    public abstract LearningAlgorithm copy();

    public abstract LearningAlgorithmSettingsPanel getSettingsPanel();

    public void setFastAccurate(double value) {
        //Underling classes only implement if implementsFastAccurate is true
    };

    public boolean implementsFastAccurate() { return false; }

    public abstract String getName();

    public abstract double classify(Instance instance) throws Exception;

    public abstract double[] distributionForInstance(Instance instance) throws Exception;

    public abstract void train(Instances instances) throws Exception;

    public abstract void forget();

    public abstract double computeAccuracy(Instances instances) throws Exception;

    public abstract double computeCVAccuracy(int numFolds, Instances instances) throws Exception;
    
    public abstract void writeToOutputStream(ObjectOutputStream o) throws IOException;

   // public static LearningAlgorithm readFromInputStream(ObjectInputStream i) throws IOException, ClassNotFoundException;

}
