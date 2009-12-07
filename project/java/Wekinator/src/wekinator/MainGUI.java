/*
 * Bigger1.java
 *
 * Created on December 2, 2008, 11:10 AM
 */
//Update.
package wekinator;

import java.awt.Dimension;
import java.awt.KeyEventPostProcessor;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import wekinator.util.Observer;
import wekinator.util.Subject;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.jdesktop.swingworker.*;
import javax.swing.event.ChangeEvent;
import wekinator.ChuckRunner.ChuckRunnerState;
import wekinator.util.OverwritePromptingFileChooser;
import wekinator.util.Util;

/**
 *
 * @author  rebecca
 */
public class MainGUI extends javax.swing.JFrame implements Observer {

    public int numRounds = 0;
    int numFolds = 10;
    double autoTrainStopThreshold = .95;
    boolean isAutoTrainStopThreshold = false;
    int fastAccurateValue = 50;
    WekinatorInstance wek = WekinatorInstance.getWekinatorInstance();

    void displayPlayalongUpdate(String string) {
        labelPlayalongUpdate.setText(string);
    }

    private void setFeatureConfigurationPanelEnabled(boolean enabled) {
        panelMainTabs.setEnabledAt(panelMainTabs.indexOfComponent(panelTabFeatureConfiguration), enabled);
    }

    private void setLearningSystemConfigurationPanelEnabled(boolean enabled) {
        panelMainTabs.setEnabledAt(panelMainTabs.indexOfComponent(panelTabLearningSystemConfiguration), enabled);
    }

    class MyWorker extends SwingWorker<ArrayList<Double>, Void> {

        @Override
        protected ArrayList<Double> doInBackground() throws Exception {
            assert (w.dataset != null);
            if (w.dataset.getNumDatapoints() > 0) {

                setProgress(1);


                w.train();
                //  Thread.sleep(2000);
                // double d[] = w.computeCVAccuracy(10); //TODO: change # folds here
                ArrayList<Double> list = new ArrayList<Double>(w.numParametersToTrain);
                for (int i = 0; i < w.numParametersToTrain; i++) {
                    if (w.myClassifierType != WekaOperator.ClassifierType.NN) {
                        //Do cross-validation
                        setProgress(2 + i);
                        double d = w.computeCVAccuracy(numFolds, i);
                        list.add(new Double(d));
                    } else {
                        //Do cross-validation
                        setProgress(2 + i);
                        double d = w.computeCVAccuracyNN(numFolds, i);
                        list.add(new Double(d));
                    }
                }
                setProgress(w.numParametersToTrain + 2);
                numRounds++;
                return list;
            } else {
                setProgress(0);
                System.out.println("Nothing to do: no instances yet");
                return new ArrayList<Double>();
            }
        }

