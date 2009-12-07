/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wekinator;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.Serializable;
import wekinator.util.SerializedFileUtil;

/**
 *
 * @author rebecca
 */
public class FeatureConfiguration implements Serializable {



    public enum WindowType {

        HAMMING,
        HANN,
        RECTANGULAR
    };

    public enum ProcessingExractorType {

        DOWNSAMPLED_100,
        COLOR_6
    };
    protected boolean useFFT = false;
    protected boolean useCentroid = false;
    protected boolean useFlux = false;
    protected boolean useRMS = false;
    protected boolean useRolloff = false;
    protected int fftSize = 512;
    protected boolean useCustomChuckFeatures = false;
    protected int numCustomChuckFeatures = 0;
    protected boolean useCustomOscFeatures = false;
    protected int numCustomOscFeatures = 0;
    protected boolean useTrackpad = false;
    protected boolean useMotionSensor = false;
    protected int motionSensorExtractionRate = 100;
    protected boolean useOtherHid = false;
    protected boolean useProcessing = false;
    protected int numProcessingFeatures = 0;
    protected int fftWindowSize = 256;
    protected WindowType windowType = WindowType.HAMMING;
    protected int audioExtractionRate = 100;
    protected ProcessingExractorType processingExtractorType = ProcessingExractorType.DOWNSAMPLED_100;
    public static final String PROP_HIDSETUP = "hidSetup";
    public HidSetup hidSetup = new HidSetup();
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

    public int getNumAudioFeatures() {
        return ((useFFT ? ((int) (fftSize / 2)) : 0) + (useCentroid ? 1 : 0) + (useFlux ? 1 : 0) + (useRMS ? 1 : 0) + (useRolloff ? 1 : 0));
    }

    public int getNumFeatures() {
        int s = 0;
        s += getNumAudioFeatures();
        if (useTrackpad) {
            s += 2;
        }
        if (useMotionSensor) {
            s += 3;
        }
        if (useOtherHid) {
            s += hidSetup.getNumFeaturesUsed();
        }
        if (useProcessing) {
            s += getNumProcessingFeatures();
        }
        if (useCustomChuckFeatures) {
            s += numCustomChuckFeatures;
        }
        if (useCustomOscFeatures) {
            s += numCustomOscFeatures;
        }
        return s;
    }

    /**
     * Get the value of processingExtractorType
     *
     * @return the value of processingExtractorType
     */
    public ProcessingExractorType getProcessingExtractorType() {
        return processingExtractorType;
    }

    /**
     * Set the value of processingExtractorType
     *
     * @param processingExtractorType new value of processingExtractorType
     */
    public void setProcessingExtractorType(ProcessingExractorType processingExtractorType) {
        this.processingExtractorType = processingExtractorType;
        if (processingExtractorType == processingExtractorType.DOWNSAMPLED_100) {
            setNumProcessingFeatures(100);
        } else {
            setNumProcessingFeatures(6);
        }
    }

    /**
     * Get the value of audioExtractionRate
     *
     * @return the value of audioExtractionRate
     */
    public int getAudioExtractionRate() {
        return audioExtractionRate;
    }

    /**
     * Set the value of audioExtractionRate
     *
     * @param audioExtractionRate new value of audioExtractionRate
     */
    public void setAudioExtractionRate(int audioExtractionRate) {
        this.audioExtractionRate = audioExtractionRate;
    }

    /**
     * Get the value of windowType
     *
     * @return the value of windowType
     */
    public WindowType getWindowType() {
        return windowType;
    }

    /**
     * Set the value of windowType
     *
     * @param windowType new value of windowType
     */
    public void setWindowType(WindowType windowType) {
        this.windowType = windowType;
    }

    /**
     * Get the value of fftWindowSize
     *
     * @return the value of fftWindowSize
     */
    public int getFftWindowSize() {
        return fftWindowSize;
    }

    /**
     * Set the value of fftWindowSize
     *
     * @param fftWindowSize new value of fftWindowSize
     */
    public void setFftWindowSize(int fftWindowSize) {
        if (fftWindowSize > 0) {
            this.fftWindowSize = fftWindowSize;
        }
    }

    /**
     * Get the value of numProcessingFeatures
     *
     * @return the value of numProcessingFeatures
     */
    public int getNumProcessingFeatures() {
        return numProcessingFeatures;
    }

    /**
     * Set the value of numProcessingFeatures
     *
     * @param numProcessingFeatures new value of numProcessingFeatures
     */
    protected void setNumProcessingFeatures(int numProcessingFeatures) {
        if (numProcessingFeatures > 0) {
            this.numProcessingFeatures = numProcessingFeatures;
        }
    }

    /**
     * Get the value of useProcessing
     *
     * @return the value of useProcessing
     */
    public boolean isUseProcessing() {
        return useProcessing;
    }

    /**
     * Set the value of useProcessing
     *
     * @param useProcessing new value of useProcessing
     */
    public void setUseProcessing(boolean useProcessing) {
        this.useProcessing = useProcessing;
    }

