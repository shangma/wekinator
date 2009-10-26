/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wekinator;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import wekinator.util.Observer;
import wekinator.util.Subject;
import weka.classifiers.lazy.IBk;
import weka.core.*;
import weka.classifiers.*;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.trees.DecisionStump;
import weka.classifiers.trees.J48;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.supportVector.PolyKernel;
import weka.classifiers.functions.supportVector.RBFKernel;

/**
 *
 * @author rebecca
 */
public class WekaOperator implements Subject, Observer {

    public boolean isPlayAlong = false;
    private boolean hasReceivedUpdatedClasses = false;
    public boolean enforceFeatureNumAgreement = true;
    public int numFeaturesToExpect = 1;
    int numParametersToTrain = 1;
    Instances[] instances;
    Classifier[] c;
    IBk cibk;
    int numNeighbors = 1;
    int numRounds = 10;
    int numEpochs = 500;
    double learningRate = .03;
    OscHandler handler;
    int currentClass = 0;
    //Bigger1 sc;
    //todo: link this dynamically, so it's swappable
    public String classifierNames[] = {"k-nearest neighbor", "AdaBoost", "NN"};
    private float trainingAccuracy = 0;
    //  private float[] realFeatures = null;
    private boolean useNNGui = false;

    public int getNumNeighbors() {
        return numNeighbors;
    }

    public double computeAccuracy() throws Exception {
        if (numParametersToTrain == 0) {
            return 0;
        }
        double[] results = computeAccuracies();
        double sum = 0;
        for (int i = 0; i < numParametersToTrain; i++) {
            sum += results[i];
        }
        return sum / numParametersToTrain;


    }

    public boolean isFastAccurateValidForClassifierType() {
        switch (myClassifierType) {
            case NONE:
                return false;

            case KNN:
                return false; //not meaningful here
            case ADABOOST:
                return true;
            case NN:
                return true;
            case DTREE:
                return false;
            case SVM:
                return false; //TODO: make something up here?
        }
        return false;
    }

    public void setClassifierFastAccurate(double val) {
        switch (myClassifierType) {
            case NONE:
                return;

            case KNN:
                return;
            case ADABOOST: //TODO: allow setting of these metaparams in GUI
                setNumRounds((int) (val * 490 + 10));
                System.out.println("Using " + getNumRounds() + " rounds");
                return;
            case NN:
                //learning rate varies from .01 to .04?
                setLearningRate(val * .02 + .01);
                setNumEpochs((int) (val * 499 + 1));
                //   ((MultilayerPerceptron) c[i]).setLearningRate(0.03);
                return;
            case DTREE:
                //TODO
                return;
            case SVM:
                return;
        }
    }

    public double[] computeAccuracies() throws Exception {
        double[] results = new double[numParametersToTrain];
        for (int i = 0; i < numParametersToTrain; i++) {

            Evaluation e = new Evaluation(instances[i]);
            e.evaluateModel(c[i], instances[i]);
            results[i] = e.correct() / instances[i].numInstances();
        }
        return results;

    }

    public void writeOut(ObjectOutputStream objout) throws IOException {
        //write all my stuff to a file
        objout.writeInt(numParametersToTrain);
        objout.writeInt(numFeaturesToExpect);
        objout.writeInt(numClasses);

        for (int i = 0; i < c.length; i++) {
            objout.writeObject(c[i]);
            objout.writeObject(instances[i]);

        }


    }

    public void readIn(ObjectInputStream instream) throws IOException, ClassNotFoundException, EOFException {


        setNumParameters(instream.readInt());
        numFeaturesToExpect = instream.readInt();
        numClasses = instream.readInt();
        c = new Classifier[numParametersToTrain];
        instances = new Instances[numParametersToTrain];

        for (int i = 0; i < numParametersToTrain; i++) {
            c[i] = (Classifier) instream.readObject();
            instances[i] = (Instances) instream.readObject();
        }

        initMyselfFromClassifier();
    }

    public int getNumParameters() {
        return numParametersToTrain;
    }

    //TODO: Fix this; no longer setting this # from the GUI, but set 1x in chuck synth
    public void setNumParameters(int num) {
      //  if (num > 0 && num < 10) {
            numParametersToTrain = num;
            // realFeatures = new float[num];
            //    System.out.println("setting new inst 1");
            c = new Classifier[num];
            realVals = new float[num];
            dists = new double[num][0];
            vals = new int[num];
            myFeatureState = FeatureState.WAITING;
            myState = OperatorState.READY;

            if (myClassifierState == ClassifierState.HAS_DATA || myClassifierState == ClassifierState.TRAINED) {
                myClassifierState = ClassifierState.HAS_DATA;

            } else {
               instances = new Instances[num];
            myClassifierState = ClassifierState.NO_DATA;

            }




       // }

    }

    void askHandlerForCurrentValue() throws IOException {
        handler.askForCurrentValue();
    }

    //TODO: check that type is always set correctly
    public ClassifierType getClassifierType() {
        return myClassifierType;

    }

