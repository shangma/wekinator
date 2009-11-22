/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wekinator;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rebecca
 */
public class WekinatorSettings implements Serializable {
    //This should persist between sessions

    protected String lastConfigurationFileLocation = null;
    protected String lastFeatureFileLocation = null;
    protected String lastClassifierFileLocation = null;
    protected String defaultFeatureFileLocation = null;
    protected String defaultClassifierFileLocation = null;
    protected String lastHidFileLocation = null;
    protected String defaultHidFileLocation =null;
    
    protected String logFile = "wekinator.log";
    protected Level logLevel = Level.WARNING;
    public static final String PROP_LOGLEVEL = "logLevel";

    public WekinatorSettings() {
        String currentDir;
        try {
            currentDir =  new File("").getCanonicalPath();
        } catch (IOException ex) {
            currentDir = new File("").getAbsolutePath(); //TODO: util for this check
        }
        //projectDir will be "" if invalid:
        File projectDir = (new File(currentDir)).getParentFile().getParentFile();
        String projectDirString[] = projectDir.getAbsolutePath().split(File.separator);
        if (projectDirString.length > 0 && projectDirString[projectDirString.length - 1].equals("java")) {
            projectDir = projectDir.getParentFile();
        }

        defaultFeatureFileLocation = projectDir + File.separator + "mySavedSettings";
        System.out.println("Set default feature location to " + defaultFeatureFileLocation);
        defaultClassifierFileLocation = projectDir + File.separator + "mySavedSettings";
        defaultHidFileLocation = projectDir + File.separator + "mySavedSettings";
        
    }

    public String getDefaultClassifierFileLocation() {
        return defaultClassifierFileLocation;
    }

    public String getDefaultFeatureFileLocation() {
        return defaultFeatureFileLocation;
    }

    public String getLastClassifierFileLocation() {
        return lastClassifierFileLocation;
    }

    public void setLastClassifierFileLocation(String lastClassifierFileLocation) {
        this.lastClassifierFileLocation = lastClassifierFileLocation;
    }

    public String getLastFeatureFileLocation() {
        return lastFeatureFileLocation;
    }

    public void setLastFeatureFileLocation(String lastFeatureFileLocation) {
        this.lastFeatureFileLocation = lastFeatureFileLocation;
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

    /**
     * Get the value of lastConfigurationFileLocation
     *
     * @return the value of lastConfigurationFileLocation
     */
    public String getLastConfigurationFileLocation() {
        return lastConfigurationFileLocation;
    }

    /**
     * Set the value of lastConfigurationFileLocation
     *
     * @param lastConfigurationFileLocation new value of lastConfigurationFileLocation
     */
    public void setLastConfigurationFileLocation(String lastConfigurationFileLocation) {
        this.lastConfigurationFileLocation = lastConfigurationFileLocation;
    }

    public ChuckConfiguration loadLastConfiguration() throws IOException {
        ChuckConfiguration c;
        if (lastConfigurationFileLocation == null) {
            throw new IOException("No previous configuration found");
        }

        FileInputStream fin = null;
        try {
            fin = new FileInputStream(lastConfigurationFileLocation);
            ObjectInputStream sin = new ObjectInputStream(fin);
            c = (ChuckConfiguration) sin.readObject();
            sin.close();
            fin.close();
        } catch (Exception ex) {
            throw new IOException("Could not load configuration from file\n");
        } finally {
            try {
                if (fin != null) {
                    fin.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(WekinatorInstance.class.getName()).log(Level.INFO, null, ex);
            }
        }
        return c;
    }

    public void saveConfiguration(ChuckConfiguration c) throws IOException {
        FileOutputStream fout = null;
        boolean fail = false;
        try {
            fout = new FileOutputStream(lastConfigurationFileLocation);
            ObjectOutputStream out = new ObjectOutputStream(fout);
            out.writeObject(c);
            out.close();
            fout.close();
        } catch (IOException ex) {
            fail = true;
        } finally {
            try {
                if (fout != null) {
                    fout.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(WekinatorSettings.class.getName()).log(Level.INFO, null, ex);
            }
        }
        if (fail) {
            throw new IOException("Could not write to file");
        }
    }

    public String getLastHidFileLocation() {
        return lastHidFileLocation;
    }

    public void setLastHidFileLocation(String lastHidFileLocation) {
        this.lastHidFileLocation = lastHidFileLocation;
    }

    public String getDefaultHidFileLocation() {
        return defaultHidFileLocation;
    }

}