    /**
     * Get the value of useOtherHid
     *
     * @return the value of useOtherHid
     */
    public boolean isUseOtherHid() {
        return useOtherHid;
    }

    /**
     * Set the value of useOtherHid
     *
     * @param useOtherHid new value of useOtherHid
     */
    public void setUseOtherHid(boolean useOtherHid) {
        this.useOtherHid = useOtherHid;
    }

    /**
     * Get the value of motionSensorExtractionRate
     *
     * @return the value of motionSensorExtractionRate
     */
    public int getMotionSensorExtractionRate() {
        return motionSensorExtractionRate;
    }

    /**
     * Set the value of motionSensorExtractionRate
     *
     * @param motionSensorExtractionRate new value of motionSensorExtractionRate
     */
    public void setMotionSensorExtractionRate(int motionSensorExtractionRate) {
        if (motionSensorExtractionRate > 0) {
            this.motionSensorExtractionRate = motionSensorExtractionRate;
        }
    }

    /**
     * Get the value of useMotionSensor
     *
     * @return the value of useMotionSensor
     */
    public boolean isUseMotionSensor() {
        return useMotionSensor;
    }

    /**
     * Set the value of useMotionSensor
     *
     * @param useMotionSensor new value of useMotionSensor
     */
    public void setUseMotionSensor(boolean useMotionSensor) {
        this.useMotionSensor = useMotionSensor;
    }

    /**
     * Get the value of useTrackpad
     *
     * @return the value of useTrackpad
     */
    public boolean isUseTrackpad() {
        return useTrackpad;
    }

    /**
     * Set the value of useTrackpad
     *
     * @param useTrackpad new value of useTrackpad
     */
    public void setUseTrackpad(boolean useTrackpad) {
        this.useTrackpad = useTrackpad;
    }

    /**
     * Get the value of numCustomOscFeatures
     *
     * @return the value of numCustomOscFeatures
     */
    public int getNumCustomOscFeatures() {
        return numCustomOscFeatures;
    }

    /**
     * Set the value of numCustomOscFeatures
     *
     * @param numCustomOscFeatures new value of numCustomOscFeatures
     */
    public void setNumCustomOscFeatures(int numCustomOscFeatures) { //TODO: more error checking >0?
        this.numCustomOscFeatures = numCustomOscFeatures;
    }

    /**
     * Get the value of useCustomOscFeatures
     *
     * @return the value of useCustomOscFeatures
     */
    public boolean isUseCustomOscFeatures() {
        return useCustomOscFeatures;
    }

    /**
     * Set the value of useCustomOscFeatures
     *
     * @param useCustomOscFeatures new value of useCustomOscFeatures
     */
    public void setUseCustomOscFeatures(boolean useCustomOscFeatures) {
        this.useCustomOscFeatures = useCustomOscFeatures;
    }

    /**
     * Get the value of numCustomChuckFeatures
     *
     * @return the value of numCustomChuckFeatures
     */
    public int getNumCustomChuckFeatures() {
        return numCustomChuckFeatures;
    }

    /**
     * Set the value of numCustomChuckFeatures
     *
     * @param numCustomChuckFeatures new value of numCustomChuckFeatures
     */
    public void setNumCustomChuckFeatures(int numCustomChuckFeatures) {
        this.numCustomChuckFeatures = numCustomChuckFeatures;
    }

    /**
     * Get the value of useCustomChuckFeatures
     *
     * @return the value of useCustomChuckFeatures
     */
    public boolean isUseCustomChuckFeatures() {
        return useCustomChuckFeatures;
    }

    /**
     * Set the value of useCustomChuckFeatures
     *
     * @param useCustomChuckFeatures new value of useCustomChuckFeatures
     */
    public void setUseCustomChuckFeatures(boolean useCustomChuckFeatures) {
        this.useCustomChuckFeatures = useCustomChuckFeatures;
    }

    /**
     * Get the value of fftSize
     *
     * @return the value of fftSize
     */
    public int getFftSize() {
        return fftSize;
    }

    /**
     * Set the value of fftSize
     *
     * @param fftSize new value of fftSize
     */
    public void setFftSize(int fftSize) {
        this.fftSize = fftSize;
    }

    /**
     * Get the value of useRolloff
     *
     * @return the value of useRolloff
     */
    public boolean isUseRolloff() {
        return useRolloff;
    }

    /**
     * Set the value of useRolloff
     *
     * @param useRolloff new value of useRolloff
     */
    public void setUseRolloff(boolean useRolloff) {
        this.useRolloff = useRolloff;
    }

    /**
     * Get the value of useRMS
     *
     * @return the value of useRMS
     */
    public boolean isUseRMS() {
        return useRMS;
    }

    /**
     * Set the value of useRMS
     *
     * @param useRMS new value of useRMS
     */
    public void setUseRMS(boolean useRMS) {
        this.useRMS = useRMS;
    }

