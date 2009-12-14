/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package wekinator.LearningAlgorithms;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import javax.swing.JPanel;
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
        NOT_TRAINED, TRAINING, TRAINED, ERROR
    };
    /**
     * Add PropertyChangeListener.
     *
     * @param listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener);

    public LearningAlgorithm copy();

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

    public LearningAlgorithmSettingsPanel getSettingsPanel();

    public void setFastAccurate(double value);

    public boolean implementsFastAccurate();

    public String getName();

    public double classify(Instance instance) throws Exception;

    public double[] distributionForInstance(Instance instance) throws Exception;

    public void train(Instances instances) throws Exception;

    public void forget();

    public double computeAccuracy(Instances instances) throws Exception;

    public double computeCVAccuracy(int numFolds, Instances instances) throws Exception;

}
