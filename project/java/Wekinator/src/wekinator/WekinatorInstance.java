/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wekinator;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Ideally, captures current state of the system, including all active component objects
 *
 * @author rebecca
 */
public class WekinatorInstance {
    //Make singleton

    private static WekinatorInstance ref = null;
    protected ChuckConfiguration configuration = null;
    protected ChuckRunner runner = null;
    private WekinatorSettings settings = null;
    private OscHandler oscHandler = null;



    private static final String settingsSaveFile = "wekinator.usersettings";
    protected FeatureManager featureManager;
    public static final String PROP_FEATUREMANAGER = "featureManager";
    private LinkedList<Handler> handlers;

    /**
     * Get the value of featureManager
     *
     * @return the value of featureManager
     */
    public FeatureManager getFeatureManager() {
        return featureManager;
    }

    /**
     * Set the value of featureManager
     *
     * @param featureManager new value of featureManager
     */
    public void setFeatureManager(FeatureManager featureManager) {
        FeatureManager oldFeatureManager = this.featureManager;
        this.featureManager = featureManager;
        propertyChangeSupport.firePropertyChange(PROP_FEATUREMANAGER, oldFeatureManager, featureManager);
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
     * Get the value of runner
     *
     * @return the value of runner
     */
    public ChuckRunner getRunner() {
        return runner;
    }

    /**
     * Get the value of configuration
     *
     * @return the value of configuration
     */
    public ChuckConfiguration getConfiguration() {
        return configuration;
    }



    /**
     * Set the value of configuration
     *
     * @param configuration new value of configuration
     */
    private void setConfiguration(ChuckConfiguration configuration) {
        this.configuration = configuration;
    }

    private WekinatorInstance() {
        //TODO: Try loading settings from file
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(settingsSaveFile);
            ObjectInputStream sin = new ObjectInputStream(fin);
            settings = (WekinatorSettings) sin.readObject();
            settings.addPropertyChangeListener(new PropertyChangeListener() {

                public void propertyChange(PropertyChangeEvent evt) {
                    settingsPropertyChange(evt);
                }
            });

            configuration = settings.loadLastConfiguration();
            sin.close();
            fin.close();
            System.out.println("Loaded user settings");
        } catch (Exception ex) { 
            System.out.println("No user settings found");
            settings = new WekinatorSettings();
            settings.setLastConfigurationFileLocation("chuckconfiguration.usersettings");
            //Save settings now.
            FileOutputStream fout = null;
            boolean fail = false;
            try {
                fout = new FileOutputStream(settingsSaveFile);
                ObjectOutputStream out = new ObjectOutputStream(fout);
                out.writeObject(settings);
                out.close();
                fout.close();
                System.out.println("Wrote to settings file");
            } catch (IOException ex1) {
                fail = true;
                System.out.println("Failed to write to settings file: " + ex1.getMessage());
                ex1.printStackTrace();
            } finally {
                try {
                    if (fout != null) {
                        fout.close();
                    }
                } catch (IOException ex2) {
                    Logger.getLogger(WekinatorSettings.class.getName()).log(Level.INFO, null, ex2);
                }
            }


            configuration = new ChuckConfiguration();
        } finally {
            try {
                if (fin != null) {
                    fin.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(WekinatorInstance.class.getName()).log(Level.INFO, null, ex);
            }
        } 

        runner = ChuckRunner.getChuckRunner();
        runner.setConfiguration(configuration);

        featureManager = new FeatureManager();

        handlers = new LinkedList<Handler>();
        try {
            //Give this a try...
            FileHandler h = new FileHandler(settings.getLogFile());
            h.setFormatter(new SimpleFormatter());
            handlers.add(h);
            Logger.getLogger(WekinatorInstance.class.getPackage().getName()).addHandler(h);
        } catch (Exception ex) {
            System.out.println("Couldn't create log file");
        }
    }

    public void saveCurrentSettings() {
        FileOutputStream fout = null;
            boolean fail = false;
            try {
                fout = new FileOutputStream(settingsSaveFile);
                ObjectOutputStream out = new ObjectOutputStream(fout);
                out.writeObject(settings);
                out.close();
                fout.close();
                System.out.println("Wrote to settings file");
            } catch (IOException ex1) {
                fail = true;
                System.out.println("Failed to write to settings file: " + ex1.getMessage());
                ex1.printStackTrace();
            } finally {
                try {
                    if (fout != null) {
                        fout.close();
                    }
                } catch (IOException ex2) {
                    Logger.getLogger(WekinatorSettings.class.getName()).log(Level.INFO, null, ex2);
                }
            }
    }

    public static synchronized WekinatorInstance getWekinatorInstance() {
        if (ref == null) {
            ref = new WekinatorInstance();
        }
        return ref;
    }

    public void useConfigurationNextSession() {
        try {
            settings.saveConfiguration(configuration);
        } catch (IOException ex) {
            System.out.println("Could not save configuration to use next session");
            Logger.getLogger(WekinatorInstance.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    private void settingsPropertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(WekinatorSettings.PROP_LOGLEVEL)) {
            for (Handler h : handlers) {
                h.setLevel(settings.getLogLevel());
            }
        }
    }

    public void addLoggingHandler(Handler h) {
        if (! handlers.contains(h)) {
            Logger.getLogger(WekinatorInstance.class.getPackage().getName()).addHandler(h);
            handlers.add(h);
        }
    }

   void removeLoggingHandler(WekinatorConsoleHandler h) {
        handlers.remove(h);
    }

       public OscHandler getOscHandler() {
        return oscHandler;
    }

    public WekinatorSettings getSettings() {
        return settings;
    }
}
