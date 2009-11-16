/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package wekinator;

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

}
