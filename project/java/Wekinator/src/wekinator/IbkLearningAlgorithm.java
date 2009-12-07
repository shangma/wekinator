/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package wekinator;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.classifiers.lazy.IBk;
import weka.core.Instance;
import weka.core.Instances;
import wekinator.util.DeepCopy;
import wekinator.util.SerializedFileUtil;

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
    protected void setTrainingState(TrainingState trainingState) {
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
        try {
            return (IbkLearningAlgorithm) DeepCopy.copy(this);
        } catch (IOException ex) {
            Logger.getLogger(IbkLearningAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(IbkLearningAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
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
        throw new UnsupportedOperationException("Not supported for IbK");
    }

    public LearningAlgorithm readFromFile(File f) throws Exception {
       return (IbkLearningAlgorithm) SerializedFileUtil.readFromFile(f);
    }

    public void saveToFile(File f) throws Exception {
        SerializedFileUtil.writeToFile(f, this);
    }

    public boolean implementsFastAccurate() {
        return false;
    }

    public String getName() {
        return "K-Nearest Neighbor";
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
