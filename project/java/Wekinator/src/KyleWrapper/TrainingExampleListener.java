/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package KyleWrapper;

import java.util.EventListener;

/**
 *
 * @author fiebrink
 */
public interface TrainingExampleListener extends EventListener {
    //Fired when new example recorded
    public void fireTrainingExampleRecorded(int id);
}
