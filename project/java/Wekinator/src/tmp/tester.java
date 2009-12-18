/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tmp;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.classifiers.meta.AdaBoostM1;
import wekinator.FeatureConfiguration;
import wekinator.LearningAlgorithms.IbkLearningAlgorithm;
import wekinator.LearningSystem;
import wekinator.LearningAlgorithms.NNLearningAlgorithm;
import wekinator.SimpleDataset;
import wekinator.util.SerializedFileUtil;

/**
 *
 * @author rebecca
 */
public class tester {
 // protected SimpleDataset dataset = null;
  /*  protected SimpleDataset dataset = null;
  //  protected FeatureConfiguration featureConfiguration = null;
    protected FeatureLearnerConfiguration featureLearnerConfiguration = null;
    protected int numParams = 0;
    protected LearningAlgorithm[] learners; */

    public static void main(String[] args) {
        boolean[] a = {true, true};
        int[] b = {3,3};
        String[] n1 = {"1","2", "3", "4", "5"};
        String[] n2 = {"a", "b"};
        
        SimpleDataset d = new SimpleDataset(5, 2, a, b,n1,n2);

        File f = new File("test.out");
        try {
            SerializedFileUtil.writeToFile(f, d);
        } catch (Exception ex) {
            System.out.println("NO db");
            Logger.getLogger(tester.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        System.out.println("DB ok");


        FeatureConfiguration fc = new FeatureConfiguration();
        try {
            SerializedFileUtil.writeToFile(f, fc);
        } catch (Exception ex) {
            System.out.println("FC problem");
            Logger.getLogger(tester.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        System.out.println("fc ok");

        NNLearningAlgorithm la = new NNLearningAlgorithm();
               try {
            SerializedFileUtil.writeToFile(f, la);
        } catch (Exception ex) {
            System.out.println("la problem");
            Logger.getLogger(tester.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        System.out.println("la ok");

       /* Logger logger = Logger.getLogger(LearningSystem.class.getName());
               try {
            SerializedFileUtil.writeToFile(f, logger);
        } catch (Exception ex) {
            System.out.println("logger problem");
            Logger.getLogger(tester.class.getName()).log(Level.SEVERE, null, ex);
            return;
        } */


        LearningSystem ls = new LearningSystem(2);
                try {
            SerializedFileUtil.writeToFile(f, ls);
        } catch (Exception ex) {
            System.out.println("LS problem");
            Logger.getLogger(tester.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        System.out.println("LS ok"); 



    }
}
