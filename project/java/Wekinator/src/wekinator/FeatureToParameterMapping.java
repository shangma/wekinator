/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wekinator;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import wekinator.FeatureManager;

/**
 *
 * @author rebecca
 */
public class FeatureToParameterMapping {

    // protected int numFeatures = 0;
    public static final String PROP_NUMFEATURES = "numFeatures";
    protected int numParams = 0;
    public static final String PROP_NUMPARAMS = "numParams";

    //List<Boolean> lists whether a feature is used for a parameter; length numParams;
    private List<Feature> features = new LinkedList<Feature>();
    private List<Set> featureFamilies = new LinkedList<Set>();
    //Set<Feature> unassignedFeatures = new HashSet<Feature>();
    Set<Feature> chuckAudioFeatures = new HashSet<Feature>();
    Set<Feature> trackpadFeatures = new HashSet<Feature>();
    Set<Feature> motionSensorFeatures = new HashSet<Feature>();
    Set<Feature> hidFeatures = new HashSet<Feature>();
    Set<Feature> customChuckFeatures = new HashSet<Feature>();
    Set<Feature> customOSCFeatures = new HashSet<Feature>();
    Set<Feature> processingFeatures = new HashSet<Feature>();

    public FeatureToParameterMapping(FeatureManager fm, int numParams) {
        //featureFamilies.add(unassignedFeatures);
        //TODO: This really belongs in FeatureManager, not here
        featureFamilies.add(chuckAudioFeatures);
        featureFamilies.add(trackpadFeatures);
        featureFamilies.add(motionSensorFeatures);
        featureFamilies.add(hidFeatures);
        featureFamilies.add(customChuckFeatures);
        featureFamilies.add(customOSCFeatures);
        featureFamilies.add(processingFeatures);

        setNumParams(numParams);

        //Populate families from feature manager
        if (fm.useAudio) {
            if (fm.useFFT) {
                for (int i = 0; i < fm.getFFTSize(); i++) {
                    features.add(new Feature(chuckAudioFeatures, "FFT_" + i, numParams));
                }
            }
            if (fm.useCentroid) {
                features.add(new Feature(chuckAudioFeatures, "Centroid", numParams));
            }
            if (fm.useFlux) {
                features.add(new Feature(chuckAudioFeatures, "Flux", numParams));

            }
            if (fm.useRMS) {
                features.add(new Feature(chuckAudioFeatures, "RMS", numParams));

            }
            if (fm.useRolloff) {
                features.add(new Feature(chuckAudioFeatures, "Rolloff", numParams));

            }
        }

        if (fm.useCustomChuck) {
            for (int i = 0; i < fm.numCustomChuck; i++) {
                features.add(new Feature(customChuckFeatures, "Chuck_" + i, numParams));
            }
        }
        if (fm.useCustomOsc) {
            for (int i = 0; i < fm.numCustomOsc; i++) {
                features.add(new Feature(customOSCFeatures, "OSC_" + i, numParams));
            }

        }

        if (fm.useTrackpad) {
            features.add(new Feature(trackpadFeatures, "Trackpad1", numParams));
            features.add(new Feature(trackpadFeatures, "Trackpad2", numParams));

        }

        if (fm.useMotionSensor) {
            features.add(new Feature(motionSensorFeatures, "Motion1", numParams));
            features.add(new Feature(motionSensorFeatures, "Motion2", numParams));
            features.add(new Feature(motionSensorFeatures, "Motion3", numParams));
        }

        if (fm.useOtherHid) {
            for (int i = 0; i < fm.getNumOtherHidFeatures(); i++) {
                features.add(new Feature(hidFeatures, "Hid_" + i, numParams));
            }
        }

        if (fm.useProcessing) {
            for (int i = 0; i < fm.getNumProcessingFeatures(); i++) {
                features.add(new Feature(processingFeatures, "Processing_" + i, numParams));
            }
        }

        //Sanity check
        if (features.size() != fm.getNumFeatures()) {
            System.out.println("ERROR MISMATCH IN FEATURE SIZES: " + features.size() + "here, " + fm.getNumFeatures() + "there");
        }
    }

    public boolean getIsFeatureUsingParam(int f, int p) {
        if (f >= 0 && f < features.size() && p >= 0 && p < numParams) {
            Feature feat = features.get(f);
            if (feat != null) {
                return features.get(f).featureMask.get(p);
            } else {

                System.out.println("Error! bad!");
                return false;
            }
        } else {
            return false;
        }
    }

    public void setIsFeatureUsingParam(int f, int p, boolean use) {
        if (f >= 0 && f < features.size() && p >= 0 && p < numParams) {
            features.get(f).featureMask.set(p, use);
        }
    }

    /**
     * Get the value of numParams
     *
     * @return the value of numParams
     */
    public int getNumParams() {
        return numParams;
    }

    /**
     * Set the value of numParams
     *
     * @param numParams new value of numParams
     */
    public void setNumParams(int numParams) {

        if (numParams < 0) {
            return;
        }

        int oldNumParams = this.numParams;
        this.numParams = numParams;

        for (Feature f : features) {
            f.setNumParams(numParams);
        }


        propertyChangeSupport.firePropertyChange(PROP_NUMPARAMS, oldNumParams, numParams);
    }

    /**
     * Get the value of numFeatures
     *
     * @return the value of numFeatures
     */
    public int getNumFeatures() {
        return features.size();
    }

    public String getFeatureName(int i) {
        if (i >= 0 && i < features.size()) { //TODO: best practice on error checking here?
            System.out.println("Name is " + features.get(i).name);
            return features.get(i).name;
        }
        return null;
    }
    /**
     * Set the value of numFeatures
     *
     * @param numFeatures new value of numFeatures
     */
    /*   public void setNumFeatures(int numFeatures) {
    if (numFeatures < 0) return;

    int oldNumFeatures = this.numFeatures;
    this.numFeatures = numFeatures;

    while (numFeatures > features.size()) {
    features.add(new Feature(unassignedFeatures));
    }

    //  propertyChangeSupport.firePropertyChange(PROP_NUMFEATURES, oldNumFeatures, numFeatures);
    } */
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

    public void setAllFeaturesTrue() {
        for (Feature f : features) {
            for (int i = 0; i < f.featureMask.size(); i++) {
                f.featureMask.set(i, true);
            }
        }

    }


}



class Feature {

    public ArrayList<Boolean> featureMask;
    public Set<Feature> featureFamily;
    public String name;

    public Feature() {
        this(new HashSet<Feature>(), "feature", 0);
    }

    public Feature(Set<Feature> featureFamily, String name, int numParams) {
        this.name = name;
        featureMask = new ArrayList<Boolean>(numParams);
        for (int i = 0; i < numParams; i++) {
            featureMask.add(true);
        }


        this.featureFamily = featureFamily;
        featureFamily.add(this);
    }

    public void setNumParams(int n) {

        while (n > featureMask.size()) {
            //Add a feature
            featureMask.add(true);
        }
        while (n < featureMask.size()) {
            featureMask.remove(featureMask.size() - 1);
        }
    }

    public void addParamAt(int i) {
        featureMask.add(i, true);
    }

    public void removeParamAt(int i) {
        featureMask.remove(i);
    }

    public void useParam(int p, boolean use) {
        if (p > 0 && p < featureMask.size()) {
            featureMask.set(p, use);
        }
    }
}


