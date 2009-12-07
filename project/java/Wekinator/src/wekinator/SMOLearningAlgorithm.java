/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package wekinator;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;
import wekinator.LearningAlgorithm.TrainingState;

/**
 *
 * @author rebecca
 */
public class SMOLearningAlgorithm implements ClassifierLearningAlgorithm {

   protected TrainingState trainingState = TrainingState.NOT_TRAINED;

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

    public Classifier getClassifier() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public LearningAlgorithm copy() {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    public LearningAlgorithm loadFromSerializedWekaClassifier(String filename) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    public void saveAsSerializedWekaClassifier(String filename) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void showSettingsFrame() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setFastAccurate() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public LearningAlgorithm readFromFile(File f) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void saveToFile(File f) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean implementsFastAccurate() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getName() {
        return "Support Vector Machine";
    }

    public String[] getFeatureNames() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setFeatureNames(String[] s) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getFeatureName(int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setFeatureName(String s, int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getNumFeatures() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setNumFeatures() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public double classify(Instance instance) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void train(Instances instances) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void forget() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public double getLastTrainingAccuracy() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public double computeCVAccuracy(int numFolds) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
