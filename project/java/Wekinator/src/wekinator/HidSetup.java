/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package wekinator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import wekinator.util.Observer;
import wekinator.util.Subject;

/**
 *
 * @author rebecca
 */
public class HidSetup implements Subject{
    private int numAxes = 0;
    private int numHats = 0;
    private int numButtons = 0;
    
    private float[] initAxes = new float[0];
    private int[] initHats = new int[0];
    private int[] initButtons = new int[0];
    
    private int[] axesMask = new int[0];
    private int[] hatsMask = new int[0];
    private int[] buttonsMask = new int[0];
    
//    private File myFile = null;
    
    private ArrayList<Observer> observers = null;
    
    OscHandler h;
    /*public HidSetup(OscHandler handler) {
        handler.setHidSetup(this);
        observers = new ArrayList<Observer>();
    }*/
    
    public HidSetup() {
                observers = new ArrayList<Observer>();

        
    }
    
    public void setOSCHandler(OscHandler h) {
        this.h = h;
    }




    public enum HidState {NONE, SETUP_REQUESTED, SETUP_BEGUN, INIT_REQUESTED, 
        INIT_DONE, SETTINGS_REQUESTED, SETTINGS_RECEIVED, RUN_REQUESTED, 
        SETUP_STOP_REQUESTED, SETUP_STOPPED};
    
    private HidState myState; 
    
