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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.EventListenerList;
import weka.classifiers.lazy.IBk;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import wekinator.util.SerializedFileUtil;

/**
 *
 * @author fiebrink
 */
//TODO: Run chuck FE from within here? Or elsewhere?
//TODO: Do we want ability to undo the last delete? or past deletes?
    // This would involve keeping a "trash" list of deleted examples, by ID, with features and order deleted. (and info about whether they were active)
//Assuming that nothing is listening for OSC parameter outputs -- only Kyle's code cares about current classification value
public class BeatboxWekinatorWrapper {

    protected int numFeatures = 0;
    protected int maxNumClasses = 100;
    int k = 1; //# nearest neighbors

    protected Remove instanceFilter; //We take out ID feature in training & executing the classifier
    protected IBk activeClassifier;

    protected Instances allInstances; //the set of all examples, with ID and features; blank class values
    protected HashMap<Integer, Instance> allInstancesHash; //get reference to each Instance in allInstances by ID
    protected Instances activeInstances; //the set of active training examples used in the curret classifier; valid class values
    protected HashMap<Integer, Instance> activeInstancesHash; //get reference to Instance is activeInstances by ID
    protected Instances dummyInstances; //used for header information, to create new compatible Instances objects.

    protected RecordingState recordingState = RecordingState.NOT_RECORDING;
    protected RunningState runningState = RunningState.NOT_RUNNING;
    protected transient EventListenerList classificationListenerList = new EventListenerList(); //listening for new classification result notification
    protected transient EventListenerList trainingListenerList = new EventListenerList(); //listening for new training example notification
    public static String PROP_RECORDINGSTATE = "recordingState";
    public static String PROP_RUNNINGSTATE = "runningState";
    private transient PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);


    public int receivePort = 6448;
    public int sendPort = 6453;
    OSCPortOut sender;
    public OSCPortIn receiver;


    public void disconnectOSC() {
        if (receiver != null) {
            receiver.stopListening();
            receiver.close(); //this line causes errors!!
        }

        if (sender != null) {
            sender.close();
        }
    }
    
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

    private void writeToOutputStream(ObjectOutputStream o) throws IOException {
        o.writeInt(1);
        o.writeObject(activeClassifier);
        o.writeObject(activeInstances);
        o.writeObject(activeInstancesHash);
        o.writeObject(allInstances);
        o.writeObject(allInstancesHash);
        o.writeObject(dummyInstances);
        o.writeObject(instanceFilter);
        o.writeInt(k);
        o.writeInt(maxNumClasses);
        o.writeInt(numFeatures);
        o.writeInt(receivePort);
        o.writeInt(sendPort);
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

    protected BeatboxWekinatorWrapper() {

    }
    
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
        int[] indicesToRemove = new int[1];
        indicesToRemove[0] = 0; //id
        instanceFilter.setAttributeIndicesArray(indicesToRemove);
        instanceFilter.setInputFormat(dummyInstances);

        //Use dummyInstances to create new Instances
        activeInstances = new Instances(dummyInstances);
        activeInstancesHash = new HashMap<Integer, Instance>();

        allInstances = new Instances(dummyInstances);
        allInstancesHash = new HashMap<Integer, Instance>();

        activeClassifier = null; //null means "not trained," "not valid"
    }

    public int getNumFeatures() {
        return numFeatures;
    }

    //public get


    //Don't use this method from outside class unless you're simulating Chuck FE sending this info to Wekinator directly.
    //This is called when OSC listener recevies a new feature vector, and Wekinator is in recording mode
    public void addTrainingExample(int id, double[] features) {
        double[] featuresWithID = new double[2 + features.length];
        featuresWithID[0] = (double) id;
        System.arraycopy(features, 0, featuresWithID, 1, features.length);
        featuresWithID[featuresWithID.length - 1] = 0;

        Instance i = new Instance(1.0, featuresWithID);
        allInstances.add(i);
        Instance ref = allInstances.lastInstance();
        ref.setClassMissing();

        allInstancesHash.put(id, ref);
        newTrainingExampleRecorded(id);
    }

    //Must all when new example is recorded into an "active" training set
    //(example ID should already have been recorded when the new feature vector was received; this call makes it part of active classifier)
    public void addTrainingExampleToActiveClassifier(int exampleId, int classValue) throws Exception {
        System.out.println("1: active null? " + (activeInstancesHash == null));
                System.out.println("2: active empty? " + (activeInstancesHash.isEmpty()));


        if (activeInstancesHash != null) {

            System.out.println("3: Contains key " + exampleId + "?" + (activeInstancesHash.containsKey(exampleId)));
        }
       

        if (activeInstancesHash == null || activeInstancesHash.isEmpty()) {
            

            //This is the first example we're adding
              activeInstances = new Instances(dummyInstances, 10);
              activeInstancesHash = new HashMap<Integer, Instance>();


            Instance activeInstance = new Instance(allInstancesHash.get(exampleId));
            activeInstances.add(activeInstance);
            Instance ref = activeInstances.lastInstance();
            ref.setClassValue(classValue);
            activeInstancesHash.put(exampleId, ref);


              //OLD:
            //  Instance i = new Instance(allInstancesHash.get(exampleId));
            //  i.setClassValue(classValue);
            //  activeInstances.add(i);
             // Instance ref = activeInstances.lastInstance();
              //activeInstancesHash.put(exampleId, ref);

              
              activeClassifier = new IBk();
              k = 1;
              activeClassifier.setKNN(k);
            try {
                activeClassifier.buildClassifier(Filter.useFilter(activeInstances, instanceFilter));
            } catch (Exception ex) {
                System.out.println("Error: Could not build classifier from examples!");
                throw ex;
            }
              
        } else if(!activeInstancesHash.containsKey(exampleId)) {
            if (!allInstancesHash.containsKey(exampleId)) {
                System.out.println("ERROR: example " + exampleId + " is not in allInstancesHash");
                return;
            }
            try {
                Instance i = new Instance(allInstancesHash.get(exampleId));
                activeInstances.add(i);
                Instance ref = activeInstances.lastInstance();
                ref.setClassValue(classValue);

                activeInstancesHash.put(exampleId, ref);

                //kNN specific: (otherwise need to retrain)
                    //ref.eq
                instanceFilter.input(ref);
            Instance n = instanceFilter.output();
                activeClassifier.updateClassifier(n);

              k = (int)Math.sqrt(activeInstances.numInstances());
              activeClassifier.setKNN(k);

            } catch (Exception ex) {
                System.out.println("Error adding training example to active classifier: Perhaps no classifier is active?");
                System.out.println(ex);
                ex.printStackTrace();
                return;
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
            //Note: For now, this should be fine even if classifier is untrained / no data.
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
            throw new IllegalArgumentException("Could not delete example with id=" + id + ": No such example exists.");
        }

        //Wish there were some way to directly do this, but no...
        for (int i = 0; i < allInstances.numInstances(); i++) {
            Instance next = allInstances.instance(i);
            if (target == next) {
                System.out.println("Found instance to delete at index " + i);
                allInstances.delete(i);
                allInstancesHash.remove(id);
                break;
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
                        System.out.println("Found active instance to delete at index " + i);
                        activeInstances.delete(i);
                        activeInstancesHash.remove(id);
                       // activeInstancesHash.
                        //Now retrain/rebuild
                        if (activeInstances.numInstances() > 0) {
                            k = (int)Math.sqrt(activeInstances.numInstances());
                           // activeClassifier.setKNN(activeInstances.numInstances());
                            activeClassifier.setKNN(k);
                            activeClassifier.buildClassifier(Filter.useFilter(activeInstances, instanceFilter));
                            break;
                        } else {
                            activeClassifier = null; //can't build a classifier with no instances!
                        }
                    } catch (Exception ex) {
                        System.out.println("Error encountered in re-building classifier after delete example!");
                        System.out.println(ex);
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
                        System.out.println("Found instance to delete at index " + i);
                        allInstances.delete(i);
                        allInstancesHash.remove(id);
                    }
                }
            } else {
                throw new IllegalArgumentException("Could not delete example with id=" + id + ": No such example exists.");
            }

            //If it's active:
            Instance activeTarget = activeInstancesHash.get(id);
            if (activeTarget != null) {
                //Wish there were some way to directly do this, but no...
                for (int i = 0; i < activeInstances.numInstances(); i++) {
                    Instance next = activeInstances.instance(i);
                    if (activeTarget == next) {
                        System.out.println("Found active instance to delete at index " + i);
                        activeInstances.delete(i);
                        activeInstancesHash.remove(id);
                        activeExampleDeleted = true;
                    }
                }
            }
        }

        if (activeExampleDeleted) {
            //rebuild classifier
            try {
                if (activeInstances.numInstances() > 0) {
                    k = (int)Math.sqrt(activeInstances.numInstances());

                    activeClassifier.setKNN(k);
                    activeClassifier.buildClassifier(Filter.useFilter(activeInstances, instanceFilter));
                } else {
                    activeClassifier = null;
                }
            } catch (Exception ex) {
                System.out.println("Error encountered in re-building classifier after delete examples!");
                System.out.println(ex);
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

    // Returns class value based on how setSelectedExamplesAndClasses was last called, accomodating any add/delete examples in active sets
    // Returns -1 if there is no active classifier (e.g., because there are no training examples)
    //assumes features lacks ID, class; if not the case, we can easily change this
    public int classifyExample(double[] features) {
        if (activeClassifier == null) {
            return -1;
        } else {
            double[] featureVector = new double[features.length + 2];
            featureVector[0] = 0.0; // add dummy ID; will filter out later
            featureVector[featureVector.length - 1] = 0.0;
            System.arraycopy(features, 0, featureVector, 1, features.length);
            instanceFilter.input(new Instance(1.0, featureVector));
            Instance n = instanceFilter.output();

            try {
                return (int) activeClassifier.classifyInstance(n);
            } catch (Exception ex) {
                System.out.println("Error: Could not classify new instance");
                System.out.println(ex);
                return -2; //I can't think of why this would ever happen
            }
        }
    }


    //Classifies example with given id using entire training set (including itself)
    //Returns -1 if no active classifier or id is bad
    public int classifyExampleWithoutHoldout(int id) {
        try {
            if (activeClassifier == null || !allInstancesHash.containsKey(id)) {
                if (activeClassifier == null )
                    System.out.println("Active null");
                else
                    System.out.println("No id " + id);
                return -1;
            }
            Instance i = allInstancesHash.get(id);
            instanceFilter.input(i);
            return  (int)activeClassifier.classifyInstance(instanceFilter.output());
            //return c;
        } catch (Exception ex) {
            System.out.println("Error: Could not classify instance:" + ex.getMessage());
            ex.printStackTrace();
            return -1;
        }
    }

    //Returns new int[0] if no active classifier, or id is not in instances, or some other error happens
    // If id is in training set, it will be one of the nearest neighbors (i.e., you get one correct class for free)
   public int[] getNearestNeighborClassesForInstance(int id) {
       if (activeClassifier == null || !allInstancesHash.containsKey(id)) {
           return new int[0];
       }
       instanceFilter.input(allInstancesHash.get(id));
       Instance i = instanceFilter.output();
        try {
            System.out.println("k is " + k + "/" + activeClassifier.getKNN() + "; numInst=" + activeInstances.numInstances());
            Instances nns = activeClassifier.getNearestNeighbourSearchAlgorithm().kNearestNeighbours(i, k);
            int[] cs = new int[nns.numInstances()];
            for (int j = 0; j < cs.length; j++) {
                cs[j] = (int)nns.instance(j).classValue();
            }
            return cs;
            //activeClassifier.getNearestNeighbourSearchAlgorithm().getDistanceFunction()
        } catch (Exception ex) {
            System.out.println("ERROR: couldn't get nearest neighbors for id=" + id);
            System.out.println(ex);
            ex.printStackTrace();
            return new int[0];
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
       // SerializedFileUtil.writeToFile(f, this);

        FileOutputStream fout = new FileOutputStream(f);
        ObjectOutputStream o = new ObjectOutputStream(fout);
        writeToOutputStream(o);
        o.close();
        fout.close();
    }

    // Kyle: IMPORTANT: Does not save listeners in serialized object!
    // if you load from file, you code will have to re-add itself as a listener (property change, training add, classification result, etc.)
    public static BeatboxWekinatorWrapper loadFromFile(File f) throws Exception {
       // BeatboxWekinatorWrapper w = (BeatboxWekinatorWrapper)SerializedFileUtil.readFromFile(f);
        FileInputStream fin = new FileInputStream(f);
        ObjectInputStream i = new ObjectInputStream(fin);
        BeatboxWekinatorWrapper w = loadFromInputStream(i);
        i.close();
        fin.close();
        return w;
    }

    public static BeatboxWekinatorWrapper loadFromInputStream(ObjectInputStream i) throws IOException, ClassNotFoundException, Exception {
        BeatboxWekinatorWrapper w = new BeatboxWekinatorWrapper();
        i.readInt(); //use for ID if necessary
        w.activeClassifier = (IBk) i.readObject();
        w.activeInstances = (Instances) i.readObject();
        w.activeInstancesHash = (HashMap<Integer, Instance>) i.readObject();
        w.allInstances = (Instances) i.readObject();
        w.allInstancesHash = (HashMap<Integer, Instance>) i.readObject();
        w.dummyInstances = (Instances) i.readObject();
        w.instanceFilter = (Remove) i.readObject();
        w.k = (int)i.readInt();
        w.maxNumClasses = i.readInt();
        w.numFeatures = i.readInt();
        w.receivePort = i.readInt();
        w.sendPort = i.readInt();

       //OSC
        w.addOscListeners();
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

    //Called when a new classification result is obtained, to notify listeners
    //Always called when a new feature vector is received and Wekinator is in RUNNING mode, even if no active classifier is available
    // In that case, returns -1 for classValue
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

    //Called when a new training example is added, to notify listeners
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

    //Build a new classifier from these examples, with these class labels
    //Example list and classList must be same length and ordering
    //Don't call this unnecessarily (e.g., when examples & classes haven't changed, or repeatedly throughout a contiguous sequence of changes)
    void setSelectedExamplesAndClasses(int[] exampleList, int[] classList) throws Exception {
        activeInstances = new Instances(dummyInstances, exampleList.length);
        activeInstancesHash = new HashMap<Integer, Instance>();
        if (exampleList.length == 0) {
            activeClassifier = null;
            return;
        }
        
        for (int i = 0; i < exampleList.length; i++) {
            Instance activeInstance = new Instance(allInstancesHash.get(exampleList[i]));
            activeInstances.add(activeInstance);
            Instance ref = activeInstances.lastInstance();
            ref.setClassValue((double)classList[i]);
            activeInstancesHash.put(exampleList[i], ref);
        }

        activeClassifier = new IBk();
        k = (int)Math.sqrt(exampleList.length);
        activeClassifier.setKNN(k);
        try {
            activeClassifier.buildClassifier(Filter.useFilter(activeInstances, instanceFilter));
        } catch (Exception ex) {
            System.out.println("Error: Could not build classifier from examples!");
            throw ex;
        }

    }

    private void addOscListeners() throws SocketException, UnknownHostException, Exception {
        try {
            receiver = new OSCPortIn(receivePort);
        } catch (Exception ex) {
            System.out.println("Could not bind to port " + receivePort + ". Please quit all other instances of Wekinator or change the receive port.");
            throw ex;
        }
        sender = new OSCPortOut(InetAddress.getLocalHost(), sendPort);
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
                    System.out.println("Warning: feature vector ID is not an integer");
                }
                double d[] = new double[o.length - 1];
                for (int i = 0; i < o.length-1; i++) {
                    if (o[i+1] instanceof Float) {
                        d[i] = ((Float) o[i+1]).floatValue();
                    } else {
                        System.out.println("Warning: Received feature value is not a float");
                    }
                }
                // Use this feature vector!
                if (getRecordingState() == RecordingState.RECORDING) {
                    addTrainingExample(id, d); //calls newTrainingExampleRecorded
                    //newTrainingExampleRecorded(id);
                }
                if (getRunningState() == RunningState.RUNNING) {
                    newClassificationResult(id, classifyExample(d));
                }

            }
        };
        receiver.addListener("/oscCustomFeaturesWithId", listener);
    }
}