        @Override
        protected void done() {
            try {
                // System.out.println("Results of train CV are: ");
                String s = "Training round " + Integer.toString(numRounds) + ": CV ";
                //TODOTODOTODO
                if (w.myClassifierType != WekaOperator.ClassifierType.NN) {
                    s += " accuracy ";
                } else {
                    s += " RMS ";
                }

                ArrayList<Double> list = get();
                DecimalFormat dd = new DecimalFormat("#.##");
                Double sum = new Double(0.0);
                for (Double d : list) {
                    // System.out.println("*** " + d);
                    s += " * " + dd.format(d);
                    sum += d;
                }


                //If there is a queue waiting, do more training.
                if (isAutoTrainStopThreshold) {
                    //compute average
                    double avg = sum / list.size();
                    if ((w.myClassifierType != WekaOperator.ClassifierType.NN && avg >= autoTrainStopThreshold) || (w.myClassifierType == WekaOperator.ClassifierType.NN && avg <= autoTrainStopThreshold)) {
                        s += " AUTO-STOPPED";
                        //trainQueue.clear(); //clear training queue
                        jCheckBoxAutomaticTraining.getModel().setSelected(false);
                        timer.cancel();
                        timer = new Timer();
                        trainQueue.clear(); //clear training queue
                        jToggleButtonPlayScore.doClick();
                        jToggleButtonRunPlayalong.doClick();
                    }
                }
                jPlayAlongCVLabel.setText(s);

                //TODO: test this!
                if (trainQueue.size() > 0) {
                    System.out.println("Removing worker from queue");
                    MyWorker w = trainQueue.removeFirst();
                    doWorker(w);
                } else {
                    System.out.println("All workers done");
                    currentWorker = null;
                }

            } catch (Exception ignore) {
            }
        }
    }
    int trainingFrequency = 50; //TODO: bind to slider
    boolean isConnected = false;
    private FeatureManager fm;
    LinkedList<MyWorker> trainQueue = new LinkedList<MyWorker>();
    MyWorker currentWorker = null;
    Timer timer = new Timer();
    boolean isAutoTrain = false;
    PropertyChangeListener hidSetupChangeListener = new PropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent evt) {
            hidSetupPropertyChange(evt);
        }
    };
    PlayalongScore score = null;
    PlayalongScoreViewer scoreViewer = null;
    boolean useMyScorePlayer = true;

    /** Creates new form Bigger1 */
    public MainGUI() {
        initComponents();
        //Anywhere we add a listener, also update to current property.


        fm = wek.getFeatureManager();
        ChuckSystem.getChuckSystem().addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                chuckSystemPropertyChange(evt);
            }
        });
        updateGUIforChuckSystem();

        Logger.getLogger(MainGUI.class.getName()).log(Level.INFO, "Here's some info");

        w = new WekaOperator();
        w.addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                wekaOperatorPropertyChange(evt);
            }
        });

        OscHandler.getOscHandler().addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                oscHandlerPropertyChange(evt);
            }
        });
        updateGUIforOscStatus();

        w.addObserver(this);
        w.setGui(this);
        fm.hidSetup = wek.getCurrentHidSetup(); //TODO: put in fm
        wek.getCurrentHidSetup().addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                hidSetupPropertyChange(evt);
            }
        });
        wek.addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                wekinatorInstancePropertyChangeEvent(evt);
            }
        });

        chuckRunnerPanel1.setRunner(WekinatorInstance.getWekinatorInstance().getRunner());
        //  buttonClearSettings.setVisible(false);
        radioClearProcessingFeature.setVisible(false);
        int n = w.getNumClasses();

        panelMainTabs.setEnabledAt(panelMainTabs.indexOfComponent(panelRun), false);
        panelMainTabs.setEnabledAt(panelMainTabs.indexOfComponent(panelFeatures), false);
        panelMainTabs.setEnabledAt(panelMainTabs.indexOfComponent(panelClassifier), false);
        panelMainTabs.setEnabledAt(panelMainTabs.indexOfComponent(panelPlayAlong), false);

        // buttonPanic.setVisible(false);
        // buttonLoadFeatureSettings.setVisible(false);
        // buttonSaveFeatureSettings.setVisible(false);


        KeyEventPostProcessor processor = new KeyEventPostProcessor() {

            public boolean postProcessKeyEvent(KeyEvent e) {
                if (e.getID() == KeyEvent.KEY_PRESSED) {
                    if (e.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
                        System.out.println("Page down pressed");
                        if (panelMainTabs.getSelectedComponent() == panelRun) {
                            buttonHoldTrain.getModel().setSelected(true);
                            buttonHoldTrain.getModel().setPressed(true);
                        } else if (panelMainTabs.getSelectedComponent() == panelPlayAlong) {
                            jToggleButtonPlayAlong.getModel().setSelected(true);
                            jToggleButtonPlayAlong.getModel().setPressed(true);
                        }

                    } else if (e.getKeyCode() == KeyEvent.VK_PAGE_UP) {
                        System.out.println("Page up presed");
                        if (panelMainTabs.getSelectedComponent() == panelPlayAlong) {
                            jToggleButtonPlayScore.getModel().setSelected(!jToggleButtonPlayScore.getModel().isSelected());
                        }

                    }
                } else if (e.getID() == KeyEvent.KEY_RELEASED) {
                    if (e.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
                        System.out.println("Page down released");
                        if (panelMainTabs.getSelectedComponent() == panelRun) {
                            buttonHoldTrain.getModel().setPressed(false);
                            buttonHoldTrain.getModel().setSelected(false);
                        } else if (panelMainTabs.getSelectedComponent() == panelPlayAlong) {
                            jToggleButtonPlayAlong.getModel().setSelected(false);
                            jToggleButtonPlayAlong.getModel().setPressed(false);
                        }
                    }
                }
                return true;
            }
        };
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventPostProcessor(processor);

        wek.runner.addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                runnerPropertyChange(evt);
            }
        });

    }

    private void runnerPropertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(ChuckRunner.PROP_RUNNERSTATE)) {
            updateRunnerState(wek.runner.getRunnerState());
        }

    }

    private void updateRunnerState(ChuckRunner.ChuckRunnerState state) {
        if (state == ChuckRunnerState.RUNNING) {
            //This configuration works: Save it.
            wek.useConfigurationNextSession();
            updateFeaturesForConfiguration();

        } else {
            //Show chuck disconnected
            w.disconnectOSC();
        }

    }

    public void displayClassNumbers(int classnum, int number) {

        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void updateFeaturesForConfiguration() {
        checkCustomChuck.setSelected(wek.configuration.isCustomChuckFeatureExtractorEnabled());
        if (wek.configuration.isCustomChuckFeatureExtractorEnabled()) {
            textNumCustomChuck.setText(Integer.toString(wek.configuration.getNumCustomChuckFeaturesExtracted()));
        }

        checkCustomOsc.setSelected(wek.configuration.isOscFeatureExtractorEnabled());
        if (wek.configuration.isOscFeatureExtractorEnabled()) {
            textNumOsc.setText(Integer.toString(wek.configuration.getNumOSCFeaturesExtracted()));
        }
    }

    public void displayClassValue(int val, double[] dist) {
        System.out.println("Class is " + val + "; P=");
        String s = Integer.toString(val) + ", P=";
        DecimalFormat dd = new DecimalFormat("#.##");
        for (int i = 0; i < dist.length; i++) {
            s += Integer.toString(i) + ":" + dd.format(dist[i]) + "/";
        }
        labelClassifiedClass.setText(s);
        labelClassifiedClass1.setText(s);
    }

    public void displayClassValue(float rv) {
        DecimalFormat dd = new DecimalFormat("#.##");
        System.out.println("Class is " + dd.format(rv));
        String s = dd.format(rv);
        labelClassifiedClass.setText(s);
        labelClassifiedClass1.setText(s);
    }

    public void displayClassValueMulti(float[] vals) {
        String s = "";
        DecimalFormat dd = new DecimalFormat("#.##");

        for (int i = 0; i < vals.length; i++) {
            //  System.out.println("for i = " + i);
            //  System.out.println("Class is " + vals[i]);

            //Update reg training panel:

            String ss = dd.format(vals[i]);
            s += ss;
            paramFields[i].setText(ss);

            if (i < vals.length - 1) {
                s += "; ";
            }

        }
        labelClassifiedClass.setText(s);
        labelClassifiedClass1.setText(s);

    //Also update regular training pane values!


    }

    public void displayClassValueMulti(int[] vals) {
        String s = "";

        for (int i = 0; i < vals.length; i++) {
            //  System.out.println("for i = " + i);
            //  System.out.println("Class is " + vals[i]);
            s += vals[i];
            if (i < vals.length - 1) {
                s += "; ";
            }
        }
        labelClassifiedClass.setText(s);
        labelClassifiedClass1.setText(s);

    }

    private void allowNewClassifier(boolean enable) {
        //  buttonLoadSavedSettings.setEnabled(enable);
        //  buttonCreateNewSettings.setEnabled(enable);

        // if (!enable) {
        //    allowNewSettings(false);
        // }
    }

    private void allowNewSettings(boolean enable) {
        //  textNumParams.setEnabled(enable);
        labelNumParams.setEnabled(enable);
        textNumFeatures.setEnabled(enable);
        labelNumFeatures.setEnabled(enable);

    }

    private void disableAllSettingsPanel() {
        panelMainTabs.setSelectedComponent(panelRun);
    //  jTabbedPane1.setEnabledAt(jTabbedPane1.indexOfComponent(panelClassifier), false);

    }

    private void enableRun() {
        //  toggleButtonRun.setEnabled(true);
        if (isChuckDiscrete) {
            buttonComputeAccuracy.setEnabled(false);
        }
        panelMainTabs.setEnabledAt(panelMainTabs.indexOfComponent(panelRun), true);
    }

    private void enableTrainButtons() {
        for (JButton b : trainingButtons) {
            b.setEnabled(true);
        }
    }

    private void disableTrainButtons() {
        for (JButton b : trainingButtons) {
            b.setEnabled(false);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroupClassifierSource = new javax.swing.ButtonGroup();
        buttonGroupSettingsSource = new javax.swing.ButtonGroup();
        buttonGroupProcessingSource = new javax.swing.ButtonGroup();
        buttonQuit = new javax.swing.JButton();
        panelMainTabs = new javax.swing.JTabbedPane();
        panelOSC = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        labelOscStatus1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        textOscReceive = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        textOscSend = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        buttonOscConnect = new javax.swing.JButton();
        buttonOscDisconnect = new javax.swing.JButton();
        labelOscStatus = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        chuckRunnerPanel1 = new wekinator.ChuckRunnerPanel();
        panelFeatures = new javax.swing.JPanel();
        buttonSaveFeatureSettings = new javax.swing.JButton();
        labelOscStatus3 = new javax.swing.JLabel();
        buttonFeaturesGo = new javax.swing.JButton();
        checkAudio = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        checkFFT = new javax.swing.JCheckBox();
        checkCentroid = new javax.swing.JCheckBox();
        checkFlux = new javax.swing.JCheckBox();
        checkRMS = new javax.swing.JCheckBox();
        checkRolloff = new javax.swing.JCheckBox();
        jLabel3 = new javax.swing.JLabel();
        textFFTSize = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        textWindowSize = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        comboWindowType = new javax.swing.JComboBox();
        jLabel6 = new javax.swing.JLabel();
        textAudioRate = new javax.swing.JTextField();
        checkTrackpad = new javax.swing.JCheckBox();
        checkMotionSensor = new javax.swing.JCheckBox();
        checkOtherHID = new javax.swing.JCheckBox();
        checkProcessing = new javax.swing.JCheckBox();
        textMotionRate = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        buttonLoadFeatureSettings = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        radioDownsampled = new javax.swing.JRadioButton();
        radioColorTracking = new javax.swing.JRadioButton();
        radioClearProcessingFeature = new javax.swing.JRadioButton();
        checkCustomChuck = new javax.swing.JCheckBox();
        checkCustomOsc = new javax.swing.JCheckBox();
        textNumOsc = new javax.swing.JTextField();
        textNumCustomChuck = new javax.swing.JTextField();
        buttonSetupOtherHid = new javax.swing.JButton();
        labelHidDescription = new javax.swing.JLabel();
        panelClassifier = new javax.swing.JPanel();
        buttonUseClassifierSettings = new javax.swing.JButton();
        labelClassifierStatus = new javax.swing.JLabel();
        buttonLoadSavedClassifier = new javax.swing.JRadioButton();
        buttonCreateNewClassifier = new javax.swing.JRadioButton();
        labelNumParams = new javax.swing.JLabel();
        textNumParams = new javax.swing.JTextField();
        labelNumFeatures = new javax.swing.JLabel();
        textNumFeatures = new javax.swing.JTextField();
        labelChuckSettings = new javax.swing.JLabel();
        textSettingsDiscrete = new javax.swing.JLabel();
        textSettingsWantDist = new javax.swing.JLabel();
        textSettingsNumClasses = new javax.swing.JLabel();
        comboClassifierType = new javax.swing.JComboBox();
        featureParameterMaskEditor = new wekinator.FeatureParameterMaskEditor();
        jLabel16 = new javax.swing.JLabel();
        panelRun = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        toggleButtonRun = new javax.swing.JToggleButton();
        jLabel7 = new javax.swing.JLabel();
        labelClassifiedClass = new javax.swing.JLabel();
        buttonSaveClassifier = new javax.swing.JButton();
        buttonComputeAccuracy = new javax.swing.JButton();
        labelFeatureStatus = new javax.swing.JLabel();
        labelRunningStatus = new javax.swing.JLabel();
        buttonForget = new javax.swing.JButton();
        buttonTrain = new javax.swing.JButton();
        labelTrainingStatus = new javax.swing.JLabel();
        buttonHoldTrain = new javax.swing.JButton();
        checkViewNNGUI = new javax.swing.JCheckBox();
        buttonListen = new javax.swing.JButton();
        buttonEditClassifier = new javax.swing.JButton();
        labelParameterValues = new javax.swing.JLabel();
        jButtonShh = new javax.swing.JButton();
        toggleGetSynthParams = new javax.swing.JToggleButton();
        buttonViewData = new javax.swing.JButton();
        scrollTrainPanel = new javax.swing.JScrollPane();
        panelRealTraining = new javax.swing.JPanel();
        jButton2 = new javax.swing.JButton();
        panelPlayAlong = new javax.swing.JPanel();
        labelRunningStatus1 = new javax.swing.JLabel();
        labelPlayalongUpdate = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jSliderFastAccurate = new javax.swing.JSlider();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jCheckBoxAutomaticTraining = new javax.swing.JCheckBox();
        jSliderTrainingFrequency = new javax.swing.JSlider();
        jProgressBarTrain = new javax.swing.JProgressBar();
        jLabel14 = new javax.swing.JLabel();
        jPlayAlongCVLabel = new javax.swing.JLabel();
        jCheckBoxAutoStopThreshold = new javax.swing.JCheckBox();
        jTextAutoStopThreshold = new javax.swing.JTextField();
        buttonTrain1 = new javax.swing.JButton();
        buttonForget1 = new javax.swing.JButton();
        jLabelPlayalongProgress = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jComboNumFolds = new javax.swing.JComboBox();
        jButton1 = new javax.swing.JButton();
        jToggleButtonRunPlayalong = new javax.swing.JToggleButton();
        jCheckFastAccurate = new javax.swing.JCheckBox();
        buttonSaveClassifier1 = new javax.swing.JButton();
        jToggleButtonPlayScore = new javax.swing.JToggleButton();
        jToggleButtonPlayAlong = new javax.swing.JToggleButton();
        labelParameterValues2 = new javax.swing.JLabel();
        labelClassifiedClass1 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jlabelNumInstances = new javax.swing.JLabel();
        checkOtfPlayalong = new javax.swing.JCheckBox();
        buttonViewOtfPlayalong = new javax.swing.JButton();
        panelTabFeatureConfiguration = new javax.swing.JPanel();
        featureConfigurationPanel1 = new wekinator.FeatureConfigurationPanel();
        panelTabLearningSystemConfiguration = new javax.swing.JPanel();
        learningSystemConfigurationPanel = new wekinator.LearningSystemConfigurationPanel();
        labelGlobalStatus = new javax.swing.JLabel();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        preferencesMenuItem = new javax.swing.JMenuItem();
        exitMenuItem = new javax.swing.JMenuItem();
        viewMenu = new javax.swing.JMenu();
        menuItemViewConsole = new javax.swing.JMenuItem();
        menuItemOtfScore = new javax.swing.JMenuItem();
        helpMenu1 = new javax.swing.JMenu();
        contentsMenuItem1 = new javax.swing.JMenuItem();
        aboutMenuItem1 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("The Wekinator");

        buttonQuit.setText("Quit");
        buttonQuit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonQuitActionPerformed(evt);
            }
        });

        panelMainTabs.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                panelMainTabsComponentShown(evt);
            }
        });
        panelMainTabs.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                panelMainTabsStateChanged(evt);
            }
        });

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("OSC"));

        labelOscStatus1.setText("Set the ports to begin.");

        jLabel2.setText("Recv Port");

        textOscReceive.setText("6448");

        jLabel9.setText("(Send port used in ChucK)");

        jLabel8.setText("(Receive port used in ChucK)");

        textOscSend.setText("6453");

        jLabel1.setText("Send Port");

        buttonOscConnect.setText("Connect");
        buttonOscConnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonOscConnectActionPerformed(evt);
            }
        });

        buttonOscDisconnect.setText("Disconnect");
        buttonOscDisconnect.setEnabled(false);
        buttonOscDisconnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonOscDisconnectActionPerformed(evt);
            }
        });

        labelOscStatus.setText("OSC Status: Not connected yet.");

        org.jdesktop.layout.GroupLayout jPanel5Layout = new org.jdesktop.layout.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel5Layout.createSequentialGroup()
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel5Layout.createSequentialGroup()
                        .addContainerGap()
                        .add(buttonOscConnect)
                        .add(227, 227, 227)
                        .add(buttonOscDisconnect))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel5Layout.createSequentialGroup()
                        .add(23, 23, 23)
                        .add(labelOscStatus, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 521, Short.MAX_VALUE))
                    .add(jPanel5Layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel2)
                            .add(jPanel5Layout.createSequentialGroup()
                                .add(jLabel1)
                                .add(10, 10, 10)
                                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jPanel5Layout.createSequentialGroup()
                                        .add(textOscReceive, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 71, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(jLabel9))
                                    .add(jPanel5Layout.createSequentialGroup()
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(textOscSend, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 71, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(jLabel8))))
                            .add(labelOscStatus1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 151, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel5Layout.createSequentialGroup()
                .add(labelOscStatus1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(textOscReceive, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel9))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(textOscSend, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel8, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 44, Short.MAX_VALUE)
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(buttonOscDisconnect)
                    .add(buttonOscConnect))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(labelOscStatus)
                .addContainerGap())
        );

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder("ChucK (experimental & optional!)"));

        org.jdesktop.layout.GroupLayout jPanel8Layout = new org.jdesktop.layout.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel8Layout.createSequentialGroup()
                .add(chuckRunnerPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 544, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel8Layout.createSequentialGroup()
                .add(chuckRunnerPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout panelOSCLayout = new org.jdesktop.layout.GroupLayout(panelOSC);
        panelOSC.setLayout(panelOSCLayout);
        panelOSCLayout.setHorizontalGroup(
            panelOSCLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, panelOSCLayout.createSequentialGroup()
                .add(panelOSCLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jPanel8, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        panelOSCLayout.setVerticalGroup(
            panelOSCLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(panelOSCLayout.createSequentialGroup()
                .add(jPanel8, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(60, Short.MAX_VALUE))
        );

        panelMainTabs.addTab("Setup", panelOSC);

        buttonSaveFeatureSettings.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        buttonSaveFeatureSettings.setText("Save settings to file...");
        buttonSaveFeatureSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSaveFeatureSettingsActionPerformed(evt);
            }
        });

        labelOscStatus3.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        labelOscStatus3.setText("Select the features to use for learning.");

        buttonFeaturesGo.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        buttonFeaturesGo.setText("Go!");
        buttonFeaturesGo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonFeaturesGoActionPerformed(evt);
            }
        });

        checkAudio.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        checkAudio.setText("Audio");
        checkAudio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkAudioActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        checkFFT.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        checkFFT.setText("FFT");
        checkFFT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkFFTActionPerformed(evt);
            }
        });

        checkCentroid.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        checkCentroid.setText("Centroid");
        checkCentroid.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkCentroidActionPerformed(evt);
            }
        });

        checkFlux.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        checkFlux.setText("Flux");
        checkFlux.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkFluxActionPerformed(evt);
            }
        });

        checkRMS.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        checkRMS.setText("RMS");
        checkRMS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkRMSActionPerformed(evt);
            }
        });

        checkRolloff.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        checkRolloff.setText("Rolloff");
        checkRolloff.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkRolloffActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        jLabel3.setText("FFT size");

        textFFTSize.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        textFFTSize.setText("512");

        jLabel4.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        jLabel4.setText("Window size");

        textWindowSize.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        textWindowSize.setText("256");

        jLabel5.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        jLabel5.setText("Window type");

        comboWindowType.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        comboWindowType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Hamming", "Hann", "Rectangular" }));

        jLabel6.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        jLabel6.setText("Rate / Hop size (ms):");

        textAudioRate.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        textAudioRate.setText("100");

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(checkFFT)
                            .add(checkCentroid)
                            .add(checkFlux))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jPanel1Layout.createSequentialGroup()
                                .add(checkRMS)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 222, Short.MAX_VALUE)
                                .add(jLabel3))
                            .add(jPanel1Layout.createSequentialGroup()
                                .add(checkRolloff)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 186, Short.MAX_VALUE)
                                .add(jLabel4))
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel5))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(textWindowSize, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(textFFTSize, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(comboWindowType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jLabel6)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(textAudioRate, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(checkFFT)
                    .add(checkRMS)
                    .add(textFFTSize, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel3))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(checkCentroid)
                        .add(checkRolloff))
                    .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(textWindowSize, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(jLabel4)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(checkFlux)
                    .add(jLabel5)
                    .add(comboWindowType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel6)
                    .add(textAudioRate, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        checkTrackpad.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        checkTrackpad.setText("Trackpad (XY)");

        checkMotionSensor.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        checkMotionSensor.setText("Motion sensor (XYZ)");

        checkOtherHID.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        checkOtherHID.setText("Other HID");

        checkProcessing.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        checkProcessing.setText("Processing");

        textMotionRate.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        textMotionRate.setText("100");

        jLabel11.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        jLabel11.setText("Extraction rate (ms)");

        buttonLoadFeatureSettings.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        buttonLoadFeatureSettings.setText("Load feature settings from file...");
        buttonLoadFeatureSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonLoadFeatureSettingsActionPerformed(evt);
            }
        });

        jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        buttonGroupProcessingSource.add(radioDownsampled);
        radioDownsampled.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        radioDownsampled.setText("Webcam edge (100 features)");
        radioDownsampled.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioDownsampledActionPerformed(evt);
            }
        });

        buttonGroupProcessingSource.add(radioColorTracking);
        radioColorTracking.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        radioColorTracking.setText("Color tracking (6 features)");

        buttonGroupProcessingSource.add(radioClearProcessingFeature);
        radioClearProcessingFeature.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        radioClearProcessingFeature.setText("jRadioButton1");

        org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel4Layout.createSequentialGroup()
                        .add(radioDownsampled)
                        .add(86, 86, 86)
                        .add(radioClearProcessingFeature))
                    .add(radioColorTracking))
                .addContainerGap(145, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel4Layout.createSequentialGroup()
                        .addContainerGap()
                        .add(radioDownsampled)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(radioColorTracking))
                    .add(radioClearProcessingFeature))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        checkCustomChuck.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        checkCustomChuck.setText("Custom ChucK features: Enter #");
        checkCustomChuck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkCustomChuckActionPerformed(evt);
            }
        });

        checkCustomOsc.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        checkCustomOsc.setText("Custom OSC features: Enter #");
        checkCustomOsc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkCustomOscActionPerformed(evt);
            }
        });

        textNumOsc.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        textNumOsc.setText("0");

        textNumCustomChuck.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        textNumCustomChuck.setText("0");
        textNumCustomChuck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textNumCustomChuckActionPerformed(evt);
            }
        });

        buttonSetupOtherHid.setText("Configure...");
        buttonSetupOtherHid.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSetupOtherHidActionPerformed(evt);
            }
        });

        labelHidDescription.setText("None configured");

        org.jdesktop.layout.GroupLayout panelFeaturesLayout = new org.jdesktop.layout.GroupLayout(panelFeatures);
        panelFeatures.setLayout(panelFeaturesLayout);
        panelFeaturesLayout.setHorizontalGroup(
            panelFeaturesLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, panelFeaturesLayout.createSequentialGroup()
                .add(panelFeaturesLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(panelFeaturesLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(panelFeaturesLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(checkProcessing)
                            .add(panelFeaturesLayout.createSequentialGroup()
                                .add(buttonSaveFeatureSettings)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(buttonFeaturesGo))
                            .add(jPanel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(panelFeaturesLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, panelFeaturesLayout.createSequentialGroup()
                                    .add(checkCustomChuck)
                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                    .add(textNumCustomChuck))
                                .add(org.jdesktop.layout.GroupLayout.LEADING, panelFeaturesLayout.createSequentialGroup()
                                    .add(checkCustomOsc)
                                    .add(18, 18, 18)
                                    .add(textNumOsc, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 37, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, panelFeaturesLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(panelFeaturesLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(panelFeaturesLayout.createSequentialGroup()
                                .add(panelFeaturesLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(panelFeaturesLayout.createSequentialGroup()
                                        .add(checkOtherHID)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(buttonSetupOtherHid))
                                    .add(panelFeaturesLayout.createSequentialGroup()
                                        .add(checkMotionSensor)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(jLabel11))
                                    .add(checkTrackpad))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(textMotionRate, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(checkAudio)
                            .add(labelOscStatus3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 556, Short.MAX_VALUE)
                            .add(buttonLoadFeatureSettings, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 556, Short.MAX_VALUE)
                            .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, panelFeaturesLayout.createSequentialGroup()
                        .add(47, 47, 47)
                        .add(labelHidDescription, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 529, Short.MAX_VALUE)))
                .addContainerGap())
        );
        panelFeaturesLayout.setVerticalGroup(
            panelFeaturesLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(panelFeaturesLayout.createSequentialGroup()
                .add(labelOscStatus3)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(buttonLoadFeatureSettings)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(checkAudio)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(4, 4, 4)
                .add(checkTrackpad)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(panelFeaturesLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(checkMotionSensor)
                    .add(jLabel11)
                    .add(textMotionRate, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(panelFeaturesLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(checkOtherHID)
                    .add(buttonSetupOtherHid))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(labelHidDescription)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(checkProcessing)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(panelFeaturesLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(checkCustomChuck)
                    .add(textNumCustomChuck, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(panelFeaturesLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(checkCustomOsc)
                    .add(textNumOsc, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(panelFeaturesLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(buttonSaveFeatureSettings)
                    .add(buttonFeaturesGo))
                .addContainerGap(81, Short.MAX_VALUE))
        );

        panelMainTabs.addTab("Features", panelFeatures);

        buttonUseClassifierSettings.setText("Go!");
        buttonUseClassifierSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonUseClassifierSettingsActionPerformed(evt);
            }
        });

        labelClassifierStatus.setText("Classifier status: Not set");

        buttonGroupClassifierSource.add(buttonLoadSavedClassifier);
        buttonLoadSavedClassifier.setText("Load saved model from file");
        buttonLoadSavedClassifier.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonLoadSavedClassifierActionPerformed(evt);
            }
        });

        buttonGroupClassifierSource.add(buttonCreateNewClassifier);
        buttonCreateNewClassifier.setSelected(true);
        buttonCreateNewClassifier.setText("Create new model");
        buttonCreateNewClassifier.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonCreateNewClassifierActionPerformed(evt);
            }
        });

        labelNumParams.setText("Number of parameters to learn");

        textNumParams.setEnabled(false);

        labelNumFeatures.setText("Number of features from ChucK");

        textNumFeatures.setEnabled(false);

        labelChuckSettings.setText("ChucK settings:");

        textSettingsDiscrete.setText("Discrete classification");

        textSettingsWantDist.setText("Class label output");

        textSettingsNumClasses.setText("2 classes");

        comboClassifierType.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        comboClassifierType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "AdaBoost", "kNN", "SVM", "DecisionTree" }));
        comboClassifierType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboClassifierTypeActionPerformed(evt);
            }
        });

        jLabel16.setText("Select features to use for parameter models (optional). Editing WILL ERASE trained classifiers!");

        org.jdesktop.layout.GroupLayout panelClassifierLayout = new org.jdesktop.layout.GroupLayout(panelClassifier);
        panelClassifier.setLayout(panelClassifierLayout);
        panelClassifierLayout.setHorizontalGroup(
            panelClassifierLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(panelClassifierLayout.createSequentialGroup()
                .add(panelClassifierLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(panelClassifierLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(panelClassifierLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(buttonLoadSavedClassifier)
                            .add(panelClassifierLayout.createSequentialGroup()
                                .add(buttonCreateNewClassifier)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(comboClassifierType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 239, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                    .add(panelClassifierLayout.createSequentialGroup()
                        .add(71, 71, 71)
                        .add(panelClassifierLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(labelNumFeatures)
                            .add(labelNumParams))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(panelClassifierLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(textNumFeatures, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 65, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(textNumParams, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 65, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(panelClassifierLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(panelClassifierLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(labelChuckSettings, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 304, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(panelClassifierLayout.createSequentialGroup()
                                .add(24, 24, 24)
                                .add(panelClassifierLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                    .add(textSettingsNumClasses, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 304, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(textSettingsDiscrete, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 304, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(textSettingsWantDist, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 304, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))))
                    .add(panelClassifierLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(labelClassifierStatus, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 304, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(panelClassifierLayout.createSequentialGroup()
                        .add(8, 8, 8)
                        .add(panelClassifierLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel16)
                            .add(buttonUseClassifierSettings)))
                    .add(panelClassifierLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(featureParameterMaskEditor, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 573, Short.MAX_VALUE)))
                .addContainerGap())
        );
        panelClassifierLayout.setVerticalGroup(
            panelClassifierLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(panelClassifierLayout.createSequentialGroup()
                .addContainerGap()
                .add(buttonLoadSavedClassifier)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(panelClassifierLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(buttonCreateNewClassifier)
                    .add(comboClassifierType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 27, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(panelClassifierLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(labelNumParams)
                    .add(textNumParams, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(panelClassifierLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(labelNumFeatures)
                    .add(textNumFeatures, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(labelChuckSettings, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(textSettingsDiscrete, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(textSettingsNumClasses, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(6, 6, 6)
                .add(textSettingsWantDist, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(labelClassifierStatus, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 16, Short.MAX_VALUE)
                .add(jLabel16)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(featureParameterMaskEditor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 268, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(buttonUseClassifierSettings)
                .addContainerGap())
        );

        panelMainTabs.addTab("Settings", panelClassifier);

        panelRun.setEnabled(false);
        panelRun.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                panelRunKeyPressed(evt);
            }
        });

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Use trained classifier"));

        toggleButtonRun.setText("Run!");
        toggleButtonRun.setEnabled(false);
        toggleButtonRun.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                toggleButtonRunItemStateChanged(evt);
            }
        });
        toggleButtonRun.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toggleButtonRunActionPerformed(evt);
            }
        });
        toggleButtonRun.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                toggleButtonRunStateChanged(evt);
            }
        });

        jLabel7.setText("Class: ");

        labelClassifiedClass.setText("?");

        buttonSaveClassifier.setText("save trained model");
        buttonSaveClassifier.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSaveClassifierActionPerformed(evt);
            }
        });

        buttonComputeAccuracy.setText("Compute accuracy");
        buttonComputeAccuracy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonComputeAccuracyActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel3Layout.createSequentialGroup()
                        .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel3Layout.createSequentialGroup()
                                .addContainerGap()
                                .add(buttonSaveClassifier, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel3Layout.createSequentialGroup()
                                .add(17, 17, 17)
                                .add(toggleButtonRun)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel7)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(labelClassifiedClass, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 313, Short.MAX_VALUE))
                    .add(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .add(buttonComputeAccuracy, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 237, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .add(buttonComputeAccuracy)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(toggleButtonRun)
                    .add(jLabel7)
                    .add(labelClassifiedClass))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(buttonSaveClassifier)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        labelFeatureStatus.setText("ChucK feature status: Waiting for feature information");

        buttonForget.setText("Forget everything!");
        buttonForget.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonForgetActionPerformed(evt);
            }
        });

        buttonTrain.setText("Train!");
        buttonTrain.setEnabled(false);
        buttonTrain.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonTrainActionPerformed(evt);
            }
        });

        labelTrainingStatus.setText("Classifier status: No instances recorded");

        buttonHoldTrain.setText("Hold to record examples");
        buttonHoldTrain.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonHoldTrainActionPerformed(evt);
            }
        });
        buttonHoldTrain.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                buttonHoldTrainStateChanged(evt);
            }
        });

        checkViewNNGUI.setText("view GUIs when training");
        checkViewNNGUI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkViewNNGUIActionPerformed(evt);
            }
        });

        buttonListen.setText("Listen to this value");
        buttonListen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonListenActionPerformed(evt);
            }
        });
        buttonListen.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                buttonListenStateChanged(evt);
            }
        });

        buttonEditClassifier.setText("Classifier Settings...");
        buttonEditClassifier.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonEditClassifierActionPerformed(evt);
            }
        });
        buttonEditClassifier.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                buttonEditClassifierStateChanged(evt);
            }
        });

        labelParameterValues.setText("Enter parameter values (integers between 0 and 2):");

        jButtonShh.setText("shh!");
        jButtonShh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonShhActionPerformed(evt);
            }
        });

        toggleGetSynthParams.setText("Get params from synth");
        toggleGetSynthParams.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                toggleGetSynthParamsItemStateChanged(evt);
            }
        });
        toggleGetSynthParams.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toggleGetSynthParamsActionPerformed(evt);
            }
        });

        buttonViewData.setText("View Data");
        buttonViewData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonViewDataActionPerformed(evt);
            }
        });

        scrollTrainPanel.setViewportView(panelRealTraining);

        jButton2.setText("Add to OTF score");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout panelRunLayout = new org.jdesktop.layout.GroupLayout(panelRun);
        panelRun.setLayout(panelRunLayout);
        panelRunLayout.setHorizontalGroup(
            panelRunLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(panelRunLayout.createSequentialGroup()
                .add(panelRunLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(panelRunLayout.createSequentialGroup()
                        .add(21, 21, 21)
                        .add(panelRunLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(labelRunningStatus)
                            .add(panelRunLayout.createSequentialGroup()
                                .add(buttonHoldTrain)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(buttonListen)
                                .add(6, 6, 6)
                                .add(panelRunLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(panelRunLayout.createSequentialGroup()
                                        .add(jButtonShh)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                        .add(buttonViewData))
                                    .add(toggleGetSynthParams)
                                    .add(jButton2)))))
                    .add(panelRunLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(panelRunLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(labelFeatureStatus)
                            .add(panelRunLayout.createSequentialGroup()
                                .add(labelTrainingStatus)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(buttonEditClassifier))
                            .add(panelRunLayout.createSequentialGroup()
                                .add(buttonTrain)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(checkViewNNGUI)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(buttonForget))))
                    .add(panelRunLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(labelParameterValues))
                    .add(panelRunLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(scrollTrainPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 316, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jPanel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        panelRunLayout.setVerticalGroup(
            panelRunLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(panelRunLayout.createSequentialGroup()
                .add(panelRunLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(buttonHoldTrain)
                    .add(buttonListen)
                    .add(toggleGetSynthParams))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(panelRunLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(labelParameterValues)
                    .add(panelRunLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(jButtonShh)
                        .add(buttonViewData)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(panelRunLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(scrollTrainPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 158, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jButton2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(panelRunLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(buttonTrain)
                    .add(checkViewNNGUI)
                    .add(buttonForget))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(labelFeatureStatus, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(panelRunLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(labelTrainingStatus, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(buttonEditClassifier))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 123, Short.MAX_VALUE)
                .add(labelRunningStatus, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        jPanel3.getAccessibleContext().setAccessibleName("Use trained model");

        panelMainTabs.addTab("Train/Run", panelRun);

        panelPlayAlong.setEnabled(false);

        labelPlayalongUpdate.setText("    ");

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder("Training"));

        jSliderFastAccurate.setEnabled(false);
        jSliderFastAccurate.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSliderFastAccurateStateChanged(evt);
            }
        });

        jLabel12.setText("FAST");

        jLabel13.setText("ACCURATE");

        org.jdesktop.layout.GroupLayout jPanel7Layout = new org.jdesktop.layout.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jSliderFastAccurate, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 507, Short.MAX_VALUE)
            .add(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel12)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 368, Short.MAX_VALUE)
                .add(jLabel13)
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel7Layout.createSequentialGroup()
                .add(jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jSliderFastAccurate, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jPanel7Layout.createSequentialGroup()
                        .add(32, 32, 32)
                        .add(jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel12)
                            .add(jLabel13))))
                .addContainerGap(10, Short.MAX_VALUE))
        );

        jCheckBoxAutomaticTraining.setText("Automatic training: Set frequency (seconds)");
        jCheckBoxAutomaticTraining.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxAutomaticTrainingActionPerformed(evt);
            }
        });

        jSliderTrainingFrequency.setMajorTickSpacing(10);
        jSliderTrainingFrequency.setMaximum(60);
        jSliderTrainingFrequency.setMinimum(1);
        jSliderTrainingFrequency.setPaintLabels(true);
        jSliderTrainingFrequency.setPaintTicks(true);
        jSliderTrainingFrequency.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSliderTrainingFrequencyStateChanged(evt);
            }
        });

        jLabel14.setText("Progress:");

        jPlayAlongCVLabel.setText("   ");

        jCheckBoxAutoStopThreshold.setText("Use auto-stop threshhold:");
        jCheckBoxAutoStopThreshold.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxAutoStopThresholdActionPerformed(evt);
            }
        });

        jTextAutoStopThreshold.setText(".95");
        jTextAutoStopThreshold.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextAutoStopThresholdActionPerformed(evt);
            }
        });

        buttonTrain1.setText("Manual train now!");
        buttonTrain1.setEnabled(false);
        buttonTrain1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonTrain1ActionPerformed(evt);
            }
        });

        buttonForget1.setText("Forget everything!");
        buttonForget1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonForget1ActionPerformed(evt);
            }
        });

        jLabelPlayalongProgress.setText("    ");

        jLabel15.setText("# CV folds");

        jComboNumFolds.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "2", "3", "4", "5", "10", "20" }));
        jComboNumFolds.setSelectedIndex(4);
        jComboNumFolds.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboNumFoldsActionPerformed(evt);
            }
        });

        jButton1.setText("set");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jToggleButtonRunPlayalong.setText("Run Current Model");
        jToggleButtonRunPlayalong.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButtonRunPlayalongActionPerformed(evt);
            }
        });

        jCheckFastAccurate.setText("Use fast/accurate parameterization slider");
        jCheckFastAccurate.setEnabled(false);
        jCheckFastAccurate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckFastAccurateActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel6Layout = new org.jdesktop.layout.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel6Layout.createSequentialGroup()
                        .add(jCheckFastAccurate)
                        .addContainerGap())
                    .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(jPanel6Layout.createSequentialGroup()
                            .add(26, 26, 26)
                            .add(jLabel15)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(jComboNumFolds, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .addContainerGap())
                        .add(jPlayAlongCVLabel)
                        .add(jPanel6Layout.createSequentialGroup()
                            .add(jCheckBoxAutoStopThreshold)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(jTextAutoStopThreshold, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(jButton1)
                            .addContainerGap())
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel6Layout.createSequentialGroup()
                            .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(jPanel6Layout.createSequentialGroup()
                                    .add(jLabel14)
                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                    .add(jLabelPlayalongProgress, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 449, Short.MAX_VALUE))
                                .add(jProgressBarTrain, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 515, Short.MAX_VALUE))
                            .addContainerGap())
                        .add(jPanel6Layout.createSequentialGroup()
                            .add(buttonTrain1)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(buttonForget1))
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel6Layout.createSequentialGroup()
                            .add(jToggleButtonRunPlayalong)
                            .addContainerGap())
                        .add(jPanel6Layout.createSequentialGroup()
                            .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(jPanel6Layout.createSequentialGroup()
                                    .add(jCheckBoxAutomaticTraining)
                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 202, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .add(jSliderTrainingFrequency, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 509, Short.MAX_VALUE)
                                .add(jPanel6Layout.createSequentialGroup()
                                    .add(jPanel7, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                            .add(26, 26, 26)))))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .add(jCheckFastAccurate)
                .add(3, 3, 3)
                .add(jPanel7, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 58, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jCheckBoxAutomaticTraining)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSliderTrainingFrequency, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 37, Short.MAX_VALUE)
                .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(buttonTrain1)
                    .add(buttonForget1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel14)
                    .add(jLabelPlayalongProgress, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(6, 6, 6)
                .add(jProgressBarTrain, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jPanel6Layout.createSequentialGroup()
                        .add(jPlayAlongCVLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel15)
                            .add(jComboNumFolds, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jCheckBoxAutoStopThreshold)
                            .add(jTextAutoStopThreshold, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jButton1))
                        .add(49, 49, 49))
                    .add(jPanel6Layout.createSequentialGroup()
                        .add(jToggleButtonRunPlayalong)
                        .addContainerGap())))
        );

        buttonSaveClassifier1.setText("save trained model");
        buttonSaveClassifier1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSaveClassifier1ActionPerformed(evt);
            }
        });

        jToggleButtonPlayScore.setText("Play Score");
        jToggleButtonPlayScore.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButtonPlayScoreItemStateChanged(evt);
            }
        });
        jToggleButtonPlayScore.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButtonPlayScoreActionPerformed(evt);
            }
        });
        jToggleButtonPlayScore.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jToggleButtonPlayScoreStateChanged(evt);
            }
        });

        jToggleButtonPlayAlong.setText("Play Along");
        jToggleButtonPlayAlong.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleButtonPlayAlongItemStateChanged(evt);
            }
        });
        jToggleButtonPlayAlong.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButtonPlayAlongActionPerformed(evt);
            }
        });
        jToggleButtonPlayAlong.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jToggleButtonPlayAlongStateChanged(evt);
            }
        });

        labelParameterValues2.setText("examples recorded:");

        labelClassifiedClass1.setText("?");

        jLabel10.setText("Class: ");

        jlabelNumInstances.setText("0");

        checkOtfPlayalong.setText("NEW: Use your own on-the-fly playalong score");

        buttonViewOtfPlayalong.setText("View OTF score");
        buttonViewOtfPlayalong.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonViewOtfPlayalongActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout panelPlayAlongLayout = new org.jdesktop.layout.GroupLayout(panelPlayAlong);
        panelPlayAlong.setLayout(panelPlayAlongLayout);
        panelPlayAlongLayout.setHorizontalGroup(
            panelPlayAlongLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(panelPlayAlongLayout.createSequentialGroup()
                .add(panelPlayAlongLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(panelPlayAlongLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(labelPlayalongUpdate))
                    .add(panelPlayAlongLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(panelPlayAlongLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jPanel6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(buttonSaveClassifier1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 207, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(panelPlayAlongLayout.createSequentialGroup()
                        .add(panelPlayAlongLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(panelPlayAlongLayout.createSequentialGroup()
                                .add(21, 21, 21)
                                .add(panelPlayAlongLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(labelRunningStatus1)
                                    .add(panelPlayAlongLayout.createSequentialGroup()
                                        .add(103, 103, 103)
                                        .add(jToggleButtonPlayAlong))))
                            .add(panelPlayAlongLayout.createSequentialGroup()
                                .addContainerGap()
                                .add(jToggleButtonPlayScore)))
                        .add(panelPlayAlongLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(panelPlayAlongLayout.createSequentialGroup()
                                .add(57, 57, 57)
                                .add(labelParameterValues2)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jlabelNumInstances, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 44, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(panelPlayAlongLayout.createSequentialGroup()
                                .add(121, 121, 121)
                                .add(buttonViewOtfPlayalong)))))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .add(panelPlayAlongLayout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel10)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(labelClassifiedClass1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE)
                .add(439, 439, 439))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, panelPlayAlongLayout.createSequentialGroup()
                .addContainerGap(21, Short.MAX_VALUE)
                .add(checkOtfPlayalong)
                .add(480, 480, 480))
        );
        panelPlayAlongLayout.setVerticalGroup(
            panelPlayAlongLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(panelPlayAlongLayout.createSequentialGroup()
                .add(panelPlayAlongLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(checkOtfPlayalong)
                    .add(buttonViewOtfPlayalong))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(panelPlayAlongLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jToggleButtonPlayScore)
                    .add(jToggleButtonPlayAlong)
                    .add(labelParameterValues2)
                    .add(jlabelNumInstances))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(labelPlayalongUpdate)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(panelPlayAlongLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel10)
                    .add(labelClassifiedClass1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(buttonSaveClassifier1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(labelRunningStatus1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        panelMainTabs.addTab("Playalong", panelPlayAlong);

        org.jdesktop.layout.GroupLayout panelTabFeatureConfigurationLayout = new org.jdesktop.layout.GroupLayout(panelTabFeatureConfiguration);
        panelTabFeatureConfiguration.setLayout(panelTabFeatureConfigurationLayout);
        panelTabFeatureConfigurationLayout.setHorizontalGroup(
            panelTabFeatureConfigurationLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(featureConfigurationPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 596, Short.MAX_VALUE)
        );
        panelTabFeatureConfigurationLayout.setVerticalGroup(
            panelTabFeatureConfigurationLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(panelTabFeatureConfigurationLayout.createSequentialGroup()
                .add(featureConfigurationPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(193, Short.MAX_VALUE))
        );

        panelMainTabs.addTab("NewFeatures", panelTabFeatureConfiguration);

        panelTabLearningSystemConfiguration.setEnabled(false);

        org.jdesktop.layout.GroupLayout panelTabLearningSystemConfigurationLayout = new org.jdesktop.layout.GroupLayout(panelTabLearningSystemConfiguration);
        panelTabLearningSystemConfiguration.setLayout(panelTabLearningSystemConfigurationLayout);
        panelTabLearningSystemConfigurationLayout.setHorizontalGroup(
            panelTabLearningSystemConfigurationLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, learningSystemConfigurationPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 596, Short.MAX_VALUE)
        );
        panelTabLearningSystemConfigurationLayout.setVerticalGroup(
            panelTabLearningSystemConfigurationLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(panelTabLearningSystemConfigurationLayout.createSequentialGroup()
                .add(learningSystemConfigurationPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 551, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(71, Short.MAX_VALUE))
        );

        panelMainTabs.addTab("NewSettings", panelTabLearningSystemConfiguration);

        fileMenu.setText("Wekinator");

        preferencesMenuItem.setText("Preferences");
        fileMenu.add(preferencesMenuItem);

        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        viewMenu.setText("View");

        menuItemViewConsole.setText("Console");
        menuItemViewConsole.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemViewConsoleActionPerformed(evt);
            }
        });
        viewMenu.add(menuItemViewConsole);

        menuItemOtfScore.setText("On-the-fly score");
        menuItemOtfScore.setEnabled(false);
        viewMenu.add(menuItemOtfScore);

        menuBar.add(viewMenu);

        helpMenu1.setText("Help");

        contentsMenuItem1.setText("Contents");
        helpMenu1.add(contentsMenuItem1);

        aboutMenuItem1.setText("About");
        aboutMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutMenuItem1ActionPerformed(evt);
            }
        });
        helpMenu1.add(aboutMenuItem1);

        menuBar.add(helpMenu1);

        setJMenuBar(menuBar);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(panelMainTabs, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 617, Short.MAX_VALUE)
                    .add(buttonQuit)
                    .add(labelGlobalStatus, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 493, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap(10, Short.MAX_VALUE)
                .add(panelMainTabs, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 668, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(buttonQuit)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(labelGlobalStatus, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 32, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void buttonForgetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonForgetActionPerformed
    w.clear();
}//GEN-LAST:event_buttonForgetActionPerformed

private void buttonQuitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonQuitActionPerformed
    w.quit();
    if (wek.runner.getRunnerState() == ChuckRunner.ChuckRunnerState.RUNNING) {
        try {
            wek.runner.stop();
        } catch (IOException ex) {
        }
    }
    //Want to save settings here!
    wek.saveCurrentSettings();

    System.exit(0);
}//GEN-LAST:event_buttonQuitActionPerformed

private void buttonOscConnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonOscConnectActionPerformed
    try {


        sendPort = Integer.parseInt(textOscSend.getText());
        receivePort = Integer.parseInt(textOscReceive.getText());
        w.begin(sendPort, receivePort);
    // w.Handler().setHidSetup(hs);
    } catch (IOException ex) {
        labelGlobalStatus.setText("Error setting up: " + ex.getMessage());
    }
}//GEN-LAST:event_buttonOscConnectActionPerformed

private void buttonOscDisconnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonOscDisconnectActionPerformed
    w.disconnectOSC();
}//GEN-LAST:event_buttonOscDisconnectActionPerformed

private void buttonUseClassifierSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonUseClassifierSettingsActionPerformed
    if (buttonLoadSavedClassifier.isSelected() || w.getTrainingState() == WekaOperator.TrainingState.TRAINED) { //TODO: OR we've just edited the settings for existing...
        //We already have classifier set. Yay.

        //TODO: Change combo box!
        myNumParams = w.getNumParameters();
        myNumFeats = w.numFeaturesToExpect;
        updateTrainingPanelForParams();
        disableAllSettingsPanel();
        enablePlayalongPanel();
        enableRunPanel();




        FeatureToParameterMapping mapping = featureParameterMaskEditor.getFeatureToParameterMapping();
        //TODO: Ask if changed! & prompt user.

        if (featureParameterMaskEditor.hasMappingChanged()) {
            //Warning!
            int clearIt = JOptionPane.showConfirmDialog(this,
                    "You have edited the relationship between features and \n" + "parameters. " + "This will cause one or more of your \n" + "trained models to be forgotten. Proceed anyway? \n" + "(Select NO and then RESET to undo this change)", "",
                    JOptionPane.YES_NO_OPTION);

            if (clearIt != JOptionPane.YES_OPTION) {
                return; //Get out of here!
            } else {
                //We are to go ahead with these settings.
                for (int f = 0; f < fm.getNumFeatures(); f++) {
                    for (int p = 0; p < myNumParams; p++) {
                        boolean b = mapping.getIsFeatureUsingParam(f, p);
                        w.dataset.setIsFeatureActiveForParameter(b, f, p);
                    }
                }

                //Now what about classifiers??

                //  w.setNumParameters(w.getNumParameters()); //Hack: Really  just want to clear the classifiers that need it
                w.resetClassifiers();
            }
        }

        panelRealTraining.setVisible(true);

    //Initialize feature parameter mapping from dataset


    } else if (validateSettingsInput()) {
        //Use new classifier
        myNumParams = Integer.parseInt(textNumParams.getText());
        myNumFeats = Integer.parseInt(textNumFeatures.getText());
        w.setNumParameters(myNumParams);
        w.numFeaturesToExpect = myNumFeats;
        if (isChuckDiscrete) {
            int chosen = comboClassifierType.getSelectedIndex();
            if (chosen == 0) {
                //adaboost
                w.chooseClassifier(WekaOperator.ClassifierType.ADABOOST);
            } else if (chosen == 1) {
                //knn
                w.chooseClassifier(WekaOperator.ClassifierType.KNN);

            } else if (chosen == 2) {
                w.chooseClassifier(WekaOperator.ClassifierType.SVM);
            } else if (chosen == 3) {
                w.chooseClassifier(WekaOperator.ClassifierType.DTREE);
            }

        } else {
            w.chooseClassifier(WekaOperator.ClassifierType.NN);
        }
        jCheckFastAccurate.setEnabled(w.isFastAccurateValidForClassifierType());

        updateTrainingPanelForParams();
        disableAllSettingsPanel();
        enablePlayalongPanel();
        panelRealTraining.setVisible(true);
        w.initializeInstances(myNumFeats, numChuckClasses);
        //TODO: inefficient:
        FeatureToParameterMapping mapping = featureParameterMaskEditor.getFeatureToParameterMapping();
        for (int f = 0; f < fm.getNumFeatures(); f++) {
            for (int p = 0; p < myNumParams; p++) {
                boolean b = mapping.getIsFeatureUsingParam(f, p);
                w.dataset.setIsFeatureActiveForParameter(b, f, p);
            }
        }

    }
    score = new PlayalongScore(myNumParams);
    score.setMainGui(this);
    menuItemOtfScore.setEnabled(true);
    scoreViewer = new PlayalongScoreViewer(score, this);
   // scoreViewer.setVisible(true);



}//GEN-LAST:event_buttonUseClassifierSettingsActionPerformed

