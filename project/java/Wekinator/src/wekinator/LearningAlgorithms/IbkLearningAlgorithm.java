/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package wekinator.LearningAlgorithms;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;
import wekinator.LearningAlgorithms.LearningAlgorithm.TrainingState;
import weka.classifiers.lazy.IBk;

/**
 *
 * @author rebecca
 */
public class IbkLearningAlgorithm implements ClassifierLearningAlgorithm {
    protected IBk classifier = null;
    protected TrainingState trainingState = TrainingState.NOT_TRAINED;
    protected transient IbkSettingsPanel myPanel = null;
    protected int defaultNumNeighbors = 10;

    public IbkLearningAlgorithm() {
        initClassifier();
        myPanel = new IbkSettingsPanel(this);
    }

    protected IbkLearningAlgorithm(IBk c) {
        classifier = c;
        setTrainingState(TrainingState.TRAINED);
        myPanel = new IbkSettingsPanel(this);
    }

    protected void initClassifier() {
        classifier = new IBk();
        classifier.setKNN(defaultNumNeighbors);
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

    public IBk getClassifier() {
        return classifier;
    }

    public LearningAlgorithm copy() {
        try {
            IbkLearningAlgorithm la = new IbkLearningAlgorithm();
            la.setTrainingState(trainingState);
            la.classifier = (IBk) Classifier.makeCopy(classifier);
            return la;
        } catch (Exception ex) {
            Logger.getLogger(IbkLearningAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
       
    }

    public String getName() {
        return "k-nearest neighbor";
    }

    public void setFastAccurate(double value) {
       return;
    }

    public boolean implementsFastAccurate() {
        return false;
    }

    public void forget() {
        initClassifier();
        setTrainingState(trainingState.NOT_TRAINED);
    }

    public IbkSettingsPanel getSettingsPanel() {

            if (myPanel == null) {
            myPanel = new IbkSettingsPanel(this);
        }
            return myPanel;
    }

    public void train(Instances instances) throws Exception {
        if (instances.numInstances() == 0) {
            return;
        }
        setTrainingState(TrainingState.TRAINING);
        try {
            ClassifierLearningAlgorithmUtil.train(this, instances);
        } catch (Exception ex) {
            Logger.getLogger(IbkLearningAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
            setTrainingState(trainingState.NOT_TRAINED);
            throw new Exception(ex);
        }
        setTrainingState(TrainingState.TRAINED);
    }

    public double classify(Instance instance) throws Exception {
        return ClassifierLearningAlgorithmUtil.classify(this, instance);
    }

    public double computeAccuracy(Instances instances) throws Exception {
        return ClassifierLearningAlgorithmUtil.computeAccuracy(this, instances);
    }

    public double computeCVAccuracy(int numFolds, Instances instances) throws Exception {
        return ClassifierLearningAlgorithmUtil.computeCVAccuracy(this, numFolds, instances);
    }

    public double[] distributionForInstance(Instance instance) throws Exception {
        return ClassifierLearningAlgorithmUtil.distributionForInstance(this, instance);
    }

}
