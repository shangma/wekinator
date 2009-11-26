/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package wekinator;

import weka.classifiers.Classifier;

/**
 *
 * @author rebecca
 */
public interface ClassifierLearningAlgorithm extends LearningAlgorithm {
    public Classifier getClassifier();
    

}
