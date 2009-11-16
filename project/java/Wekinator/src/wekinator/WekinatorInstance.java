/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wekinator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rebecca
 */
public class WekinatorInstance {
    //Make singleton

    private static WekinatorInstance ref = null;
    protected ChuckConfiguration configuration = null;
    protected ChuckRunner runner = null;
    private WekinatorSettings settings = null;
    private static final String settingsSaveFile = "wekinator.usersettings";

    /**
     * Get the value of runner
     *
     * @return the value of runner
     */
    public ChuckRunner getRunner() {
        return runner;
    }

    /**
     * Set the value of runner
     *
     * @param runner new value of runner
     */
    private void setRunner(ChuckRunner runner) {
        this.runner = runner;
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
                    Logger.getLogger(WekinatorSettings.class.getName()).log(Level.INFO, null, ex);
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

        runner = new ChuckRunner(configuration);

    }

    public static WekinatorInstance getWekinatorInstance() {
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
}