    double[] computeCVAccuracy(int numFolds) throws Exception {
        //Compute CV accuracy on all classifiers
        double[] results = new double[numParametersToTrain];
        for (int i = 0; i < numParametersToTrain; i++) {
            //double[] res = new double[numFolds];
            Random r = new Random();
            Instances randData = new Instances(instances[i]);
            randData.randomize(r);
            randData.stratify(numFolds);
            double sum = 0;
            for (int j = 0; j < numFolds; j++) {
                Evaluation eval = new Evaluation(randData);

                Instances train = randData.trainCV(numFolds, j);
                Instances test = randData.testCV(numFolds, j);
                Classifier clsCopy = Classifier.makeCopy(c[i]);
                clsCopy.buildClassifier(train);
                eval.evaluateModel(clsCopy, test);
                double a = eval.correct();


                int b = test.numInstances();
                sum += a / b;
            }
            results[i] = sum / (double) numFolds;
        }

        return results;

    }

    double computeCVAccuracy(int numFolds, int classifierNum) throws Exception {
        //Compute CV accuracy on oneClassifier
        System.out.println("computing " + numFolds + "folds");
        double results;
        //for (int i = 0; i < numParametersToTrain; i++) {
        //double[] res = new double[numFolds];
        Random r = new Random();
        Instances randData = new Instances(instances[classifierNum]);
        randData.randomize(r);
        randData.stratify(numFolds);
        double sum = 0;
        for (int j = 0; j < numFolds; j++) {
            Evaluation eval = new Evaluation(randData);

            Instances train = randData.trainCV(numFolds, j);
            Instances test = randData.testCV(numFolds, j);
            Classifier clsCopy = Classifier.makeCopy(c[classifierNum]);
            clsCopy.buildClassifier(train);
            eval.evaluateModel(clsCopy, test);
            double a = eval.correct();


            int b = test.numInstances();
            sum += a / b;
        }
        results = sum / (double) numFolds;
        //}

        return results;

    }

    double computeCVAccuracyNN(int numFolds, int classifierNum) throws Exception {
        //Compute CV accuracy on oneClassifier
        System.out.println("computing " + numFolds + "folds");
        double results;
        //for (int i = 0; i < numParametersToTrain; i++) {
        //double[] res = new double[numFolds];
        Random r = new Random();
        Instances randData = new Instances(instances[classifierNum]);
        randData.randomize(r);
        randData.stratify(numFolds);
        double sum = 0;
        for (int j = 0; j < numFolds; j++) {
            Evaluation eval = new Evaluation(randData);

            Instances train = randData.trainCV(numFolds, j);
            Instances test = randData.testCV(numFolds, j);
            Classifier clsCopy = Classifier.makeCopy(c[classifierNum]);
            clsCopy.buildClassifier(train);
            eval.evaluateModel(clsCopy, test);
            // double a = eval.correct();
            double a = eval.errorRate();
            System.out.println("err rate is " + a);


            double b = (double) test.numInstances() / randData.numInstances(); //weight here
            sum += a * b;
        }
        results = sum;
        //}

        return results;

    }

    double[] computeTrainingAccuracy() throws Exception {
        double[] results = new double[numParametersToTrain];
        for (int i = 0; i < numParametersToTrain; i++) {

            //double[] res = new double[numFolds];
            Instances data = new Instances(instances[i]);

            Evaluation eval = new Evaluation(data);

            eval.evaluateModel(c[i], data);

            results[i] = eval.correct() / data.numInstances();
        }
        return results;

    }

    void requestChuckSettings() {
        handler.requestChuckSettings();
    }

    void setNNUseGUI(boolean useGUI) {
        if (myClassifierType == ClassifierType.NN) {
            for (int i = 0; i < c.length; i++) {
                ((MultilayerPerceptron) c[i]).setGUI(useGUI);
            }
        }
        useNNGui = useGUI;
    }

    void setRealVal(float val) {
        realVals[0] = val;
    }

    void setRealVals(float[] f) {

        //  realVals = f;
        realVals = f;
    }

    public void sendCurrentRealVals() {
        if (!useDistribution) {
            handler.sendRealValueMulti(realVals);
        } else {
            double[][] d = new double[numParametersToTrain][numClasses];
            for (int i = 0; i < realVals.length; i++) {
                int v = (int) realVals[i]; //class v should have 100%, others 0%
                for (int j = 0; j < numClasses; j++) {
                    if (v == j) {
                        d[i][j] = 1;
                    } else {
                        d[i][j] = 0;
                    }

                }
            }

            handler.sendDistMulti(d);

        }

    }

    void startRecordFeatures() {
        System.out.println("Start record features!");
        try {
            if (myState == OperatorState.READY && myProblemType == ProblemType.CONTINUOUS) {
                handler.initiateRecord();
                myState = OperatorState.RECORD;

                // sc.giveUpdate("Recording features");
                notifyFeatureObservers();
            } else if (myState == OperatorState.READY) {
                handler.initiateRecord();
                myState = OperatorState.RECORD;
            //   notifyErrorObservers();
            }



        } catch (IOException ex) {
            Logger.getLogger(WekaOperator.class.getName()).log(Level.SEVERE, null, ex);
            myErrorString = "Could not record: " + ex.getMessage();
            notifyErrorObservers();
        }


    }

    void startSound() {
        try {
            if (handler != null) {
                handler.startSound();
            }
        } catch (IOException ex) {
            Logger.getLogger(WekaOperator.class.getName()).log(Level.SEVERE, null, ex);
            myErrorString = "Error in starting sound:" + ex.getMessage();
            notifyErrorObservers();
            notifyOperatorObservers();
        }

    }

    void stopSound() {
        try {
            if (handler != null) {
                handler.stopSound();
            }
        } catch (IOException ex) {
            Logger.getLogger(WekaOperator.class.getName()).log(Level.SEVERE, null, ex);
            myErrorString = "Error in stopping sound:" + ex.getMessage();
            notifyErrorObservers();
            notifyOperatorObservers();
        }

    }

