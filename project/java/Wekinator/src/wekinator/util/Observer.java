/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package wekinator.util;

/**
 *
 * @author rebecca
 */
public interface Observer {
      public void update( Subject o, Object state, String updateString );
}
