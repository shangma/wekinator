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
   //     labelPlayalongUpdate.setText(string);
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
          /*  try {
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
                      //  jCheckBoxAutomaticTraining.getModel().setSelected(false);
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
            } */
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
//        radioClearProcessingFeature.setVisible(false);
        int n = w.getNumClasses();

    /*    panelMainTabs.setEnabledAt(panelMainTabs.indexOfComponent(panelRun), false);
        panelMainTabs.setEnabledAt(panelMainTabs.indexOfComponent(panelFeatures), false);
        panelMainTabs.setEnabledAt(panelMainTabs.indexOfComponent(panelClassifier), false);
        panelMainTabs.setEnabledAt(panelMainTabs.indexOfComponent(panelPlayAlong), false); */

        // buttonPanic.setVisible(false);
        // buttonLoadFeatureSettings.setVisible(false);
        // buttonSaveFeatureSettings.setVisible(false);


        WekinatorLearningManager.getInstance().addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                learningManagerPropertyChange(evt);
            }
        });


        

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
    /*    checkCustomChuck.setSelected(wek.configuration.isCustomChuckFeatureExtractorEnabled());
        if (wek.configuration.isCustomChuckFeatureExtractorEnabled()) {
            textNumCustomChuck.setText(Integer.toString(wek.configuration.getNumCustomChuckFeaturesExtracted()));
        }

        checkCustomOsc.setSelected(wek.configuration.isOscFeatureExtractorEnabled());
        if (wek.configuration.isOscFeatureExtractorEnabled()) {
            textNumOsc.setText(Integer.toString(wek.configuration.getNumOSCFeaturesExtracted()));
        } */
    }

    public void displayClassValue(int val, double[] dist) {
        System.out.println("Class is " + val + "; P=");
        String s = Integer.toString(val) + ", P=";
        DecimalFormat dd = new DecimalFormat("#.##");
        for (int i = 0; i < dist.length; i++) {
            s += Integer.toString(i) + ":" + dd.format(dist[i]) + "/";
        }
//        labelClassifiedClass.setText(s);
      //  labelClassifiedClass1.setText(s);
    }

    public void displayClassValue(float rv) {
        DecimalFormat dd = new DecimalFormat("#.##");
        System.out.println("Class is " + dd.format(rv));
        String s = dd.format(rv);
      //  labelClassifiedClass.setText(s);
     //   labelClassifiedClass1.setText(s);
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
     //   labelClassifiedClass.setText(s);
     //   labelClassifiedClass1.setText(s);

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
       // labelClassifiedClass.setText(s);
      //  labelClassifiedClass1.setText(s);

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
      //  labelNumParams.setEnabled(enable);
      //  textNumFeatures.setEnabled(enable);
      //  labelNumFeatures.setEnabled(enable);

    }

    private void disableAllSettingsPanel() {
    //    panelMainTabs.setSelectedComponent(panelRun);
    //  jTabbedPane1.setEnabledAt(jTabbedPane1.indexOfComponent(panelClassifier), false);

    }

    private void enableRun() {
        //  toggleButtonRun.setEnabled(true);
        if (isChuckDiscrete) {
//            buttonComputeAccuracy.setEnabled(false);
        }
   //     panelMainTabs.setEnabledAt(panelMainTabs.indexOfComponent(panelRun), true);
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
        panelTabFeatureConfiguration = new javax.swing.JPanel();
        featureConfigurationPanel1 = new wekinator.FeatureConfigurationPanel();
        panelTabLearningSystemConfiguration = new javax.swing.JPanel();
        learningSystemConfigurationPanel = new wekinator.LearningSystemConfigurationPanel();
        trainRunPanel1 = new wekinator.TrainRunPanel();
        labelGlobalStatus = new javax.swing.JLabel();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        preferencesMenuItem = new javax.swing.JMenuItem();
        exitMenuItem = new javax.swing.JMenuItem();
        viewMenu = new javax.swing.JMenu();
        menuItemViewConsole = new javax.swing.JMenuItem();
        menuItemViewFeatures = new javax.swing.JMenuItem();
        menuItemViewDataset = new javax.swing.JMenuItem();
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
                        .add(labelOscStatus, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 780, Short.MAX_VALUE))
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
                .add(chuckRunnerPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 803, Short.MAX_VALUE)
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
            .add(jPanel8, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(jPanel5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        panelOSCLayout.setVerticalGroup(
            panelOSCLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(panelOSCLayout.createSequentialGroup()
                .add(jPanel8, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        panelMainTabs.addTab("Chuck & OSC Setup", panelOSC);

        org.jdesktop.layout.GroupLayout panelTabFeatureConfigurationLayout = new org.jdesktop.layout.GroupLayout(panelTabFeatureConfiguration);
        panelTabFeatureConfiguration.setLayout(panelTabFeatureConfigurationLayout);
        panelTabFeatureConfigurationLayout.setHorizontalGroup(
            panelTabFeatureConfigurationLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(panelTabFeatureConfigurationLayout.createSequentialGroup()
                .add(featureConfigurationPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(283, Short.MAX_VALUE))
        );
        panelTabFeatureConfigurationLayout.setVerticalGroup(
            panelTabFeatureConfigurationLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(panelTabFeatureConfigurationLayout.createSequentialGroup()
                .add(featureConfigurationPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(223, Short.MAX_VALUE))
        );

        panelMainTabs.addTab("Features Setup", panelTabFeatureConfiguration);

        panelTabLearningSystemConfiguration.setEnabled(false);

        org.jdesktop.layout.GroupLayout panelTabLearningSystemConfigurationLayout = new org.jdesktop.layout.GroupLayout(panelTabLearningSystemConfiguration);
        panelTabLearningSystemConfiguration.setLayout(panelTabLearningSystemConfigurationLayout);
        panelTabLearningSystemConfigurationLayout.setHorizontalGroup(
            panelTabLearningSystemConfigurationLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, learningSystemConfigurationPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 835, Short.MAX_VALUE)
        );
        panelTabLearningSystemConfigurationLayout.setVerticalGroup(
            panelTabLearningSystemConfigurationLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(panelTabLearningSystemConfigurationLayout.createSequentialGroup()
                .add(learningSystemConfigurationPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 551, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(101, Short.MAX_VALUE))
        );

        panelMainTabs.addTab("Model Setup", panelTabLearningSystemConfiguration);
        panelMainTabs.addTab("Use it!", trainRunPanel1);

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

        menuItemViewFeatures.setText("Feature viewer");
        menuItemViewFeatures.setEnabled(false);
        menuItemViewFeatures.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemViewFeaturesActionPerformed(evt);
            }
        });
        viewMenu.add(menuItemViewFeatures);

        menuItemViewDataset.setText("Examples (dataset)");
        menuItemViewDataset.setEnabled(false);
        menuItemViewDataset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemViewDatasetActionPerformed(evt);
            }
        });
        viewMenu.add(menuItemViewDataset);

        menuItemOtfScore.setText("Parameter clipboard");
        menuItemOtfScore.setEnabled(false);
        menuItemOtfScore.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemOtfScoreActionPerformed(evt);
            }
        });
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
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(buttonQuit)
                            .add(labelGlobalStatus, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 493, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(20, 20, 20)
                        .add(panelMainTabs, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 839, Short.MAX_VALUE)))
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
      //  labelClassifiedClass.setText(Integer.toString(i));
      //  labelClassifiedClass1.setText(Integer.toString(i));

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
                int p =0; //= Integer.parseInt(textNumParams.getText());
                int f = 0;// Integer.parseInt(textNumFeatures.getText());
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
//        BoxLayout layout = new BoxLayout(panelRealTraining, BoxLayout.Y_AXIS);

        //panelRealTraining.setLayout(layout);

        //panelRealTraining.removeAll();
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
         //   panelRealTraining.add(next);

        }