    private double computeNNError(int i) {
        if (c.length > i && c[i] instanceof MultilayerPerceptron) {
            //  return ((MultilayerPerceptron)c[i]).getError(); //TODO: actualy compute error
        }
        return -1.0;
    }

    private void initMyselfFromClassifier() {
        if (c.length > 0) {
            Classifier cc = c[0];
            if (cc instanceof IBk) {
                numNeighbors = ((IBk) cc).getKNN();
                myClassifierType = ClassifierType.KNN;
                myProblemType = ProblemType.DISCRETE;
            } else if (cc instanceof AdaBoostM1) {
                numRounds = ((AdaBoostM1) cc).getNumIterations();
                myClassifierType = ClassifierType.ADABOOST;
                myProblemType = ProblemType.DISCRETE;

            } else if (cc instanceof MultilayerPerceptron) {
                //do something? //TODONN
                myClassifierType = ClassifierType.NN;
                myProblemType = ProblemType.CONTINUOUS;
            } else if (cc instanceof J48) {
                myClassifierType = ClassifierType.DTREE;
                myProblemType = ProblemType.DISCRETE;
            } else if (cc instanceof SMO) {
                myClassifierType = ClassifierType.SVM;
                myProblemType = ProblemType.DISCRETE;

            } else {
                myClassifierType = ClassifierType.NONE;
                throw new UnsupportedOperationException("Not yet implemented");
            }
        } else {
            myClassifierType = ClassifierType.NONE;
            throw new UnsupportedOperationException("Classifier array not yet initialized");
        }
        myState = OperatorState.READY;
        myClassifierState = ClassifierState.TRAINED;
        myFeatureState = FeatureState.OK;
        notifyClassifierStateObservers();
        notifyOperatorObservers();
        notifyClassifierTypeObservers();


    }

    private String myClassifierTypeString() {
        switch (myClassifierType) {
            case NONE:
                return "No classifier chosen";
            case KNN:
                return "k-nearest neighbor with " + numNeighbors + " neighbors";
            case ADABOOST:
                return "AdaBoost.M1 with " + numRounds + " rounds";
            case NN:
                return "Neural net (multilayer perceptron";
            case DTREE:
                return "Decision tree (J48)";
            case SVM:
                return "Support vector machine";
        }
        return "";


    }

    private void notifyClassObservers(int[] vals) {
        gui.displayClassValueMulti(vals);
    }

    private void notifyClassObservers(float[] vals) {
        gui.displayClassValueMulti(vals);
    }

    private void notifyClassObservers(int[] vals, double[][] dists) {
        gui.displayClassValueMulti(vals, dists);
    }

    private void notifyClassObservers(float[] rvals, double[][] dists) {
        gui.displayClassValueMulti(rvals, dists);
    }

    private void setLearningRate(double d) {
        learningRate = d;

        if (c.length > 0 && c[0] instanceof MultilayerPerceptron) {
            for (int j = 0; j < c.length; j++) {
                ((MultilayerPerceptron) c[j]).setLearningRate(learningRate);
            }
        }

    }

    private void setNumEpochs(int i) {
        numEpochs = i;

        if (c.length > 0 && c[0] instanceof MultilayerPerceptron) {
            for (int j = 0; j < c.length; j++) {
                ((MultilayerPerceptron) c[j]).setTrainingTime(numEpochs);
            }
        }
    }

    public void receivedPlayalongUpdate(String s) {
        //   gui.displayPlayalongUpdate(string);
        gui.displayPlayalongUpdate(s);
    // System.out.println("RECEIVED PLAYALONG ");
    }

    public enum ClassifierType {

        KNN, ADABOOST, NN, DTREE, SVM, NONE
    };
    public ClassifierType myClassifierType = ClassifierType.NONE;

    public enum ProblemType {

        DISCRETE, CONTINUOUS
    };
    public ProblemType myProblemType = ProblemType.DISCRETE;

