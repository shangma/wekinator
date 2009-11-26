/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package wekinator;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import weka.classifiers.lazy.IBk;

/**
 *
 * @author rebecca
 */
public class IbkLearningAlgorithm implements ClassifierLearningAlgorithm {

    IBk knn = null;
    KnnSettingsFrame settingsFrame = null;
    
    protected TrainingState trainingState = TrainingState.NOT_TRAINED;


    public IbkLearningAlgorithm() {
        knn = new IBk();
        //TODO: init settingsPanel?
    }


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
    private void setTrainingState(TrainingState trainingState) {
        TrainingState oldTrainingState = this.trainingState;
        this.trainingState = trainingState;
        propertyChangeSupport.firePropertyChange(PROP_TRAININGSTATE, oldTrainingState, trainingState);
    }
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);


    public IBk getClassifier() {
        return knn;
    }

    public IbkLearningAlgorithm loadFromSerializedWekaClassifier(String filename) {
        //TODO: Implement this! 
        return null;
    }

    public void saveAsSerializedWekaClassifier(String filename) {
        //TODO: implement
    }
    
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

    public IbkLearningAlgorithm copy() {
        //TODO; see Weka's approach using serialization!
        return null;
    }

    public void showSettingsFrame() {
        if (settingsFrame != null) {
            settingsFrame = new KnnSettingsFrame();
            settingsFrame.setLearner(this);
        }
        if (! settingsFrame.isVisible()) {
            settingsFrame.setVisible(true);
        } else {
            settingsFrame.toFront();
        }
    }

    public void setFastAccurate() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    


}