private void buttonTrainActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonTrainActionPerformed
    spawnWorkerThread();

//w.train();

}//GEN-LAST:event_buttonTrainActionPerformed

    private void spawnWorkerThread() {

        //See http://www.j2ee.me/javase/6/docs/api/javax/swing/SwingWorker.html

        //Do this on separate thread!
        SwingWorker worker = new SwingWorker<Integer, Void>() {

            @Override
            public Integer doInBackground() {
                w.train();
                return new Integer(0);
            }
        };

        worker.execute();
    }

    public void displayClassValue(int i) {
        System.out.println("Class is " + i);
        labelClassifiedClass.setText(Integer.toString(i));
        labelClassifiedClass1.setText(Integer.toString(i));

    }

    private void saveSettings() {

        JFileChooser fc = new JFileChooser();
        fc.setDialogType(JFileChooser.SAVE_DIALOG);
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);


        int returnVal = fc.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            FileOutputStream outstream = null;

            ObjectOutputStream objout = null;

            try {


                File file = fc.getSelectedFile();
                outstream = new FileOutputStream(file);
                objout = new ObjectOutputStream(outstream);
                int p = Integer.parseInt(textNumParams.getText());
                int f = Integer.parseInt(textNumFeatures.getText());
                objout.writeInt(p);
                objout.writeInt(f);

            } catch (FileNotFoundException ex) {
                Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
            } finally {

                try {
                    objout.close();
                } catch (IOException ex) {
                    Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                    outstream.close();
                } catch (IOException ex) {
                    Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
    }
    javax.swing.JLabel[] paramLabels;
    javax.swing.JTextField[] paramFields;
    javax.swing.JCheckBox[] paramCheckBoxes;

    private void updateTrainingPanelForParams() {
        int height = 4;
        if (myNumParams > 4) {
            height = myNumParams;
        }
        //  GridLayout experimentLayout = new GridLayout(myNumParams, 1);
        BoxLayout layout = new BoxLayout(panelRealTraining, BoxLayout.Y_AXIS);

        panelRealTraining.setLayout(layout);

        panelRealTraining.removeAll();
        paramLabels = new javax.swing.JLabel[myNumParams];
        paramFields = new javax.swing.JTextField[myNumParams];
        paramCheckBoxes = new javax.swing.JCheckBox[myNumParams];
        for (int i = 0; i < myNumParams; i++) {
            javax.swing.JPanel next = new javax.swing.JPanel();
            BoxLayout layout2 = new BoxLayout(next, BoxLayout.X_AXIS);
            next.setLayout(layout2);
            paramLabels[i] = new javax.swing.JLabel("Parameter " + i);
            paramFields[i] = new javax.swing.JTextField(5);
            paramFields[i].setText("0");
            Dimension d = new Dimension(100, 20);
            paramLabels[i].setMaximumSize(d);
            paramFields[i].setMaximumSize(d);
            // panelRealTraining.add(paramLabels[i]);
            // panelRealTraining.add(paramFields[i]);
            paramCheckBoxes[i] = new javax.swing.JCheckBox();
            paramCheckBoxes[i].getModel().setSelected(true);
            paramCheckBoxes[i].addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    paramCheckBoxesActionPerformed(evt);
                }
            });
            next.add(paramCheckBoxes[i]);
            next.add(paramLabels[i]);
            next.add(paramFields[i]);
            panelRealTraining.add(next);

        }

        panelRealTraining.repaint();
        scrollTrainPanel.repaint();
    }

    private void paramCheckBoxesActionPerformed(ActionEvent evt) {
        System.out.println("check change");
        boolean useParams[] = new boolean[myNumParams];
        for (int i = 0; i < myNumParams; i++) {
            useParams[i] = paramCheckBoxes[i].getModel().isSelected();
        }
        w.setUseParams(useParams);

    }

    public void displayFeatureManager() {
        checkAudio.setSelected(fm.useAudio);
        checkFFT.setSelected(fm.useFFT);
        checkCentroid.setSelected(fm.useCentroid);
        checkFlux.setSelected(fm.useFlux);
        checkRMS.setSelected(fm.useRMS);
        checkRolloff.setSelected(fm.useRolloff);
        // checkCustom.setSelected(false);
        textFFTSize.setText(Integer.toString(fm.getFFTSize()));
        textWindowSize.setText(Integer.toString(fm.getWindowSize()));
        switch (fm.windowType) {
            case Hamming:
                comboWindowType.setSelectedIndex(0);
                break;
            case Hann:
                comboWindowType.setSelectedIndex(1);
                break;
            case Rectangular:
            default:
                comboWindowType.setSelectedIndex(2);

        }
        textAudioRate.setText(Integer.toString(fm.getAudioExtractionRate()));
        checkTrackpad.setSelected(fm.useTrackpad);
        checkMotionSensor.setSelected(fm.useMotionSensor);
        textMotionRate.setText(Integer.toString(fm.getMotionExtractionRate()));
        checkOtherHID.setSelected(fm.useOtherHid);
        //   displayHidSettings(); //TODO: Decide how to deal with this
        //Maybe hidsetup is part of the featuremanager, and not part of wekinatorinstance...
        checkProcessing.setSelected(fm.useProcessing);
        if (fm.useProcessing) {
            switch (fm.getProcessingOption()) {
                case DOWNSAMPLED_100:
                    buttonGroupProcessingSource.setSelected(radioDownsampled.getModel(), true);
                    break;
                case COLOR_6:
                default:
                    buttonGroupProcessingSource.setSelected(radioColorTracking.getModel(), true);
                    break;

            }
        } else {
            buttonGroupProcessingSource.setSelected(radioClearProcessingFeature.getModel(), true);
        }
        checkCustomChuck.setSelected(fm.useCustomChuck);
        checkCustomOsc.setSelected(fm.useCustomOsc);
        textNumCustomChuck.setText(Integer.toString(fm.numCustomChuck));
        textNumOsc.setText(Integer.toString(fm.numCustomOsc));
    }

    public void setFeatureManager() {
        fm.useAudio = checkAudio.isSelected();
        fm.useFFT = checkFFT.isSelected();
        fm.useCentroid = checkCentroid.isSelected();
        fm.useFlux = checkFlux.isSelected();
        fm.useRMS = checkRMS.isSelected();
        fm.useRolloff = checkRolloff.isSelected();
        fm.useCustomChuck = checkCustomChuck.isSelected();
        fm.useCustomOsc = checkCustomOsc.isSelected();

        try {
            fm.numCustomChuck = Integer.parseInt(textNumCustomChuck.getText());
        } catch (NumberFormatException ex) {
            fm.useCustomChuck = false;
            System.out.println("ERROR: Can't use custom chucK: invalid # features!");
        }
        try {
            fm.numCustomOsc = Integer.parseInt(textNumOsc.getText());
        } catch (NumberFormatException ex) {
            fm.useCustomOsc = false;
            System.out.println("ERROR: Can't use custom osc: invalid # features!");
        }

        try {
            fm.setFFTandWindowSize(Integer.parseInt(textFFTSize.getText()), Integer.parseInt(textWindowSize.getText()));
        } catch (NumberFormatException ex) {
            fm.setFFTandWindowSize(512, 256);
            System.out.println("overrode fft and window size");
        }
        int i = comboWindowType.getSelectedIndex();
        if (i == 0) {
            fm.windowType = FeatureManager.WindowTypes.Hamming;
        } else if (i == 1) {
            fm.windowType = FeatureManager.WindowTypes.Hann;
        } else {
            fm.windowType = FeatureManager.WindowTypes.Rectangular;
        }
        try {
            fm.setAudioExtractionRate(Integer.parseInt(textAudioRate.getText()));
        } catch (NumberFormatException ex) {
            fm.setAudioExtractionRate(100);
            System.out.println("overrode feature extraction rate");
        }
        fm.useTrackpad = checkTrackpad.isSelected();
        fm.useMotionSensor = checkMotionSensor.isSelected();
        try {
            fm.setMotionExtractionRate(Integer.parseInt(textMotionRate.getText()));
        } catch (NumberFormatException ex) {
            fm.setMotionExtractionRate(100);
            System.out.println("overrode feature extraction rate");
        }
        fm.useOtherHid = checkOtherHID.isSelected();
        if (fm.useOtherHid) {
            fm.hidSetup = wek.getCurrentHidSetup(); //TODO: delete this
        }
        fm.useProcessing = checkProcessing.isSelected();
        if (fm.useProcessing) {
            if (radioDownsampled.isSelected()) {
                fm.setProcessingOption(FeatureManager.ProcessingOptions.DOWNSAMPLED_100, 100);
            } else if (radioColorTracking.isSelected()) {
                fm.setProcessingOption(FeatureManager.ProcessingOptions.COLOR_6, 6);
            } else {
                fm.useProcessing = false;
                buttonGroupProcessingSource.setSelected(radioClearProcessingFeature.getModel(), true);
                checkProcessing.setSelected(false);
            }
        }

    }

