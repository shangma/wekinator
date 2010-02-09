/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package wekinator.LearningAlgorithms;

import java.io.File;
import wekinator.util.SerializedFileUtil;
import wekinator.WekinatorInstance;

/**
 *
 * @author rebecca
 */
public class LearningAlgorithms {

    public static String getFileExtension() {
        return "wmodel";
    }

    public static String getFileTypeDescription() {
        return "learning algorithm";
    }

    public static String getDefaultLocation() {
        return "individualModels";
    }

    public static LearningAlgorithm readFromFile(File f) throws ClassCastException, Exception {
        Object o = SerializedFileUtil.readFromFile(f);
        LearningAlgorithm la = null;
       
        try {
           la = (LearningAlgorithm) o;
        } catch (ClassCastException ex) {
            throw new ClassCastException("File does not contain a learning algorithm");
        }
        return la;
    }

    public static void writeToFile(LearningAlgorithm la, File f) throws Exception {
         SerializedFileUtil.writeToFile(f, la);
    }

}