    private void setMyState(HidState newState) {
        if (myState != newState) {
            myState = newState;
            notifyOperatorObservers();
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
   
   private String myUpdateString() {
       switch (myState) {
           case INIT_DONE:
               return "Initializing HID begun";
           case INIT_REQUESTED:
               return "Initialization requested";
           case NONE:
               return "No activity";
           case RUN_REQUESTED:
               return "Run requested";
           case SETTINGS_RECEIVED:
               return "Settings received";
           case SETTINGS_REQUESTED:
               return "Settings requested";
           case SETUP_BEGUN:
               return "Setup begun";
           case SETUP_REQUESTED:
               return "Setup requested";
           case SETUP_STOP_REQUESTED:
               return "Setup stop requested";
           case SETUP_STOPPED:
               return "Setup stopped";
       }
       return "unknown state";
       
   }
    
   /**
    * Request HidDiscoverer to start setup phase
    * @throws java.io.IOException
    */
    public void requestHidSetup() throws IOException {
        h.requestHidSetup();
        setMyState(HidState.SETUP_REQUESTED);
    }
    
    /**
     * Notify me that ChucK says hid setup has begun
     */
    public void setupBegun() {
        setMyState(HidState.SETUP_BEGUN);        
    }
    
    /**
     * Request HidDiscoverer to stop setup phase
     * @throws java.io.IOException
     */
    public void requestSetupStop() throws IOException {
        System.out.println("Requested stop");
        h.requestHidSetupStop();
        setMyState(HidState.SETUP_STOP_REQUESTED);
    
    }
    
    /**
     * Notify me that ChucK says setup has stopped
     * @throws java.io.IOException
     */
    public void setupStopped() throws IOException {
        System.out.println("Setup stopped.");
        setMyState(HidState.SETUP_STOPPED);
        startHidRequestSettings();
    }
   
    /**
     * Start HID init phase, for new device
     * @throws java.io.IOException
     */
    public void startHidInit() throws IOException {
        h.sendHidInit(numAxes, numHats, numButtons);
        setMyState(HidState.INIT_REQUESTED);        
    }
    
    /**
     * Notify me that ChucK is waiting for init hid values
     * @throws java.io.IOException
     */
    public void receivedSendHidInitValues() throws IOException {
        System.out.println("sending and #buttons is " + initButtons.length);
        h.sendHidValues(initAxes, initHats, initButtons, axesMask, hatsMask, buttonsMask);
        setMyState(HidState.INIT_DONE);
        System.out.println("state should be done now!");
    }
    
    /**
     * Request current HID settings from HidDiscoverer
     * @throws java.io.IOException
     */
    public void startHidRequestSettings() throws IOException {
        h.sendHidSettingsRequest();
        setMyState(HidState.SETTINGS_REQUESTED);
    }
    
    /*public void receivedHidSettings(float axes[], int hats[], int buttons[]) {
        initAxes = axes.clone();
        initHats = hats.clone();
        initButtons = buttons.clone();
        numAxes = initAxes.length;
        numHats = initHats.length;
        numButtons = initButtons.length;
        setMyState(HidState.SETTINGS_RECEIVED);
    } */
    
    /**
     * Notify me that chucK has given me the # of axes, hats, and buttons
     * in its current settings
     * @param numAxes
     * @param numHats
     * @param numButtons
     */
    public void receivedHidSettingsNums(int numAxes, int numHats, int numButtons) {
        //for now, don't care except to see if we don't have to wait for one
        if (numAxes == 0) 
            gotAxes = true;
        if (numHats == 0)
            gotHats = true;
        if (numButtons == 0)
            gotButtons = true; 
        axesMask = new int[numAxes];
        hatsMask = new int[numHats];
        buttonsMask = new int[numButtons];
        System.out.println("masks are " + axesMask.toString() + "," + hatsMask.toString() + "," + buttonsMask.toString());
    }
    
    
    
    boolean gotAxes = false;
    boolean gotHats = false;
    boolean gotButtons = false;
    boolean gotMasks = false;
    
    /**
     * Notify me that chuck has sent initial axis values
     * @param f
     */
    public void receivedHidAxisSettings(float[] f) {
        initAxes = f.clone();
        numAxes = initAxes.length;
        gotAxes = true;
        if (checkForAllSettings()) {
            setMyState(HidState.SETTINGS_RECEIVED);
        }
    }
    
    /**
     * notify me that chuck has sent initial hat values
     * @param i
     */
    public void receivedHidHatsSettings(int[] i) {
        initHats = i.clone();
        numHats = initHats.length;
        gotHats = true;
        if (checkForAllSettings()) {
            setMyState(HidState.SETTINGS_RECEIVED);
        }
    }
    
    /**
     * notify me that chuck has sent initial buttons values
     * @param i
     */
   public void receivedHidButtonsSettings(int[] i) {
        initButtons = i.clone();
        numButtons = initButtons.length;
        gotButtons = true;
        if (checkForAllSettings()) {
            setMyState(HidState.SETTINGS_RECEIVED);
        }
    }
   
    public void receivedHidMaskSettings(int[] m) {
        int j = 0;
        
        for (int i = 0; i < numAxes; i++) {
            axesMask[i] = m[j];
            j++;
                    
        }
        for (int i = 0; i < numHats; i++) {
            hatsMask[i] = m[j];
            j++;
        }
        for (int i = 0; i < numButtons; i++) {
            buttonsMask[i] = m[j];
            j++;
        }
        
        gotMasks = true;
        if (checkForAllSettings()) {
            setMyState(HidState.SETTINGS_RECEIVED);
        }
    }
    
    private boolean checkForAllSettings() {
        return (gotAxes && gotHats && gotButtons && gotMasks);        
    }
    
    /**
     * Request that HidDiscoverer begin running, using current settings
     * @throws java.io.IOException
     */
    public void startHidRun() throws IOException {
        h.startHidRun();      
    }
    
    /**
     * Add observer
     * @param o
     */
    public void addObserver(Observer o) {
        observers.add(o);
    }

    /**
     * Remove observer
     * @param o
     */
    public void removeObserver(Observer o) {
        observers.remove(o);
    }
    
    
    public void writeToStream(ObjectOutputStream objout) throws IOException {
       
        objout.writeInt(numAxes);
            objout.writeInt(numHats);
            objout.writeInt(numButtons);
            for (int i = 0; i < numAxes; i++) {
                objout.writeFloat(initAxes[i]);
                objout.writeInt(axesMask[i]);
            }
            for (int i = 0; i < numHats; i++) {
                objout.writeInt(initHats[i]);
                objout.writeInt(hatsMask[i]);
            }
            for (int i = 0; i < numButtons; i++) {
                objout.writeInt(initButtons[i]);
                objout.writeInt(buttonsMask[i]);
            }
    }
    
    public void readFromStream(ObjectInputStream objin) throws IOException {
         
            numAxes = objin.readInt();
            initAxes = new float[numAxes];
            axesMask = new int[numAxes];
            numHats = objin.readInt();
            initHats = new int[numHats];
            hatsMask = new int[numHats];
            numButtons = objin.readInt();
            initButtons = new int[numButtons];
            buttonsMask = new int[numButtons];
            for (int i = 0; i <numAxes; i++) {
                initAxes[i] = objin.readFloat();
                axesMask[i] = objin.readInt();
            }
            for (int i = 0; i < numHats; i++) {
               initHats[i] = objin.readInt();
               hatsMask[i] = objin.readInt();
            }
            for (int i = 0; i < numButtons; i++) {
               initButtons[i] = objin.readInt();
               buttonsMask[i] = objin.readInt();
            }
        
    }
    
    public boolean writeToFile(File f) {
         FileOutputStream outstream = null;
        ObjectOutputStream objout = null;
        boolean success = false;
        try {
            outstream = new FileOutputStream(f);
            objout = new ObjectOutputStream(outstream);
            writeToStream(objout);
            success = true;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(HidSetup.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(HidSetup.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                objout.close();
                outstream.close();
            } catch (IOException ex) {
                Logger.getLogger(HidSetup.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        return success;
        
    }
    
    public void readFromFile(File f) {
         FileInputStream instream = null;
        ObjectInputStream objin = null;
        try {
            instream = new FileInputStream(f);
            objin = new ObjectInputStream(instream);
            readFromStream(objin);
           

        }
        catch (FileNotFoundException ex) {
            Logger.getLogger(HidSetup.class.getName()).log(Level.SEVERE, null, ex);
        
        } catch (IOException ex) {
            Logger.getLogger(HidSetup.class.getName()).log(Level.SEVERE, null, ex);
        }  finally {
            try {
                objin.close();
                instream.close();
            } catch (IOException ex) {
                Logger.getLogger(FeatureManager.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        
 System.out.println("masks are " + print(axesMask) + "," + print(hatsMask) + "," +print(buttonsMask));

        
    }
    
    public String print(int a[]) {
        String s="";
        for (int i = 0; i < a.length; i++) {
            s += Integer.toString(a[i]);
        }
        return s;
    }
    
    public int getNumAxesTotal() {
        return initAxes.length;
    }
    
    public int getNumHatsTotal() {
        return initHats.length;
    }
    
    public int getNumButtonsTotal() {
        return initButtons.length;
    }
    
    public int getNumAxesReal() {
        int s = 0;
        for (int i=0; i < axesMask.length; i++) {
            s += axesMask[i];
        }
        return s;
    }
    
    public int getNumHatsReal() {
        int s = 0;
        for (int i=0; i < hatsMask.length; i++) {
            s += hatsMask[i];
        }
        return s;
    }
        public int getNumButtonsReal() {
        int s = 0;
        for (int i=0; i < buttonsMask.length; i++) {
            s += buttonsMask[i];
        }
        return s;
    }
        
    public int getNumFeaturesUsed() {
        return getNumAxesReal() + getNumHatsReal() + getNumButtonsReal();
    }
    
    public float[] getInitAxes() {
        return initAxes;
    }
    
    public int[] getInitHats() {
        return initHats;
    }
    
    public int[] getInitButtons() {
        return initButtons;
    }    
}
