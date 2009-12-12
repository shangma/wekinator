/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package wekinator.LearningAlgorithms;

import java.io.File;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.functions.SMO;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.trees.J48;
import weka.core.Instance;
import weka.core.Instances;
import wekinator.LearningAlgorithms.LearningAlgorithm.TrainingState;
import wekinator.util.SerializedFileUtil;

/**
 *
 * @author rebecca
 */
public class ClassifierLearningAlgorithmUtil {
    public static void saveAsSerializedWekaClassifier(ClassifierLearningAlgorithm la, File f) throws Exception {
        SerializedFileUtil.writeToFile(f, la.getClassifier());
    }
    

    public static LearningAlgorithm loadFromSerializedWekaClassifier(File f) throws ClassCastException, Exception {
        Object o = SerializedFileUtil.readFromFile(f);
        LearningAlgorithm la = null;
        Classifier c;
        try {
            c = (Classifier) o;
        } catch (ClassCastException ex) {
            throw new ClassCastException("File does not contain a Weka classifier");
        }

        if (c instanceof IBk) {
            la = new IbkLearningAlgorithm((IBk)c);
        } else if (c instanceof AdaBoostM1) {
            la = new AdaboostM1LearningAlgorithm((AdaBoostM1)c);
        } else if (c instanceof J48) {
            la = new J48LearningAlgorithm((J48)c);
        } else if (c instanceof SMO) {
            la = new SMOLearningAlgorithm((SMO)c);
        } else if (c instanceof MultilayerPerceptron) {
            la = new NNLearningAlgorithm((MultilayerPerceptron)c);
        } else {
            la = new OtherClassifierLearningAlgorithm(c);
            Logger.getLogger(ClassifierLearningAlgorithmUtil.class.getName()).log(Level.SEVERE, null, "Created other learning algorithm of type " + la.getName());
        }
        return la;

    }

      protected static double classify(ClassifierLearningAlgorithm la, Instance instance) throws Exception {
        if (la.getTrainingState() == TrainingState.TRAINED) {
            return la.getClassifier().classifyInstance(instance);
        } else {
            throw new Exception("Cannot classify: Not trained");
        }
    }

    protected static double computeAccuracy(ClassifierLearningAlgorithm la, Instances instances) throws Exception {
        if (la.getTrainingState() == TrainingState.TRAINED) {

        Evaluation eval = new Evaluation(instances);
            eval.evaluateModel(la.getClassifier(), instances);
            return eval.correct() / instances.numInstances();
        } else {
            throw new Exception("Cannot evaluate: Not trained");
        }
    }

     protected static void train(ClassifierLearningAlgorithm la, Instances instances) throws Exception {
        try {
            la.getClassifier().buildClassifier(instances);
        } catch (Exception ex) {
            Logger.getLogger(ClassifierLearningAlgorithmUtil.class.getName()).log(Level.SEVERE, null, ex);
            throw new Exception(ex);
        }
    }

    protected static double computeCVAccuracy(ClassifierLearningAlgorithm la, int numFolds, Instances instances) throws Exception {
        if (la.getTrainingState() == TrainingState.TRAINED) {
    //TODO: is it necessary to copy here? Depends on implementation of SimpleDataset
            Random r = new Random();
            Instances randData = new Instances(instances);
            randData.randomize(r);
            randData.stratify(numFolds);
            
            double sum = 0;
            for (int j = 0; j < numFolds; j++) {    //TODO: more efficient way to do this?!
                Evaluation eval = new Evaluation(randData);
                Instances train = randData.trainCV(numFolds, j);
                Instances test = randData.testCV(numFolds, j);
                Classifier clsCopy = Classifier.makeCopy(la.getClassifier());
                clsCopy.buildClassifier(train);
                eval.evaluateModel(clsCopy, test);
                double a = eval.correct();
                int b = test.numInstances();
                sum += a / b;
            }
            return sum / (double) numFolds;
                
        } else {
            throw new Exception("Cannot evaluate: Not trained");
        }

    }

    protected static double[] distributionForInstance(ClassifierLearningAlgorithm la, Instance instance) throws Exception {
        if (la.getTrainingState() == TrainingState.TRAINED) {
            return la.getClassifier().distributionForInstance(instance);
        } else {
            throw new Exception("Cannot classify: Not trained");
        }
    }

}