    public void chooseClassifier(ClassifierType ct) {
        if (myClassifierState == ClassifierState.NO_CLASSIFIER || myClassifierState == ClassifierState.NO_DATA) {
            myClassifierState = ClassifierState.NO_DATA;
        } else { //if has data or is already trained: Don't ignore existing training data.
            myClassifierState = ClassifierState.HAS_DATA;
        }

        System.out.println("choosing classifier, #=" + numParametersToTrain);
        c = new Classifier[numParametersToTrain];
        if (ct == ClassifierType.KNN) {
            for (int i = 0; i < c.length; i++) {
                c[i] = new IBk();
            }
            myClassifierType = ClassifierType.KNN;
            myProblemType = ProblemType.DISCRETE;
        } else if (ct == ClassifierType.ADABOOST) {
            for (int i = 0; i < c.length; i++) {
                c[i] = new AdaBoostM1();

                ((AdaBoostM1) c[i]).setClassifier(new DecisionStump());
                ((AdaBoostM1) c[i]).setNumIterations(numRounds);
            }

            myClassifierType = ClassifierType.ADABOOST;
            myProblemType = ProblemType.DISCRETE;

        } else if (ct == ClassifierType.NN) {
            for (int i = 0; i < c.length; i++) {

                c[i] = new MultilayerPerceptron();
                ((MultilayerPerceptron) c[i]).setAutoBuild(true);
                ((MultilayerPerceptron) c[i]).setGUI(useNNGui);
                ((MultilayerPerceptron) c[i]).setLearningRate(learningRate);
                ((MultilayerPerceptron) c[i]).setTrainingTime(numEpochs);

                ((MultilayerPerceptron) c[i]).setHiddenLayers("i"); //for now, # of attributes

            }
            //use defaults for now.
            myClassifierType = ClassifierType.NN;
            myProblemType = ProblemType.CONTINUOUS;


        } else if (ct == ClassifierType.NONE) {
            myClassifierType = ClassifierType.NONE;
            myClassifierState = ClassifierState.NO_CLASSIFIER;
        } else if (ct == ClassifierType.DTREE) {
            for (int i = 0; i < c.length; i++) {

                c[i] = new J48();

            }
            myClassifierType = ClassifierType.DTREE;
            myProblemType = ProblemType.DISCRETE;

        } else if (ct == ClassifierType.SVM) {
            for (int i = 0; i < c.length; i++) {

                c[i] = new SMO();
                // ((SMO) c[i]).setKernel(new PolyKernel());
                //Todo in future: allow different kernel; kernal params
                if (i == 0) {
                    SVMComplexityConstant = ((SMO) c[0]).getC();
                }
            }

            setKernelType(1);
            myClassifierType = ClassifierType.SVM;
            myProblemType = ProblemType.DISCRETE;


        } else {


            myClassifierType = ClassifierType.NONE;
            myErrorString = "NO such thing!";
            myClassifierState = ClassifierState.NO_CLASSIFIER;
            notifyErrorObservers();
        }
        notifyClassifierTypeObservers();
        notifyClassifierStateObservers();
    }

    public void setNumNeighbors(int i) {
        numNeighbors = i;
        if (c.length > 0 && c[0] instanceof IBk) {
            for (int j = 0; j < c.length; j++) {
                ((IBk) c[j]).setKNN(i);
            }
        }
    }

    public void setNumRounds(int i) {
        numRounds = i;

        if (c.length > 0 && c[0] instanceof AdaBoostM1) {
            for (int j = 0; j < c.length; j++) {
                ((AdaBoostM1) c[j]).setNumIterations(numRounds);
            }
        }

    }

    public int getNumRounds() {
        return numRounds;
    }
    double SVMgamma;
    boolean SVMuseLowerOrderTerms;
    double SVMExponent;
    double SVMComplexityConstant;
    // int SVMnormalizeOption;
    int SVMKernelType;

    public int getKernelType() {
        return SVMKernelType;
    }

    public void setKernelType(int k) {
        if (c.length > 0 && c[0] instanceof SMO) {
            if (k == 0) {
                for (int j = 0; j < c.length; j++) {

                    PolyKernel p = new PolyKernel();

                    p.setExponent(1.0);
                    ((SMO) c[j]).setKernel(p);
                    if (j == 0) {
                        SVMuseLowerOrderTerms = p.getUseLowerOrder();
                        SVMExponent = 1.0;
                    }

                }
                SVMKernelType = 0;
            } else if (k == 1) {
                for (int j = 0; j < c.length; j++) {
                    PolyKernel p = new PolyKernel();
                    ((SMO) c[j]).setKernel(p);
                    if (j == 0) {
                        SVMuseLowerOrderTerms = p.getUseLowerOrder();
                        SVMExponent = p.getExponent();
                    }
                }
                SVMKernelType = 1;
            } else if (k == 2) {
                for (int j = 0; j < c.length; j++) {
                    RBFKernel p = new RBFKernel();
                    ((SMO) c[j]).setKernel(p);
                    if (j == 0) {
                        SVMgamma = p.getGamma();
                    }
                }
                SVMKernelType = 2;
            } else {
                System.out.println("ERROR: Invalid kernel specifier");
            }
        }

    }

    public double getGamma() {
        return SVMgamma;
    }

    public void setGamma(double g) {

        if (c.length > 0 && c[0] instanceof SMO && SVMKernelType == 2) {
            SVMgamma = g;
            for (int j = 0; j < c.length; j++) {
                ((RBFKernel) ((SMO) c[j]).getKernel()).setGamma(SVMgamma);
            }
        }
    }

    public boolean getUseLowerOrderTerms() {
        return SVMuseLowerOrderTerms;
    }

    public void setUseLowerOrderTerms(boolean u) {
        if (c.length > 0 && c[0] instanceof SMO && (SVMKernelType == 0 || SVMKernelType == 1)) {
            SVMuseLowerOrderTerms = u;

            for (int j = 0; j < c.length; j++) {
                ((PolyKernel) ((SMO) c[j]).getKernel()).setUseLowerOrder(u);
            }
        } else {
            System.out.println("ERROR: Invalid smo lower term request");
        }
    }

    public double getComplexityConstant() {
        return SVMComplexityConstant;
    }

    public void setComplexityConstant(double cc) {
        if (c.length > 0 && c[0] instanceof SMO) {
            SVMComplexityConstant = cc;

            for (int j = 0; j < c.length; j++) {
                ((SMO) c[j]).setC(cc);
            }
        } else {
            System.out.println("ERROR: Invalid smo lower term request");
        }
    }

    public double getExponent() {
        return SVMExponent;
    }

    public void setExponent(double ee) {
        if (c.length > 0 && c[0] instanceof SMO && SVMKernelType == 1) {
            SVMExponent = ee;

            for (int j = 0; j < c.length; j++) {
                ((PolyKernel) ((SMO) c[j]).getKernel()).setExponent(ee);
            }
        } else {
            System.out.println("ERROR: Invalid smo exponent");
        }
    }

