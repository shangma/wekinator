/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package wekinator;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 *
 * @author rebecca
 */
public class FeatureExtractorProxy {
    protected FeatureViewer featureViewer = null;

    protected boolean extractingForLearning = false;
    protected boolean extractingForViewing = false;
    private boolean isActuallyExtracting = false;
    public static final String PROP_EXTRACTING_FOR_LEARNING = "extractingForLearning";

   
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

        private static FeatureExtractorProxy ref = null;

        public static synchronized FeatureExtractorProxy get() {
        if (ref == null) {
            ref = new FeatureExtractorProxy();
        }
        return ref;
    }

        private FeatureExtractorProxy() {
           // WekinatorInstance.getWekinatorInstance().ad : Ideally listen to any feature config changes!
        }

    /**
     * Get the value of extractingForViewing
     *
     * @return the value of extractingForViewing
     */
    public boolean isExtractingForViewing() {
        return extractingForViewing;
    }

    /**
     * Set the value of extractingForViewing
     *
     * @param extractingForViewing new value of extractingForViewing
     */
    public void setExtractingForViewing(boolean extractingForViewing) {
        this.extractingForViewing = extractingForViewing;
        adjustExtracting();
    }

    /**
     * Get the value of extractingForLearning
     *
     * @return the value of extractingForLearning
     */
    public boolean isExtractingForLearning() {
        return extractingForLearning;
    }

    /**
     * Set the value of extractingForLearning
     *
     * @param extractingForLearning new value of extractingForLearning
     */
    public void setExtractingForLearning(boolean extractingForLearning) {

        boolean oldTmpProp = this.extractingForLearning;
        this.extractingForLearning = extractingForLearning;
        adjustExtracting();
        propertyChangeSupport.firePropertyChange(PROP_EXTRACTING_FOR_LEARNING, oldTmpProp, extractingForLearning);

    }

    public void showFeatureViewer() {
        if (featureViewer == null) {
            featureViewer = new FeatureViewer();
            featureViewer.setNames(WekinatorLearningManager.getInstance().getFeatureConfiguration().getAllEnabledFeatureNames());
        }
        featureViewer.setVisible(true);
        featureViewer.toFront();
    }
    
    void updateFeatures(double[] d) {
       //  WekinatorLearningManager.getInstance().updateFeatures(d); //may have to decouple this if want to view features too
         double[] fs = WekinatorLearningManager.getInstance().getFeatureConfiguration().process(d);

         if (featureViewer != null) {
            featureViewer.updateFeatures(fs);
         }

         WekinatorLearningManager.getInstance().updateFeatures(fs);
         
    }

    private void adjustExtracting() {

         if ((extractingForLearning || extractingForViewing) && ! isActuallyExtracting)
            startExtracting();

        else if (!extractingForLearning && !extractingForViewing && isActuallyExtracting) {
            stopExtracting();
        }
    }

    private void startExtracting() {
        isActuallyExtracting = true;
        OscHandler.getOscHandler().initiateRecord();
    }

    private void stopExtracting() {
        isActuallyExtracting = false;
        OscHandler.getOscHandler().stopExtractingFeatures();
    }




}
