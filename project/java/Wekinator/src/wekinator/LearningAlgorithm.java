/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package wekinator;

import java.beans.PropertyChangeListener;
import java.io.Serializable;
import weka.classifiers.Classifier;

/**
 *
 * @author rebecca
 */
public interface LearningAlgorithm extends Serializable {
    String PROP_TRAININGSTATE = "trainingState";

    public enum TrainingState {
        NOT_TRAINED, TRAINING, TRAINED
    };
    /**
     * Add PropertyChangeListener.
     *
     * @param listener
     */
    void addPropertyChangeListener(PropertyChangeListener listener);

    LearningAlgorithm copy();

   // Classifier getClassifier(); // Don't want to do this!

    /**
     * Get the value of trainingState
     *
     * @return the value of trainingState
     */
    TrainingState getTrainingState();

    LearningAlgorithm loadFromSerializedWekaClassifier(String filename);

    /**
     * Remove PropertyChangeListener.
     *
     * @param listener
     */
    void removePropertyChangeListener(PropertyChangeListener listener);

    void saveAsSerializedWekaClassifier(String filename);

    void showSettingsFrame();

    void setFastAccurate();

    

}