    enum ClassifierState {

        NO_CLASSIFIER, NO_DATA, HAS_DATA, TRAINED
    };

    enum FeatureState {

        WAITING, OK
    };

    enum OperatorState {

        BEGIN, WAITING_FOR_HANDSHAKE, READY, RECORD, RUN, END, FAIL
    };
    OperatorState myState = OperatorState.BEGIN;
    FeatureState myFeatureState = FeatureState.WAITING;
    ClassifierState myClassifierState = ClassifierState.NO_CLASSIFIER;
    private ArrayList<Observer> observers;
    private String myErrorString = "";

    public String myClassifierUpdateString() {
        switch (myClassifierState) {
            case NO_CLASSIFIER:
                return "No classifier chosen";
            case NO_DATA:
                return "Classifier needs some data";
            case HAS_DATA:
                return "Classifier has data, ready to train";
            case TRAINED:
                return "Classifier trained, ready to use.";

        }
        return "";
    }
    int numClasses = 2;

    public int getNumClasses() {
        return numClasses;

    }

    public void setNumClasses(int i) {
        numClasses = i;
    }

    public String myFeatureUpdateString() {
        switch (myFeatureState) {
            case WAITING:
                return "Waiting for feature info from chuck";
            case OK:
                return "Received feature info from chuck. Ready.";
        }
        return "";
    }

    public String myUpdateString() {

        switch (myState) {
            case BEGIN:
                return "Waiting for OSC connection set up";
            case WAITING_FOR_HANDSHAKE:
                return "Waiting to hear from ChucK";
            case READY:
                return "Ready to go";
            case RECORD:
                return "Recording features";
            case RUN:
                return "Running";
            case END:
                return "Done";
            case FAIL:
                return "Failed";
        }
        return "";
    }

    public WekaOperator() {
        myState = OperatorState.BEGIN;
        observers = new ArrayList<Observer>();
    }

    public void begin(int portSend, int portReceive) throws IOException {
        if (myState == OperatorState.BEGIN) {
            myState = OperatorState.WAITING_FOR_HANDSHAKE;
            try {
                handler = new OscHandler(this, portReceive, portSend);
                handler.addObserver(this);
                //    sc.giveUpdate("Initialized OSC send and receive.");
                handler.startHandshake();
            } catch (IOException ex) {
                myState = OperatorState.FAIL;
                notifyOperatorObservers();
                throw ex;
            }

        } else {
            // sc.giveUpdate("Could not initialize: state is " + myState);
        }
    }

    public void receivedHandshake() {
        myState = OperatorState.READY;
        System.out.println("GOT IT");
        notifyOperatorObservers();
    //   myStatusMessage = "Connected";
    // sc.giveUpdate("Received handshake. Go!");

    //   initializeClassifier();

    }

    public OscHandler Handler() {
        return handler;
    }