//
 //       panelRealTraining.repaint();
 //       scrollTrainPanel.repaint();
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
   /*     checkAudio.setSelected(fm.useAudio);
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
        textNumOsc.setText(Integer.toString(fm.numCustomOsc)); */
    }

    public void setFeatureManager() {
      /*  fm.useAudio = checkAudio.isSelected();
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
*/
    }
    

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
//                labelClassifierStatus.setText("Not a multilayer perceptron file");
                success = false;
            } else {
           //     labelClassifierStatus.setText("Loaded classifier from " + classifierFilename);
            }
        } catch (EOFException ex) {
            success = false;
        //    labelClassifierStatus.setText("Could not read from this file");
        } catch (ClassNotFoundException ex) {
            success = false;
         //   labelClassifierStatus.setText("No class found");
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
         //   labelClassifierStatus.setText("No file found");
            success = false;

            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
        //    labelClassifierStatus.setText("Could not read from file");
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
           // labelClassifierStatus.setText("Problem with reading from file");
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
              //      labelClassifierStatus.setText("Error: Number of loaded model features does not match # features in file");
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, "Error: Number of loaded model features does not match # features in file");

                    success = false;
                }

                //if (w.getClassifierType() != WekaOperator.ClassifierType.NN) {
                //    labelClassifierStatus.setText("Not a multilayer perceptron file");
                //    success = false;
                //} else {
         //       labelClassifierStatus.setText("Loaded classifier from " + classifierFilename);
            //}
            } catch (EOFException ex) {
                success = false;
             //   labelClassifierStatus.setText("Could not read from this file");
            } catch (ClassNotFoundException ex) {
                success = false;
             //   labelClassifierStatus.setText("No class found");
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            } catch (FileNotFoundException ex) {
            //    labelClassifierStatus.setText("No file found");
                success = false;

                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
             //   labelClassifierStatus.setText("Could not read from file");
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
         //   labelClassifierStatus.setText("Problem with reading from file");
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
     //       jProgressBarTrain.setValue(0);
     //       jLabelPlayalongProgress.setText(""); //TODO: use this
        } else if (p == 1) {
     //       jProgressBarTrain.setValue((int) (1.0 / (c + 2) * 100));
     //       jLabelPlayalongProgress.setText("Training...");
        } else if (p < c + 2) {
            //meaning & value contingent on # of classes.
            String s = "Cross-validating model " + (p - 1) + " of " + c;
       //     jLabelPlayalongProgress.setText(s);
       //     jProgressBarTrain.setValue((int) ((float) p / (c + 2) * 100));

        } else {
            String s = "Done training";
        //    jLabelPlayalongProgress.setText(s);
         //   jProgressBarTrain.setValue(100);
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

private void menuItemViewDatasetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemViewDatasetActionPerformed
        WekinatorLearningManager.getInstance().getLearningSystem().getDataset().showViewer();
}//GEN-LAST:event_menuItemViewDatasetActionPerformed

