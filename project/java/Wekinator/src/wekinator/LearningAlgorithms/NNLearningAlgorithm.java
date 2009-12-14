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
import java.util.Random;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.Instance;
import weka.core.Instances;
import wekinator.LearningAlgorithms.LearningAlgorithm.TrainingState;

/**
 *
 * @author rebecca
 */
public class NNLearningAlgorithm implements ClassifierLearningAlgorithm {
    protected MultilayerPerceptron classifier = null;
    protected TrainingState trainingState = TrainingState.NOT_TRAINED;
    protected transient NNSettingsPanel myPanel = null;
    protected int defaultNumNeighbors = 10;

    public NNLearningAlgorithm() {
        initClassifier();
        myPanel = new NNSettingsPanel(this);
    }

    protected NNLearningAlgorithm(MultilayerPerceptron c) {
        classifier = c;
        setTrainingState(TrainingState.TRAINED);
        myPanel = new NNSettingsPanel(this);
    }

    protected void initClassifier() {
        classifier = new MultilayerPerceptron();
            //TODO: settings
    }

    public void setUseGui(boolean use) {
        classifier.setGUI(use);
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

    public MultilayerPerceptron getClassifier() {
        return classifier;
    }

    public LearningAlgorithm copy() {
        try {
            NNLearningAlgorithm la = new NNLearningAlgorithm();
            la.setTrainingState(trainingState);
            la.classifier = (MultilayerPerceptron) Classifier.makeCopy(classifier);
            return la;
        } catch (Exception ex) {
            Logger.getLogger(NNLearningAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
       
    }

    public String getName() {
        return "Neural Network";
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

    public NNSettingsPanel getSettingsPanel() {
                if (myPanel == null) {
            myPanel = new NNSettingsPanel(this);
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
            Logger.getLogger(NNLearningAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
            setTrainingState(trainingState.ERROR);
            throw new Exception(ex);
        }
        setTrainingState(TrainingState.TRAINED);
    }

    public double classify(Instance instance) throws Exception {
        return ClassifierLearningAlgorithmUtil.classify(this, instance);
    }

    public double computeAccuracy(Instances instances) throws Exception {
         boolean oldUseGui = classifier.getGUI();
            classifier.setGUI(false);
       double accuracy =  ClassifierLearningAlgorithmUtil.computeAccuracy(this, instances);
        classifier.setGUI(oldUseGui);
       return accuracy;
    }

    public double computeCVAccuracy(int numFolds, Instances instances) throws Exception {
      //  return ClassifierLearningAlgorithmUtil.computeCVAccuracy(this, numFolds, instances);
        double results;
        Random r = new Random();
        //Instances randData = new Instances(instances);
       // randData.randomize(r);
       // randData.stratify(numFolds);
       // double sum = 0;
            Evaluation eval = new Evaluation(instances);

            boolean oldUseGui = classifier.getGUI();
            classifier.setGUI(false);
            eval.crossValidateModel(classifier, instances, numFolds, r);

            classifier.setGUI(oldUseGui);
            return eval.errorRate();
     /*   for (int j = 0; j < numFolds; j++) {



            Instances train = randData.trainCV(numFolds, j);
            Instances test = randData.testCV(numFolds, j);
            Classifier clsCopy = Classifier.makeCopy(c[classifierNum]);
            clsCopy.buildClassifier(train);
            eval.evaluateModel(clsCopy, test);
            // double a = eval.correct();
            double a = eval.errorRate();
            System.out.println("err rate is " + a);


            double b = (double) test.numInstances() / randData.numInstances(); //weight here
            sum += a * b;
        }
        results = sum;
        //}

        return results;

 */
    }

    public double[] distributionForInstance(Instance instance) throws Exception {
        return ClassifierLearningAlgorithmUtil.distributionForInstance(this, instance);
    }

}
