/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wekinator;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rebecca
 */
public class ChuckRunner {

    private ChuckConfiguration configuration;
    protected boolean running = false;
    public static final String PROP_ISRUNNING = "isRunning";

    /**
     * Get the value of running
     *
     * @return the value of running
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Set the value of running
     *
     * @param running new value of running
     */
    private void setRunning(boolean isRunning) {
        boolean oldIsRunning = this.running;
        this.running = isRunning;
        propertyChangeSupport.firePropertyChange(PROP_ISRUNNING, oldIsRunning, isRunning);
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

    public ChuckRunner(ChuckConfiguration c) {
        configuration = c;
    }

    public static void main(String[] args) {
        //Test
    }

    public ChuckConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(ChuckConfiguration c) {
        configuration = c;
    }

    public static void createChuckFileFromConfiguration(ChuckConfiguration configuration, File file) throws IOException {

        FileWriter fstream;
        BufferedWriter out;
        fstream = new FileWriter(file);
        out = new BufferedWriter(fstream);

        String nextLine = "// File automatically produced from ChuckConfiguration\n";
        nextLine += "// " + new Date();
        fstream.write(nextLine);

        System.out.println("NOT IMPLEMENTED YET!");

        out.close();
        fstream.close();
    }

    public void run() throws IOException {
        stop();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(ChuckRunner.class.getName()).log(Level.SEVERE, null, ex);
        }

        LinkedList<String> cmds = new LinkedList<String>();
        // cmds.add("ovisodiu oheroh");
        cmds.add(configuration.getChuckExecutable() + " --loop");

        cmds.add(configuration.getChuckExecutable() + " + " + configuration.getChuckDir() + "/core_chuck/TrackpadFeatureExtractor.ck");
        cmds.add(configuration.getChuckExecutable() + " + " + configuration.getChuckDir() + "/core_chuck/MotionFeatureExtractor.ck");
        cmds.add(configuration.getChuckExecutable() + " + " + configuration.getChuckDir() + "/core_chuck/AudioFeatureExtractor.ck");

        cmds.add(configuration.getChuckExecutable() + " + " + configuration.getChuckDir() + "/core_chuck/HidDiscoverer.ck");
        cmds.add(configuration.getChuckExecutable() + " + " + configuration.getChuckDir() + "/core_chuck/CustomOSCFeatureExtractor.ck");

        cmds.add(configuration.getChuckExecutable() + " + " + configuration.getChuckDir() + "/core_chuck/ProcessingFeatureExtractor.ck");

        if (configuration.isCustomChuckFeatureExtractorEnabled()) {
            cmds.add(configuration.getChuckExecutable() + " + " + configuration.getCustomChuckFeatureExtractorFilename());
        } else {
            cmds.add(configuration.getChuckExecutable() + " + " + configuration.getChuckDir() + "/feature_extractors/keyboard_rowcol.ck");
        }

        if (configuration.isUseOscSynth()) {
            cmds.add(configuration.getChuckExecutable() + " + " + configuration.getChuckDir() + "/synths/OSC_synth_proxy.ck");
        } else {
            cmds.add(configuration.getChuckExecutable() + " + " + configuration.getChuckSynthFilename());
        }

        if (configuration.isIsPlayalongLearningEnabled()) {
            cmds.add(configuration.getChuckExecutable() + " + " + configuration.getPlayalongLearningFile());
        } else {
            cmds.add(configuration.getChuckExecutable() + " + " + configuration.getChuckDir() + "/score_players/icmc_melody.ck");
        }

        if (configuration.isUseOscSynth()) {
            String args = ":synthNumParams:" + configuration.getNumOscSynthParams();
            
            args += ":synthIsDiscrete:" + (configuration.getIsOscSynthParamDiscrete()[0] ? "1" : "0" );
            args += ":synthUsingDistribution:" + (configuration.getOscUseDistribution()[0] ? "1" : "0");
            args += ":synthNumClasses:" + configuration.getNumOscSynthMaxParamVals();
            args += ":synthPort:" + configuration.getOscSynthReceivePort();
            cmds.add(configuration.getChuckExecutable() + " + " + configuration.getChuckDir() + "/core_chuck/main_chuck_new.ck" + args);
        } else {
            cmds.add(configuration.getChuckExecutable() + " + " + configuration.getChuckDir() + "/core_chuck/main_chuck.ck");
        }

        //Now we want to execute these commands.
        int numErrLines = 0;

        for (int i = 0; i < cmds.size(); i++) {
            System.out.println("Executing: " + cmds.get(i));

            try {
                String line, output;
                output = "";
                Process child = Runtime.getRuntime().exec(cmds.get(i));
                if (i != 0) {
                    try {

                        child.waitFor();
                    } catch (InterruptedException ex) {
                        System.out.println("Couldn't waits");
                        Logger.getLogger(ChuckRunner.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    BufferedReader input = new BufferedReader(new InputStreamReader(child.getErrorStream()));
                    while ((line = input.readLine()) != null) {
                        numErrLines++;
                        output += "In executing command " + cmds.get(i) + " received error:\n";
                        output += (line + '\n');
                        System.out.println("**" + output);
                    }
                    input.close();
                } else {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ChuckRunner.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

            } catch (IOException ex) {
                System.out.println("Yikes got an exception here");
                Logger.getLogger(ChuckRunner.class.getName()).log(Level.SEVERE, null, ex);
            }



        }

        if (numErrLines != 0) {
            System.out.println("Errors were encountered running chuck. Please try again.");
            stop();
            throw new IOException("Could not run: Bad configuration");
        } else {
            System.out.println("A miracle! Chuck runs.");
            setRunning(true);
        }

    }

    public void stop() throws IOException {
        String cmd = "chuck --kill";
        Process child = Runtime.getRuntime().exec(cmd);
        String cmd2 = "killall chuck";
        Process child2 = Runtime.getRuntime().exec(cmd2);


        System.out.println("Attempted to kill chuck.");
        setRunning(false);
    }

    public void restart() throws IOException {
        stop();
        run();
    }
}
