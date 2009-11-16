/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ChuckRunnerPanel2.java
 *
 * Created on Nov 13, 2009, 11:27:03 PM
 */

package wekinator;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rebecca
 */
public class ChuckRunnerPanel2 extends javax.swing.JFrame implements Serializable {
    ChuckRunner runner = null;
    protected boolean isRunning = false;
    public static final String PROP_ISRUNNING = "isRunning";

    /**
     * Get the value of running
     *
     * @return the value of running
     */
    public boolean isIsRunning() {
        return isRunning;
    }

    /**
     * Set the value of running
     *
     * @param running new value of running
     */
    public void setIsRunning(boolean isRunning) {
        boolean oldIsRunning = this.isRunning;
        this.isRunning = isRunning;
        propertyChangeSupport.firePropertyChange(PROP_ISRUNNING, oldIsRunning, isRunning);
    }
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    /**
     * Add PropertyChangeListener.
     *
     * @param listener
     */
    @Override
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

    public ChuckRunner getRunner() {
        return runner;
    }

    public void setRunner(ChuckRunner runner) {
        this.runner = runner;
        runner.addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                runnerPropertyChange(evt);
            }
        });

        runner.getConfiguration().addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                configurationPropertyChange(evt);
            }
        });
    }



    public ChuckRunnerPanel2() {
        initComponents();
    }

    /** Creates new form ChuckRunnerPanel2 */
    public ChuckRunnerPanel2(ChuckRunner r) {
        setRunner(r);
        initComponents();
        updateRunnerIsRunning(false);
        updateConfigurationUsable(false);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        labelAboutConfiguration = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        buttonRun = new javax.swing.JButton();
        labelStatus = new javax.swing.JLabel();
        buttonStop = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("ChucK configuration");

        labelAboutConfiguration.setText("A description of the configuration goes here.");

        jButton1.setText("Edit");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        buttonRun.setText("Run");
        buttonRun.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonRunActionPerformed(evt);
            }
        });

        labelStatus.setText("Status: ");

        buttonStop.setText("Stop");
        buttonStop.setEnabled(false);
        buttonStop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonStopActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 347, Short.MAX_VALUE)
            .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(jPanel1Layout.createSequentialGroup()
                    .addContainerGap()
                    .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(jPanel1Layout.createSequentialGroup()
                            .add(buttonRun)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                            .add(buttonStop))
                        .add(jPanel1Layout.createSequentialGroup()
                            .add(jLabel1)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(jButton1))
                        .add(jPanel1Layout.createSequentialGroup()
                            .add(24, 24, 24)
                            .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(labelStatus)
                                .add(labelAboutConfiguration))))
                    .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 148, Short.MAX_VALUE)
            .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(jPanel1Layout.createSequentialGroup()
                    .addContainerGap()
                    .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(jLabel1)
                        .add(jButton1))
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(labelAboutConfiguration)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(labelStatus)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(buttonRun)
                        .add(buttonStop))
                    .addContainerGap()))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void buttonRunActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonRunActionPerformed
        try {
            runner.run();
        } catch (IOException ex) {
            labelStatus.setText("Chuck encountered an error while running.");
            Logger.getLogger(ChuckRunnerPanel2.class.getName()).log(Level.SEVERE, null, ex);
        }
}//GEN-LAST:event_buttonRunActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        ChuckConfigurationForm p = new ChuckConfigurationForm(runner.getConfiguration());
        p.setVisible(true);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void buttonStopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonStopActionPerformed
        try {
            runner.stop();
        } catch (IOException ex) {
            Logger.getLogger(ChuckRunnerPanel2.class.getName()).log(Level.SEVERE, null, ex);
        }
}//GEN-LAST:event_buttonStopActionPerformed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                ChuckConfiguration c = new ChuckConfiguration();
                 ChuckRunner r = new ChuckRunner(c);
       
           // ChuckConfigurationForm panel = new ChuckConfigurationForm(c);

           // panel.setVisible(true);
       
                new ChuckRunnerPanel2(r).setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonRun;
    private javax.swing.JButton buttonStop;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel labelAboutConfiguration;
    private javax.swing.JLabel labelStatus;
    // End of variables declaration//GEN-END:variables

            private void runnerPropertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(ChuckRunner.PROP_ISRUNNING)) {
                updateRunnerIsRunning(runner.isRunning());
            }

        }

                        private void configurationPropertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(ChuckConfiguration.PROP_USABLE)) {
                updateConfigurationUsable(runner.getConfiguration().isUsable());
            }

        }

    private void updateRunnerIsRunning(boolean running) {
        buttonRun.setEnabled(!running);
        buttonStop.setEnabled(running);

        if (running) {
            labelStatus.setText("Chuck running successfully.");
        } else {
            labelStatus.setText("Chuck not running.");
        }
        
    }

    private void updateConfigurationUsable(boolean usable) {
        buttonRun.setEnabled(usable);
        if (!usable) {
            labelAboutConfiguration.setText("No valid configuration");
        } else {
            labelAboutConfiguration.setText("Configuration is usable and ready to run");
        }

    }

}
