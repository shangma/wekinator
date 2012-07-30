/*
 * WekinatorSettings: Stores user settings across sessions,
 * in particular the file locations used for chuck, Wekinator project directory,
 * etc.
 *
 * Revised 4/9/2011
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
import java.util.HashMap;
import java.util.logging.Level;
import wekinator.util.Util;

/**
 *
 * @author Rebecca Fiebrink
 */
public class WekinatorSettings {

    protected HashMap<String, String> storedKeyValuePairs = null; //
    protected String defaultSettingsDirectory; //should be project/mySavedSettings
    protected String logFile = "wekinator.log";
    protected Level logLevel = Level.WARNING;
    public static final String PROP_LOGLEVEL = "logLevel";
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    public WekinatorSettings() {
        storedKeyValuePairs = new HashMap<String, String>();
        String currentDir = Util.getCanonicalPath(new File(""));
        //projectDir will be "" if invalid:
        File t = new File(currentDir);
        if (t != null && t.getParentFile() != null && t.getParentFile().getParentFile() != null) {


            File projectDir = (new File(currentDir)).getParentFile().getParentFile();
            // String projectDirString[] = splitDirectorString(projectDir.getAbsolutePath());

            //String projectDirString[] = projectDir.getAbsolutePath().split(File.);
            if (projectDir.getName().equals("java")) {

                //    projectDirString.length > 0 && projectDirString[projectDirString.length - 1].equals("java")) {
                projectDir = projectDir.getParentFile();
            }
            defaultSettingsDirectory = Util.getCanonicalPath(projectDir) + File.separator + "mySavedSettings";
            System.out.println("default dir is " + defaultSettingsDirectory);

            File f = new File(defaultSettingsDirectory);
            if (! f.exists()) {
                String s = "/Applications/Wekinator/mySavedSettings";
                File f2 = new File(s);
                if (f2.exists()) {
                    defaultSettingsDirectory = s;
                }
                
            }
            
            
        } else {
            defaultSettingsDirectory = Util.getCanonicalPath(new File(""));
        }

    }

    public String getDefaultSettingsDirectory() {
        return defaultSettingsDirectory;
    }

    public String getLastKeyValue(String key) {
        return storedKeyValuePairs.get(key);
    }

    public void setLastKeyValue(String key, String value) {
        storedKeyValuePairs.put(key, value);
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
     * Set the value of logLevel. A change will fire a property change event.
     *
     * @param logLevel new value of logLevel
     */
    public void setLogLevel(Level logLevel) {
        Level oldLogLevel = this.logLevel;
        this.logLevel = logLevel;
        propertyChangeSupport.firePropertyChange(PROP_LOGLEVEL, oldLogLevel, logLevel);
    }

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

    public void writeToFile(File f) throws IOException {
        FileOutputStream fout = new FileOutputStream(f);
        ObjectOutputStream out = new ObjectOutputStream(fout);
        this.writeToOutputStream(out);
        out.close();
        fout.close();
    }

    protected void writeToOutputStream(ObjectOutputStream out) throws IOException {
        out.writeObject(storedKeyValuePairs);
        out.writeObject(defaultSettingsDirectory);
        out.writeObject(logFile);
    }

    public static WekinatorSettings readFromFile(File f) throws FileNotFoundException, IOException, ClassNotFoundException {
        FileInputStream fin = new FileInputStream(f);
        ObjectInputStream in = new ObjectInputStream(fin);
        WekinatorSettings ws = WekinatorSettings.readFromIntputStream(in);
        in.close();
        fin.close();
        return ws;
    }

    private static WekinatorSettings readFromIntputStream(ObjectInputStream in) throws IOException, ClassNotFoundException {
        WekinatorSettings ws = new WekinatorSettings();
        ws.storedKeyValuePairs = (HashMap<String, String>) in.readObject();
        ws.defaultSettingsDirectory = (String) in.readObject();
        ws.setLogFile((String) in.readObject());
        return ws;
    }
}
