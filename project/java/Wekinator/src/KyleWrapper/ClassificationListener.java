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
public interface ClassificationListener extends EventListener {
    //Fired when new example classified
    public void fireClassificationResult(int id, int classValue);
}
