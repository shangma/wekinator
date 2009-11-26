/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/* TODO this class:
 *
 *  Fix problem waiting for chuck --loop to initialize VM -- can I use waitFor there?
 *  Send chuck output to stdout at least for now, to help with debugging
 *  Grab chuck error output also and send somewhere
 *  Ultimately display in console.
 *
 * */
package wekinator;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

//TODO: kill listener threads when chuck runner stops!

/**
 *
 * @author rebecca
 */
public class ChuckRunner {

    private static ChuckRunner ref = null;
    private ChuckConfiguration configuration;

    private String lastErrorMessages = "";

    public String getLastErrorMessages() {
        return lastErrorMessages;
    }

    public void setLastErrorMessages(String lastErrorMessages) {
        this.lastErrorMessages = lastErrorMessages;
    }

    public enum ChuckRunnerState {
        NOT_RUNNING,
        TRYING_TO_RUN,
        RUNNING
    }

    protected ChuckRunnerState runnerState;
    public static final String PROP_RUNNERSTATE = "runnerState";

    /**
     * Get the value of runnerState
     *
     * @return the value of runnerState
     */
    public ChuckRunnerState getRunnerState() {
        return runnerState;
    }

    /**
     * Set the value of runnerState
     *
     * @param runnerState new value of runnerState
     */
    protected void setRunnerState(ChuckRunnerState runnerState) {
        ChuckRunnerState oldRunnerState = this.runnerState;
        this.runnerState = runnerState;
        propertyChangeSupport.firePropertyChange(PROP_RUNNERSTATE, oldRunnerState, runnerState);
    }