private void toggleButtonRunActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toggleButtonRunActionPerformed
//GEN-LAST:event_toggleButtonRunActionPerformed
    }

private void toggleButtonRunStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_toggleButtonRunStateChanged
}//GEN-LAST:event_toggleButtonRunStateChanged

private void toggleButtonRunItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_toggleButtonRunItemStateChanged
    if (toggleButtonRun.getModel().isSelected()) {
        w.startSound();
        w.startRun();
    } else {
        w.stopSound();
        w.stopRecording();
    }
}//GEN-LAST:event_toggleButtonRunItemStateChanged

private void buttonSaveClassifierActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSaveClassifierActionPerformed

    saveTrainedModel();


}//GEN-LAST:event_buttonSaveClassifierActionPerformed

    private void saveTrainedModel() {
        JFileChooser fc = new OverwritePromptingFileChooser();

        fc.setDialogType(JFileChooser.SAVE_DIALOG);
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);

        String location = wek.getSettings().getLastClassifierFileLocation();
        if (location == null || location.equals("")) {
            location = wek.getSettings().getDefaultClassifierFileLocation();
        }
        fc.setCurrentDirectory(new File(location));

        int returnVal = fc.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            FileOutputStream outstream = null;
            {
                ObjectOutputStream objout = null;
                try {
                    File file = fc.getSelectedFile();
                    outstream = new FileOutputStream(file);
                    objout = new ObjectOutputStream(outstream);
                    w.writeOut(objout);
                    wek.getSettings().setLastClassifierFileLocation(file.getCanonicalPath());

                } catch (FileNotFoundException ex) {
                    Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    System.out.println("error writing to file");
                    Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
                } finally {

                    try {
                        objout.close();
                    } catch (IOException ex) {
                        Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    try {
                        outstream.close();
                    } catch (IOException ex) {
                        Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }



        } else if (returnVal == JFileChooser.ERROR_OPTION) {
            throw new Error("huh?");
        }
    }

private void buttonHoldTrainActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonHoldTrainActionPerformed
    //should work more or less like other buttons
    //collect features while holding; stop when release.
}//GEN-LAST:event_buttonHoldTrainActionPerformed

