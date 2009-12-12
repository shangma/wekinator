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

/**
 *
 * @author rebecca
 */
public class OtherClassifierLearningAlgorithm implements ClassifierLearningAlgorithm {
    protected Classifier classifier = null;
    protected TrainingState trainingState = TrainingState.NOT_TRAINED;
    protected OtherClassifierSettingsPanel myPanel = null;

    protected OtherClassifierLearningAlgorithm(Classifier c) {
        classifier = c;
        setTrainingState(TrainingState.TRAINED);
        myPanel = new OtherClassifierSettingsPanel(this);
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

    public Classifier getClassifier() {
        return classifier;
    }

    public LearningAlgorithm copy() {
        try {
            Classifier newc = Classifier.makeCopy(classifier);
            OtherClassifierLearningAlgorithm la = new OtherClassifierLearningAlgorithm(newc);
            la.setTrainingState(trainingState);
            return la;
        } catch (Exception ex) {
            Logger.getLogger(OtherClassifierLearningAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
       
    }

    public String getName() {
        return classifier.getClass().getSimpleName();
    }

    public void setFastAccurate(double value) {
        return;
    }

    public boolean implementsFastAccurate() {
        return false;
    }

    public void forget() {
        setTrainingState(trainingState.NOT_TRAINED); //better than disallowing this
    }

    public JPanel getSettingsPanel() {
        return myPanel;
    }

    public void train(Instances instances) throws Exception {
        setTrainingState(TrainingState.TRAINING);
        try {
            ClassifierLearningAlgorithmUtil.train(this, instances);
        } catch (Exception ex) {
            Logger.getLogger(OtherClassifierLearningAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
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
