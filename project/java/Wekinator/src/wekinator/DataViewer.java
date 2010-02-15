/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DataViewer.java
 *
 * Created on Oct 24, 2009, 2:38:19 PM
 */
package wekinator;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 *
 * @author rebecca
 */
public class DataViewer extends javax.swing.JFrame {

    SimpleDataset myDataset = null;

    /** Creates new form DataViewer */
    /* public DataViewer() {
    initComponents();
    populateTable();
    } */

    /* public DataViewer(Instances[] ii, MainGUI gui) {
    initComponents();
    populateTable(ii);
    this.gui = gui;
    }*/
    public DataViewer(SimpleDataset dataset) {
        initComponents();
        myDataset = dataset;
        populateTable(dataset);
    // this.gui = gui;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scrollTable = new javax.swing.JScrollPane();
        buttonDone = new javax.swing.JButton();
        buttonDelete = new javax.swing.JButton();
        buttonAdd = new javax.swing.JButton();
        buttonListen = new javax.swing.JButton();
        buttonListen1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        buttonDone.setText("Done");
        buttonDone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonDoneActionPerformed(evt);
            }
        });

        buttonDelete.setText("Delete selected");
        buttonDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonDeleteActionPerformed(evt);
            }
        });

        buttonAdd.setText("Add row");
        buttonAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonAddActionPerformed(evt);
            }
        });

        buttonListen.setText("Listen");
        buttonListen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonListenActionPerformed(evt);
            }
        });

        buttonListen1.setText("Save to ARFF...");
        buttonListen1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonListen1ActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(scrollTable, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 628, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(buttonDelete)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(buttonAdd)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(buttonListen)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 67, Short.MAX_VALUE)
                .add(buttonListen1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(buttonDone))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(scrollTable, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 411, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(buttonDone)
                    .add(buttonDelete)
                    .add(buttonAdd)
                    .add(buttonListen)
                    .add(buttonListen1)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void buttonDoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonDoneActionPerformed
        this.dispose();

    }//GEN-LAST:event_buttonDoneActionPerformed

    private void buttonDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonDeleteActionPerformed
        model.deleteRows(table.getSelectedRows());
        table.repaint();
}//GEN-LAST:event_buttonDeleteActionPerformed

    private void buttonAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonAddActionPerformed
        model.addRow();
}//GEN-LAST:event_buttonAddActionPerformed

    private void buttonListenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonListenActionPerformed

        if (table.getSelectedRow() == -1) {
            return;
        }


        int row = table.getSelectedRow();
        double d[] = model.getSelectedParams(row);
        for (int i = 0; i < d.length; i++) {
            Double dd = new Double(d[i]);
            if (dd.isNaN()) {
                d[i] = WekinatorLearningManager.getInstance().getParams(i);
            }
        }
        //gui.listenToValues(f);
        WekinatorLearningManager.getInstance().setParams(d);

        OscHandler.getOscHandler().startSound();
        OscHandler.getOscHandler().sendParamsToSynth(d);
}//GEN-LAST:event_buttonListenActionPerformed

    private void buttonListen1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonListen1ActionPerformed
        try {
            File file = findArffFileToSave();
            if (file != null) {
                myDataset.writeInstancesToArff(file);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid feature configuration", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_buttonListen1ActionPerformed

     private File findArffFileToSave() throws IOException {
         return null;
         //TODOTODOTODO: handle this after get fileext support in.
     }

  /*  private File findArffFileToSave() throws IOException {
        JFileChooser fc = new OverwritePromptingFileChooser();
        fc.setDialogType(JFileChooser.SAVE_DIALOG);
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);

        fc.setFileFilter(new FileFilter() {

            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }

                String ext = "";
                String s = f.getName();
                int i = s.lastIndexOf('.');

                if (i > 0 && i < s.length() - 1) {
                    ext = s.substring(i + 1).toLowerCase();
                }

                if (ext != null) {
                    if (ext.equals("arff")) {
                        return true;
                    } else {
                        return false;
                    }
                }
                return false;
            }

            @Override
            public String getDescription() {
                return ".arff files";
            }
        });

        //TODO: handle extension appropriately here
        String location = WekinatorInstance.getWekinatorInstance().getSettings().getLastFeatureFileLocation();
        if (location == null || location.equals("")) {
            location = WekinatorInstance.getWekinatorInstance().getSettings().getDefaultFeatureFileLocation();
        }
        fc.setCurrentDirectory(new File(location));

        File file = null;

        int returnVal = fc.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {

            file = fc.getSelectedFile();
            try {
                WekinatorInstance.getWekinatorInstance().getSettings().setLastFeatureFileLocation(file.getCanonicalPath());
            } catch (IOException ex) {
                WekinatorInstance.getWekinatorInstance().getSettings().setLastFeatureFileLocation(file.getAbsolutePath());
            }
        }
        return file;
    } */

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {

                boolean isDiscrete[] = {true, false};
                int numVals[] = {3, 3};
                //String featureNames[] = {"F1", "f2", "F3", "f4", "f5"};
                String featureNames[] = new String[100];
                for (int i = 0; i < featureNames.length; i++) {
                    featureNames[i] = "F" + i;
                }
                String paramNames[] = {"P1", "p2"};
                SimpleDataset s = new SimpleDataset(featureNames.length, 2, isDiscrete, numVals, featureNames, paramNames);


                new DataViewer(s).setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonAdd;
    private javax.swing.JButton buttonDelete;
    private javax.swing.JButton buttonDone;
    private javax.swing.JButton buttonListen;
    private javax.swing.JButton buttonListen1;
    private javax.swing.JScrollPane scrollTable;
    // End of variables declaration//GEN-END:variables
    private javax.swing.JTable table;
    private DataTableModel model;
    //  private MainGUI gui;

    /*
    private void populateTable() {
    //  table = new JTable(new DataTableModel(2, 3));
    BufferedReader reader = null;
    Instances instances[] = new Instances[2];
    try {
    reader = new BufferedReader(new FileReader("/Users/rebecca/work/weka-3-5-6/data/iris.arff"));
    ArffReader arff = new ArffReader(reader);
    Instances data = arff.getData();
    Instances data2 = new Instances(data);
    data.setClassIndex(data.numAttributes() - 1);
    data2.setClassIndex(data.numAttributes() - 1);
    instances[0] = data;
    instances[1] = data2;
    } catch (IOException ex) {
    Logger.getLogger(DataViewer.class.getName()).log(Level.SEVERE, null, ex);
    } finally {
    try {
    reader.close();
    } catch (IOException ex) {
    Logger.getLogger(DataViewer.class.getName()).log(Level.SEVERE, null, ex);
    }
    }

    model = new DataTableModel(instances);
    table = new JTable(model);
    scrollTable.setViewportView(table);
    } */
    private void populateTable(SimpleDataset data) {
        model = new DataTableModel(data);
        table = new JTable(model);
        setTableColumns();


        //table.setPreferredScrollableViewportSize(new Dimension(1000,1000));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        scrollTable.setViewportView(table);

        table.repaint();
        //scrollTable.setVie
        //scrollTable.repaint();

    }

    private void setTableColumns() {
       // table.getColumnModel().get
       table.getColumnModel().getColumn(0).setPreferredWidth(30);
       table.getColumnModel().getColumn(1).setPreferredWidth(40);
         table.getColumnModel().getColumn(2).setPreferredWidth(100); 
        for (int i = 3; i < table.getColumnCount(); i++) {
            TableColumn c = table.getColumnModel().getColumn(i);
            c.setPreferredWidth(50);
           c.setMinWidth(20);
        
            c.setResizable(true);

        }
    }
}

class DataTableModel extends AbstractTableModel {

    private String[] columnNames;
    //  private Object[][] data;
    int numMetaData, numFeats, numParams;
    SimpleDataset dataset;
    // Instances instances[];

    /* public DataTableModel(Instances ii[]) {
    instances = ii;
    //Hack: Assume all instances same length (includes same rows in all)
    if (ii.length == 0) {
    this.numFeats = 0;
    this.numParams = 0;
    //  data = new Object[0][0];
    return;
    }

    numFeats = ii[0].numAttributes() - 1;
    numParams = ii.length;

    setColNames();

    //  setFeatures(ii[0]);
    /*  for (int i = 0; i < ii.length; i++) {
    setParamsAll(i, ii[i].attributeToDoubleArray(ii[i].classIndex()));
    }


    }*/
    public DataTableModel(SimpleDataset dataset) {
        this.dataset = dataset;
        this.numFeats = dataset.getNumFeatures();
        this.numParams = dataset.getNumParameters();
        this.numMetaData = 3; //for now, ID, time, & training round


        setColNames();

        //  setFeatures(ii[0]);
      /*  for (int i = 0; i < ii.length; i++) {
        setParamsAll(i, ii[i].attributeToDoubleArray(ii[i].classIndex()));
        } */
        dataset.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                fireTableDataChanged(); //TODO for efficiency, update this... 
            }
        });

    }

    protected void setColNames() {
        columnNames = new String[numMetaData + numFeats + numParams];
        columnNames[0] = "ID";
        columnNames[1] = "Time";
        columnNames[2] = "Training round";

        //  columnNames[0] = "ID";
        for (int i = 0; i < numFeats; i++) {
            columnNames[i + numMetaData] = dataset.getFeatureName(i);
        }
        for (int i = 0; i < numParams; i++) {
            columnNames[numMetaData + numFeats + i] = dataset.getParameterName(i);
        }
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        return dataset.getNumDatapoints(); //TODO TODO TODO this is 0 for some reason!
    }

    public void addRow() {

        double[] features = new double[numFeats];
        double[] params = new double[numParams];
        boolean[] mask = new boolean[numParams];
        dataset.addInstance(features, params, mask, new Date());
        int row = dataset.getNumDatapoints();

        fireTableRowsInserted(row, row);

    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    public Object getValueAt(int row, int col) {
        if (row >= dataset.getNumDatapoints()) {
            return null;
        }

        if (col == 0) {
            return dataset.getID(row);
        } else if (col == 1) {
            return dataset.dateDoubleToString(dataset.getTimestamp(row));
        } else if (col == 2) {
            return dataset.getTrainingRound(row);
        } else if (col < numMetaData + numFeats) {
            return dataset.getFeature(row, (col - numMetaData));
        } else if (col < numMetaData + numFeats + numParams) {
            //Treat as strings to represent missing values!
            Double d = dataset.getParam(row, col - numMetaData - numFeats);
            if (d.isNaN()) {
                return "?";
            } else {
                return Double.toString(d);
            }
        } else {
            return null;
        }
    }

    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    /*
     * Don't need to implement this method unless your table's
     * editable.
     */
    public boolean isCellEditable(int row, int col) {
        //Note that the data/cell address is constant,
        //no matter where the cell appears onscreen.
        if (col >= 1) { //don't allow editing of ID
            return true;
        } else {
            return false;
        }
    }

    /*
     * Don't need to implement this method unless your table's
     * data can change.
     */
    public void setValueAt(Object value, int row, int col) {
        if (col == 0) {
            //shouldn't be editing this: it's an ID
            System.out.println("Error: shouldn't edit this cell");
            return;
        }

        //TODO: check that this value is legal!
        if (col == 1) {
            //Timestamp
            if (value instanceof String) {
                try {
                    dataset.setTimestamp(row, (String) value);
                } catch (ParseException ex) {
                    //TODO
                    Logger.getLogger(DataTableModel.class.getName()).log(Level.SEVERE, null, ex);
                    return;
                }
            } else {
                //TODO: return or set back to 1st val?
                System.out.println("Bad");
                return;
            }

        } else if (col == 2) {
            //Training round
            if (value instanceof Integer) {
                dataset.setTrainingRound(row, (Integer) value);
            } else {
                //TODO
                System.out.println("Bad");
                return;
            }

        } else if (col < numMetaData + numFeats) {
            //Assume all double feats for now
            if (value instanceof Double) {

                dataset.setFeatureValue(row, (col - numMetaData), (Double) value);
            } else {
                System.out.println("Uh oh");
                return;
            }

        } else if (col < numMetaData + numFeats + numParams) {
            //Check if legal? Probably should.
            int paramNum = col - numMetaData - numFeats;
            double d = 0;


            if (value instanceof Integer) {
                d = ((Integer) value).intValue();
            } else if (value instanceof Double) {
                d = ((Double) value).doubleValue();
            } else if (value instanceof String) {
                String s = (String) value;
                if (s.equals("?")) {
                    dataset.setParameterMissing(row, paramNum);
                    fireTableCellUpdated(row, col);
                    return; //TODO: clean up position logic here

                } else {
                    try {
                        d = Double.parseDouble((String) value);
                    } catch (Exception ex) {
                        System.out.println("BAD!"); //TODO
                        return;
                    }
                }
            }

            if (dataset.isParameterDiscrete(paramNum)) {
                if (d >= 0 && d <= dataset.maxLegalDiscreteParamValue(paramNum)) {
                    dataset.setParameterValue(row, paramNum, d);
                } else {
                    //TODO: can I erase this?
                    System.out.println("Bad value!");
                    return;
                }

            } else {
                dataset.setParameterValue(row, paramNum, d);
            }
        }

        // data[row][col] = value;
        fireTableCellUpdated(row, col);
    }

    void deleteRows(int[] selectedRows) {

        //tmp: examine:
        //Assumption is that selectedRows will always be in increasing order
     /*   for (int i = 0; i < selectedRows.length; i++) {
        System.out.println(selectedRows[i]);
        } */

        for (int j = selectedRows.length - 1; j >= 0; j--) {

            //Delete the weka representation
            System.out.println("Trying to delete row " + selectedRows[j]);

            dataset.deleteInstance(selectedRows[j]);

            fireTableRowsDeleted(selectedRows[j], selectedRows[j]);
        }

    }

    double[] getSelectedParams(int row) {
        System.out.println("made it here");
        double f[] = new double[numParams];
        for (int i = 0; i < numParams; i++) {
            // f[i] = (float) instances[i].instance(row).classValue();
            f[i] = dataset.getParam(row, i);
            Double d = new Double(f[i]);
            if (d.isNaN()) {
                System.out.println("AHFALJFLSDJGSDFDLFJDFDJ NaN here");
            }
        }
        return f;
    }

    


}