private void buttonHoldTrainStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_buttonHoldTrainStateChanged
    JButton p = (JButton) (evt.getSource());
    //kind of hacky error detection!
    System.out.println("hold train state changed");
    if (p.getModel().isPressed()) {
        float[] f = new float[myNumParams];
        boolean bad = false;
        for (int i = 0; i < myNumParams; i++) {
            try {
                f[i] = Float.parseFloat(paramFields[i].getText());
                //System.out.println("got val " + f[i]);
                if (isChuckDiscrete && (f[i] < 0 || f[i] >= numChuckClasses)) {
                    System.out.println("Error: bad input!");
                    paramFields[i].setText("0");
                    bad = true;
                }
                if (isChuckDiscrete) {
                    f[i] = (float) (int) f[i];
                    paramFields[i].setText(Integer.toString((int) f[i]));
                }
            } catch (NumberFormatException ex) {
                paramFields[i].setText("0");
                f[i] = 0;
            }
        }
        if (!bad) {
            w.setRealVals(f);
            System.out.println("AH");
            w.startRecordFeatures();
        }
    } else {
        System.out.println("stopping recording");
        w.stopRecording();
    }

}//GEN-LAST:event_buttonHoldTrainStateChanged
    private int myNumParams;
    private int myNumFeats;

    private boolean loadSettings() {
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        boolean success = true;
        int returnVal = fc.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            FileInputStream fin = null;
            ObjectInputStream sin = null;
            try {


                File file = fc.getSelectedFile();
                classifierFilename = file.getName();
                fin = new FileInputStream(file);
                sin = new ObjectInputStream(fin);
                myNumParams = sin.readInt();
                myNumFeats = sin.readInt();
                System.out.println("Num p " + myNumParams + " f " + myNumFeats);
            } catch (FileNotFoundException ex) {
                success = false;
                Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                success = false;
                Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    fin.close();
                } catch (Exception ex) {
                }
                try {
                    sin.close();
                } catch (Exception ex) {
                }
            }


        } else {
            success = false;
        }


        return success;
    }
    private File originalClassifierFile = null;

    private boolean loadClassifierFromOldFile() {
        FileInputStream fin = null;
        boolean success = false;
        ObjectInputStream sin = null;
        try {

            File file = originalClassifierFile;
            fin = new FileInputStream(file);
            sin = new ObjectInputStream(fin);
            w.readIn(sin);
            if (w.getClassifierType() != WekaOperator.ClassifierType.NN) {
                labelClassifierStatus.setText("Not a multilayer perceptron file");
                success = false;
            } else {
                labelClassifierStatus.setText("Loaded classifier from " + classifierFilename);
            }
        } catch (EOFException ex) {
            success = false;
            labelClassifierStatus.setText("Could not read from this file");
        } catch (ClassNotFoundException ex) {
            success = false;
            labelClassifierStatus.setText("No class found");
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            labelClassifierStatus.setText("No file found");
            success = false;

            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            labelClassifierStatus.setText("Could not read from file");
            success = false;

            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                sin.close();
            } catch (IOException ex) {

                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            }
            try {
                fin.close();
            } catch (IOException ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            }

        }


        if (!success) {
            System.out.println("fail");
            w.chooseClassifier(WekaOperator.ClassifierType.NONE);
            labelClassifierStatus.setText("Problem with reading from file");
        }
        return success;

    }

    private boolean loadModelsFromFile() {
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        String location = wek.getSettings().getLastClassifierFileLocation();
        if (location == null || location.equals("")) {
            location = wek.getSettings().getDefaultClassifierFileLocation();
        }

        fc.setCurrentDirectory(new File(location));
        boolean success = true;
        File file = null;
        int returnVal = fc.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            FileInputStream fin = null;

            ObjectInputStream sin = null;
            try {
                file = fc.getSelectedFile();
                originalClassifierFile = file;

                classifierFilename = file.getName();
                fin = new FileInputStream(file);
                sin = new ObjectInputStream(fin);
                w.readIn(sin);

                if (w.numFeaturesToExpect != fm.getNumFeatures()) {
                    labelClassifierStatus.setText("Error: Number of loaded model features does not match # features in file");
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, "Error: Number of loaded model features does not match # features in file");

                    success = false;
                }

                //if (w.getClassifierType() != WekaOperator.ClassifierType.NN) {
                //    labelClassifierStatus.setText("Not a multilayer perceptron file");
                //    success = false;
                //} else {
                labelClassifierStatus.setText("Loaded classifier from " + classifierFilename);
            //}
            } catch (EOFException ex) {
                success = false;
                labelClassifierStatus.setText("Could not read from this file");
            } catch (ClassNotFoundException ex) {
                success = false;
                labelClassifierStatus.setText("No class found");
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            } catch (FileNotFoundException ex) {
                labelClassifierStatus.setText("No file found");
                success = false;

                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                labelClassifierStatus.setText("Could not read from file");
                success = false;

                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    sin.close();
                } catch (IOException ex) {

                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                }
                try {
                    fin.close();
                } catch (IOException ex) {
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                }

            }

        } else {
            success = false;
        }
        if (!success) {
            System.out.println("fail");
            w.chooseClassifier(WekaOperator.ClassifierType.NONE);
            labelClassifierStatus.setText("Problem with reading from file");
        } else {
            try {
                wek.getSettings().setLastClassifierFileLocation(file.getCanonicalPath());
            } catch (Exception ex) {
                wek.getSettings().setLastClassifierFileLocation(file.getAbsolutePath());

            }
        }
        return success;
    }
    private String classifierFilename;
private void buttonLoadSavedClassifierActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonLoadSavedClassifierActionPerformed
    //System.out.println("Load");
    boolean success = loadModelsFromFile();
    if (!success) {
        buttonGroupClassifierSource.setSelected(buttonCreateNewClassifier.getModel(), true);
    //buttonSaveSettings.setEnabled(false);
    //buttonGroupSettingsSource.setSelected(buttonClearSettings.getModel(), true);
    // buttonUseClassifierSettings.setEnabled(false);

    } else {
        //TODO: update combo box to reflect classifier choice.
        allowNewClassifier(false);
        textNumParams.setText(Integer.toString(w.getNumParameters()));
        textNumFeatures.setText(Integer.toString(w.numFeaturesToExpect));
        featureParameterMaskEditor.setMapping(new FeatureToParameterMapping(w.dataset));
    //  buttonSaveSettings.setEnabled(true);
    //   buttonUseClassifierSettings.setEnabled(true);
    //  buttonGroupSettingsSource.setSelected(buttonClearSettings.getModel(), true);
    //   buttonSaveSettings.setEnabled(true);
    //   buttonUseClassifierSettings.setEnabled(true);
    //  buttonUseClassifierSettings.setEnabled(true);

    }
}//GEN-LAST:event_buttonLoadSavedClassifierActionPerformed

private void buttonCreateNewClassifierActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonCreateNewClassifierActionPerformed
    allowNewClassifier(true);
//   buttonUseClassifierSettings.setEnabled(true);


}//GEN-LAST:event_buttonCreateNewClassifierActionPerformed

private void checkViewNNGUIActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkViewNNGUIActionPerformed
    boolean useGUI = checkViewNNGUI.isSelected();
    w.setNNUseGUI(useGUI);

}//GEN-LAST:event_checkViewNNGUIActionPerformed

private void buttonListenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonListenActionPerformed
    float[] f = new float[myNumParams];
    for (int i = 0; i < myNumParams; i++) {

        try {
            f[i] = Float.parseFloat(paramFields[i].getText());

        } catch (NumberFormatException ex) {
            paramFields[i].setText("0");
            f[i] = 0;
        }
    //   System.out.println("got val " + f[i]);
    }
    listenToValues(f);
}//GEN-LAST:event_buttonListenActionPerformed

    public void listenToValues(float[] params) {
        w.startSound();
        w.setRealVals(params);
        w.sendCurrentRealVals();
    }

    public float getCurrentParamValue(int paramNum) {
        //TODO: error check
        float f;
        if (paramNum < 0 || paramNum >= myNumParams) {
            f = 0;
        }
        try {
            f = Float.parseFloat(paramFields[paramNum].getText());

        } catch (NumberFormatException ex) {
            f = 0;
        }
        return f;
    }

private void buttonListenStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_buttonListenStateChanged
// TODO add your handling code here:
}//GEN-LAST:event_buttonListenStateChanged

private void buttonSaveFeatureSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSaveFeatureSettingsActionPerformed
    File f = findFeatureSetupFileToSave();
    if (f != null) {
        setFeatureManager();
        fm.saveSettingsToFile(f);
        System.out.println("Saved settings to file");

        try {
            wek.getSettings().setLastFeatureFileLocation(f.getCanonicalPath());
        } catch (Exception ex) {
            wek.getSettings().setLastFeatureFileLocation(f.getAbsolutePath());

        }
    }

}//GEN-LAST:event_buttonSaveFeatureSettingsActionPerformed

    public File findFeatureSetupFileToSave() {
        JFileChooser fc = new OverwritePromptingFileChooser();
        fc.setDialogType(JFileChooser.SAVE_DIALOG);
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        String location = wek.getSettings().getLastFeatureFileLocation();
        if (location == null || location.equals("")) {
            location = wek.getSettings().getDefaultFeatureFileLocation();
        }
        fc.setCurrentDirectory(new File(location));

        File file = null;

        int returnVal = fc.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {

            file = fc.getSelectedFile();
            try {
                wek.getSettings().setLastFeatureFileLocation(file.getCanonicalPath());
            } catch (IOException ex) {
                wek.getSettings().setLastFeatureFileLocation(file.getAbsolutePath());
            }
        }
        return file;

    }

private void buttonFeaturesGoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonFeaturesGoActionPerformed
    try {
        panelMainTabs.setEnabledAt(panelMainTabs.indexOfComponent(panelClassifier), false);
        disableRunPanel();
        disablePlayalongPanel();
        buttonFeaturesGo.setEnabled(false);
        setFeatureManager(); //TODO TODO TODO :
        fm.sendToChuck(w.Handler()); //This does: ?
        textNumFeatures.setText(Integer.toString(fm.getNumFeatures()));
        w.requestChuckSettings(); //Add
        System.out.println("Waiting for chuck to send settings");
    } catch (Exception ex) {
        System.out.println("Error sending to chuck!");
    }
}//GEN-LAST:event_buttonFeaturesGoActionPerformed

private void checkFFTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkFFTActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_checkFFTActionPerformed

private void checkCentroidActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkCentroidActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_checkCentroidActionPerformed

private void checkFluxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkFluxActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_checkFluxActionPerformed

private void checkRMSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkRMSActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_checkRMSActionPerformed

private void checkRolloffActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkRolloffActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_checkRolloffActionPerformed

private void checkAudioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkAudioActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_checkAudioActionPerformed

private void buttonLoadFeatureSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonLoadFeatureSettingsActionPerformed
    File f = findFeatureSetupFile();
    boolean success = false;
    if (f != null) {
        fm.readSettingsFromFile(f);
        if (fm.useOtherHid) {
            try {
                wek.getCurrentHidSetup().startHidInit();
            } catch (IOException ex) {
                Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        displayFeatureManager();
        System.out.println("loaded feature settings");
    }
}//GEN-LAST:event_buttonLoadFeatureSettingsActionPerformed

private void checkCustomOscActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkCustomOscActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_checkCustomOscActionPerformed

private void textNumCustomChuckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textNumCustomChuckActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_textNumCustomChuckActionPerformed

private void checkCustomChuckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkCustomChuckActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_checkCustomChuckActionPerformed

private void buttonEditClassifierActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonEditClassifierActionPerformed
    if (w.myClassifierType == WekaOperator.ClassifierType.KNN) {
        KnnSettingsOld j = new KnnSettingsOld(w);
        j.setVisible(true);
    } else if (w.myClassifierType == WekaOperator.ClassifierType.ADABOOST) {
        AdaboostSettings j = new AdaboostSettings(w);
        j.setVisible(true);
    }
}//GEN-LAST:event_buttonEditClassifierActionPerformed

private void buttonEditClassifierStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_buttonEditClassifierStateChanged
// TODO add your handling code here:
}//GEN-LAST:event_buttonEditClassifierStateChanged

private void comboClassifierTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboClassifierTypeActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_comboClassifierTypeActionPerformed

private void buttonComputeAccuracyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonComputeAccuracyActionPerformed
    ComputeAccuracy c = new ComputeAccuracy(w);
    c.setVisible(true);
}//GEN-LAST:event_buttonComputeAccuracyActionPerformed

private void radioDownsampledActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioDownsampledActionPerformed
    // TODO add your handling code here:
}//GEN-LAST:event_radioDownsampledActionPerformed

private void buttonSaveClassifier1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSaveClassifier1ActionPerformed
    saveTrainedModel();
}//GEN-LAST:event_buttonSaveClassifier1ActionPerformed

private void buttonForget1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonForget1ActionPerformed
    w.clear();
}//GEN-LAST:event_buttonForget1ActionPerformed

private void buttonTrain1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonTrain1ActionPerformed
    attemptTrainingNow();

}//GEN-LAST:event_buttonTrain1ActionPerformed

    private void attemptTrainingNow() {
        if (currentWorker == null) {
            System.out.println("Attempting to train; no current worker; doing it now");
            MyWorker worker = new MyWorker();
            doWorker(worker);
        } else {
            System.out.println("Adding new worker to queue; current worker already busy");
            MyWorker worker = new MyWorker();
            trainQueue.add(worker);
        }
    }

    private void updateProgress(int p) {
        int c = w.numParametersToTrain;
        //Our progress bar will be out of c + 2
        if (p == -1 || p == 0) {
            //We're stopped
            jProgressBarTrain.setValue(0);
            jLabelPlayalongProgress.setText(""); //TODO: use this
        } else if (p == 1) {
            jProgressBarTrain.setValue((int) (1.0 / (c + 2) * 100));
            jLabelPlayalongProgress.setText("Training...");
        } else if (p < c + 2) {
            //meaning & value contingent on # of classes.
            String s = "Cross-validating model " + (p - 1) + " of " + c;
            jLabelPlayalongProgress.setText(s);
            jProgressBarTrain.setValue((int) ((float) p / (c + 2) * 100));

        } else {
            String s = "Done training";
            jLabelPlayalongProgress.setText(s);
            jProgressBarTrain.setValue(100);
        }
    }

    private void doWorker(MyWorker worker) {
        currentWorker = worker;

        worker.addPropertyChangeListener(
                new PropertyChangeListener() {

                    public void propertyChange(PropertyChangeEvent evt) {
                        if ("progress".equals(evt.getPropertyName())) {
                            //  progressBar.setValue((Integer)evt.getNewValue());
                            // System.out.println("Got progress: " + (Integer)evt.getNewValue());
                            updateProgress((Integer) evt.getNewValue());
                        }
                    }
                });


        worker.execute();
    }

private void jToggleButtonPlayScoreItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButtonPlayScoreItemStateChanged
    if (!checkOtfPlayalong.isSelected()) {
        if (jToggleButtonPlayScore.getModel().isSelected()) {
            w.playScore();
        } else {
            if (jToggleButtonPlayAlong.getModel().isSelected()) {
                jToggleButtonPlayAlong.getModel().setSelected(false);
            }
            w.stopPlaying();
        }
    } else {

        if (jToggleButtonPlayScore.getModel().isSelected()) {
            score.play();
        } else {
            if (jToggleButtonPlayAlong.getModel().isSelected()) {
                jToggleButtonPlayAlong.getModel().setSelected(false);
            }
            score.stop();
        }
    }
}//GEN-LAST:event_jToggleButtonPlayScoreItemStateChanged

private void jToggleButtonPlayScoreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButtonPlayScoreActionPerformed
    // TODO add your handling code here:
}//GEN-LAST:event_jToggleButtonPlayScoreActionPerformed

private void jToggleButtonPlayScoreStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jToggleButtonPlayScoreStateChanged
    // TODO add your handling code here:
}//GEN-LAST:event_jToggleButtonPlayScoreStateChanged

private void jToggleButtonPlayAlongItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleButtonPlayAlongItemStateChanged

    if (jToggleButtonPlayAlong.getModel().isSelected()) {
        if (!jToggleButtonPlayScore.getModel().isSelected()) {
            jToggleButtonPlayScore.getModel().setSelected(true);
        }
        w.startPlayAlong();
    } else {
        //System.out.println("Should be false now");
        // jToggleButtonPlayScore.getModel().setSelected(false);
        w.stopPlayAlong();
    }
    if (!jToggleButtonPlayScore.getModel().isSelected()) {
        displayPlayalongUpdate("  ");
    }
}//GEN-LAST:event_jToggleButtonPlayAlongItemStateChanged

private void jToggleButtonPlayAlongActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButtonPlayAlongActionPerformed
    // TODO add your handling code here:
}//GEN-LAST:event_jToggleButtonPlayAlongActionPerformed

private void jToggleButtonPlayAlongStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jToggleButtonPlayAlongStateChanged
    // TODO add your handling code here:
}//GEN-LAST:event_jToggleButtonPlayAlongStateChanged

private void panelMainTabsComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_panelMainTabsComponentShown
    System.out.println("Component shown");

}//GEN-LAST:event_panelMainTabsComponentShown

private void panelMainTabsStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_panelMainTabsStateChanged
    //  System.out.println(jTabbedPane1.getModel().getSelectedIndex());
    if (panelMainTabs.getModel().getSelectedIndex() == 4) {
        w.isPlayAlong = true;
    } else if (panelMainTabs.getModel().getSelectedIndex() == 3) {
        w.isPlayAlong = false;
    }
}//GEN-LAST:event_panelMainTabsStateChanged

private void jSliderTrainingFrequencyStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSliderTrainingFrequencyStateChanged
    // System.out.println("Slider state changed");
    trainingFrequency = jSliderTrainingFrequency.getModel().getValue();
    //TODO: Adjust timer, if running...
    if (isAutoTrain) {
        timer.purge();
        TimerTask task = new TimerTask() {

            public void run() {
                attemptTrainingNow();
            }
        };
        timer.scheduleAtFixedRate(task, trainingFrequency * 1000, trainingFrequency * 1000);
    }