    /**
     * Get the value of useFlux
     *
     * @return the value of useFlux
     */
    public boolean isUseFlux() {
        return useFlux;
    }

    /**
     * Set the value of useFlux
     *
     * @param useFlux new value of useFlux
     */
    public void setUseFlux(boolean useFlux) {
        this.useFlux = useFlux;
    }

    /**
     * Get the value of useCentroid
     *
     * @return the value of useCentroid
     */
    public boolean isUseCentroid() {
        return useCentroid;
    }

    /**
     * Set the value of useCentroid
     *
     * @param useCentroid new value of useCentroid
     */
    public void setUseCentroid(boolean useCentroid) {
        this.useCentroid = useCentroid;
    }

    /**
     * Get the value of hidSetup
     *
     * @return the value of hidSetup
     */
    public HidSetup getHidSetup() {
        return hidSetup;
    }

    /**
     * Set the value of hidSetup
     *
     * @param hidSetup new value of hidSetup
     */
    public void setHidSetup(HidSetup hidSetup) {
        HidSetup oldHidSetup = this.hidSetup;
        this.hidSetup = hidSetup;
        propertyChangeSupport.firePropertyChange(PROP_HIDSETUP, oldHidSetup, hidSetup);
    }

    public FeatureConfiguration() {
    }

    /**
     * Get the value of useFFT
     *
     * @return the value of useFFT
     */
    public boolean isUseFFT() {
        return useFFT;
    }

    /**
     * Set the value of useFFT
     *
     * @param useFFT new value of useFFT
     */
    public void setUseFFT(boolean useFFT) {
        this.useFFT = useFFT;
    }

    public void validate() throws Exception {
        String errorString = "";
        if (getNumFeatures() == 0) {
            errorString += "Must have more than 0 features.\n";
        }

        if (useCustomChuckFeatures && numCustomChuckFeatures <= 0) {
            errorString += "If using a custom chuck feature extractor, must have more than 0 custom chuck features\n";
        }

        if (useCustomOscFeatures && numCustomOscFeatures <= 0) {
            errorString += "If using a custom OSC feature extractor, must have more than 0 custom OSC features\n";
        }

        if (getNumAudioFeatures() > 0) {
            if (fftSize <= 0 || fftWindowSize <= 0 || audioExtractionRate <= 0) {
                errorString += "If using audio features, must have a valid (> 0) fft size, window size, and extraction rate\n";
            }

            if (fftWindowSize > fftSize) {
                errorString += "If using audio features, window size must be no greater than the FFT size\n";
            }
        }

        if (useMotionSensor && motionSensorExtractionRate <= 0) {
            errorString += "Invalid motion sensor extraction rate\n";
        }

        if (useOtherHid && (hidSetup == null || !hidSetup.isUsable())) {
            errorString += "Must have valid HID setup in order to use other hid\n";
        }

        if (errorString.length() > 0) {
            throw new Exception(errorString);
        }
    }

    public static FeatureConfiguration readFromFile(File f) throws Exception {
        return (FeatureConfiguration) SerializedFileUtil.readFromFile(f);
    }

    void writeToFile(File file) throws Exception {
        SerializedFileUtil.writeToFile(file, this);
    }

    public String[] getFeatureNames() {
        String s[] = new String[getNumFeatures()];
        int n = 0;
        if (useFFT) {
            for (int i = 0; i < (fftSize * .5); i++) {
           //     System.out.println(i + " of " + fftSize + "; " + getNumFeatures());
                s[n++] = "FFT_" + i;
            }
        }
        if (useRMS) {
            s[n] = "RMS";
            n++;
        }
        if (useCentroid) {
            s[n] = "Centroid";
            n++;
        }
        if (useRolloff) {
            s[n] = "Rolloff";
            n++;
        }
        if (useFlux) {
            s[n] = "Flux";
            n++;
        }


        if (useTrackpad) {
            s[n++] = "Trackpad1";
            s[n++] = "Trackpad2";
        }

        if (useMotionSensor) {
            s[n++] = "Motion1";
            s[n++] = "Motion2";
            s[n++] = "Motion3";
        }

        if (useOtherHid) {
            for (int i = 0; i < getHidSetup().getNumFeaturesUsed(); i++) {
                s[n++] = "Hid_" + i;
            }
        }

        if (useProcessing) {
            for (int i = 0; i < getNumProcessingFeatures(); i++) {
                s[n] = "Processing_" + i;
                n++;
            }
        }

        if (useCustomChuckFeatures) {
            for (int i = 0; i < numCustomChuckFeatures; i++) {
                s[n] = "Chuck_" + i;
                n++;
            }
        }

        if (useCustomOscFeatures) {
            for (int i = 0; i < numCustomOscFeatures; i++) {
                s[n] = "OSC_" + i;
                n++;
            }

        }

        return s;
    }

    double[] process(double[] features) {
        //TODO: Add ability to compute additional statistics here.
        return features;
    }
}
