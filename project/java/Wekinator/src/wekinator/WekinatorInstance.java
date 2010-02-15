/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wekinator;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
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
    private WekinatorSettings settings = null;
    protected HidSetup currentHidSetup;
    private static final String settingsSaveFile = "wekinator.usersettings";
    public static final String PROP_CURRENTHIDSETUP = "currentHidSetup";
    private LinkedList<Handler> handlers;
    private static final String chuckConfigSaveFile = "lastChuckConfig";
    protected FeatureConfiguration featureConfiguration = null;
    public static final String PROP_FEATURECONFIGURATION = "featureConfiguration";

    /**
     * Get the value of featureConfiguration
     *
     * @return the value of featureConfiguration
     */
    public FeatureConfiguration getFeatureConfiguration() {
        return featureConfiguration;
    }

    /**
     * Set the value of featureConfiguration
     *
     * @param featureConfiguration new value of featureConfiguration
     */
    public void setFeatureConfiguration(FeatureConfiguration featureConfiguration) {
        FeatureConfiguration oldFeatureConfiguration = this.featureConfiguration;
        this.featureConfiguration = featureConfiguration;
        propertyChangeSupport.firePropertyChange(PROP_FEATURECONFIGURATION, oldFeatureConfiguration, featureConfiguration);

        if (featureConfiguration == null) {

            setForOscState(); //TODO: check on this -- not sensible?


        } else {
            ChuckSystem.getChuckSystem().waitForNewSettings();
            try {
                OscHandler.getOscHandler().sendFeatureConfiguration(featureConfiguration);
            } catch (IOException ex) {
                Logger.getLogger(WekinatorInstance.class.getName()).log(Level.SEVERE, null, ex);
            }
            OscHandler.getOscHandler().requestChuckSettingsArray();
            if (state == State.OSC_CONNECTION_MADE) {
                setState(State.FEATURE_SETUP_DONE);
            }
        }
    }

    boolean canUse(LearningSystem ls) {
        if (ls == null || featureConfiguration == null) {
            System.out.println("Cannot use: ls or featconfig null");
            return false;
        }

        SimpleDataset sd = ls.getDataset();
        if (sd != null) {
            int sF = sd.getNumFeatures();
            int tF = featureConfiguration.getNumFeaturesEnabled();
            int sP = sd.getNumParameters();


            if (sd.getNumFeatures() != featureConfiguration.getNumFeaturesEnabled()
            || sd.getNumParameters() != ChuckSystem.getChuckSystem().getNumParams()) {
                System.out.println("cannot use: feature/param mismatch");
                System.out.println(sF + "/" + tF + ", " + sP + "/" + ChuckSystem.getChuckSystem().getNumParams());
                return false;
            }

            //TODO check that each param type is same
            // ?? uhh ?
            return true;

        }

        return false;

    }

    public enum State {

        INIT,
        OSC_CONNECTION_MADE,
        FEATURE_SETUP_DONE,
        MODELS_SETUP_DONE
    };
    protected State state = State.INIT;
    public static final String PROP_STATE = "state";

    /**
     * Get the value of state
     *
     * @return the value of state
     */
    public State getState() {
        return state;
    }

    /**
     * Set the value of state
     *
     * @param state new value of state
     */
    public void setState(State state) {
        State oldState = this.state;
        this.state = state;
        propertyChangeSupport.firePropertyChange(PROP_STATE, oldState, state);
    }

    /**
     * Get the value of featureManager
     *
     * @return the value of featureManager
     */
   /* public FeatureManager getFeatureManager() {
        return featureManager;
    } */

    /**
     * Set the value of featureManager
     *
     * @param featureManager new value of featureManager
     */
  /*  public void setFeatureManager(FeatureManager featureManager) {
        FeatureManager oldFeatureManager = this.featureManager;
        this.featureManager = featureManager;
        propertyChangeSupport.firePropertyChange(PROP_FEATUREMANAGER, oldFeatureManager, featureManager);
    } */
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    /**
     * Get the value of currentHidSetup
     *
     * @return the value of currentHidSetup
     */
    public HidSetup getCurrentHidSetup() {
        return currentHidSetup;
    }

    /**
     * Set the value of currentHidSetup
     *
     * @param currentHidSetup new value of currentHidSetup
     */
    public void setCurrentHidSetup(HidSetup currentHidSetup) {
        try {
            HidSetup oldCurrentHidSetup = this.currentHidSetup;
            this.currentHidSetup = currentHidSetup;
            if (currentHidSetup != null) {
            currentHidSetup.startHidRun();
            currentHidSetup.startHidInit();
            }
            propertyChangeSupport.firePropertyChange(PROP_CURRENTHIDSETUP, oldCurrentHidSetup, currentHidSetup);
        } catch (IOException ex) {
            Logger.getLogger(WekinatorInstance.class.getName()).log(Level.SEVERE, null, ex);
        }
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
        FileInputStream fin = null;
        boolean useChuckFromCL = (WekinatorRunner.chuckFile != null);


        try {
            fin = new FileInputStream(settingsSaveFile);
            ObjectInputStream sin = new ObjectInputStream(fin);
            settings = (WekinatorSettings) sin.readObject();
            settings.addPropertyChangeListener(new PropertyChangeListener() {

                public void propertyChange(PropertyChangeEvent evt) {
                    settingsPropertyChange(evt);
                }
            });

            if (!useChuckFromCL) {
                String cLoc = settings.getDefaultSettingsDirectory() + File.separator + ChuckConfiguration.getDefaultLocation() + File.separator + chuckConfigSaveFile + "." + ChuckConfiguration.getFileExtension();
                configuration = ChuckConfiguration.readFromFile(new File(cLoc));
                System.out.println("read chuck config from " + cLoc);
            }
            sin.close();
            fin.close();
            System.out.println("Loaded user settings");
        } catch (Exception ex) {
            System.out.println("No user settings found");
            settings = new WekinatorSettings();
            //Save 1st settings now.
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

        if (useChuckFromCL) {
            try {
                configuration = ChuckConfiguration.readFromFile(WekinatorRunner.getChuckConfigFile());
                Logger.getLogger(WekinatorInstance.class.getName()).log(Level.INFO, null, "Loaded chuck configuration file successfully");
            } catch (IOException ex) {
                configuration = new ChuckConfiguration();
                Logger.getLogger(WekinatorInstance.class.getName()).log(Level.SEVERE, null, "Could not load chuck configuration from specified file");
                Logger.getLogger(WekinatorInstance.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        ChuckRunner.setConfiguration(configuration);

       // featureManager = new FeatureManager();

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

        currentHidSetup = new HidSetup();


        //add state listeners
        OscHandler.getOscHandler().addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                oscPropertyChanged(evt);
            }
        });

        WekinatorLearningManager.getInstance().addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                learningManagerPropertyChanged(evt);
            }
        });

    // TODO RAF add check for valid model state
    }

    private void oscPropertyChanged(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(OscHandler.PROP_CONNECTIONSTATE)) {
            setForOscState();
        }
    }

    private void setForOscState() {
        if (OscHandler.getOscHandler().getConnectionState() != OscHandler.ConnectionState.CONNECTED) {
            setState(State.INIT);
        } else if (state == State.INIT && OscHandler.getOscHandler().getConnectionState() == OscHandler.ConnectionState.CONNECTED) {
            setState(State.OSC_CONNECTION_MADE);
        }
    }

    private void learningManagerPropertyChanged(PropertyChangeEvent evt) {
        /*   if (evt.getPropertyName().equals(WekinatorLearningManager.PROP_FEATURECONFIGURATION)) {
        if (WekinatorLearningManager.getInstance().getFeatureConfiguration() == null) {
        setForOscState();
        } else {
        if (state == State.OSC_CONNECTION_MADE) {
        setState(State.FEATURE_SETUP_DONE);
        }

        }

        } */
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
            //   settings.saveConfiguration(configuration);
            String cLoc = WekinatorInstance.getWekinatorInstance().getSettings().getDefaultSettingsDirectory() + File.separator + ChuckConfiguration.getDefaultLocation() + File.separator + chuckConfigSaveFile + "." + ChuckConfiguration.getFileExtension();
            configuration.writeToFile(new File(cLoc));

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
        if (!handlers.contains(h)) {
            Logger.getLogger(WekinatorInstance.class.getPackage().getName()).addHandler(h);
            handlers.add(h);
        }
    }

    void removeLoggingHandler(Handler h) {
        handlers.remove(h);
    }

    public WekinatorSettings getSettings() {
        return settings;
    }
}