    static void exportConfigurationToChuckFile(ChuckConfiguration configuration, File file) throws IOException {
        //Open output stream
        BufferedWriter w = null;
        w = new BufferedWriter(new FileWriter(file));

        w.write("//Automatically generated machine.add file\n");
        w.write("//Created " + (new Date()).toString() + "\n\n");
        w.write("Machine.add(\"" + configuration.getChuckDir() + File.separator + "core_chuck" + File.separator + "TrackpadFeatureExtractor.ck" + "\");\n");
        w.write("Machine.add(\"" + configuration.getChuckDir() + File.separator + "core_chuck" + File.separator + "MotionFeatureExtractor.ck" + "\");\n");
        w.write("Machine.add(\"" + configuration.getChuckDir() + File.separator + "core_chuck" + File.separator + "AudioFeatureExtractor.ck" + "\");\n");
        w.write("Machine.add(\"" + configuration.getChuckDir() + File.separator + "core_chuck" + File.separator + "HidDiscoverer.ck" + "\");\n");
        w.write("Machine.add(\"" + configuration.getChuckDir() + File.separator + "core_chuck" + File.separator + "CustomOSCFeatureExtractor.ck" + "\");\n");

        w.write("Machine.add(\"" + configuration.getChuckDir() + File.separator + "core_chuck" + File.separator + "ProcessingFeatureExtractor.ck" + "\");\n");



        if (configuration.isCustomChuckFeatureExtractorEnabled()) {
            w.write("Machine.add(\"" + configuration.getCustomChuckFeatureExtractorFilename() + "\");\n");

        } else {
                        w.write("Machine.add(\"" + configuration.getChuckDir() + File.separator + "feature_extractors" + File.separator + "keyboard_rowcol.ck" + "\");\n");
        }

        if (configuration.isUseOscSynth()) {
            w.write("Machine.add(\"" + configuration.getChuckDir() + File.separator + "synths" + File.separator + "OSC_synth_proxy.ck" + "\");\n");
        } else {
            w.write("Machine.add(\"" + configuration.getChuckSynthFilename() + "\");\n");
        }

        if (configuration.isIsPlayalongLearningEnabled()) {
           w.write("Machine.add(\"" + configuration.getPlayalongLearningFile() + "\");\n");
        } else {
           w.write("Machine.add(\"" + configuration.getChuckDir() + File.separator + "score_players"+ File.separator + "icmc_melody.ck" + "\");\n");
        }

        if (configuration.isUseOscSynth()) {
            String args = ":synthNumParams:" + configuration.getNumOscSynthParams();

            args += ":synthIsDiscrete:" + (configuration.getIsOscSynthParamDiscrete()[0] ? "1" : "0");
            args += ":synthUsingDistribution:" + (configuration.getOscUseDistribution()[0] ? "1" : "0");
            args += ":synthNumClasses:" + configuration.getNumOscSynthMaxParamVals();
            args += ":synthPort:" + configuration.getOscSynthReceivePort();
            w.write("Machine.add(\"" + configuration.getChuckDir() + File.separator + "core_chuck" + File.separator + "main_chuck_new.ck" + args + "\");\n");
        } else {
           w.write("Machine.add(\"" + configuration.getChuckDir() + File.separator + "core_chuck" + File.separator + "main_chuck.ck" + "\");\n");
        }

        w.close();
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

    private ChuckRunner() {
       // running = false;
        setRunnerState(ChuckRunnerState.NOT_RUNNING);
    }

    public static synchronized ChuckRunner getChuckRunner() {
        if (ref == null) {
            ref = new ChuckRunner();
        }
        return ref;
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
        lastErrorMessages = "";
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(ChuckRunner.class.getName()).log(Level.SEVERE, null, ex);
        }

        LinkedList<String> cmds = new LinkedList<String>();
        // cmds.add("ovisodiu oheroh");
        cmds.add(configuration.getChuckExecutable() + " --loop");

        cmds.add(configuration.getChuckExecutable() + " + " + configuration.getChuckDir() + File.separator + "core_chuck" + File.separator + "TrackpadFeatureExtractor.ck");
        cmds.add(configuration.getChuckExecutable() + " + " + configuration.getChuckDir() + File.separator + "core_chuck" + File.separator + "MotionFeatureExtractor.ck");
        cmds.add(configuration.getChuckExecutable() + " + " + configuration.getChuckDir() + File.separator + "core_chuck" + File.separator + "AudioFeatureExtractor.ck");

        cmds.add(configuration.getChuckExecutable() + " + " + configuration.getChuckDir() + File.separator + "core_chuck" + File.separator + "HidDiscoverer.ck");
        cmds.add(configuration.getChuckExecutable() + " + " + configuration.getChuckDir() + File.separator + "core_chuck" + File.separator + "CustomOSCFeatureExtractor.ck");

        cmds.add(configuration.getChuckExecutable() + " + " + configuration.getChuckDir() + File.separator + "core_chuck" + File.separator + "ProcessingFeatureExtractor.ck");

        if (configuration.isCustomChuckFeatureExtractorEnabled()) {
            cmds.add(configuration.getChuckExecutable() + " + " + configuration.getCustomChuckFeatureExtractorFilename());
        } else {
            cmds.add(configuration.getChuckExecutable() + " + " + configuration.getChuckDir() + File.separator + "feature_extractors" + File.separator + "keyboard_rowcol.ck");
        }

        if (configuration.isUseOscSynth()) {
            cmds.add(configuration.getChuckExecutable() + " + " + configuration.getChuckDir() + File.separator + "synths" + File.separator + "OSC_synth_proxy.ck");
        } else {
            cmds.add(configuration.getChuckExecutable() + " + " + configuration.getChuckSynthFilename());
        }

        if (configuration.isIsPlayalongLearningEnabled()) {
            cmds.add(configuration.getChuckExecutable() + " + " + configuration.getPlayalongLearningFile());
        } else {
            cmds.add(configuration.getChuckExecutable() + " + " + configuration.getChuckDir() + File.separator + "score_players" + File.separator + "icmc_melody.ck");
        }

        if (configuration.isUseOscSynth()) {
            String args = ":synthNumParams:" + configuration.getNumOscSynthParams();

            args += ":synthIsDiscrete:" + (configuration.getIsOscSynthParamDiscrete()[0] ? "1" : "0");
            args += ":synthUsingDistribution:" + (configuration.getOscUseDistribution()[0] ? "1" : "0");
            args += ":synthNumClasses:" + configuration.getNumOscSynthMaxParamVals();
            args += ":synthPort:" + configuration.getOscSynthReceivePort();
            cmds.add(configuration.getChuckExecutable() + " + " + configuration.getChuckDir() + File.separator + "core_chuck" + File.separator + "main_chuck_new.ck" + args);
        } else {
            cmds.add(configuration.getChuckExecutable() + " + " + configuration.getChuckDir() + File.separator + "core_chuck" + File.separator + "main_chuck.ck");
        }

        //Now we want to execute these commands.
        int numErrLines = 0;

        for (int i = 0; i < cmds.size(); i++) {
            System.out.println("Executing: " + cmds.get(i));

            try {
                String line, output;
                output = "";
                Process child = Runtime.getRuntime().exec(cmds.get(i));

                if (i == 0) {
                    //Special! Fork a thread that listens to the output of this process,
                    //and log lines using logger
                    // Runtime.getRuntime().
                    new LoggerThread(child.getErrorStream());
                    new LoggerThread(child.getInputStream());
                }
                if (i != 0) {
                    try {

                        child.waitFor();
                    } catch (InterruptedException ex) {
                        System.out.println("Couldn't wait");
                        Logger.getLogger(ChuckRunner.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    BufferedReader input = new BufferedReader(new InputStreamReader(child.getErrorStream()));

                    while ((line = input.readLine()) != null) {
                        numErrLines++;
                        output += "In executing command " + cmds.get(i) + " received error:\n";
                        output += (line + '\n');
                        System.out.println("**" + output);
                        lastErrorMessages += line + "\n";
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

       // numErrLines = 1;
        //lastErrorMessages += "";
        if (numErrLines != 0) {
            System.out.println("Errors were encountered running chuck.");
            setRunnerState(ChuckRunnerState.TRYING_TO_RUN);
            //stop();
//            throw new IOException("Could not run: Bad configuration");
        } else {
            System.out.println("A miracle! Chuck runs.");
        //    setRunning(true);
            setRunnerState(ChuckRunnerState.RUNNING);
        }

    }

    public void ignoreRunErrors(boolean ignore) {
        if (runnerState == ChuckRunnerState.TRYING_TO_RUN) {
            if (ignore) {
                setRunnerState(ChuckRunnerState.RUNNING);
            } else {
                try {
                    stop();
                } catch (IOException ex) {
                    Logger.getLogger(ChuckRunner.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public void stop() throws IOException {
        //String cmd = "chuck --kill";
        String cmd = configuration.getChuckExecutable() + " --kill";
        Process child = Runtime.getRuntime().exec(cmd);
        String cmd2 = "killall chuck";
        Process child2 = Runtime.getRuntime().exec(cmd2);


        System.out.println("Attempted to kill chuck.");
    //    setRunning(false);
        setRunnerState(ChuckRunnerState.NOT_RUNNING);
    }

    public void restart() throws IOException {
        stop();
        run();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
}
class LoggerThread implements Runnable {

    Thread t;
    BufferedReader input;

    LoggerThread(InputStream is) {
        input = new BufferedReader(new InputStreamReader(is));
        t = new Thread(this, "my thread");
        t.start();
    }

    public void run() {
        boolean stop = false;
        while (!stop) {
            try {
                ///byte[] byteArray = new byte[2];
                int b = input.read();
                // input.read

                if (b == -1) {
                    stop = true;
                    System.out.println("made it to end of stream");
                } else {
                    //TODO: send to console in reasonable way
                    System.out.print((char) b);
                    //String s = String.
                    Console.getInstance().log(String.valueOf((char) b));
                }
            } catch (IOException ex) {
                Logger.getLogger(LoggerThread.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

    }
}