private void menuItemOtfScoreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemOtfScoreActionPerformed
    LearningSystem learningSystem = WekinatorLearningManager.getInstance().getLearningSystem();
    if (learningSystem != null && learningSystem.getScore() != null) {
            learningSystem.getScore().view();
        }

}//GEN-LAST:event_menuItemOtfScoreActionPerformed

private void menuItemViewFeaturesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemViewFeaturesActionPerformed
    FeatureExtractorProxy.get().showFeatureViewer();
}//GEN-LAST:event_menuItemViewFeaturesActionPerformed

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
    /*    System.out.println("validating settings");
        try {
            int a = Integer.parseInt(textNumFeatures.getText());
            int b = Integer.parseInt(textNumParams.getText());
            if (a > 0 && b > 0) {
                return true;
            }
        } catch (Exception ex) {
            return false;
        } */
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
    private javax.swing.ButtonGroup buttonGroupClassifierSource;
    private javax.swing.ButtonGroup buttonGroupProcessingSource;
    private javax.swing.ButtonGroup buttonGroupSettingsSource;
    private javax.swing.JButton buttonOscConnect;
    private javax.swing.JButton buttonOscDisconnect;
    private javax.swing.JButton buttonQuit;
    private wekinator.ChuckRunnerPanel chuckRunnerPanel1;
    private javax.swing.JMenuItem contentsMenuItem1;
    private javax.swing.JMenuItem exitMenuItem;
    private wekinator.FeatureConfigurationPanel featureConfigurationPanel1;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenu helpMenu1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JLabel labelGlobalStatus;
    private javax.swing.JLabel labelOscStatus;
    private javax.swing.JLabel labelOscStatus1;
    private wekinator.LearningSystemConfigurationPanel learningSystemConfigurationPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem menuItemOtfScore;
    private javax.swing.JMenuItem menuItemViewConsole;
    private javax.swing.JMenuItem menuItemViewDataset;
    private javax.swing.JMenuItem menuItemViewFeatures;
    private javax.swing.JTabbedPane panelMainTabs;
    private javax.swing.JPanel panelOSC;
    private javax.swing.JPanel panelTabFeatureConfiguration;
    private javax.swing.JPanel panelTabLearningSystemConfiguration;
    private javax.swing.JMenuItem preferencesMenuItem;
    private javax.swing.JTextField textOscReceive;
    private javax.swing.JTextField textOscSend;
    private wekinator.TrainRunPanel trainRunPanel1;
    private javax.swing.JMenu viewMenu;
    // End of variables declaration//GEN-END:variables
    private ArrayList<javax.swing.JButton> trainingButtons = new ArrayList<javax.swing.JButton>();

    public void enableFeaturePanel() {
//        panelMainTabs.setEnabledAt(panelMainTabs.indexOfComponent(panelFeatures), true);
    }

    public void enableClassifierPanel() {
    //    panelMainTabs.setEnabledAt(panelMainTabs.indexOfComponent(panelClassifier), true);

    }

    public void disableFeaturePanel() {
      //  panelMainTabs.setEnabledAt(panelMainTabs.indexOfComponent(panelFeatures), false);
    }

    public void disableRunPanel() {
     //   panelMainTabs.setEnabledAt(panelMainTabs.indexOfComponent(panelRun), false);
    }

    public void enableRunPanel() {
      //  panelMainTabs.setEnabledAt(panelMainTabs.indexOfComponent(panelRun), true);


    }

    public void enablePlayalongPanel() {
      //  panelMainTabs.setEnabledAt(panelMainTabs.indexOfComponent(panelPlayAlong), true);

    }

    public void disablePlayalongPanel() {
    //    panelMainTabs.setEnabledAt(panelMainTabs.indexOfComponent(panelPlayAlong), false);
    }

    private void setFastAccurate(int value) {
        double d = value * .01; //Now have value between 0 and 1
        fastAccurateValue = value;
        w.setClassifierFastAccurate(d);
    }

    public void setNumParams(int n) {
//        textNumParams.setText(Integer.toString(n));
    }

    public void update(Subject o, Object state, String updateString) {
        if (o == w) {
            //update from WekaOperator
            if (state instanceof WekaOperator.OperatorState) {
                WekaOperator.OperatorState s = (WekaOperator.OperatorState) state;
            //   labelRunningStatus.setText("Running status: " + updateString);




            } else if (state instanceof WekaOperator.ClassifierState) {
            //    labelTrainingStatus.setText("Training status: " + updateString);
                if (((WekaOperator.ClassifierState) state) == WekaOperator.ClassifierState.HAS_DATA) {
                 //   buttonTrain.setEnabled(true);
                 //   buttonTrain1.setEnabled(true);
                    //  toggleButtonRun.setEnabled(false);
                 //   buttonComputeAccuracy.setEnabled(false);
                } else if (((WekaOperator.ClassifierState) state) == WekaOperator.ClassifierState.TRAINED) {
                  //  buttonTrain.setEnabled(true);
                  //  buttonTrain1.setEnabled(true);

                    //  toggleButtonRun.setEnabled(true);
                    if (isChuckDiscrete) {
                   //     buttonComputeAccuracy.setEnabled(true);
                    }
                } else {
                  //  buttonTrain.setEnabled(false);
                  //  buttonTrain1.setEnabled(false);


                    //  toggleButtonRun.setEnabled(false);
                  //  buttonComputeAccuracy.setEnabled(false);
                }

            } else if (state instanceof WekaOperator.FeatureState) {
             //   labelFeatureStatus.setText("ChucK feature status: " + updateString);

            } else if (state instanceof WekaOperator.ClassifierType) {
             //   labelClassifierStatus.setText("Classifier status: " + updateString);

                if (((WekaOperator.ClassifierType) state) == WekaOperator.ClassifierType.NONE) {
             //       panelMainTabs.setEnabledAt(panelMainTabs.indexOfComponent(panelRun), false);
              //      panelMainTabs.setEnabledAt(panelMainTabs.indexOfComponent(panelPlayAlong), false);
                } else {
               //     panelMainTabs.setEnabledAt(panelMainTabs.indexOfComponent(panelRun), true);
               //     panelMainTabs.setEnabledAt(panelMainTabs.indexOfComponent(panelPlayAlong), true);
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
      //  jlabelNumInstances.setText(Integer.toString(numInstances));
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
      //  labelClassifiedClass.setText(s);
      //  labelClassifiedClass1.setText(s);
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
      //  labelClassifiedClass.setText(s);
      //  labelClassifiedClass1.setText(s);
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
//            textSettingsDiscrete.setText("Discrete classification");
        //    textSettingsNumClasses.setText(nc + " classes");
            if (useD) {
       ///         textSettingsWantDist.setText("Probability distribution output");
            } else {
         //       textSettingsWantDist.setText("Class label output");
            }
         //   buttonEditClassifier.setVisible(true);
         //   comboClassifierType.setVisible(true);
         //   checkViewNNGUI.setVisible(false);
         //   buttonComputeAccuracy.setVisible(true);
         //   labelParameterValues.setText("Enter parameter values (integers between 0 and " + (nc - 1) + "):");

        } else {
          //  textSettingsDiscrete.setText("Neural network function learning");
          //  textSettingsWantDist.setText("");
          //  textSettingsNumClasses.setText("");
          //  buttonEditClassifier.setVisible(false);
           // comboClassifierType.setVisible(false);
           // checkViewNNGUI.setVisible(true);
           // buttonComputeAccuracy.setVisible(false);
           // labelParameterValues.setText("Enter parameter values (any real numbers)");
        }

      //  FeatureToParameterMapping newMapping = new FeatureToParameterMapping(fm, nParam);
      //  featureParameterMaskEditor.setMapping(newMapping);
        areChuckSettingsReceived = true;
        enableClassifierPanel();
      //  buttonFeaturesGo.setEnabled(true);
        //  disableFeaturePanel();
    //    panelMainTabs.setSelectedComponent(panelClassifier);
    }

    private void wekaOperatorPropertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(WekaOperator.PROP_TRAININGSTATE)) {
            if (w.getTrainingState() == WekaOperator.TrainingState.TRAINED) {
               // toggleButtonRun.setEnabled(true);

            } else if (w.getTrainingState() == WekaOperator.TrainingState.TRAINING) {
            //    toggleButtonRun.setEnabled(false);

            } else { //Not trained
            //    toggleButtonRun.setEnabled(false);
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


//                panelMainTabs.setSelectedComponent(panelFeatures);
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
      //  labelHidDescription.setText(wek.getCurrentHidSetup().getDescription());
    }

    private void wekinatorInstancePropertyChangeEvent(PropertyChangeEvent evt) {
        // System.out.println("NEW HID SETUP");
        //  labelHidDescription.setText(wek.getCurrentHidSetup().getDescription());
        if (evt.getPropertyName().equals(WekinatorInstance.PROP_CURRENTHIDSETUP)) {
            ((HidSetup) evt.getOldValue()).removePropertyChangeListener(hidSetupChangeListener);
            ((HidSetup) evt.getNewValue()).addPropertyChangeListener(hidSetupChangeListener);
       //     labelHidDescription.setText(wek.getCurrentHidSetup().getDescription());

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

    private void learningManagerPropertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(WekinatorLearningManager.PROP_LEARNINGSYSTEM)) {
            boolean e = (WekinatorLearningManager.getInstance().getLearningSystem() != null);
            System.out.println("enabling otf data " + e);
            menuItemOtfScore.setEnabled(e);
            menuItemViewDataset.setEnabled(e);

        } else if (evt.getPropertyName().equals(WekinatorLearningManager.PROP_FEATURECONFIGURATION)) {
            boolean e = (WekinatorLearningManager.getInstance().getFeatureConfiguration() != null);
            System.out.println("enabling feat " + e);
            menuItemViewFeatures.setEnabled(e);
        }

    }
}