//See http://java.sun.com/j2se/1.4.2/docs/api/java/util/Timer.html

}//GEN-LAST:event_jSliderTrainingFrequencyStateChanged

private void jCheckBoxAutomaticTrainingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxAutomaticTrainingActionPerformed
    if (jCheckBoxAutomaticTraining.getModel().isSelected()) {
        trainingFrequency = jSliderTrainingFrequency.getModel().getValue();
        System.out.println("Setting timer for ");
        timer.purge();
        TimerTask task = new TimerTask() {

            public void run() {
                attemptTrainingNow();
            //System.out.println("YO");
            }
        };
        timer.scheduleAtFixedRate(task, trainingFrequency * 1000, trainingFrequency * 1000);

    } else {
        System.out.println("Cancelling timer");
        timer.cancel();
        timer = new Timer();
        trainQueue.clear(); //clear training queue
    }

}//GEN-LAST:event_jCheckBoxAutomaticTrainingActionPerformed

private void jComboNumFoldsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboNumFoldsActionPerformed
    String s = (String) jComboNumFolds.getModel().getSelectedItem();
    numFolds = Integer.parseInt(s);

}//GEN-LAST:event_jComboNumFoldsActionPerformed

private void jCheckBoxAutoStopThresholdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxAutoStopThresholdActionPerformed
    if (jCheckBoxAutoStopThreshold.getModel().isSelected()) {
        isAutoTrainStopThreshold = true;
        autoTrainStopThreshold = Double.parseDouble(jTextAutoStopThreshold.getText());

    } else {
        isAutoTrainStopThreshold = false;
    }

}//GEN-LAST:event_jCheckBoxAutoStopThresholdActionPerformed

private void jTextAutoStopThresholdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextAutoStopThresholdActionPerformed
    autoTrainStopThreshold = Double.parseDouble(jTextAutoStopThreshold.getText());
}//GEN-LAST:event_jTextAutoStopThresholdActionPerformed

private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
    autoTrainStopThreshold = Double.parseDouble(jTextAutoStopThreshold.getText());
}//GEN-LAST:event_jButton1ActionPerformed

private void jToggleButtonRunPlayalongActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButtonRunPlayalongActionPerformed
    if (jToggleButtonRunPlayalong.getModel().isSelected()) {
        w.startRun(); //TODO: make sure that other run buttons' state is synched to this one
    } else {
        w.stopRecording();
    }
}//GEN-LAST:event_jToggleButtonRunPlayalongActionPerformed

private void jSliderFastAccurateStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSliderFastAccurateStateChanged
    setFastAccurate(jSliderFastAccurate.getModel().getValue());
}//GEN-LAST:event_jSliderFastAccurateStateChanged

private void jButtonShhActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonShhActionPerformed
    w.stopSound();
}//GEN-LAST:event_jButtonShhActionPerformed

private void jCheckFastAccurateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckFastAccurateActionPerformed
    if (jCheckFastAccurate.getModel().isSelected()) {
        jSliderFastAccurate.setEnabled(true);
        fastAccurateValue = jSliderFastAccurate.getModel().getValue();
        setFastAccurate(fastAccurateValue);
    } else {
        jSliderFastAccurate.setEnabled(false);
    }

}//GEN-LAST:event_jCheckFastAccurateActionPerformed

private void panelRunKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_panelRunKeyPressed
}//GEN-LAST:event_panelRunKeyPressed

private void toggleGetSynthParamsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toggleGetSynthParamsActionPerformed
    // TODO add your handling code here:
}//GEN-LAST:event_toggleGetSynthParamsActionPerformed

private void toggleGetSynthParamsItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_toggleGetSynthParamsItemStateChanged
    if (toggleGetSynthParams.getModel().isSelected()) {
        try {
            w.Handler().startGettingParams();
        } catch (IOException ex) {
            Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    } else {
        try {
            w.Handler().stopGettingParams();
        } catch (IOException ex) {
            Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}//GEN-LAST:event_toggleGetSynthParamsItemStateChanged

private void buttonViewDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonViewDataActionPerformed
    if (w.dataset != null) {
        DataViewer v = new DataViewer(w.dataset, this);
        v.setVisible(true);
    }
}//GEN-LAST:event_buttonViewDataActionPerformed

private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
    System.exit(0);
}//GEN-LAST:event_exitMenuItemActionPerformed

private void menuItemViewConsoleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemViewConsoleActionPerformed
    Console c = Console.getInstance();
    if (c.isVisible()) {
        c.toFront();
    } else {
        c.setVisible(true);
    }
}//GEN-LAST:event_menuItemViewConsoleActionPerformed

private void aboutMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuItem1ActionPerformed
    //SHow something about wekinator TODO
}//GEN-LAST:event_aboutMenuItem1ActionPerformed

private void buttonSetupOtherHidActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSetupOtherHidActionPerformed
    HidSetupForm p = new HidSetupForm();
    p.setVisible(true);
}//GEN-LAST:event_buttonSetupOtherHidActionPerformed

private void buttonViewOtfPlayalongActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonViewOtfPlayalongActionPerformed
    scoreViewer.setVisible(true);
    scoreViewer.toFront();
}//GEN-LAST:event_buttonViewOtfPlayalongActionPerformed

