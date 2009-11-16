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

/**
 *
 * @author rebecca
 */
public class ChuckConfiguration implements Serializable {

    private String chuckDirectory = "No directory set";
    private String chuckExecutable = "/usr/bin/chuck";
    private boolean customChuckFeatureExtractorEnabled = false;
    private String customChuckFeatureExtractorFilename = "No file selected";
    private int numCustomChuckFeaturesExtracted = 0;
    private boolean oscFeatureExtractorEnabled = false;
    private int numOSCFeaturesExtracted = 0;
    private int oscFeatureExtractorSendPort = 6453; //Matches defaults in chuck code
    private boolean useChuckSynthClass = true;
    private String chuckSynthFilename = "No file selected";
    private boolean useOscSynth = false;
    private int numOscSynthParams = 0;
    private boolean isOscSynthParamDiscrete[] = new boolean[0];
        private boolean oscUseDistribution[] = new boolean[0];

    protected boolean usable = false;
    public static final String PROP_USABLE = "usable";

    /**
     * Get the value of usable
     *
     * @return the value of usable
     */
    public boolean isUsable() {
        return usable;
    }

    /**
     * Set the value of usable
     *
     * @param usable new value of usable
     */
    private void setUsable(boolean usable) {
        boolean oldUsable = this.usable;
        this.usable = usable;
        propertyChangeSupport.firePropertyChange(PROP_USABLE, oldUsable, usable);
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


    public boolean[] getOscUseDistribution() {
        return oscUseDistribution;
    }

    public void setOscUseDistribution(boolean[] oscUseDistribution) {
        this.oscUseDistribution = new boolean[oscUseDistribution.length];
        for (int i = 0; i < oscUseDistribution.length; i++) {
            this.oscUseDistribution[i] = oscUseDistribution[i];
        }
    }

    private int oscSynthReceivePort = 12000; //Matches defaults in chuck code
    private int oscSynthSendPort = 6448; //Matches defaults in chuck code
    private boolean isPlayalongLearningEnabled = false;
    private String playalongLearningFile = "No file selected";
    private String locationToSaveMyself = "myConfiguration.chuckconfiguration";
    private int numOscSynthMaxParamVals = 2;
   // private boolean oscSynthUseDistribution = false;


    public ChuckConfiguration() {
        isOscSynthParamDiscrete = new boolean[0];
    }

    /**
     * TODO: Get around fact that we have to copy here... want 1 version that just loads serialized obj
     *  from file, another that doesn't destroy object reference (this one)
     * @param settingsFile
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     * @throws java.lang.ClassNotFoundException
     */
    public void loadFromFile(File settingsFile) throws FileNotFoundException, IOException, ClassNotFoundException {
        FileInputStream fin = new FileInputStream(settingsFile);
        ObjectInputStream sin = new ObjectInputStream(fin);
        ChuckConfiguration c = (ChuckConfiguration) sin.readObject();
        sin.close();
        fin.close();

        setEqualTo(c);
    }

    public void writeToFile(File settingsFile) throws FileNotFoundException, IOException {
        FileOutputStream fout = new FileOutputStream(settingsFile);
        ObjectOutputStream out = new ObjectOutputStream(fout);
        locationToSaveMyself = settingsFile.getAbsolutePath();
        out.writeObject(this);
        out.close();
        fout.close();
    }

    public ChuckConfiguration(ChuckConfiguration c) {
        setEqualTo(c);
    }

    public void validate() throws Exception {
        String errorString = "";

        String s;
        if (chuckDirectory.contains("/")) {
            String ss[] = chuckDirectory.split("/");
            if (ss.length > 0) {
                s = ss[ss.length - 1];
            } else {
                s = "";
            }
        } else if (chuckDirectory.contains("\\")) {
            String ss[] = chuckDirectory.split("\\");
            if (ss.length > 0) {
                s = ss[ss.length - 1];
            } else {
                s = "";
            }
        } else {
            s = chuckDirectory;
        }
        if (!s.equals("chuck")) {
            errorString += "Core chuck directory must refer to location of \"chuck\" within wekinator project/\n";
        }

        

        File f = new File(chuckDirectory);
        if (!f.exists() || !f.isDirectory()) {
            errorString += "Chuck directory does not exist\n";
        }

        String coreString = chuckDirectory + "/core_chuck/"; //TODO: make work for windows
        File coreF = new File(coreString);
        if (! coreF.exists() || !coreF.isDirectory())
        {
            errorString += "Chuck directory must be the chuck wekinator directory, containing sub directory core_chuck.\n";

        }

      if (numOscSynthParams <=0) {
                errorString += "Number of params must be > 0";
        }

           if (useChuckSynthClass && !isChuckFile(chuckSynthFilename)) {
            errorString += "Invalid chuck synth class file.";
        }

                   if (customChuckFeatureExtractorEnabled && !isChuckFile(customChuckFeatureExtractorFilename)) {
            errorString += "Invalid chuck feature extractor class file.";
        }

                   if (isPlayalongLearningEnabled && !isChuckFile(playalongLearningFile)) {
            errorString += "Invalid chuck score player class file.";
        }

        //TODO: complete.
        if (errorString.length() != 0) {
            throw new Exception(errorString);
        }



    //Make sure fields valid

    //Make sure chuck executable is present & executable




        setUsable(true);

    }

    private boolean isChuckFile(String filename) {
        File f = new File(filename);
        if (!(f.exists() && f.isFile())) {
            return false;
        }

        String lastPart = f.getName();

        String parts[] = lastPart.split(".");
        if (parts.length <2 || ! parts[1].equals("ck")) {
            return false;
        }

        return true;

    }

    public void setEqualTo(ChuckConfiguration c) {
        chuckDirectory = c.chuckDirectory;
        customChuckFeatureExtractorEnabled = c.customChuckFeatureExtractorEnabled;
        customChuckFeatureExtractorFilename = c.customChuckFeatureExtractorFilename;
        numCustomChuckFeaturesExtracted = c.numCustomChuckFeaturesExtracted;
        oscFeatureExtractorEnabled = c.oscFeatureExtractorEnabled;
        numOSCFeaturesExtracted = c.numOSCFeaturesExtracted;
        oscFeatureExtractorSendPort = c.oscFeatureExtractorSendPort;
        useChuckSynthClass = c.useChuckSynthClass;
        chuckSynthFilename = c.chuckSynthFilename;
        useOscSynth = c.useOscSynth;
        numOscSynthParams = c.numOscSynthParams;
        isOscSynthParamDiscrete = new boolean[c.isOscSynthParamDiscrete.length];
        for (int i = 0; i < isOscSynthParamDiscrete.length; i++) {
            isOscSynthParamDiscrete[i] = c.isOscSynthParamDiscrete[i];
        }
                oscUseDistribution = new boolean[c.oscUseDistribution.length];

                for (int i = 0; i < oscUseDistribution.length; i++) {
            oscUseDistribution[i] = c.oscUseDistribution[i];
        }

        oscSynthReceivePort = c.oscSynthReceivePort;
        oscSynthSendPort = c.oscSynthSendPort;
        isPlayalongLearningEnabled = c.isPlayalongLearningEnabled;
        playalongLearningFile = c.playalongLearningFile;
        locationToSaveMyself = c.locationToSaveMyself;
           numOscSynthMaxParamVals = c.numOscSynthMaxParamVals;

           setUsable(false);
     //       setUsable(c.usable);
           try {
               validate();
           } catch (Exception ex) {
                //Do nothing
           }
    }

    public String getChuckSynthFilename() {
        return chuckSynthFilename;
    }

    public void setChuckSynthFilename(String chuckSynthFilename) {
        this.chuckSynthFilename = chuckSynthFilename;
                setUsable(false);

    }

    public String getChuckDir() {
        return chuckDirectory;
    }

    public void setChuckDir(String chuckDir) {
        this.chuckDirectory = chuckDir;
                        setUsable(false);

    }

    public boolean isCustomChuckFeatureExtractorEnabled() {
        return customChuckFeatureExtractorEnabled;
    }

    public void setCustomChuckFeatureExtractorEnabled(boolean customChuckFeatureExtractorEnabled) {
        this.customChuckFeatureExtractorEnabled = customChuckFeatureExtractorEnabled;
                        setUsable(false);

    }

    public String getCustomChuckFeatureExtractorFilename() {
        return customChuckFeatureExtractorFilename;
    }

    public void setCustomChuckFeatureExtractorFilename(String customChuckFeatureExtractorFilename) {
        this.customChuckFeatureExtractorFilename = customChuckFeatureExtractorFilename;
                        setUsable(false);

    }

    public boolean[] getIsOscSynthParamDiscrete() {
        return isOscSynthParamDiscrete;
    }

    public void setIsOscSynthParamDiscrete(boolean[] isOscSynthParamDiscrete) {
    //    this.isOscSynthParamDiscrete = isOscSynthParamDiscrete;
        this.isOscSynthParamDiscrete = new boolean[isOscSynthParamDiscrete.length];
        for (int i = 0; i < isOscSynthParamDiscrete.length; i++) {
            this.isOscSynthParamDiscrete[i] = isOscSynthParamDiscrete[i];
        }
                setUsable(false);

    }

    public boolean isIsPlayalongLearningEnabled() {
        return isPlayalongLearningEnabled;
    }

    public void setIsPlayalongLearningEnabled(boolean isPlayalongLearningEnabled) {
        this.isPlayalongLearningEnabled = isPlayalongLearningEnabled;
                        setUsable(false);

    }

    public int getNumCustomChuckFeaturesExtracted() {
        return numCustomChuckFeaturesExtracted;
    }

    public void setNumCustomChuckFeaturesExtracted(int numCustomChuckFeaturesExtracted) {
        this.numCustomChuckFeaturesExtracted = numCustomChuckFeaturesExtracted;
                        setUsable(false);

    }

    public int getNumOSCFeaturesExtracted() {
        return numOSCFeaturesExtracted;
    }

    public void setNumOSCFeaturesExtracted(int numOSCFeaturesExtracted) {
        this.numOSCFeaturesExtracted = numOSCFeaturesExtracted;
                        setUsable(false);

    }

    public int getNumOscSynthParams() {
        return numOscSynthParams;
    }

    public void setNumOscSynthParams(int numOscSynthParams) {
        this.numOscSynthParams = numOscSynthParams;
                        setUsable(false);

    }

    public boolean isOscFeatureExtractorEnabled() {
        return oscFeatureExtractorEnabled;
    }

    public void setOscFeatureExtractorEnabled(boolean oscFeatureExtractorEnabled) {
        this.oscFeatureExtractorEnabled = oscFeatureExtractorEnabled;
                        setUsable(false);

    }

    public int getOscFeatureExtractorSendPort() {
        return oscFeatureExtractorSendPort;
    }

    public void setOscFeatureExtractorSendPort(int oscFeatureExtractorSendPort) {
        this.oscFeatureExtractorSendPort = oscFeatureExtractorSendPort;
                        setUsable(false);

    }

    public int getOscSynthReceivePort() {
        return oscSynthReceivePort;
    }

    public void setOscSynthReceivePort(int oscSynthReceivePort) {
        this.oscSynthReceivePort = oscSynthReceivePort;
                        setUsable(false);

    }

    public int getOscSynthSendPort() {
        return oscSynthSendPort;
    }

    public void setOscSynthSendPort(int oscSynthSendPort) {
        this.oscSynthSendPort = oscSynthSendPort;
                        setUsable(false);

    }

    public String getPlayalongLearningFile() {
        return playalongLearningFile;
    }

    public void setPlayalongLearningFile(String playalongLearningFile) {
        this.playalongLearningFile = playalongLearningFile;
                        setUsable(false);

    }

    public boolean isUseChuckSynthClass() {
        return useChuckSynthClass;
    }

    public void setUseChuckSynthClass(boolean useChuckSynthClass) {
        this.useChuckSynthClass = useChuckSynthClass;
                        setUsable(false);

    }

    public boolean isUseOscSynth() {
        return useOscSynth;
    }

    public void setUseOscSynth(boolean useOscSynth) {
        this.useOscSynth = useOscSynth;
                        setUsable(false);

    }

    public String getSaveLocation() {
        return locationToSaveMyself;
    }

    public void setSaveLocation(String locationToSaveMyself) {
        this.locationToSaveMyself = locationToSaveMyself;
                        setUsable(false);

    }

    public String getChuckExecutable() {
        return chuckExecutable;
    }

    public void setChuckExecutable(String chuckExecutable) {
        this.chuckExecutable = chuckExecutable;
                        setUsable(false);

    }

    public String getLocationToSaveMyself() {
        return locationToSaveMyself;
    }

    public void setLocationToSaveMyself(String locationToSaveMyself) {
        this.locationToSaveMyself = locationToSaveMyself;
                        setUsable(false);

    }

    public int getNumOscSynthMaxParamVals() {
        return numOscSynthMaxParamVals;
    }

    public void setNumOscSynthMaxParamVals(int numOscSynthMaxParamVals) {
        this.numOscSynthMaxParamVals = numOscSynthMaxParamVals;
                        setUsable(false);

    }




}
