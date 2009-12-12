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
import weka.classifiers.Evaluation;
import weka.core.Instance;
import weka.core.Instances;
import wekinator.LearningAlgorithms.LearningAlgorithm.TrainingState;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.trees.DecisionStump;
import weka.classifiers.trees.j48.ClassifierTree;

/**
 *
 * @author rebecca
 */
public class AdaboostM1LearningAlgorithm implements ClassifierLearningAlgorithm {
    protected AdaBoostM1 classifier = null;
    protected TrainingState trainingState = TrainingState.NOT_TRAINED;
    protected AdaboostM1SettingsPanel myPanel = null;
    protected int defaultNumRounds = 100;

    public AdaboostM1LearningAlgorithm() {
        initClassifier();
        myPanel = new AdaboostM1SettingsPanel(this);
    }

    protected AdaboostM1LearningAlgorithm(AdaBoostM1 adaBoostM1) {
        classifier = adaBoostM1;
        setTrainingState(TrainingState.TRAINED);
        myPanel = new AdaboostM1SettingsPanel(this);
    }

    protected void initClassifier() {
        classifier = new AdaBoostM1();
        classifier.setClassifier(new DecisionStump());
        classifier.setNumIterations(defaultNumRounds);
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

    public AdaBoostM1 getClassifier() {
        return classifier;
    }

    public LearningAlgorithm copy() {
        try {
            AdaboostM1LearningAlgorithm la = new AdaboostM1LearningAlgorithm();
            la.setTrainingState(trainingState);
            la.classifier = (AdaBoostM1) Classifier.makeCopy(classifier);
            return la;
        } catch (Exception ex) {
            Logger.getLogger(AdaboostM1LearningAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
       
    }

    public String getName() {
        return "AdaboostM.1";
    }

    public void setFastAccurate(double value) {
        if (value >= 0 && value <= 1)
            classifier.setNumIterations((int)(value * 490 + 10));
    }

    public boolean implementsFastAccurate() {
        return true;
    }

    public void forget() {
        initClassifier();
        setTrainingState(trainingState.NOT_TRAINED);
    }

    public JPanel getSettingsPanel() {
        return myPanel;
    }

    public void train(Instances instances) throws Exception {
        setTrainingState(TrainingState.TRAINING);
        try {
            ClassifierLearningAlgorithmUtil.train(this, instances);
        } catch (Exception ex) {
            Logger.getLogger(AdaboostM1LearningAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
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
