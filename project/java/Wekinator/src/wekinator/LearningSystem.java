/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package wekinator;

import java.util.List;

/**
 *
 * @author rebecca
 */
public class LearningSystem {
    //Take over from Weka operator:
    //Manage training, testing in general
    //Not specific to the GUI

    //Has a bunch of classifiers, most recent evaluation results
    List<IbkLearningAlgorithm> learners;
    SimpleDataset dataset;
  //  FeatureExtractor extractor;
    FeatureToParameterMapping mapping;

    //Properties: Num params, type of params, feature/param mask,
    //Other methods: train, compute accuracy (summary), compute individual accuracy, get learner types
    
    





}
