/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package KyleWrapper;

import com.illposed.osc.OSCListener;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortIn;
import com.illposed.osc.OSCPortOut;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import javax.swing.event.EventListenerList;
import weka.classifiers.lazy.IBk;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import wekinator.util.SerializedFileUtil;
import wekinator.util.Util;

/**
 *
 * @author fiebrink
 */
//TODO: Run chuck FE from within here? Or elsewhere?
//TODO: What about synthesis? Is it listening for OSC message from Wekinator, or from Kyle's code?
//TODO: Set k to be sqrt(n) every time classifier is built or updated.
public class BeatboxWekinatorWrapper {

    protected int numFeatures = 0;
    protected int maxNumClasses = 100;
    protected Remove instanceFilter;
    protected IBk activeClassifier;
    protected Instances allInstances;
    protected HashMap<Integer, Instance> allInstancesHash; //get reference to Instance in allInstances by ID
    protected Instances activeInstances;
    protected HashMap<Integer, Instance> activeInstancesHash; //get reference to Instance is activeInstances by ID
    protected Instances dummyInstances;
    // protected HashSet<Integer> activeInstanceIDs = new HashSet<Integer>();
    // protected HashMap<Integer, double[]> allInstances = new HashMap<Integer, double[]>(); //value includes id and 0-value class in double array
    protected RecordingState recordingState = RecordingState.NOT_RECORDING;
    protected RunningState runningState = RunningState.NOT_RUNNING;
    protected EventListenerList classificationListenerList = new EventListenerList(); //listening for new classification result notification
    protected EventListenerList trainingListenerList = new EventListenerList(); //listening for new training example notification
    public static String PROP_RECORDINGSTATE = "recordingState";
    public static String PROP_RUNNINGSTATE = "runningState";
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);


    public int receivePort = 6448;
    public int sendPort = 6453;
    OSCPortOut sender;
    public OSCPortIn receiver;
    String paramSendString = "/params";

    //TODO: Do we want ability to undo the last delete?
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

    public void addClassificationListener(ClassificationListener l) {
        classificationListenerList.add(ClassificationListener.class, l);
    }

    public void removeClassificationListener(ClassificationListener l) {
        classificationListenerList.remove(ClassificationListener.class, l);
    }

    public void addTrainingExampleListener(TrainingExampleListener l) {
        trainingListenerList.add(TrainingExampleListener.class, l);
    }

    public void removeTrainingExampleListener(TrainingExampleListener l) {
        trainingListenerList.remove(TrainingExampleListener.class, l);
    }



    //Whether Wekinator should add incoming feature vectors to the training set
    public enum RecordingState {

        RECORDING,
        NOT_RECORDING
    };

    //Whether Wekinator should be classifying incoming feature vectors
    public enum RunningState {

        RUNNING,
        NOT_RUNNING
    };

    public BeatboxWekinatorWrapper(int numFeatures, int maxClasses) throws Exception {
        this.numFeatures = numFeatures;
        this.maxNumClasses = maxClasses;

        addOscListeners(); //Listen for everything Wekinator needs to: incoming features/IDs, other?

        //build infrastructure for classifier
        //A fastvector element for each feature
        FastVector ff = new FastVector(numFeatures + 2); //Include ID, class
        //Add ID
        ff.addElement(new Attribute("ID"));

        //Add features
        for (int i = 0; i < numFeatures; i++) {
            ff.addElement(new Attribute("Feature_" + i));
        }

        //Add class
        FastVector classes = new FastVector(maxNumClasses);
        for (int val = 0; val < maxNumClasses; val++) {
            classes.addElement((new Integer(val)).toString());
        }
        ff.addElement(new Attribute("Class", classes));

        dummyInstances = new Instances("dataset", ff, 0);
        dummyInstances.setClassIndex(dummyInstances.numAttributes() - 1);

         //A filter to remove ID so it's not used in classification
        instanceFilter = new Remove();
        int[] indicesToRemove = new int[1]; //just ID
        indicesToRemove[0] = 0; //id
        instanceFilter.setAttributeIndicesArray(indicesToRemove);
        instanceFilter.setInputFormat(dummyInstances);

        //Set up dummy instances to reflect state of actual instances
        activeInstances = new Instances(dummyInstances);
        activeInstancesHash = new HashMap<Integer, Instance>();

        allInstances = new Instances(dummyInstances);
        allInstancesHash = new HashMap<Integer, Instance>();

        activeClassifier = null;
    }

    public int getNumFeatures() {
        return numFeatures;
    }

    //
    //Don't use this method from outside class unless you're simulating Chuck FE sending this info to Wekinator directly.
    public void addTrainingExample(int id, double[] features) {
        double[] featuresWithID = new double[2 + features.length];
        featuresWithID[0] = (double) id;
        System.arraycopy(features, 0, featuresWithID, 1, features.length);
        featuresWithID[featuresWithID.length - 1] = 0;

        Instance i = new Instance(1.0, featuresWithID);
        i.setClassMissing();
        allInstances.add(i);
        Instance ref = allInstances.lastInstance();
        allInstancesHash.put(id, ref);
        newTrainingExampleRecorded(id);
    }

    //example ID must already have been recorded here
    public void addTrainingExampleToActiveClassifier(int exampleId, int classValue) {
        if (!activeInstancesHash.containsKey(exampleId)) {
            try {
                Instance i = new Instance(allInstancesHash.get(exampleId));
                i.setClassValue(classValue);
                activeInstances.add(i);
                Instance ref = activeInstances.lastInstance();
                activeInstancesHash.put(exampleId, ref);

                //kNN specific: (otherwise need to retrain)
                activeClassifier.updateClassifier(i);

            } catch (Exception ex) {
                //TODO: something useful if classifier cannot be updated.
            }
        }
    }

    public boolean exampleIdExists(int id) {
        return allInstancesHash.containsKey(id);
    }

    public int numTrainingExamples() {
        return allInstancesHash.size();
    }

    public void startRecordingExamples() throws Exception {
        if (recordingState == RecordingState.NOT_RECORDING) {
            setRecordingState(RecordingState.RECORDING);
        }
    }

    public void stopRecordingExamples() {
        if (recordingState == RecordingState.RECORDING) {
            setRecordingState(RecordingState.NOT_RECORDING);
        }
    }

    public void startRunning() {
        if (runningState != RunningState.RUNNING) {
            //For now, this should be fine even if classifier is untrained / no data.
            setRunningState(RunningState.RUNNING);
        }
    }

    public void stopRunning() {
        if (runningState == RunningState.RUNNING) {
            setRunningState(RunningState.NOT_RUNNING);
        }
    }

    public void deleteTrainingExample(int id) {
        Instance target = allInstancesHash.get(id);

        if (target == null) {
            //TODO: something useful
            return;
        }

        //Wish there were some way to directly do this, but no...
        for (int i = 0; i < allInstances.numInstances(); i++) {
            Instance next = allInstances.instance(i);
            if (target == next) {
                System.out.println("FOund instance to delete at index " + i);
                allInstances.delete(i);
                allInstancesHash.remove(i);
            }
        }

        //If it's active:
        Instance activeTarget = activeInstancesHash.get(id);
        if (activeTarget != null) {
            //Wish there were some way to directly do this, but no...
            for (int i = 0; i < activeInstances.numInstances(); i++) {
                Instance next = activeInstances.instance(i);
                if (activeTarget == next) {
                    try {
                        System.out.println("FOund active instance to delete at index " + i);
                        activeInstances.delete(i);
                        activeInstancesHash.remove(i);
                        //Now retrain/rebuild
                        activeClassifier.buildClassifier(Filter.useFilter(activeInstances, instanceFilter));
                    } catch (Exception ex) {
                        System.out.println("Error encountered in re-building classifier after delete example");
                        //TODO: Something useful.
                    }
                }
            }
        }
    }

    public void deleteTrainingExamples(int[] ids) {
        boolean activeExampleDeleted = false;
        for (int j = 0; j < ids.length; j++) {
            int id = ids[j];
            Instance target = allInstancesHash.get(id);

            if (target != null) {
                for (int i = 0; i < allInstances.numInstances(); i++) {
                    Instance next = allInstances.instance(i);
                    if (target == next) {
                        System.out.println("FOund instance to delete at index " + i);
                        allInstances.delete(i);
                        allInstancesHash.remove(i);
                    }
                }
            } else {
                //Target is null
                //TODO: something useful? don't return.
            }

            //If it's active:
            Instance activeTarget = activeInstancesHash.get(id);
            if (activeTarget != null) {
                //Wish there were some way to directly do this, but no...
                for (int i = 0; i < activeInstances.numInstances(); i++) {
                    Instance next = activeInstances.instance(i);
                    if (activeTarget == next) {
                        System.out.println("FOund active instance to delete at index " + i);
                        activeInstances.delete(i);
                        activeInstancesHash.remove(i);
                        activeExampleDeleted = true;
                    }
                }
            }
        }

        if (activeExampleDeleted) { //TODO: check actually deleted above
            //rebuild classifier
            try {
                //TODO: check actually deleted above
                //rebuild classifier
                if (activeInstances.numInstances() > 0) {
                    activeClassifier.buildClassifier(Filter.useFilter(activeInstances, instanceFilter));
                } else {
                    activeClassifier = null;
                }
            } catch (Exception ex) {
                System.out.println("Error encountered in re-building classifier after delete examples");
                //TODO: something more useful
            }
        }

    }

    public void deleteAllTrainingExamples() {
       allInstancesHash = new HashMap<Integer, Instance>();
       activeInstancesHash = new HashMap<Integer, Instance>();
       allInstances = new Instances(dummyInstances);
       activeInstances = new Instances(dummyInstances);
       activeClassifier = null;
    }

    //assumes features lacks ID, class; if not the case, we can easily change this
    public int classifyExample(double[] features) {
        if (activeClassifier == null) {
            return -1;
        } else {
            double[] featureVector = new double[features.length + 2];
            featureVector[0] =0.0; // add dummy ID
            featureVector[featureVector.length - 1] = 0.0;
            System.arraycopy(features, 0, featureVector, 1, features.length);
            instanceFilter.input(new Instance(1.0, featureVector));
            try {
                return (int) activeClassifier.classifyInstance(instanceFilter.output());
            } catch (Exception ex) {
                System.out.println("Error: Could not classify new instance");
                return -2; //TODO throw exception instead.
            }
        }
    }

    public RecordingState getRecordingState() {
        return recordingState;
    }

    protected void setRecordingState(RecordingState recordingState) {
        RecordingState oldState = this.recordingState;
        this.recordingState = recordingState;
        propertyChangeSupport.firePropertyChange(PROP_RECORDINGSTATE, oldState, recordingState);
    }

    public RunningState getRunningState() {
        return runningState;
    }

    protected void setRunningState(RunningState runningState) {
        RunningState oldState = this.runningState;
        this.runningState = runningState;
        propertyChangeSupport.firePropertyChange(PROP_RUNNINGSTATE, oldState, runningState);
    }

    public void saveWekinatorToFile(File f) throws Exception {
        SerializedFileUtil.writeToFile(f, this);
    }

    public static BeatboxWekinatorWrapper loadFromFile(File f) throws Exception {
        BeatboxWekinatorWrapper w = (BeatboxWekinatorWrapper)SerializedFileUtil.readFromFile(f);
        return w;
    }

    public int[] getExampleIds() {
        Integer[] tmp = new Integer[0];
        Integer[] ids = allInstancesHash.keySet().toArray(tmp);
        int[] intIds = new int[ids.length];
        for (int i = 0; i < ids.length; i++) {
            intIds[i] = ids[i].intValue();
        }
        return intIds;
    }

    protected void newClassificationResult(int id, int classValue) {
        // Guaranteed to return a non-null array
        Object[] listeners = classificationListenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ClassificationListener.class) {
                ((ClassificationListener) listeners[i + 1]).fireClassificationResult(id, classValue);
            }
        }
    }

    //TODO: Call this when appropriate
    protected void newTrainingExampleRecorded(int id) {
        Object[] listeners = trainingListenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TrainingExampleListener.class) {
                ((TrainingExampleListener) listeners[i + 1]).fireTrainingExampleRecorded(id);
            }
        }
    }

    //Example list and classList must be same length and ordering
    void setSelectedExamplesAndClasses(int[] exampleList, int[] classList) {
        activeInstances = new Instances(dummyInstances, exampleList.length);
        activeInstancesHash = new HashMap<Integer, Instance>();
        for (int i = 0; i < exampleList.length; i++) {
            Instance activeInstance = new Instance(allInstancesHash.get(exampleList[i]));
            activeInstance.setClassValue((double)classList[i]);
            activeInstances.add(activeInstance); //todo: check that class always set properly when adding to activeInstances
            Instance ref = activeInstances.lastInstance();
            activeInstancesHash.put(exampleList[i], ref);
        }
    }

    private void addOscListeners() throws SocketException, UnknownHostException {
        try {
            receiver = new OSCPortIn(receivePort);
        } catch (Exception ex) {
            System.out.println("Could not bind to port " + receivePort + ". Please quit all other instances of Wekinator or change the receive port.");
            //TODO: Throw exception
            return;
        }
        //  System.out.println("Java listening on " + receivePort);
        sender = new OSCPortOut(InetAddress.getLocalHost(), sendPort);
        // System.out.println("Java sending on " + sendPort);


        addOscFeatureListener();

        receiver.startListening();
    }

    private void addOscFeatureListener() {
        OSCListener listener = new OSCListener() {

            public void acceptMessage(java.util.Date time, OSCMessage message) {
               Object[] o = message.getArguments();
                Integer id = new Integer(0);
                if (o[0] instanceof Integer) {
                    id = (Integer)o[0];
                } else {
                    System.out.println("Warning: ID is not an integer");
                }
                double d[] = new double[o.length - 1];
                for (int i = 0; i < o.length-1; i++) {
                    if (o[i+1] instanceof Float) {
                        d[i] = ((Float) o[i+1]).floatValue();
                    } else {
                        System.out.println("Warning: Received feature is not a float");
                    }
                }
                // Use this feature vector!
                if (getRecordingState() == RecordingState.RECORDING) {
                    addTrainingExample(id, d);
                    newTrainingExampleRecorded(id);
                }
                if (getRunningState() == RunningState.RUNNING) {
                    newClassificationResult(id, classifyExample(d));
                }

            }
        };
        receiver.addListener("/oscCustomFeaturesWithId", listener);
    }
}
