/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package KyleWrapper;

import java.util.logging.Level;
import java.util.logging.Logger;
import weka.classifiers.lazy.IBk;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

/**
 *
 * @author fiebrink
 */
public class TempTester {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //Create new instances
//Set up instances
        try {
            int numFeatures = 2;
            int maxNumClasses = 100;
            Remove removeID = new Remove();

            FastVector ff = new FastVector(numFeatures + 2); //Include ID, class
            //Add ID
            ff.addElement(new Attribute("ID"));

            //Add features
            for (int i = 0; i < numFeatures; i++) {
                ff.addElement(new Attribute("Feature_" + i));
            }

            //Add parameters

            //Create fastVector w/ possible
            FastVector classes = new FastVector(maxNumClasses);
            for (int val = 0; val < maxNumClasses; val++) {
                classes.addElement((new Integer(val)).toString());
            }
            ff.addElement(new Attribute("Class", classes));

            Instances allInstances = new Instances("dataset", ff, 0);
            allInstances.setClassIndex(allInstances.numAttributes() - 1);

            //Set up dummy instances to reflect state of actual instances
            Instances dummyInstances = new Instances(allInstances);

            //set up filter object

            int[] indicesToRemove = new int[1]; //just ID
            indicesToRemove[0] = 0; //id
            removeID.setAttributeIndicesArray(indicesToRemove);
            removeID.setInputFormat(dummyInstances);

            //Add instances:
            double[] featVec = {1.0, 1.0, 1.0, 0.0};

            allInstances.add(new Instance(1.0, featVec));
            double[] featVec2 = new double[4];
            featVec2[0] = 2.0;
            featVec2[1] = 2.0;
            featVec2[2] = 2.0;
            featVec2[3] =5.0;
            allInstances.add(new Instance(1.0, featVec2));

            // double[] featVec4 = {1.0, 1.0, 1.0, 1.0};
            // allInstances.add(new Instance(1.0, featVec4));
            
            //Pass to kNN
            IBk knn = new IBk(1);

                    System.out.println("numat" + allInstances.numAttributes());

            knn.buildClassifier(Filter.useFilter(allInstances, removeID));
            //Add an instance to Instances
            double[] featVec3 = {1.0, 1.0, 1.0, 1.0};
            removeID.input(new Instance(1.0, featVec3));
            System.out.println(knn.classifyInstance(removeID.output()));


            //Verify that knns don't change training size
            //Delete an instance from instances, verify knn train size is same
            //
            System.out.println("before: " + knn.getNumTraining());

            allInstances.delete(0);

            knn.buildClassifier(Filter.useFilter(allInstances, removeID));
            System.out.println("after: " + knn.getNumTraining());
            removeID.input(new Instance(1.0, featVec3));
            System.out.println(knn.classifyInstance(removeID.output()));

            //Can I access an Instances in Instances by a reference to Instance itself?
           /* double[] newv = {1.0, 3.0, 1.0, 3.0};
            Instance i = new Instance(1.0, featVec);
            allInstances.add(i);
            System.out.println(allInstances.lastInstance().dataset()); //fails if look at i.dataset :( */

            double[] newv = {1.0, 3.0, 1.0, 3.0};
            Instance i = new Instance(1.0, featVec);
            allInstances.add(i);
            Instance ref = allInstances.lastInstance();

            System.out.println("final test hope?");
            System.out.println(allInstances.lastInstance().classValue());
            ref.setClassValue(10.0);
            System.out.println(allInstances.lastInstance().classValue()); //YES! YAY!





        } catch (Exception ex) {
            Logger.getLogger(TempTester.class.getName()).log(Level.SEVERE, null, ex);
        }



    }
}
