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
import weka.classifiers.trees.J48;
import weka.core.Instance;
import weka.core.Instances;
import wekinator.LearningAlgorithms.LearningAlgorithm.TrainingState;


/**
 *
 * @author rebecca
 */
public class J48LearningAlgorithm implements ClassifierLearningAlgorithm {
    protected J48 classifier = null;
    protected TrainingState trainingState = TrainingState.NOT_TRAINED;
    protected transient J48SettingsPanel myPanel = null;
//    protected int defaultNumRounds = 100; Any default params?

    public J48LearningAlgorithm() {
        initClassifier();
        myPanel = new J48SettingsPanel(this);
    }

    protected J48LearningAlgorithm(J48 c) {
        classifier = c;
        setTrainingState(TrainingState.TRAINED);
        myPanel = new J48SettingsPanel(this);
    }

    protected void initClassifier() {
        classifier = new J48();
        //Any settings?
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

    public J48 getClassifier() {
        return classifier;
    }

    public LearningAlgorithm copy() {
        try {
            J48LearningAlgorithm la = new J48LearningAlgorithm();
            la.setTrainingState(trainingState);
            la.classifier = (J48) Classifier.makeCopy(classifier);
            return la;
        } catch (Exception ex) {
            Logger.getLogger(J48LearningAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
       
    }

    public String getName() {
        return "J48 Decision Tree";
    }

    public void setFastAccurate(double value) {
         return; //TODO?
    }

    public boolean implementsFastAccurate() {
        return false;
    }

    public void forget() {
        initClassifier();
        setTrainingState(trainingState.NOT_TRAINED);
    }

    public J48SettingsPanel getSettingsPanel() {
                if (myPanel == null) {
            myPanel = new J48SettingsPanel(this);
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
            Logger.getLogger(J48LearningAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
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