private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
    double[] f = new double[myNumParams];
    for (int i = 0; i < myNumParams; i++) {

        try {
            f[i] = Double.parseDouble(paramFields[i].getText());

        } catch (NumberFormatException ex) {
            paramFields[i].setText("0");
            f[i] = 0;
        }
    //   System.out.println("got val " + f[i]);
    }
    score.addParams(f, 1.0);
}//GEN-LAST:event_jButton2ActionPerformed

    private File findHidSetupFileToSave() {

        JFileChooser fc = new OverwritePromptingFileChooser();
        fc.setDialogType(JFileChooser.SAVE_DIALOG);
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        File file = null;

        int returnVal = fc.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {

            file = fc.getSelectedFile();
        }
        if (file != null) {
            wek.getSettings().setLastHidFileLocation(Util.getCanonicalPath(file));
        }
        return file;
    }

    /*  private void displayHidSettings() {
    HidSetup hs = wek.getCurrentHidSetup();
    float a[] = hs.getInitAxes();
    int b[] = hs.getInitHats();
    int c[] = hs.getInitButtons();
    String aa = "";
    String bb = "";
    String cc = "";
    for (int i = 0; i < a.length; i++) {
    aa += Float.toString(a[i]) + " ";
    }
    for (int i = 0; i < b.length; i++) {
    bb += Integer.toString(b[i]) + " ";
    }
    for (int i = 0; i < c.length; i++) {
    cc += Integer.toString(c[i]) + " ";
    }

    // String s = hs.getNumAxes() + " axes: " + aa + " "
    // hs.getNumHats() + " hats: " + bb + " "
    // hs.getNumButtons() + " buttons: " + cc;


    String s = hs.getNumAxesTotal() + " axes, " + hs.getNumHatsTotal() + " hats," + hs.getNumButtonsTotal() + " buttons";
    //labelHIDDescription.setText(s);

    }*/
    private File findFeatureSetupFile() {
        JFileChooser fc = new JFileChooser();
        String location = wek.getSettings().getLastFeatureFileLocation();
        if (location == null || location.equals("")) {
            location = wek.getSettings().getDefaultFeatureFileLocation();
        }
        fc.setCurrentDirectory(new File(location)); //TODO: Could set directory vs file here according to above results
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        boolean success = true;
        File file = null;
        int returnVal = fc.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            file = fc.getSelectedFile();
        }

        if (file != null) {
            try {
                wek.getSettings().setLastFeatureFileLocation(file.getCanonicalPath());
            } catch (Exception ex) {
                wek.getSettings().setLastFeatureFileLocation(file.getAbsolutePath());

            }
        }
        return file;
    }

    private File findHidSetupFile() {
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        String location = wek.getSettings().getLastFeatureFileLocation();
        if (location == null || location.equals("")) {
            location = wek.getSettings().getDefaultFeatureFileLocation();
        }
        fc.setCurrentDirectory(new File(location)); //TODO: Could set directory vs file here according to above results

        boolean success = true;
        File file = null;
        int returnVal = fc.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            file = fc.getSelectedFile();
        }
        if (file != null) {
            wek.getSettings().setLastHidFileLocation(Util.getCanonicalPath(file));

        }
        return file;
    }

    private boolean validateSettingsInput() {
        System.out.println("validating settings");
        try {
            int a = Integer.parseInt(textNumFeatures.getText());
            int b = Integer.parseInt(textNumParams.getText());
            if (a > 0 && b > 0) {
                return true;
            }
        } catch (Exception ex) {
            return false;
        }
        return false;

    }

    public void displayReceivedRealValue(float d) {
        //   labelRealValue.setText(Float.toString(d));    
    }

    private void buttonTrainEvent(ChangeEvent evt, int i) {

        JButton p = (JButton) (evt.getSource());
        if (p.getModel().isPressed()) {
            System.out.println("starting recording1");
            w.startRecordFeatures(i);
        } else {
            System.out.println("stopping recording2");
            w.stopRecording();
        }
    }
    /*
     * My fields
     */
    private WekaOperator w;
    int sendPort, receivePort;

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                MainGUI b = new MainGUI();
                b.setVisible(true);

            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem1;
    private javax.swing.JButton buttonComputeAccuracy;
    private javax.swing.JRadioButton buttonCreateNewClassifier;
    private javax.swing.JButton buttonEditClassifier;
    private javax.swing.JButton buttonFeaturesGo;
    private javax.swing.JButton buttonForget;
    private javax.swing.JButton buttonForget1;
    private javax.swing.ButtonGroup buttonGroupClassifierSource;
    private javax.swing.ButtonGroup buttonGroupProcessingSource;
    private javax.swing.ButtonGroup buttonGroupSettingsSource;
    private javax.swing.JButton buttonHoldTrain;
    private javax.swing.JButton buttonListen;
    private javax.swing.JButton buttonLoadFeatureSettings;
    private javax.swing.JRadioButton buttonLoadSavedClassifier;
    private javax.swing.JButton buttonOscConnect;
    private javax.swing.JButton buttonOscDisconnect;
    private javax.swing.JButton buttonQuit;
    private javax.swing.JButton buttonSaveClassifier;
    private javax.swing.JButton buttonSaveClassifier1;
    private javax.swing.JButton buttonSaveFeatureSettings;
    private javax.swing.JButton buttonSetupOtherHid;
    private javax.swing.JButton buttonTrain;
    private javax.swing.JButton buttonTrain1;
    private javax.swing.JButton buttonUseClassifierSettings;
    private javax.swing.JButton buttonViewData;
    private javax.swing.JButton buttonViewOtfPlayalong;
    private javax.swing.JCheckBox checkAudio;
    private javax.swing.JCheckBox checkCentroid;
    private javax.swing.JCheckBox checkCustomChuck;
    private javax.swing.JCheckBox checkCustomOsc;
    private javax.swing.JCheckBox checkFFT;
    private javax.swing.JCheckBox checkFlux;
    private javax.swing.JCheckBox checkMotionSensor;
    private javax.swing.JCheckBox checkOtfPlayalong;
    private javax.swing.JCheckBox checkOtherHID;
    private javax.swing.JCheckBox checkProcessing;
    private javax.swing.JCheckBox checkRMS;
    private javax.swing.JCheckBox checkRolloff;
    private javax.swing.JCheckBox checkTrackpad;
    private javax.swing.JCheckBox checkViewNNGUI;
    private wekinator.ChuckRunnerPanel chuckRunnerPanel1;
    private javax.swing.JComboBox comboClassifierType;
    private javax.swing.JComboBox comboWindowType;
    private javax.swing.JMenuItem contentsMenuItem1;
    private javax.swing.JMenuItem exitMenuItem;
    private wekinator.FeatureConfigurationPanel featureConfigurationPanel1;
    private wekinator.FeatureParameterMaskEditor featureParameterMaskEditor;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenu helpMenu1;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButtonShh;
    private javax.swing.JCheckBox jCheckBoxAutoStopThreshold;
    private javax.swing.JCheckBox jCheckBoxAutomaticTraining;
    private javax.swing.JCheckBox jCheckFastAccurate;
    private javax.swing.JComboBox jComboNumFolds;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelPlayalongProgress;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JLabel jPlayAlongCVLabel;
    private javax.swing.JProgressBar jProgressBarTrain;
    private javax.swing.JSlider jSliderFastAccurate;
    private javax.swing.JSlider jSliderTrainingFrequency;
    private javax.swing.JTextField jTextAutoStopThreshold;
    private javax.swing.JToggleButton jToggleButtonPlayAlong;
    private javax.swing.JToggleButton jToggleButtonPlayScore;
    private javax.swing.JToggleButton jToggleButtonRunPlayalong;
    private javax.swing.JLabel jlabelNumInstances;
    private javax.swing.JLabel labelChuckSettings;
    private javax.swing.JLabel labelClassifiedClass;
    private javax.swing.JLabel labelClassifiedClass1;
    private javax.swing.JLabel labelClassifierStatus;
    private javax.swing.JLabel labelFeatureStatus;
    private javax.swing.JLabel labelGlobalStatus;
    private javax.swing.JLabel labelHidDescription;
    private javax.swing.JLabel labelNumFeatures;
    private javax.swing.JLabel labelNumParams;
    private javax.swing.JLabel labelOscStatus;
    private javax.swing.JLabel labelOscStatus1;
    private javax.swing.JLabel labelOscStatus3;
    private javax.swing.JLabel labelParameterValues;
    private javax.swing.JLabel labelParameterValues2;
    private javax.swing.JLabel labelPlayalongUpdate;
    private javax.swing.JLabel labelRunningStatus;
    private javax.swing.JLabel labelRunningStatus1;
    private javax.swing.JLabel labelTrainingStatus;
    private wekinator.LearningSystemConfigurationPanel learningSystemConfigurationPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem menuItemOtfScore;
    private javax.swing.JMenuItem menuItemViewConsole;
    private javax.swing.JPanel panelClassifier;
    private javax.swing.JPanel panelFeatures;
    private javax.swing.JTabbedPane panelMainTabs;
    private javax.swing.JPanel panelOSC;
    private javax.swing.JPanel panelPlayAlong;
    private javax.swing.JPanel panelRealTraining;
    private javax.swing.JPanel panelRun;
    private javax.swing.JPanel panelTabFeatureConfiguration;
    private javax.swing.JPanel panelTabLearningSystemConfiguration;
    private javax.swing.JMenuItem preferencesMenuItem;
    private javax.swing.JRadioButton radioClearProcessingFeature;
    private javax.swing.JRadioButton radioColorTracking;
    private javax.swing.JRadioButton radioDownsampled;
    private javax.swing.JScrollPane scrollTrainPanel;
    private javax.swing.JTextField textAudioRate;
    private javax.swing.JTextField textFFTSize;
    private javax.swing.JTextField textMotionRate;
    private javax.swing.JTextField textNumCustomChuck;
    private javax.swing.JTextField textNumFeatures;
    private javax.swing.JTextField textNumOsc;
    private javax.swing.JTextField textNumParams;
    private javax.swing.JTextField textOscReceive;
    private javax.swing.JTextField textOscSend;
    private javax.swing.JLabel textSettingsDiscrete;
    private javax.swing.JLabel textSettingsNumClasses;
    private javax.swing.JLabel textSettingsWantDist;
    private javax.swing.JTextField textWindowSize;
    private javax.swing.JToggleButton toggleButtonRun;
    private javax.swing.JToggleButton toggleGetSynthParams;
    private javax.swing.JMenu viewMenu;
    // End of variables declaration//GEN-END:variables
    private ArrayList<javax.swing.JButton> trainingButtons = new ArrayList<javax.swing.JButton>();

    public void enableFeaturePanel() {
        panelMainTabs.setEnabledAt(panelMainTabs.indexOfComponent(panelFeatures), true);
    }

    public void enableClassifierPanel() {
        panelMainTabs.setEnabledAt(panelMainTabs.indexOfComponent(panelClassifier), true);

    }

    public void disableFeaturePanel() {
        panelMainTabs.setEnabledAt(panelMainTabs.indexOfComponent(panelFeatures), false);
    }

    public void disableRunPanel() {
        panelMainTabs.setEnabledAt(panelMainTabs.indexOfComponent(panelRun), false);
    }

    public void enableRunPanel() {
        panelMainTabs.setEnabledAt(panelMainTabs.indexOfComponent(panelRun), true);


    }

    public void enablePlayalongPanel() {
        panelMainTabs.setEnabledAt(panelMainTabs.indexOfComponent(panelPlayAlong), true);

    }

    public void disablePlayalongPanel() {
        panelMainTabs.setEnabledAt(panelMainTabs.indexOfComponent(panelPlayAlong), false);
    }

    private void setFastAccurate(int value) {
        double d = value * .01; //Now have value between 0 and 1
        fastAccurateValue = value;
        w.setClassifierFastAccurate(d);
    }

    public void setNumParams(int n) {
        textNumParams.setText(Integer.toString(n));
    }

    public void update(Subject o, Object state, String updateString) {
        if (o == w) {
            //update from WekaOperator
            if (state instanceof WekaOperator.OperatorState) {
                WekaOperator.OperatorState s = (WekaOperator.OperatorState) state;
            //   labelRunningStatus.setText("Running status: " + updateString);




            } else if (state instanceof WekaOperator.ClassifierState) {
                labelTrainingStatus.setText("Training status: " + updateString);
                if (((WekaOperator.ClassifierState) state) == WekaOperator.ClassifierState.HAS_DATA) {
                    buttonTrain.setEnabled(true);
                    buttonTrain1.setEnabled(true);
                    //  toggleButtonRun.setEnabled(false);
                    buttonComputeAccuracy.setEnabled(false);
                } else if (((WekaOperator.ClassifierState) state) == WekaOperator.ClassifierState.TRAINED) {
                    buttonTrain.setEnabled(true);
                    buttonTrain1.setEnabled(true);

                    //  toggleButtonRun.setEnabled(true);
                    if (isChuckDiscrete) {
                        buttonComputeAccuracy.setEnabled(true);
                    }
                } else {
                    buttonTrain.setEnabled(false);
                    buttonTrain1.setEnabled(false);


                    //  toggleButtonRun.setEnabled(false);
                    buttonComputeAccuracy.setEnabled(false);
                }

            } else if (state instanceof WekaOperator.FeatureState) {
                labelFeatureStatus.setText("ChucK feature status: " + updateString);

            } else if (state instanceof WekaOperator.ClassifierType) {
                labelClassifierStatus.setText("Classifier status: " + updateString);

                if (((WekaOperator.ClassifierType) state) == WekaOperator.ClassifierType.NONE) {
                    panelMainTabs.setEnabledAt(panelMainTabs.indexOfComponent(panelRun), false);
                    panelMainTabs.setEnabledAt(panelMainTabs.indexOfComponent(panelPlayAlong), false);
                } else {
                    panelMainTabs.setEnabledAt(panelMainTabs.indexOfComponent(panelRun), true);
                    panelMainTabs.setEnabledAt(panelMainTabs.indexOfComponent(panelPlayAlong), true);
                }

            } else if (state == null) {
                labelGlobalStatus.setText("Error: " + updateString);
            }


        }
    /*else if (o == hs) {
    buttonSetupDone.setEnabled((HidSetup.HidState) state == HidSetup.HidState.SETUP_BEGUN);
    if (((HidSetup.HidState) state == HidSetup.HidState.INIT_DONE) || ((HidSetup.HidState) state == HidSetup.HidState.SETUP_STOPPED)) {
    buttonSaveHidFile.setEnabled(true);
    }

    if ((HidSetup.HidState) state == HidSetup.HidState.INIT_DONE) {
    System.out.println("init done!");
    labelHIDStatus.setText("Initialization done.");
    displayHidSettings();
    } else if ((HidSetup.HidState) state == HidSetup.HidState.INIT_REQUESTED) {
    labelHIDStatus.setText("Initialization requested.");
    } else if ((HidSetup.HidState) state == HidSetup.HidState.NONE) {
    labelHIDStatus.setText("No status.");
    //   } else if ((HidSetup.HidState) state == HidSetup.HidState.RUN_REQUESTED) {
    //       labelHIDStatus.setText("Run requested.");
    } else if ((HidSetup.HidState) state == HidSetup.HidState.SETTINGS_RECEIVED) {
    labelHIDStatus.setText("Settings received.");
    displayHidSettings();
    } else if ((HidSetup.HidState) state == HidSetup.HidState.SETTINGS_REQUESTED) {
    labelHIDStatus.setText("Settings requested.");
    } else if ((HidSetup.HidState) state == HidSetup.HidState.SETUP_BEGUN) {
    System.out.println("Setup begun seen");
    labelHIDStatus.setText("Setup begun. Engage HID and press button to stop.");
    buttonSetupDone.setEnabled(true);
    } else if ((HidSetup.HidState) state == HidSetup.HidState.SETUP_REQUESTED) {
    labelHIDStatus.setText("Setup requested.");
    } else if ((HidSetup.HidState) state == HidSetup.HidState.SETUP_STOPPED) {
    labelHIDStatus.setText("Setup done.");
    displayHidSettings();
    } else if ((HidSetup.HidState) state == HidSetup.HidState.SETUP_STOP_REQUESTED) {
    labelHIDStatus.setText("Setup stop requested.");
    }
    } */
    }

    public void displayNumInstances(int numInstances) {
        jlabelNumInstances.setText(Integer.toString(numInstances));
    }

    public void displayClassValueMulti(float[] vals, double[][] dists) {
        String s = "";
        DecimalFormat dd = new DecimalFormat("#.##");

        for (int i = 0; i < vals.length; i++) {
            //  System.out.println("for i = " + i);
            //  System.out.println("Class is " + vals[i]);
            s += "p" + dd.format(i) + ": ";
            for (int j = 0; j < dists[i].length; j++) {
                s += dd.format(dists[i][j]);
                if (j < dists[i].length - 1) {
                    s += "/";
                }
            }
            s += " ";
        }
        labelClassifiedClass.setText(s);
        labelClassifiedClass1.setText(s);
    // System.out.println("set label to" + s);
    }

    public void displayClassValueMulti(int[] vals, double[][] dists) {
        String s = "";
        DecimalFormat dd = new DecimalFormat("#.##");

        for (int i = 0; i < vals.length; i++) {
            //  System.out.println("for i = " + i);
            //  System.out.println("Class is " + vals[i]);
            s += "p" + i + ": ";
            for (int j = 0; j < dists[i].length; j++) {
                s += dd.format(dists[i][j]);
                if (j < dists[i].length - 1) {
                    s += "/";
                }
            }
            s += " ";
        }
        labelClassifiedClass.setText(s);
        labelClassifiedClass1.setText(s);
    }
    private boolean isChuckDiscrete = false;
    private int numChuckClasses = 0;
    private boolean isUseDistribution = false;
    private boolean areChuckSettingsReceived = false;

    public void setChuckSettings(int nParam, boolean useD, boolean discrete, int nc) {
        setNumParams(nParam);
        numChuckClasses = nc;
        isChuckDiscrete = discrete;
        isUseDistribution = useD;

        if (discrete) {
            textSettingsDiscrete.setText("Discrete classification");
            textSettingsNumClasses.setText(nc + " classes");
            if (useD) {
                textSettingsWantDist.setText("Probability distribution output");
            } else {
                textSettingsWantDist.setText("Class label output");
            }
            buttonEditClassifier.setVisible(true);
            comboClassifierType.setVisible(true);
            checkViewNNGUI.setVisible(false);
            buttonComputeAccuracy.setVisible(true);
            labelParameterValues.setText("Enter parameter values (integers between 0 and " + (nc - 1) + "):");

        } else {
            textSettingsDiscrete.setText("Neural network function learning");
            textSettingsWantDist.setText("");
            textSettingsNumClasses.setText("");
            buttonEditClassifier.setVisible(false);
            comboClassifierType.setVisible(false);
            checkViewNNGUI.setVisible(true);
            buttonComputeAccuracy.setVisible(false);
            labelParameterValues.setText("Enter parameter values (any real numbers)");
        }

        FeatureToParameterMapping newMapping = new FeatureToParameterMapping(fm, nParam);
        featureParameterMaskEditor.setMapping(newMapping);
        areChuckSettingsReceived = true;
        enableClassifierPanel();
        buttonFeaturesGo.setEnabled(true);
        //  disableFeaturePanel();
        panelMainTabs.setSelectedComponent(panelClassifier);
    }

    private void wekaOperatorPropertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(WekaOperator.PROP_TRAININGSTATE)) {
            if (w.getTrainingState() == WekaOperator.TrainingState.TRAINED) {
                toggleButtonRun.setEnabled(true);

            } else if (w.getTrainingState() == WekaOperator.TrainingState.TRAINING) {
                toggleButtonRun.setEnabled(false);

            } else { //Not trained
                toggleButtonRun.setEnabled(false);
            }


        }


    }

    private void oscHandlerPropertyChange(PropertyChangeEvent evt) {
        updateGUIforOscStatus();
    }

    protected void updateGUIforOscStatus() {
        OscHandler h = OscHandler.getOscHandler();
        labelOscStatus.setText("OSC status: " + h.getStatusMessage());

        if (h.getConnectionState() == OscHandler.ConnectionState.CONNECTED ||
                h.getConnectionState() == OscHandler.ConnectionState.CONNECTING) {
            buttonOscDisconnect.setEnabled(true);
            buttonOscConnect.setEnabled(false);


            if (h.getConnectionState() == OscHandler.ConnectionState.CONNECTED) {
                isConnected = true;
                enableTrainButtons();
                enableFeaturePanel();


                panelMainTabs.setSelectedComponent(panelFeatures);
            } else {
                isConnected = false;
            }

        } else {
            isConnected = false;
            disableTrainButtons();
            buttonOscDisconnect.setEnabled(false);
            buttonOscConnect.setEnabled(true);
        }

        setFeatureConfigurationPanelEnabled(isConnected);
        if (!isConnected) {
            setLearningSystemConfigurationPanelEnabled(false);
        }

    }

    //TODO: get rid of this.
    private void hidSetupPropertyChange(PropertyChangeEvent evt) {
        System.out.println("GUI RECVD HID SETUP change w/ name: " + evt.getPropertyName());
        labelHidDescription.setText(wek.getCurrentHidSetup().getDescription());
    }

    private void wekinatorInstancePropertyChangeEvent(PropertyChangeEvent evt) {
        // System.out.println("NEW HID SETUP");
        //  labelHidDescription.setText(wek.getCurrentHidSetup().getDescription());
        if (evt.getPropertyName().equals(WekinatorInstance.PROP_CURRENTHIDSETUP)) {
            ((HidSetup) evt.getOldValue()).removePropertyChangeListener(hidSetupChangeListener);
            ((HidSetup) evt.getNewValue()).addPropertyChangeListener(hidSetupChangeListener);
            labelHidDescription.setText(wek.getCurrentHidSetup().getDescription());

        }
    }

    /** To keep */
    //Possibly put in non-gui file... e.g. WekinatorManager
    private void chuckSystemPropertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(ChuckSystem.PROP_STATE)) {
            ChuckSystem cs = ChuckSystem.getChuckSystem();
            updateGUIforChuckSystem();
            // panelTabLearningSystemConfiguration.setEnabled(cs.getState() == ChuckSystem.ChuckSystemState.CONNECTED_AND_VALID);
            if (evt.getOldValue() != ChuckSystem.ChuckSystemState.CONNECTED_AND_VALID && evt.getNewValue() == ChuckSystem.ChuckSystemState.CONNECTED_AND_VALID) {
                learningSystemConfigurationPanel.configure(cs.getNumParams(), cs.getParamNames(), cs.isIsParamDiscrete(), WekinatorLearningManager.getInstance().getFeatureConfiguration());
                panelMainTabs.setSelectedComponent(panelTabLearningSystemConfiguration);
            }

        } else if (evt.getPropertyName().equals(ChuckSystem.PROP_NUMPARAMS)) {
            //Currently don't worry about this -- only happens as part of a chuck system total update.
        }
    }

    private void updateGUIforChuckSystem() {
        setLearningSystemConfigurationPanelEnabled(ChuckSystem.getChuckSystem().getState() == ChuckSystem.ChuckSystemState.CONNECTED_AND_VALID);
    }
}


