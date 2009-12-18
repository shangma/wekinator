/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wekinator;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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

    //TODO: add support for multiple channels!
    public static final String FFT = "FFT";
    public static final String CENTROID = "Centroid";
    public static final String FLUX = "Flux";
    public static final String RMS = "RMS";
    public static final String ROLLOFF = "Rolloff";
    public static final String CUSTOMCHUCK = "CustomChuck";
    public static final String CUSTOMOSC = "CustomOsc";
    public static final String TRACKPAD = "Trackpad";
    public static final String MOTION = "Motion";
    public static final String PROCESSING = "Processing";
    public static final String HID = "Hid";
    protected int motionSensorExtractionRate = 100;
    protected int fftWindowSize = 256;
    protected WindowType windowType = WindowType.HAMMING;
    protected int audioExtractionRate = 100;
    protected ProcessingExractorType processingExtractorType = ProcessingExractorType.DOWNSAMPLED_100;
    public static final String PROP_HIDSETUP = "hidSetup";
    public HidSetup hidSetup = new HidSetup();
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    protected int committedNumTotalFeatures = 0;
    protected int committedNumBaseFeatures = 0;

    protected void populateFeatureList() {
        featuresInOrder = new LinkedList<Feature>();
        features = new HashMap<String, Feature>();
        addFeature(FFT, 512);
        addFeature(RMS, 1);
        addFeature(CENTROID, 1);
        addFeature(ROLLOFF, 1);
        addFeature(FLUX, 1);
        addFeature(TRACKPAD, 2);
        addFeature(MOTION, 3);
        Feature f = new HidFeature(HID, 0);
        features.put(HID, f);
        featuresInOrder.add(f);
        addFeature(PROCESSING, 100);
        addFeature(CUSTOMCHUCK, 0);
        addFeature(CUSTOMOSC, 0);
    }

    protected void addFeature(String name, int dim) {
        Feature f = new Feature(name, dim);
        features.put(name, f);
        featuresInOrder.add(f);
    }
    protected HashMap<String, Feature> features;
    protected LinkedList<Feature> featuresInOrder;

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
        Feature fft = features.get(FFT);
        Feature centroid = features.get(CENTROID);
        Feature flux = features.get(FLUX);
        Feature rms = features.get(RMS);
        Feature rolloff = features.get(ROLLOFF);
        return ((fft.enabled ? ((int) (fft.dimensionality / 2)) : 0) + (centroid.enabled ? 1 : 0) + (flux.enabled ? 1 : 0) + (rms.enabled ? 1 : 0) + (rolloff.enabled ? 1 : 0));
    }

    public int getNumBaseFeatureClassesEnabled() {
        int s = 0;
        for (Feature f : features.values()) {
            if (f.enabled) {
                s++;
            }
        }
        return s;
    }

    public int getNumBaseFeaturesEnabled() {
        int s = 0;
        for (Feature f : features.values()) {
            if (f.enabled) {
                s += f.dimensionality;
            }
        }
        return s;
    }

    public int getNumFeaturesEnabled() {
        return getNumBaseFeaturesEnabled() + getNumMetaFeaturesEnabled();
    }

    public int getNumMetaFeaturesEnabled() {
        int s = 0;
        for (Feature f : features.values()) {
            if (f.enabled) {
                for (List<MetaFeature> l : f.metaFeatures) {
                    if (l != null) {
                        s += l.size();
                    }
                }
            }
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
        return features.get(PROCESSING).dimensionality;
    }

    /**
     * Set the value of numProcessingFeatures
     *
     * @param numProcessingFeatures new value of numProcessingFeatures
     */
    protected void setNumProcessingFeatures(int numProcessingFeatures) {
        if (numProcessingFeatures > 0) {
            features.get(PROCESSING).setDimensionality(numProcessingFeatures);
        }
    }

    /**
     * Get the value of useProcessing
     *
     * @return the value of useProcessing
     */
    public boolean isUseProcessing() {
        return features.get(PROCESSING).enabled;

    }

    /**
     * Set the value of useProcessing
     *
     * @param useProcessing new value of useProcessing
     */
    public void setUseProcessing(boolean useProcessing) {
        features.get(PROCESSING).enabled = useProcessing;
    }

    /**
     * Get the value of useOtherHid
     *
     * @return the value of useOtherHid
     */
    public boolean isUseOtherHid() {
        return features.get(HID).enabled;
    }

    /**
     * Set the value of useOtherHid
     *
     * @param useOtherHid new value of useOtherHid
     */
    public void setUseOtherHid(boolean useOtherHid) {
        features.get(HID).enabled = useOtherHid;
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
        return features.get(MOTION).enabled;
    }

    /**
     * Set the value of useMotionSensor
     *
     * @param useMotionSensor new value of useMotionSensor
     */
    public void setUseMotionSensor(boolean useMotionSensor) {
        features.get(MOTION).enabled = useMotionSensor;
    }

    /**
     * Get the value of useTrackpad
     *
     * @return the value of useTrackpad
     */
    public boolean isUseTrackpad() {
        return features.get(TRACKPAD).enabled;
    }

    /**
     * Set the value of useTrackpad
     *
     * @param useTrackpad new value of useTrackpad
     */
    public void setUseTrackpad(boolean useTrackpad) {
        features.get(TRACKPAD).enabled = useTrackpad;
    }

    /**
     * Get the value of numCustomOscFeatures
     *
     * @return the value of numCustomOscFeatures
     */
    public int getNumCustomOscFeatures() {
        return (features.get(CUSTOMOSC).dimensionality);
    }

    /**
     * Set the value of numCustomOscFeatures
     *
     * @param numCustomOscFeatures new value of numCustomOscFeatures
     */
    public void setNumCustomOscFeatures(int numCustomOscFeatures) { //TODO: more error checking >0?
        features.get(CUSTOMOSC).setDimensionality(numCustomOscFeatures);
    }

    /**
     * Get the value of useCustomOscFeatures
     *
     * @return the value of useCustomOscFeatures
     */
    public boolean isUseCustomOscFeatures() {
        return features.get(CUSTOMOSC).enabled;
    }

    /**
     * Set the value of useCustomOscFeatures
     *
     * @param useCustomOscFeatures new value of useCustomOscFeatures
     */
    public void setUseCustomOscFeatures(boolean useCustomOscFeatures) {
        features.get(CUSTOMOSC).enabled = useCustomOscFeatures;
    }

    /**
     * Get the value of numCustomChuckFeatures
     *
     * @return the value of numCustomChuckFeatures
     */
    public int getNumCustomChuckFeatures() {
        return features.get(CUSTOMCHUCK).dimensionality;
    }

    /**
     * Set the value of numCustomChuckFeatures
     *
     * @param numCustomChuckFeatures new value of numCustomChuckFeatures
     */
    public void setNumCustomChuckFeatures(int numCustomChuckFeatures) {
        features.get(CUSTOMCHUCK).setDimensionality(numCustomChuckFeatures);
    }

    /**
     * Get the value of useCustomChuckFeatures
     *
     * @return the value of useCustomChuckFeatures
     */
    public boolean isUseCustomChuckFeatures() {
        return features.get(CUSTOMCHUCK).enabled;
    }

    /**
     * Set the value of useCustomChuckFeatures
     *
     * @param useCustomChuckFeatures new value of useCustomChuckFeatures
     */
    public void setUseCustomChuckFeatures(boolean useCustomChuckFeatures) {
        features.get(CUSTOMCHUCK).enabled = useCustomChuckFeatures;
    }

    /**
     * Get the value of fftSize
     *
     * @return the value of fftSize
     */
    public int getFftSize() {
        return features.get(FFT).dimensionality*2;
    }

    /**
     * Set the value of fftSize
     *
     * @param fftSize new value of fftSize
     */
    public void setFftSize(int fftSize) {
        features.get(FFT).setDimensionality(fftSize/2);
    }

    /**
     * Get the value of useRolloff
     *
     * @return the value of useRolloff
     */
    public boolean isUseRolloff() {
        return features.get(ROLLOFF).enabled;
    }

    /**
     * Set the value of useRolloff
     *
     * @param useRolloff new value of useRolloff
     */
    public void setUseRolloff(boolean useRolloff) {
        features.get(ROLLOFF).enabled = useRolloff;
    }

    /**
     * Get the value of useRMS
     *
     * @return the value of useRMS
     */
    public boolean isUseRMS() {
        return features.get(RMS).enabled;
    }

    /**
     * Set the value of useRMS
     *
     * @param useRMS new value of useRMS
     */
    public void setUseRMS(boolean useRMS) {
        features.get(RMS).enabled = useRMS;
    }

    /**
     * Get the value of useFlux
     *
     * @return the value of useFlux
     */
    public boolean isUseFlux() {
        return features.get(FLUX).enabled;
    }

    /**
     * Set the value of useFlux
     *
     * @param useFlux new value of useFlux
     */
    public void setUseFlux(boolean useFlux) {
        features.get(FLUX).enabled = useFlux;
    }

    /**
     * Get the value of useCentroid
     *
     * @return the value of useCentroid
     */
    public boolean isUseCentroid() {
        return features.get(CENTROID).enabled;
    }

    /**
     * Set the value of useCentroid
     *
     * @param useCentroid new value of useCentroid
     */
    public void setUseCentroid(boolean useCentroid) {
        features.get(CENTROID).enabled = useCentroid;
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
        populateFeatureList();
    }

    /**
     * Get the value of useFFT
     *
     * @return the value of useFFT
     */
    public boolean isUseFFT() {
        return features.get(FFT).enabled;
    }

    /**
     * Set the value of useFFT
     *
     * @param useFFT new value of useFFT
     */
    public void setUseFFT(boolean useFFT) {
        features.get(FFT).enabled = useFFT;
    }

    public void validate() throws Exception {
        String errorString = "";
        if (getNumFeaturesEnabled() == 0) {
            errorString += "Must have more than 0 features.\n";
        }

        if (isUseCustomChuckFeatures() && getNumCustomChuckFeatures() <= 0) {
            errorString += "If using a custom chuck feature extractor, must have more than 0 custom chuck features\n";
        }

        if (isUseCustomOscFeatures() && getNumCustomOscFeatures() <= 0) {
            errorString += "If using a custom OSC feature extractor, must have more than 0 custom OSC features\n";
        }

        if (getNumAudioFeatures() > 0) {
            if (getFftSize() <= 0 || fftWindowSize <= 0 || audioExtractionRate <= 0) {
                errorString += "If using audio features, must have a valid (> 0) fft size, window size, and extraction rate\n";
            }

            if (fftWindowSize > getFftSize()) {
                errorString += "If using audio features, window size must be no greater than the FFT size\n";
            }
        }

        if (isUseMotionSensor() && motionSensorExtractionRate <= 0) {
            errorString += "Invalid motion sensor extraction rate\n";
        }

        if (isUseOtherHid() && (hidSetup == null || !hidSetup.isUsable())) {
            errorString += "Must have valid HID setup in order to use other hid\n";
        }

        if (errorString.length() > 0) {
            throw new Exception(errorString);
        }

        //Commit # features
        committedNumTotalFeatures = getNumFeaturesEnabled();
        committedNumBaseFeatures = getNumBaseFeaturesEnabled();
    }

    public static FeatureConfiguration readFromFile(File f) throws Exception {
        FeatureConfiguration fc = (FeatureConfiguration) SerializedFileUtil.readFromFile(f);
        fc.committedNumTotalFeatures = fc.getNumFeaturesEnabled();
        fc.committedNumBaseFeatures = fc.getNumBaseFeaturesEnabled();
        return fc;
    }

    void writeToFile(File file) throws Exception {
        SerializedFileUtil.writeToFile(file, this);
    }

    public String[] getEnabledBaseFeatureClassNames() {
        //Problem: Could result in different order every time!
       // String s[] = new String[getNumBaseFeaturesEnabled()]; //Problem: is base feature # dimensons or # features?
        LinkedList<String> s = new LinkedList<String>();

        int i = 0;
        for (Feature f : featuresInOrder) {
            if (f.enabled) {
                s.add(f.name);
            }
        }
        return s.toArray(new String[0]);
    }

    public void addMetaFeature(String featureName, MetaFeature.Type metafeatureType, int featureDimension) {
        if (features.containsKey(featureName)) { //TODO: also check that metafeature name is ok!
            Feature f = features.get(featureName);
            if (featureDimension < f.dimensionality) {
                LinkedList<MetaFeature> metafeatures = f.metaFeatures.get(featureDimension);
                metafeatures.add(MetaFeature.createForType(metafeatureType, f));
            } else {
                System.out.println("invalid feature dimension " + featureDimension);
            }
        } else {
            System.out.println("Error: no feature with name " + featureName);
        }
    }

    public void removeAllMetaFeatures() {
        for (Feature f : features.values()) {
            f.metaFeatures = new ArrayList<LinkedList<MetaFeature>>(f.dimensionality);
            for (int i = 0; i < f.dimensionality; i++) {
                LinkedList<MetaFeature> l = new LinkedList<MetaFeature>();
                f.metaFeatures.add(l);
            }
        }
    }

    protected HashMap<String, Feature> getBaseFeatures() {
        return features;
    }

    //Doesn't affect features not in the HashMap!
    public void setMetaFeaturesFromMatrix(HashMap<String, ArrayList<LinkedList<MetaFeature>>> list) {
        for (String fname : list.keySet()) {
            features.get(fname).metaFeatures = list.get(fname);
        }
    }

    public String[] getBaseEnabledFeatureNames() {
        String[] s = new String[getNumBaseFeaturesEnabled()];
        int index = 0;

        for (Feature f : featuresInOrder) {
            if (f.enabled) {
                for (int i = 0; i < f.dimensionality; i++) {
                    s[index++] = f.name + "_" + i;
                }
            }
        }
        return s;
    }

    public String[] getAllEnabledFeatureNames() {
        if (getNumMetaFeaturesEnabled() ==0) {
            return getBaseEnabledFeatureNames();
        }

        String[] s = new String[getNumFeaturesEnabled()];
        int i = 0;
        for (Feature feat : featuresInOrder) {
            int featNum = 0;

            if (feat.enabled) {
                ArrayList<LinkedList<MetaFeature>> mflists = feat.metaFeatures;
                for (int j = 0; j < feat.dimensionality; j++) {
                    s[i++] = feat.name + "_" + featNum;
                    featNum++;
                    for (MetaFeature mf : mflists.get(j)) {
                        s[i++] = mf.getFeatureName() + "_" + featNum; //err: mf is null here!
                    }

                }
            }
        }
        return s;
    }

    double[] process(double[] f) {
        if (f.length != committedNumBaseFeatures) {
            System.out.println("Error: wrong num features received. Expected " + committedNumBaseFeatures + ", received " + f.length);
            return new double[0];
        }

        if (committedNumTotalFeatures == committedNumBaseFeatures) {
            return f;
        }

        double[] out = new double[committedNumTotalFeatures];

        int i = 0; //index into output array
        int j = 0; //index into original feature array
        for (Feature feat : featuresInOrder) {
            if (feat.enabled) {
                for (int d = 0; d < feat.dimensionality; d++) {
                    out[i++] = f[j];
                    LinkedList<MetaFeature> mflist = feat.metaFeatures.get(d); //this is causing exception for index that should be ok (16 when 19 feats - metafeats not counted?)
                    for (MetaFeature mf : mflist) {
                        double[] mfout = mf.computeForNextFeature(f, j);
                        for (int k = 0; k < mfout.length; k++) {
                            out[i++] = mfout[k];
                        }
                    }

                    j++;
                }
            }
        }
        return out;
    }

    protected class Feature implements Serializable {

        public Feature(String name, int dimensionality) {
            this.name = name;
            setDimensionality(dimensionality);
            this.metaFeatures = new ArrayList<LinkedList<MetaFeature>>(dimensionality);
            for (int i = 0; i < dimensionality; i++) {
                this.metaFeatures.add(new LinkedList<MetaFeature>());
            }
        }

        public void setDimensionality(int dim) {
            this.dimensionality = dim;
            if (metaFeatures == null) {
                metaFeatures = new ArrayList<LinkedList<MetaFeature>>(dimensionality);

            }
            while (metaFeatures.size() < dim) {
                metaFeatures.add(new LinkedList<MetaFeature>());
            }
            while (metaFeatures.size() > dim) {
                metaFeatures.remove(metaFeatures.size() - 1);
            }
        }
        public String name = "feature";
        public boolean enabled = false;
        public ArrayList<LinkedList<MetaFeature>> metaFeatures = null;
        protected int dimensionality = 0;

        public int getDimensionality() {
            return dimensionality;
        }
    }

    protected class HidFeature extends Feature {

        public HidFeature(String name, int dimensionality) {
            super(name, dimensionality);
        }

        @Override
        public int getDimensionality() {
            if (hidSetup == null) {
                return 0;
            }

            return hidSetup.getNumFeaturesUsed();
        }
    }
}
