/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wekinator;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * //TODO: listen for feature name # / type changes in global wekinator system.
 *
 * @author rebecca
 */
public class FeatureExtractionController {
    private static final FeatureExtractionController ref = new FeatureExtractionController();
    protected FeatureViewer featureViewer = null;
    protected boolean extracting = false;
    public static final String PROP_EXTRACTING = "extracting";
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    /**
     * Add PropertyChangeListener.
     *
     * @param listener
     */
    public static void addPropertyChangeListener(PropertyChangeListener listener) {
        ref.propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Remove PropertyChangeListener.
     *
     * @param listener
     */
    public static void removePropertyChangeListener(PropertyChangeListener listener) {
        ref.propertyChangeSupport.removePropertyChangeListener(listener);
    }

    private FeatureExtractionController() {

    }

    /**
     * Get the value of extractingForViewing
     *
     * @return the value of extractingForViewing
     */
    public static boolean isExtracting() {
        return ref.extracting;
    }

    /**
     * Set the value of extractingForLearning
     *
     * @param extractingForLearning new value of extractingForLearning
     */
    private static void setExtracting(boolean extracting) {

        boolean oldTmpProp = ref.extracting;
        ref.extracting = extracting;
        ref.propertyChangeSupport.firePropertyChange(PROP_EXTRACTING, oldTmpProp, ref.extracting);

    }

    public static void showFeatureViewer() {
        //TODO: update when global features change!
        if (ref.featureViewer == null) {
            ref.featureViewer = new FeatureViewer();
            ref.featureViewer.setNames(WekinatorInstance.getWekinatorInstance().getFeatureConfiguration().getAllEnabledFeatureNames());
        }
        ref.featureViewer.setVisible(true);
        ref.featureViewer.toFront();
    }

    public static void updateFeatures(double[] d) {
        double[] fs = WekinatorInstance.getWekinatorInstance().getFeatureConfiguration().process(d);

        if (ref.featureViewer != null) {
            ref.featureViewer.updateFeatures(fs);
        }
        WekinatorLearningManager.getInstance().updateFeatures(fs);
    }

    public static void startExtracting() {
        setExtracting(true);
        OscHandler.getOscHandler().initiateRecord();
    }

    public static void stopExtracting() {
        setExtracting(false);
        OscHandler.getOscHandler().stopExtractingFeatures();
    }
}
