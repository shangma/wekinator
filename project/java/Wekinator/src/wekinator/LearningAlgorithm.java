/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package wekinator;

import java.beans.PropertyChangeListener;
import java.io.File;
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
public interface LearningAlgorithm extends Serializable {
    public String PROP_TRAININGSTATE = "trainingState";

    public enum TrainingState {
        NOT_TRAINED, TRAINING, TRAINED
    };
    /**
     * Add PropertyChangeListener.
     *
     * @param listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener);

    public LearningAlgorithm copy();

    public LearningAlgorithm readFromFile(File f) throws Exception;

    public void saveToFile(File f) throws Exception;

   // Classifier getClassifier(); // Don't want to do this!

    /**
     * Get the value of trainingState
     *
     * @return the value of trainingState
     */
    public TrainingState getTrainingState();

    /**
     * Remove PropertyChangeListener.
     *
     * @param listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener);

    public void showSettingsFrame();

    public void setFastAccurate();

    public boolean implementsFastAccurate();

    public String getName();

    public String[] getFeatureNames();

    public void setFeatureNames(String[] s);

    public String getFeatureName(int i);

    public void setFeatureName(String s, int i);

    public int getNumFeatures();

    public void setNumFeatures(); //TODO: use in constructor?

    public double classify(Instance instance);

    public void train(Instances instances);

    public void forget();

    public double getLastTrainingAccuracy();

    public double computeCVAccuracy(int numFolds);
}
