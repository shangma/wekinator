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
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.supportVector.Kernel;
import weka.classifiers.functions.supportVector.PolyKernel;
import weka.classifiers.functions.supportVector.RBFKernel;
import weka.core.Instance;
import weka.core.Instances;
import wekinator.LearningAlgorithms.LearningAlgorithm.TrainingState;


/**
 *
 * @author rebecca
 */
public class SMOLearningAlgorithm implements ClassifierLearningAlgorithm {
    protected SMO classifier = null;
    protected TrainingState trainingState = TrainingState.NOT_TRAINED;
    protected SMOSettingsPanel myPanel = null;
//    protected int defaultNumRounds = 100; Any default params?

    public SMOLearningAlgorithm() {
        initClassifier();
        myPanel = new SMOSettingsPanel(this);
    }

    protected SMOLearningAlgorithm(SMO c) {
        classifier = c;
        setTrainingState(TrainingState.TRAINED);
        myPanel = new SMOSettingsPanel(this);
    }

    protected void initClassifier() {
        classifier = new SMO();
        PolyKernel k = new PolyKernel();
        k.setExponent(2.0);
        classifier.setKernel(k);
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

    public SMO getClassifier() {
        return classifier;
    }

    public SMOLearningAlgorithm copy() {
        try {
            SMOLearningAlgorithm la = new SMOLearningAlgorithm();
            la.setTrainingState(trainingState);
            la.classifier = (SMO) Classifier.makeCopy(classifier);
            return la;
        } catch (Exception ex) {
            Logger.getLogger(SMOLearningAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
       
    }

    public String getName() {
        return "Support Vector Machine";
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

    public JPanel getSettingsPanel() {
        return myPanel;
    }

    public void train(Instances instances) throws Exception {
        setTrainingState(TrainingState.TRAINING);
        try {
            ClassifierLearningAlgorithmUtil.train(this, instances);
        } catch (Exception ex) {
            Logger.getLogger(SMOLearningAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
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

    public void setLinearKernel() {
        Kernel k = classifier.getKernel();
        if (k instanceof PolyKernel && ((PolyKernel)k).getExponent() == 1.0) {
            return; // already got it
        }
        else {
            PolyKernel nk = new PolyKernel();
            nk.setExponent(1.0);
            classifier.setKernel(nk);
        }
    }

    public void setPolyKernel(double e, boolean useLowerOrder) {
        Kernel k = classifier.getKernel();
        if (k instanceof PolyKernel) {
            ((PolyKernel)k).setExponent(e);
            ((PolyKernel)k).setUseLowerOrder(useLowerOrder);
            return;
        }
        else {
            PolyKernel nk = new PolyKernel();
            nk.setExponent(e);
            nk.setUseLowerOrder(useLowerOrder);
            classifier.setKernel(nk);
        }
    }

    public void setRbfKernel(double gamma) {
        Kernel k = classifier.getKernel();
        if (k instanceof RBFKernel) {
            ((RBFKernel)k).setGamma(gamma);
            return;
        }
        else {
            RBFKernel nk = new RBFKernel();
            nk.setGamma(gamma);
            classifier.setKernel(nk);
        }
    }
}