    //disconnects, but does not quit. Use if changing ports.
    public void disconnectOSC() {
        if (myState == OperatorState.RUN || myState == OperatorState.RECORD) {
            try {
                //Tell chuck to stop extracting features
                handler.stopTrainTest();
            } catch (IOException ex) {
                Logger.getLogger(WekaOperator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (myState != OperatorState.BEGIN && myState != OperatorState.END) {
            //Shut down listener, free up ports
            handler.end();
        }

        myState = OperatorState.BEGIN;
        // sc.giveUpdate("I've ended.");
        notifyOperatorObservers();
    }

    public void quit() {
        if (myState == OperatorState.RUN || myState == OperatorState.RECORD) {
            try {
                //Tell chuck to stop extracting features
                handler.stopTrainTest();
            } catch (IOException ex) {
                Logger.getLogger(WekaOperator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (myState != OperatorState.BEGIN && myState != OperatorState.END) {
            //Shut down listener, free up ports
            handler.end();
        }

        myState = OperatorState.END;
        // sc.giveUpdate("I've ended.");
        notifyOperatorObservers();
    }

    public void startRecordFeatures(int i) {
        //todo: check state
        try {
            if (myState == OperatorState.READY) {
                handler.initiateRecord();
                myState =
                        OperatorState.RECORD;
                currentClass =
                        i;
                // sc.giveUpdate("Recording features");
                notifyFeatureObservers();
            } else {
                myErrorString = "Could not record: Stat is " + myState;
                notifyErrorObservers();
            }



        } catch (IOException ex) {
            Logger.getLogger(WekaOperator.class.getName()).log(Level.SEVERE, null, ex);
            myErrorString = "Could not record: " + ex.getMessage();
            notifyErrorObservers();
        }

    //now when features come in I should record them with the appropriate class!
    }

    public void train() {
        try {
            //train using recorded features
         /*   if (instances[0] == null) {
            System.out.println("inst0 is null");
            } */
            if (instances != null && instances[0] != null && instances.length > 0 && instances[0].numInstances() > 0) {
                for (int i = 0; i < c.length; i++) {
                    c[i].buildClassifier(instances[i]);
                }
                System.out.println("trained " + c.length);

                //  cibk.buildClassifier(instances);
                //  sc.giveUpdate("Trained classifier");
                myClassifierState =
                        ClassifierState.TRAINED;
                notifyClassifierStateObservers();
            } else {
                myErrorString = "Could not train: no instances recorded!";
                notifyErrorObservers();
            }



        } catch (Exception ex) {
            Logger.getLogger(WekaOperator.class.getName()).log(Level.SEVERE, null, ex);
            myErrorString = "Could not train:" + ex.getMessage();
            notifyErrorObservers();
        }

    }
    int counter = 0;

    public void receivedFeatureInfo(int i) {
        //   initializeInstances(currentClass, currentClass);
        //   System.out.println("YO");
        if (myFeatureState != FeatureState.OK) {
            System.out.println("i is " + i);
            initializeInstances(i, numClasses);
            myFeatureState =
                    FeatureState.OK;
            notifyFeatureObservers();

        }
//ignore feature info if we already saw it
//alternatively, could double check to make sure it didn't change

    }

    public void receivedNumParams(Object[] o) {
        if (o.length >= 1) {
            System.out.println("received # params");

            int n = (Integer) o[0];
            setNumParameters(n);
            gui.setNumParams(n);
        }

    }
    private boolean useDistribution = false;
    private boolean isChuckDiscrete = false;

    public void setUseDistribution(boolean d) {
        useDistribution = d;
    }

    public void setIsChuckDiscrete(boolean d) {
        isChuckDiscrete = d;
    }

    //Should set numParams, useDistribution, isDiscrete, numClasses
    public void receivedChuckSettings(Object[] o) {
        //DISASTER
        System.out.println("in receive ch set");
        if (o.length == 4) {
            System.out.println("yes");
            int nParam = (Integer) o[0];
            setNumParameters(nParam);

            int n = (Integer) o[1];
            boolean useD = (n == 1) ? true : false;
            setUseDistribution(useD);

            n = (Integer) o[2];
            boolean isDiscrete = (n == 1) ? true : false;

            setIsChuckDiscrete(isDiscrete);

            int nc = (Integer) o[3];
            setNumClasses(nc);

            gui.setChuckSettings(nParam, useD, isDiscrete, nc);

        } else {
            System.out.println("Received wrong number of params");
        }


    }


    //todo: fix f to be generic
    public void receivedFeature(Object[] o) {
        //   System.out.println("received feature");
        // sc.giveUpdate("received feature");
        if (myFeatureState == FeatureState.OK && (o.length == numFeaturesToExpect || !enforceFeatureNumAgreement)) {

            if (myState == OperatorState.RECORD) {
                if (!isPlayAlong || hasReceivedUpdatedClasses) {
                    if (myProblemType == ProblemType.DISCRETE) {
                        //   addInstance(o, currentClass);
                        addInstanceDiscrete(o);
                    } else {
                        addInstance(o);
                    }
                    if (myClassifierState != ClassifierState.TRAINED) { //If we've added more data since last train, still want to be able to run!  
                        myClassifierState = ClassifierState.HAS_DATA;
                    }
                    notifyClassifierStateObservers();
                }

            } else if (myState == OperatorState.RUN) {
                classify(o);
                if (myProblemType == ProblemType.DISCRETE) {
                    if (useDistribution) {
                        handler.sendDistMulti(dists);
                        notifyClassObservers(vals, dists);
                    } else {
                        handler.sendClassMulti(vals);
                        notifyClassObservers(vals);
                    }
                } else {
                    notifyClassObservers(realVals, dists);
                    handler.sendRealValueMulti(realVals);
                }
            }
// 

        }
    }

    public void receivedRealValue(Object[] o) {
        if (o.length > 0 && o[0] instanceof Float && realVals.length > 0) {
            //   realVals[0] = Float.valueOf((Float) o[0]);
            //   gui.displayReceivedRealValue(realVals[0]);
            // System.out.println("in received real value");
            float[] f = new float[o.length];
            for (int i = 0; i < o.length; i++) {
                f[i] = ((Float) o[i]).floatValue();
            }
            //  System.out.println("real val is " + f[0]);

            this.setRealVals(f);
            hasReceivedUpdatedClasses = true;

            gui.displayClassValueMulti(f);

        } else {
            throw new Error("Not a float or feats not initialized");
        }

    }

    public void startRun() {
        try {
            if (myState == OperatorState.READY && myClassifierState == ClassifierState.TRAINED) {
                handler.initiateClassify();
                handler.startSound();
                myState =
                        OperatorState.RUN;
                notifyOperatorObservers();
            } else {
                myErrorString = "Could not run; state is " + myState + "," + myClassifierState;
                notifyErrorObservers();
            }



        } catch (IOException ex) {
            Logger.getLogger(WekaOperator.class.getName()).log(Level.SEVERE, null, ex);
            myErrorString = "Could not run: " + ex.getMessage();
            notifyErrorObservers();
        }

    }

    public void stopRecording() {
        try {
            System.out.println("stopping recording");
            if (handler != null) {
                handler.stopTrainTest();
                // handler.stopSound();
                myState = OperatorState.READY;
                notifyOperatorObservers();
            }
        } catch (IOException ex) {
            Logger.getLogger(WekaOperator.class.getName()).log(Level.SEVERE, null, ex);
            myErrorString = "Error in stopping features:" + ex.getMessage();
            notifyErrorObservers();
            myState = OperatorState.FAIL;
            notifyOperatorObservers();
        }
    }

    public void clear() {
        try {
            System.out.println("Clearing");
            //Forget everything!
            //TODO: check if train/test first??
            handler.stopTrainTest();
            myFeatureState =
                    FeatureState.WAITING;
            myClassifierState =
                    ClassifierState.NO_DATA;
            myState =
                    OperatorState.READY;
            if (instances != null) {
                for (int i = 0; i < instances.length; i++) {

                    //This causes null pointer...
                    if (instances[i] != null) {
                        instances[i].delete();
                    }
                }
            }
            notifyFeatureObservers();
            notifyClassifierStateObservers();
            notifyOperatorObservers();
            gui.displayNumInstances(0);
        //   c.
        //  sc.giveUpdate("Training set cleared.");

        } catch (IOException ex) {
            Logger.getLogger(WekaOperator.class.getName()).log(Level.SEVERE, null, ex);
            myErrorString = "Error in clearing:" + ex.getMessage();
            notifyErrorObservers();
            myState = OperatorState.FAIL;
            notifyOperatorObservers();
        }

    }

    /*public void initializeClassifier() {
    // cibk = new IBk(1);
    if (myClassifierType == ClassifierType.KNN) {
    c = new IBk(numNeighbors);
    } else if (myClassifierType == ClassifierType.ADABOOST) {
    c = new AdaBoostM1();
    } else {
    throw new Error("Unexpected initialization");
    }
    
    }*/
    /* Problem: Does this happen every time I get a feature info? */
    public void initializeInstances(int numFeatures, int numClasses) {
        System.out.println("In initialize instances");
        if (numFeatures != numFeaturesToExpect && enforceFeatureNumAgreement) {
            System.out.println("Error: Expecting " + numFeaturesToExpect + "features, seeing " + numFeatures);
        }

        System.out.println(numFeatures + " features initializing");

        if (myProblemType == ProblemType.DISCRETE) {
            FastVector classes = new FastVector(numClasses);
            for (int i = 0; i <
                    numClasses; i++) {
                classes.addElement((new Integer(i)).toString());
            }

            FastVector ff = new FastVector(numFeatures + 1);
            for (int i = 0; i <
                    numFeatures; i++) {
                ff.addElement(new Attribute(Integer.toString(i)));
            }

            ff.addElement(new Attribute("class", classes));

            //initialize
            System.out.println("setting new inst 2");
            instances = new Instances[numParametersToTrain];
            for (int i = 0; i < numParametersToTrain; i++) {

                instances[i] =
                        new Instances("myname", (FastVector) ff.copy(), 10);
                instances[i].setClassIndex(numFeatures);
            }
        } else {
            FastVector ff = new FastVector(numFeatures + 1);
            for (int i = 0; i <
                    numFeatures; i++) {
                ff.addElement(new Attribute(Integer.toString(i)));
            }

            ff.addElement(new Attribute("class", numFeatures));
            System.out.println("setting new inst ");
            instances = new Instances[numParametersToTrain];
            for (int i = 0; i < numParametersToTrain; i++) {
                System.out.println("creating i=" + i);

                System.out.println("init new instances");
                instances[i] = new Instances("myname", (FastVector) ff.copy(), 10);
                instances[i].setClassIndex(numFeatures);
                //System.out.println("ii null?" + (ii==null));
                System.out.println("inst[0] null?" + instances[0] == null);

            }

        }
    }

    public void addInstance(Object[] o, int classval) {
        if (instances.length > 0 && o.length == instances[0].numAttributes() - 1) {
            double[] d = new double[o.length + 1];
            for (int i = 0; i <
                    o.length; i++) {
                d[i] = (double) ((Float) o[i]).floatValue();
            //   System.out.println("next is " + d[i]);
            }

            d[o.length] = classval;
            Instance instance = new Instance(1.0, d);

            for (int i = 0; i < instances.length; i++) {
                instances[i].add((Instance) instance.copy());
            }
        }

    }

    // Add an instance with current *real* value (for continuous problems)
    private void addInstance(Object[] o) {
        //HACK!
        if (instances.length > 0 && instances[0] != null && o.length == instances[0].numAttributes() - 1) {
            double[] d = new double[o.length + 1];
            for (int i = 0; i < o.length; i++) {
                d[i] = (double) ((Float) o[i]).floatValue();
            // System.out.println("next is " + d[i]);
            }
            for (int i = 0; i < instances.length; i++) {
                Instances ii = instances[i];
                d[o.length] = realVals[i]; //use current value of real param

                Instance instance = new Instance(1.0, (double[]) d.clone());
                ii.add(instance);
                gui.displayNumInstances(ii.numInstances());
            }
        }
    }

    private void addInstanceDiscrete(Object[] o) {
        //HACK!
        if (instances.length > 0 && instances[0] != null && o.length == instances[0].numAttributes() - 1) {
            double[] d = new double[o.length + 1];
            for (int i = 0; i < o.length; i++) {
                d[i] = (double) ((Float) o[i]).floatValue();
            // System.out.println("next is " + d[i]);
            }
            for (int i = 0; i < instances.length; i++) {
                Instances ii = instances[i];
                d[o.length] = (int) realVals[i]; //use current value of real param
                //  System.out.println("real val is " + realVals[i]);
                Instance instance = new Instance(1.0, (double[]) d.clone());
                ii.add(instance);
                gui.displayNumInstances(ii.numInstances());
            }
        }
    }
    private int[] vals = new int[numParametersToTrain];
    private float[] realVals = new float[numParametersToTrain];
    private double dists[][] = new double[numParametersToTrain][0];

    public void classify(Object[] o) {

        if (myClassifierState == ClassifierState.TRAINED && (instances.length > 0 &&
                o.length == instances[0].numAttributes() - 1)) {
            double[] d = new double[o.length + 1];
            for (int i = 0; i < o.length; i++) {
                d[i] = (double) ((Float) o[i]).floatValue();
            }

            d[o.length] = 0.0;
            Instance instance = new Instance(1.0, d);
            for (int i = 0; i < numParametersToTrain; i++) {
                Instance in = (Instance) instance.copy();
                in.setDataset(instances[i]);
                try {

                    //  System.out.println("clength is " + c.length+ "c0 null? " + (c[0]==null) + "c1 null?" + (c[1]==null) + realVals.length);
                    realVals[i] = (float) c[i].classifyInstance(in);
                    vals[i] = (int) realVals[i];
                    dists[i] = c[i].distributionForInstance(in);
                //    sc.giveUpdate("Classified as " + val);
                //  return val;
                } catch (Exception ex) {
                    Logger.getLogger(WekaOperator.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println(ex.getMessage());
                    myErrorString = "Couldn't classify:" + ex.getMessage();
                // return 0;
                }
            }
        } else {
            myErrorString = "Can't classify; no classifier trained, or wrong # features";
            notifyErrorObservers();
        //  return 0;
        }

    }

    private void notifyObservers(Subject s, Object state, String string) {
        Iterator<Observer> i = observers.iterator();
        while (i.hasNext()) {
            Observer o = i.next();
            o.update(s, state, string);
        }
    }

    private void notifyOperatorObservers() {
        // loop through and notify each observer
        Iterator<Observer> i = observers.iterator();
        while (i.hasNext()) {
            Observer o = i.next();
            o.update(this, myState, myUpdateString());
        }
    }

    private void notifyClassifierTypeObservers() {
        Iterator<Observer> i = observers.iterator();
        while (i.hasNext()) {
            Observer o = i.next();
            o.update(this, myClassifierType, myClassifierTypeString());
        }
    }

    private void notifyClassifierStateObservers() {
        // loop through and notify each observer
        Iterator<Observer> i = observers.iterator();
        while (i.hasNext()) {
            Observer o = i.next();
            o.update(this, myClassifierState, myClassifierUpdateString());
        }
    }

    private void notifyFeatureObservers() {
        // loop through and notify each observer
        Iterator<Observer> i = observers.iterator();
        while (i.hasNext()) {
            Observer o = i.next();
            o.update(this, myFeatureState, myFeatureUpdateString());
        }
    }

    private void notifyErrorObservers() {
        Iterator<Observer> i = observers.iterator();
        while (i.hasNext()) {
            Observer o = i.next();
            o.update(this, null, myErrorString);
        }

    }
    //total hack
    MainGUI gui;

    public void setGui(MainGUI b) {
        gui = b;
    }

    //hack!
    private void notifyClassObservers(int val, double[] dist) {
        gui.displayClassValue(val, dist);
    }

    private void notifyClassObservers(float rv, double[] dist) {
        gui.displayClassValue(rv);

    }

    private void notifyInstanceObservers(int classnum, int number) {
        gui.displayClassNumbers(classnum, number);
    }

    public void addObserver(Observer o) {
        observers.add(o);
    }

    public void removeObserver(Observer o) {
        observers.remove(o);
    }

    public void update(Subject o, Object state, String updateString) {
        if (o == handler) {
            notifyObservers(handler, handler.state, handler.statusString());
        }

    }

    public void playScore() {
        try {
            System.out.println("starting playback");
            if (handler != null) {
                handler.playScore();
            // myState = OperatorState.READY;
            // notifyOperatorObservers();
            }
        } catch (IOException ex) {
            Logger.getLogger(WekaOperator.class.getName()).log(Level.SEVERE, null, ex);
            myErrorString = "Error in starting playback:" + ex.getMessage();
            notifyErrorObservers();
            myState = OperatorState.FAIL;
            notifyOperatorObservers();
        }
    }

    public void stopPlaying() {
        try {
            System.out.println("stopping playback");
            if (handler != null) {
                handler.stopPlayback();
            // myState = OperatorState.READY;
            // notifyOperatorObservers();
            }
        } catch (IOException ex) {
            Logger.getLogger(WekaOperator.class.getName()).log(Level.SEVERE, null, ex);
            myErrorString = "Error in stopping playback:" + ex.getMessage();
            notifyErrorObservers();
            myState = OperatorState.FAIL;
            notifyOperatorObservers();
        }
    }


    public void startPlayAlong() {
        try {
            System.out.println("stopping playback");
            if (handler != null) {
                handler.startSound();
                handler.startGettingParams();
                hasReceivedUpdatedClasses = false;
                startRecordFeatures();
            }
        } catch (IOException ex) {
            Logger.getLogger(WekaOperator.class.getName()).log(Level.SEVERE, null, ex);
            myErrorString = "Error in stopping playback:" + ex.getMessage();
            notifyErrorObservers();
            myState = OperatorState.FAIL;
            notifyOperatorObservers();
        }
    }

    public void stopPlayAlong() {


        try {
            System.out.println("stopping playback");
            if (handler != null) {
                handler.stopGettingParams();                   // myState = OperatorState.READY;
                //Stop getting features!!
                //  handler.stopTrainTest(); //I hope this works?
                this.stopRecording();
            // notifyOperatorObservers();
            }
            hasReceivedUpdatedClasses = false;
        } catch (IOException ex) {
            Logger.getLogger(WekaOperator.class.getName()).log(Level.SEVERE, null, ex);
            myErrorString = "Error in stopping playback:" + ex.getMessage();
            notifyErrorObservers();
            myState = OperatorState.FAIL;
            notifyOperatorObservers();
        }
    }
}
