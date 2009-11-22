/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * Console.java
 *
 * Created on Nov 18, 2009, 8:23:56 PM
 */
package wekinator;

import java.util.logging.ErrorManager;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.swing.JTextArea;

/**
 *
 * @author rebecca
 */
public class Console extends javax.swing.JFrame {

    private static Console ref = null;
    WekinatorConsoleHandler h;

    /** Creates new form Console */
    private Console() {
        initComponents();
        WekinatorInstance wek = WekinatorInstance.getWekinatorInstance();
       h = WekinatorConsoleHandler.getInstance();
        h.setTextArea(text1);
        wek.addLoggingHandler(h);
    }

    public static synchronized Console getInstance() {
        if (ref == null) {
           ref = new Console();
        }
        return ref;
    }

    public void log(String s) {
        if (isVisible()) {
           text1.append(s);
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

        jButton1 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        text1 = new javax.swing.JTextArea();
        buttonClear = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jButton1.setText("Log something");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        text1.setColumns(20);
        text1.setEditable(false);
        text1.setRows(5);
        jScrollPane1.setViewportView(text1);

        buttonClear.setText("Clear");
        buttonClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonClearActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jButton1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 417, Short.MAX_VALUE)
                .add(buttonClear)
                .addContainerGap())
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 665, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 271, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(buttonClear)
                    .add(jButton1)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        Logger.getLogger(Console.class.getPackage().getName()).severe("MEMORY HANDLER ADDED");

    }//GEN-LAST:event_jButton1ActionPerformed

    private void buttonClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonClearActionPerformed
        text1.setText("");
    }//GEN-LAST:event_buttonClearActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        //Remove the handler
        WekinatorInstance.getWekinatorInstance().removeLoggingHandler(h);
    }//GEN-LAST:event_formWindowClosing

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                Console b = new Console();
                b.setVisible(true);
            //
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonClear;
    private javax.swing.JButton jButton1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea text1;
    // End of variables declaration//GEN-END:variables

    public void showInfo(String data) {
        text1.append(data);
        this.getContentPane().validate();
    }

        @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
}

class WekinatorConsoleHandler extends Handler {
//  private Console tmp = null;

    private JTextArea textArea = null;
    private Formatter formatter = null;
    private Level level = null;
    WekinatorInstance wek = null;

    //the singleton instance
    private static WekinatorConsoleHandler handler = null;

    private WekinatorConsoleHandler() {
        setup();
    }

    public static synchronized WekinatorConsoleHandler getInstance() {
        if (handler == null) {
            handler = new WekinatorConsoleHandler();
        }
        return handler;
    }

    public void setTextArea(JTextArea textArea) {
        this.textArea = textArea;
    }

    private void setup() {
        wek = WekinatorInstance.getWekinatorInstance();
        setLevel(wek.getSettings().getLogLevel());
        setFilter(null);
        setFormatter(new SimpleFormatter());
    }

    public void publish(LogRecord record) {
        String message = null;
        if (!isLoggable(record)) {
            return;
        }
        try {
            message = getFormatter().format(record);
        } catch (Exception e) {
            reportError(null, e, ErrorManager.FORMAT_FAILURE);
        }

        try {
            textArea.append(message);
        } catch (Exception ex) {
            reportError(null, ex, ErrorManager.WRITE_FAILURE);
        }

    }

    public void close() {
    }

    public void flush() {
    }
}