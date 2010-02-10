/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wekinator;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.logging.Level;
import wekinator.util.Util;

/**
 * TODOTODOTODO: change custom file stuff to hash table; everyone responsible for their own thing.
 *
 * @author rebecca
 */
public class WekinatorSettings implements Serializable {
    //This should persist between sessions

    protected HashMap<String, String> lastLocations = null;
    protected String defaultDir;   
    protected String logFile = "wekinator.log";
    protected Level logLevel = Level.WARNING;
    public static final String PROP_LOGLEVEL = "logLevel";

    public WekinatorSettings() {
        lastLocations = new HashMap<String, String>();
        String currentDir = Util.getCanonicalPath(new File(""));
        //projectDir will be "" if invalid:
        File projectDir = (new File(currentDir)).getParentFile().getParentFile();
        String projectDirString[] = projectDir.getAbsolutePath().split(File.separator);
        if (projectDirString.length > 0 && projectDirString[projectDirString.length - 1].equals("java")) {
            projectDir = projectDir.getParentFile();
        }
        defaultDir = Util.getCanonicalPath(projectDir) + File.separator + "mySavedSettings";
        System.out.println("default dir is " + defaultDir);

  }

    public String getDefaultSettingsDirectory() {
        return defaultDir;
    }

    public String getLastLocation(String key) {
            return lastLocations.get(key);
    }

    public void setLastLocation(String key, String value) {
        lastLocations.put(key, value);
    }

    /**
     * Get the value of logLevel
     *
     * @return the value of logLevel
     */
    public Level getLogLevel() {
        return logLevel;
    }

    /**
     * Set the value of logLevel
     *
     * @param logLevel new value of logLevel
     */
    public void setLogLevel(Level logLevel) {
        Level oldLogLevel = this.logLevel;
        this.logLevel = logLevel;
        propertyChangeSupport.firePropertyChange(PROP_LOGLEVEL, oldLogLevel, logLevel);
    }
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    /**
     * Add PropertyChangeListener.
     *
     * @param listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Remove PropertyChangeListener.
     *
     * @param listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    /**
     * Get the value of logFile
     *
     * @return the value of logFile
     */
    public String getLogFile() {
        return logFile;
    }

    /**
     * Set the value of logFile
     *
     * @param logFile new value of logFile
     */
    public void setLogFile(String logFile) {
        this.logFile = logFile;
    }

}
