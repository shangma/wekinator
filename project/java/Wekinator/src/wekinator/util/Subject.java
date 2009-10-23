/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package wekinator.util;

import wekinator.util.Observer;

/**
 *
 * @author rebecca
 */
public interface Subject {
      public void addObserver( Observer o );
      public void removeObserver( Observer o );
}
