/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wekinator;

import com.illposed.osc.OSCListener;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortIn;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Date;

/**
 *
 * @author rebecca
 */
public class OscController {

    private static OscController ref = new OscController();
    private static final String OSCrecordMessage = "/control/OSCrecord";

    private OscController() {
    }

    public static void addListeners(OSCPortIn receiver) {
        // return listeners;
        addRecordListener(receiver);
    }

    private static void addRecordListener(OSCPortIn receiver) {
        OSCListener l = new OSCListener() {

            public void acceptMessage(Date arg0, OSCMessage message) {
                System.out.println("Control record message received");
                Object[] o = message.getArguments();
                if (o.length > 0 && (o[0] instanceof java.lang.Integer)) {
                    controlRecordReceived(((Integer) o[0]) == 1);
                }
            }
        };
        receiver.addListener(OSCrecordMessage, l);
    }
    protected boolean recordControllable = false;
    public static final String PROP_RECORDCONTROLLABLE = "recordControllable";

    /**
     * Get the value of recordControllable
     *
     * @return the value of recordControllable
     */
    public static boolean isRecordControllable() {
        return ref.recordControllable;
    }

    /**
     * Set the value of recordControllable
     *
     * @param recordControllable new value of recordControllable
     */
    public static void setRecordControllable(boolean recordControllable) {
        boolean oldRecordControllable = ref.recordControllable;
        ref.recordControllable = recordControllable;
        ref.propertyChangeSupport.firePropertyChange(PROP_RECORDCONTROLLABLE, oldRecordControllable, recordControllable);
    }
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    /**
     * Add PropertyChangeListener.
     *
     * @param listener
     */
    public static void addPropertyChangeListener(PropertyChangeListener listener) {
        ref.propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Remove PropertyChangeListener.
     *
     * @param listener
     */
    public static void removePropertyChangeListener(PropertyChangeListener listener) {
        ref.propertyChangeSupport.removePropertyChangeListener(listener);
    }

    private static void controlRecordReceived(boolean start) {
        if (isRecordControllable()) {
            if (start) {
                WekinatorLearningManager.getInstance().startDatasetCreation();
            } else {
                WekinatorLearningManager.getInstance().stopDatasetCreation();

            }
        }
    }
}
