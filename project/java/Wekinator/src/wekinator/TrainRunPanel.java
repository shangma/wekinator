/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * TrainRunPanel.java
 *
 * Created on Dec 4, 2009, 7:40:57 PM
 */
package wekinator;

import java.awt.CardLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import wekinator.LearningSystem.*;
import wekinator.util.OverwritePromptingFileChooser;
import wekinator.util.Util;

/**
 *
 * @author rebecca
 */
public class TrainRunPanel extends javax.swing.JPanel {

    LearningSystem ls = null;
    PropertyChangeListener learningSystemChangeListener = new PropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent evt) {
            learningSystemChange(evt);
        }
    };

    /** Creates new form TrainRunPanel */
    public TrainRunPanel() {
        initComponents();

        setCurrentPane(Panes.COLLECT);
        WekinatorInstance.getWekinatorInstance().addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(WekinatorInstance.PROP_LEARNINGSYSTEM)) {
                    setLearningSystem(WekinatorInstance.getWekinatorInstance().getLearningSystem());
                }

            }
        });


    }

    public enum Panes {

        COLLECT,
        TRAIN,
        RUN,
        CONFIGURE
    };
    protected Panes currentPane = Panes.COLLECT;

    /**
     * Get the value of currentPane
     *
     * @return the value of currentPane
     */
    public Panes getCurrentPane() {
        return currentPane;
    }

    /**
     * Set the value of currentPane
     *
     * @param currentPane new value of currentPane
     */
    public void setCurrentPane(Panes currentPane) {
        CardLayout c = (CardLayout) layoutPanel.getLayout();
        switch (currentPane) {
            case CONFIGURE:
                toggleConfigure.setSelected(true);
                toggleCollect.setSelected(false);
                toggleRun.setSelected(false);
                toggleTrain.setSelected(false);
                c.show(layoutPanel, "cardEdit");
                break;
            case RUN:
                toggleConfigure.setSelected(false);
                toggleCollect.setSelected(false);
                toggleRun.setSelected(true);
                toggleTrain.setSelected(false);
                c.show(layoutPanel, "cardRun");
                break;
            case TRAIN:
                toggleConfigure.setSelected(false);
                toggleCollect.setSelected(false);
                toggleRun.setSelected(false);
                toggleTrain.setSelected(true);
                c.show(layoutPanel, "cardTrain");
                break;
            case COLLECT:
            default:
                toggleConfigure.setSelected(false);
                toggleCollect.setSelected(true);
                toggleRun.setSelected(false);
                toggleTrain.setSelected(false);
                c.show(layoutPanel, "cardBuild");
                break;
        }
    }


    //Called when WekinatorInstance learning system changes
    protected void setLearningSystem(LearningSystem ls) {
        if (this.ls != null) {
            this.ls.removePropertyChangeListener(learningSystemChangeListener);
        }
        this.ls = ls;
        if (this.ls != null) {
            ls.addPropertyChangeListener(learningSystemChangeListener);
            setButtonsEnabled();
        }
        buildPanel.setLearningSystem(ls);
        trainPanel.setLearningSystem(ls);
        runPanel.setLearningSystem(ls);
        editPanel.setLearningSystem(ls);
        setCurrentPane(Panes.COLLECT);
        setButtonsEnabled();
    }

    public boolean canRun() {
        /* return  (ls.getSystemTrainingState() == LearningSystemTrainingState.TRAINED
        && ls.getEvaluationState() != EvaluationState.EVALUTATING
        && ls.getInitializationState() == LearningAlgorithmsInitializationState.ALL_INITIALIZED );*/
        return (ls != null && ls.isIsRunnable());
    }

    private void setButtonsEnabled() {
        /*  LearningSystemTrainingState t = ls.getSystemTrainingState();
        EvaluationState e = ls.getEvaluationState();
        DatasetState d = ls.getDatasetState();
        LearningAlgorithmsInitializationState i = ls.getInitializationState();

        boolean enableRun = canRun();


        boolean enableTrain = (d == DatasetState.HAS_DATA
        && i == LearningAlgorithmsInitializationState.ALL_INITIALIZED);

        boolean enableCollect = (i == LearningAlgorithmsInitializationState.ALL_INITIALIZED);
        boolean enableConfigure = (i == LearningAlgorithmsInitializationState.ALL_INITIALIZED);
         */
        boolean enableConfigure = ls != null;
        boolean enableRun = canRun();
        boolean enableTrain = ls != null && ls.isIsTrainable();
        boolean enableCollect = ls != null;

        toggleConfigure.setEnabled(enableConfigure);
        toggleRun.setEnabled(canRun());
        toggleTrain.setEnabled(enableTrain);
        toggleCollect.setEnabled(enableCollect);

        //TODO: Also go back to another pane if current is bogus?
        if (currentPane == Panes.CONFIGURE && !enableConfigure) {
            setCurrentPane(Panes.TRAIN);
        }
        if (currentPane == Panes.RUN && !enableRun) {
            setCurrentPane(Panes.TRAIN);
        }
        if (currentPane == Panes.TRAIN && !enableTrain) {
            setCurrentPane(Panes.COLLECT);
        }
        if (currentPane == Panes.COLLECT && !enableCollect) {
            System.out.println("TODO log error");
        //Should request of main gui to go back somewhere else.
        }

    }

    private void learningSystemChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(LearningSystem.PROP_ISTRAINABLE) || evt.getPropertyName().equals(LearningSystem.PROP_ISRUNNABLE)) {

            setButtonsEnabled();
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

        jLabel3 = new javax.swing.JLabel();
        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        layoutPanel = new javax.swing.JPanel();
        trainPanel = new wekinator.TrainPanel();
        runPanel = new wekinator.RunPanel();
        buildPanel = new wekinator.BuildPanel();
        editPanel = new wekinator.EditPanel();
        menuPanel = new javax.swing.JPanel();
        toggleCollect = new javax.swing.JToggleButton();
        toggleTrain = new javax.swing.JToggleButton();
        toggleRun = new javax.swing.JToggleButton();
        toggleConfigure = new javax.swing.JToggleButton();
        buttonShh = new javax.swing.JButton();
        buttonSave = new javax.swing.JButton();

        jLabel3.setText("jLabel3");

        layoutPanel.setLayout(new java.awt.CardLayout());
        layoutPanel.add(trainPanel, "cardTrain");
        layoutPanel.add(runPanel, "cardRun");
        layoutPanel.add(buildPanel, "cardBuild");
        layoutPanel.add(editPanel, "cardEdit");

        toggleCollect.setText("Collect data...");
        toggleCollect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toggleCollectActionPerformed(evt);
            }
        });

        toggleTrain.setText("Train...");
        toggleTrain.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toggleTrainActionPerformed(evt);
            }
        });

        toggleRun.setText("Run...");
        toggleRun.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toggleRunActionPerformed(evt);
            }
        });

        toggleConfigure.setText("Configure & evaluate...");
        toggleConfigure.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toggleConfigureActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout menuPanelLayout = new org.jdesktop.layout.GroupLayout(menuPanel);
        menuPanel.setLayout(menuPanelLayout);
        menuPanelLayout.setHorizontalGroup(
            menuPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(menuPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(menuPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, toggleConfigure, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, toggleRun, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, toggleTrain, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, toggleCollect, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 226, Short.MAX_VALUE))
                .addContainerGap())
        );
        menuPanelLayout.setVerticalGroup(
            menuPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(menuPanelLayout.createSequentialGroup()
                .add(toggleCollect, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 37, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(toggleTrain, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 37, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(toggleRun, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 37, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(toggleConfigure, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 37, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(172, Short.MAX_VALUE))
        );

        buttonShh.setText("audio off");
        buttonShh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonShhActionPerformed(evt);
            }
        });

        buttonSave.setText("Save model file");
        buttonSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSaveActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(buttonShh)
                    .add(buttonSave)
                    .add(menuPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layoutPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 483, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layoutPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 712, Short.MAX_VALUE)
                        .addContainerGap())
                    .add(layout.createSequentialGroup()
                        .add(menuPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(buttonShh)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(buttonSave)
                        .add(310, 310, 310))))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void buttonShhActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonShhActionPerformed
        OscHandler.getOscHandler().stopSound();
}//GEN-LAST:event_buttonShhActionPerformed

    public void startAutoRun() {

        setCurrentPane(Panes.RUN);
        WekinatorLearningManager.getInstance().startRunning();
        OscHandler.getOscHandler().startSound();

    }

    private void buttonSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSaveActionPerformed


        File file = Util.findSaveFile(LearningSystem.getFileExtension(),
                LearningSystem.getFileTypeDescription(),
                LearningSystem.getDefaultLocation(),
                this);
        if (file != null) {
            try {
                ls.writeToFile(file); //TODOTODOTODO: update last path on this.
                Util.setLastFile(LearningSystem.getFileExtension(), file);
            } catch (Exception ex) {
                Logger.getLogger(TrainRunPanel.class.getName()).log(Level.INFO, null, ex);
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Could not save to file", JOptionPane.ERROR_MESSAGE);
            }
        }
}//GEN-LAST:event_buttonSaveActionPerformed

    private void toggleCollectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toggleCollectActionPerformed
        if (toggleCollect.isSelected()) {
            setCurrentPane(Panes.COLLECT);
        } else {
            toggleCollect.setSelected(true);
        }
}//GEN-LAST:event_toggleCollectActionPerformed

    private void toggleTrainActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toggleTrainActionPerformed
        if (toggleTrain.isSelected()) {
            setCurrentPane(Panes.TRAIN);
        } else {
            toggleTrain.setSelected(true);
        }
    }//GEN-LAST:event_toggleTrainActionPerformed

    private void toggleRunActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toggleRunActionPerformed
        if (toggleRun.isSelected()) {
            setCurrentPane(Panes.RUN);
        } else {
            toggleRun.setSelected(true);
        }
    }//GEN-LAST:event_toggleRunActionPerformed

    private void toggleConfigureActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toggleConfigureActionPerformed
        if (toggleConfigure.isSelected()) {
            setCurrentPane(Panes.CONFIGURE);
        } else {
            toggleConfigure.setSelected(true);
        }
    }//GEN-LAST:event_toggleConfigureActionPerformed

    private File findLearningSystemFileToSave() {
        JFileChooser fc = new OverwritePromptingFileChooser();
        fc.setDialogType(JFileChooser.SAVE_DIALOG);
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        String location = WekinatorInstance.getWekinatorInstance().getSettings().getLastLocation(LearningSystem.getFileExtension());
        if (location == null || location.equals("")) {
            //location = HidSetup.getDefaultLocation();
            location = WekinatorInstance.getWekinatorInstance().getSettings().getDefaultSettingsDirectory() + File.separator + HidSetup.getDefaultLocation();
        }
        fc.setCurrentDirectory(new File(location)); //TODO: Could set directory vs file here according to above results


        File file = null;

        int returnVal = fc.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {

            file = fc.getSelectedFile();
            if (file != null) {
                WekinatorInstance.getWekinatorInstance().getSettings().setLastLocation(LearningSystem.getFileExtension(), Util.getCanonicalPath(file));
            }
        }
        return file;

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private wekinator.BuildPanel buildPanel;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JButton buttonSave;
    private javax.swing.JButton buttonShh;
    private wekinator.EditPanel editPanel;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel layoutPanel;
    private javax.swing.JPanel menuPanel;
    private wekinator.RunPanel runPanel;
    private javax.swing.JToggleButton toggleCollect;
    private javax.swing.JToggleButton toggleConfigure;
    private javax.swing.JToggleButton toggleRun;
    private javax.swing.JToggleButton toggleTrain;
    private wekinator.TrainPanel trainPanel;
    // End of variables declaration//GEN-END:variables

    public static void main(String[] args) {
        JFrame f = new JFrame();
        TrainRunPanel p = new TrainRunPanel();
        f.add(p);
        f.setVisible(true);
    }
}
