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
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;
import wekinator.LearningAlgorithms.LearningAlgorithm.TrainingState;

/**
 *
 * @author rebecca
 */
public class OtherClassifierLearningAlgorithm extends ClassifierLearningAlgorithm {
    protected transient OtherClassifierSettingsPanel myPanel = null;

    protected OtherClassifierLearningAlgorithm(Classifier c) {
        classifier = c;
        setTrainingState(TrainingState.TRAINED);
        myPanel = new OtherClassifierSettingsPanel(this);
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


    @Override
    public void forget() {
        setTrainingState(trainingState.NOT_TRAINED); //better than disallowing this
    }

    public OtherClassifierSettingsPanel getSettingsPanel() {
                if (myPanel == null) {
            myPanel = new OtherClassifierSettingsPanel(this);
        }
        return myPanel;
    }

    public static OtherClassifierLearningAlgorithm readFromInputStream(ObjectInputStream i) throws IOException, ClassNotFoundException {
        Classifier c = (Classifier) i.readObject();
        OtherClassifierLearningAlgorithm a = new OtherClassifierLearningAlgorithm(c);
        a.setTrainingState((TrainingState) i.readObject());
        return a;
    }

    @Override
    protected void initClassifier() {

    }

}
