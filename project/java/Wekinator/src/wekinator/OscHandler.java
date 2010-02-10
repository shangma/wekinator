/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wekinator;

import com.illposed.osc.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;
import wekinator.FeatureManager.*;

/**
 * //TODO here: fix  handling of backup, and of HID setup.
 * @author rebecca
 */
public class OscHandler {

    private static OscHandler ref = null;



    private BuildPanel bp = null;
    public int receivePort = 6448;
    public int sendPort = 6453;
    OSCPortOut sender;
    public OSCPortIn receiver;
   // WekaOperator w;
    String paramSendString = "/params";
    String returnHandshakeString = "/hiback";
    String featureInfoString = "/featureInfo";
    String featuresString = "/features"; //used for chuck features (not osc feats)
    String stopString = "/stop";
    String classLabelString = "/classLabel";
    String classDistString = "/classDist";
    String realValueRequestString = "/realValueRequest";
    String realValueString = "/realValue";
    String realLabelString = "/realLabel";
    String hidSetupString = "/hidSetup";
    String hidSetupBegunString = "/hidSetupBegun";
    String hidSetupStopString = "/hidSetupStop";
    String hidSetupStoppedString = "/hidSetupStopped";
    String hidInitString = "/hidInit";
    String sendHidInitValuesString = "/sendHidInitValues";
    String hidInitAllString = "/hidInitAll";
    String hidSettingsRequestString = "/hidSettingsRequest";
    String hidSettingsAllString = "/hidSettingsAll";
    String hidSettingsNumsString = "/hidSettingsNums";
    String hidSettingsAxesString = "/hidSettingsAxes";
    String hidSettingsHatsString = "/hidSettingsHats";
    String hidSettingsMaskString = "/hidSettingsMask";
    String hidSettingButtonsString = "/hidSettingsButtons";
    String hidRunString = "/hidRun";
    String setUseTrackpadFeature = "/useTrackpadFeature";
    String setUseMotionFeature = "/useMotionFeature";
    String setUseOtherHidFeature = "/useOtherHidFeature";
    String setUseProcessingFeature = "/useProcessingFeature";
    String requestNumParamsString = "/requestNumParams";
    String numParamsString = "/numParams"; //TODO TODO TODO Use this...
    String setUseAudioFeatureString = "/useAudioFeature";
    String setUseAllFeatureString = "/useFeatureList";
    String sendFeatureMessageString = "/useFeatureMessage";
    String trackpadMessageString = "trackpad";
    String motionMessageString = "motion";
    String processingMessageString = "processing";
    String customMessageString = "custom";
    String oscCustomMessageString = "oscCustom";
    String otherHidMessageString = "otherHid";
    String sendControlMessageString = "/control";
    String helloMessageString = "hello";
    String requestNumParamsMessageString = "requestNumParams";
    String requestChuckSettingsMessageString = "requestChuckSettings";
    String requestChuckSettingsArrayMsgString = "requestChuckSettingsArrays";
    String extractMessageString = "extract";
    String stopMessageString = "stop";
    String stopSoundMessageString = "stopSound";
    String startSoundMessageString = "startSound";
    String realValueRequestMessageString = "realValueRequest";
    String chuckSettingsString = "/chuckSettings";
    String startGettingParamsMessageString = "startGettingParams";
    String stopGettingParamsMessageString = "stopGettingParams";
    String startPlaybackMessageString = "startPlayback";
    String stopPlaybackMessageString = "stopPlayback";
    String playAlongMessage = "/playAlongMessage";
    String chuckSettingsArrayString = "/chuckSettingsArrays";
    Logger logger = Logger.getLogger(OscHandler.class.getName());

  

    public enum ConnectionState {

        NOT_CONNECTED, CONNECTING, CONNECTED, FAIL
    };
    private String myStatusMessage = "Not initialized";
    protected ConnectionState connectionState;
    public static final String PROP_CONNECTIONSTATE = "connectionState";

    /**
     * Get the value of connectionState
     *
     * @return the value of connectionState
     */
    public ConnectionState getConnectionState() {
        return connectionState;
    }

    /**
     * Set the value of connectionState
     *
     * @param connectionState new value of connectionState
     */
    private void setConnectionState(ConnectionState connectionState) {
        ConnectionState oldConnectionState = this.connectionState;
        this.connectionState = connectionState;
        setStatusStringForState();
        propertyChangeSupport.firePropertyChange(PROP_CONNECTIONSTATE, oldConnectionState, connectionState);
    }
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

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

    public static synchronized OscHandler getOscHandler() {
        if (ref == null) {
            ref = new OscHandler();
        }
        return ref;
    }

    private OscHandler() {
        // h.setOSCHandler(this); //TODO: get rid of this
    }

    /*  private OscHandler(WekaOperator w, int receivePort, int sendPort) throws SocketException, UnknownHostException {
    this.receivePort = receivePort;
    this.sendPort = sendPort;
    this.w = w;
    receiver = new OSCPortIn(receivePort);
    //  System.out.println("Java listening on " + receivePort);
    sender = new OSCPortOut(InetAddress.getLocalHost(), sendPort);
    // System.out.println("Java sending on " + sendPort);
    } */
    public void setHidSetup(HidSetup h) {
        //    this.h = h;
        //    h.setOSCHandler(this);
    }

    private void setStatusStringForState() {
        switch (connectionState) {
            case NOT_CONNECTED:
                myStatusMessage = "Not connected yet.";
                break;
            case CONNECTING:
                myStatusMessage = "Waiting for ChucK to finish connection";
                break;
            case CONNECTED:
                myStatusMessage = "Connected";
                break;
            case FAIL:
                myStatusMessage = "Connection failed.";
                break;
            default:
                myStatusMessage = "Other";


        }

    }

    public void startHandshake(int rPort, int sPort) throws IOException {
        /*if (receiver.isListening()) {
        receiver.close();
        } */ //TODO??

        setReceivePort(rPort);
        setSendPort(sPort);

        receiver = new OSCPortIn(receivePort);
        //  System.out.println("Java listening on " + receivePort);
        sender = new OSCPortOut(InetAddress.getLocalHost(), sendPort);
        // System.out.println("Java sending on " + sendPort);

        OSCListener listener = new OSCListener() {

            public void acceptMessage(java.util.Date time, OSCMessage message) {
             //   w.receivedHandshake();
                // oldState = ConnectionState.CONNECTED;
                setConnectionState(ConnectionState.CONNECTED);
                myStatusMessage = "Connected";
            //      notifyObservers();
            }
        };

        receiver.addListener(returnHandshakeString, listener);
        addFeatureInfoListener();
        addFeatureListener();
        addParamFromSynthListener();
        addHidSetupBegunListener();
        addHidSetupStoppedListener();
        addSendHidInitValuesListener();
        /*  addHidSettingsNumsListener();
        addHidSettingsAxesListener();
        addHidSettingsHatsListener();
        addHidSettingsButtonsListener();
        addHidSettingsMaskListener(); */
        addNumParamsListener();
        addChuckSettingsListener();
                addPlayalongMessageListener();
        addHidSettingsAllListener();
        addChuckSettingsArrayListener();
                OscController.addListeners(receiver);

        receiver.startListening();


        sendHandshakeMessage();

        //  oldState = ConnectionState.CONNECTING;


        setConnectionState(ConnectionState.CONNECTING);
    // notifyObservers();

    }

    public String getStatusMessage() {
        return myStatusMessage;
    }

    /* private void notifyObservers() {
    // loop through and notify each observer
    Iterator<Observer> i = observers.iterator();
    while (i.hasNext()) {
    Observer o = i.next();
    o.update(this, oldState, getStatusMessage());
    }
    } */
    private void sendHandshakeMessage() throws IOException {
        Object[] o = new Object[2];
        o[0] = helloMessageString;
        o[1] = new Integer(0);
        OSCMessage msg = new OSCMessage(sendControlMessageString, o);
        sender.send(msg);
    }

    public void stopSound() {
        try {
            Object[] o = new Object[2];
        o[0] = stopSoundMessageString;
        o[1] = new Integer(0);
        OSCMessage msg = new OSCMessage(sendControlMessageString, o);
        sender.send(msg);
         } catch (IOException ex) {
            Logger.getLogger(OscHandler.class.getName()).log(Level.SEVERE, null, ex);
            errorHappened(ex);
        }
    }

    public void startSound() {
        try {
            Object[] o = new Object[2];
            o[0] = startSoundMessageString;
            o[1] = new Integer(0);
            OSCMessage msg = new OSCMessage(sendControlMessageString, o);
            sender.send(msg);
        } catch (IOException ex) {
            Logger.getLogger(OscHandler.class.getName()).log(Level.SEVERE, null, ex);
            errorHappened(ex);
        }
    }

    private void errorHappened(Exception ex) {
        //TODO: Action to advertise that OSC connection is having problems
        System.out.println("OSC connection problem");
    }

    void askForCurrentValue() {
        try {
            Object[] o = new Object[2];
        o[0] = realValueRequestMessageString;
        o[1] = new Integer(0);
        OSCMessage msg = new OSCMessage(sendControlMessageString, o);
        sender.send(msg);
        System.out.println("Asked for current value");
         } catch (IOException ex) {
            Logger.getLogger(OscHandler.class.getName()).log(Level.SEVERE, null, ex);
            errorHappened(ex);
        }
    }

    //Start getting params from the synth
    void startGettingParams() {
        Object[] o = new Object[2];
        o[0] = startGettingParamsMessageString;
        o[1] = new Integer(0);
        OSCMessage msg = new OSCMessage(sendControlMessageString, o);
        try {
            sender.send(msg);
        } catch (IOException ex) {
            Logger.getLogger(OscHandler.class.getName()).log(Level.SEVERE, null, ex);
            errorHappened(ex);
        }

    }

    //Stop getting params from the synth
    void stopGettingParams()  {
        Object[] o = new Object[2];
        o[0] = stopGettingParamsMessageString;
        o[1] = new Integer(0);
        OSCMessage msg = new OSCMessage(sendControlMessageString, o);
        try {
            sender.send(msg);
        } catch (IOException ex) {
            Logger.getLogger(OscHandler.class.getName()).log(Level.SEVERE, null, ex);
            errorHappened(ex);
        }
    }

    void requestHidSetup() throws IOException {
        Object[] o = new Object[1];
        o[0] = new Integer(0);
        OSCMessage msg = new OSCMessage(hidSetupString, o);
        sender.send(msg);
        System.out.println("Requested hid setup start");
    }

    void setUseTrackpad(boolean useTrackpad) throws IOException {
        Object[] o = new Object[2];
        o[0] = trackpadMessageString;
        o[1] = new Integer(useTrackpad ? 1 : 0);
        OSCMessage msg = new OSCMessage(sendFeatureMessageString, o);
        sender.send(msg);
    }

    void setUseCustom(boolean useCustom, int numCustomChuck) throws IOException {
        Object[] o = new Object[2];
        o[0] = customMessageString;
        o[1] = new Integer(useCustom ? numCustomChuck : 0);
        OSCMessage msg = new OSCMessage(sendFeatureMessageString, o);
        sender.send(msg);
    }

    void setUseOscCustom(boolean useOscCustom, int numCustom) throws IOException {
        Object[] o = new Object[2];
        o[0] = oscCustomMessageString;
        o[1] = new Integer(useOscCustom ? numCustom : 0);
        OSCMessage msg = new OSCMessage(sendFeatureMessageString, o);
        sender.send(msg);
    }

    void setUseAudio(boolean useAudio, boolean useFFT, boolean useRMS, boolean useCentroid, boolean useRolloff, boolean useFlux, int fftSize, int windowSize, WindowTypes windowType, int audioExtractionRate) throws IOException {
        System.out.println("Setting use audio: " + useAudio);
        Object[] o = new Object[10];
        o[0] = new Integer(useAudio ? 1 : 0);
        o[1] = new Integer(useFFT ? 1 : 0);
        o[2] = new Integer(useRMS ? 1 : 0);
        o[3] = new Integer(useCentroid ? 1 : 0);
        o[4] = new Integer(useRolloff ? 1 : 0);
        o[5] = new Integer(useFlux ? 1 : 0);
        o[6] = new Integer(fftSize);
        o[7] = new Integer(windowSize);
        if (windowType == WindowTypes.Hamming) {
            o[8] = new Integer(3);
        } else if (windowType == WindowTypes.Hann) {
            o[8] = new Integer(2);
        } else {
            o[8] = new Integer(0);
        }
        o[9] = new Integer(audioExtractionRate);

        OSCMessage msg = new OSCMessage(setUseAudioFeatureString, o);
        sender.send(msg);
    }

    void setUseMotion(boolean useMotion, int motionExtractionRate) throws IOException {
        Object[] o = new Object[2];
        o[0] = motionMessageString;
        o[1] = new Integer(useMotion ? motionExtractionRate : 0);
        OSCMessage msg = new OSCMessage(sendFeatureMessageString, o);
        sender.send(msg);
    }

    void setUseOtherHid(boolean useOtherHid) throws IOException {
        Object[] o = new Object[2];
        o[0] = otherHidMessageString;
        o[1] = new Integer(useOtherHid ? 1 : 0);
        OSCMessage msg = new OSCMessage(sendFeatureMessageString, o);
        sender.send(msg);
    }

    void setUseProcessing(boolean useProcessing, int numFeats) throws IOException {
        Object[] o = new Object[2];
        o[0] = processingMessageString;
        o[1] = new Integer(numFeats);
        OSCMessage msg = new OSCMessage(sendFeatureMessageString, o);
        sender.send(msg);
    }

    void requestHidSetupStop() throws IOException {
        Object[] o = new Object[1];
        o[0] = new Integer(0);
        OSCMessage msg = new OSCMessage(hidSetupStopString, o);
        sender.send(msg);
        System.out.println("Requested hid setup stop");
    }

    void sendHidInit(int numAxes, int numHats, int numButtons) throws IOException {
        Object[] o = new Object[3];
        o[0] = new Integer(numAxes);
        o[1] = new Integer(numHats);
        o[2] = new Integer(numButtons);
        OSCMessage msg = new OSCMessage(hidInitString, o);
        sender.send(msg);
        System.out.println("Sent first hid init string");
    }

    void sendHidSettingsRequest() throws IOException {
        Object[] o = new Object[1];
        o[0] = new Integer(0);
        OSCMessage msg = new OSCMessage(hidSettingsRequestString, o);
        sender.send(msg);
        System.out.println("Requested hid settings");

    }

    void startHidRun() throws IOException {
        Object[] o = new Object[1];
        o[0] = new Integer(0);
        OSCMessage msg = new OSCMessage(hidRunString, o);
        sender.send(msg);
        System.out.println("Requested hid run");
    }

    void sendHidValues(float[] initAxes, int[] initHats, int[] initButtons, int[] axesMask, int[] hatsMask, int[] buttonsMask) throws IOException {
        Object[] o = new Object[1 + (initAxes.length + initHats.length + initButtons.length) * 2];
        int counter = 0;

        //Send dummy value in case HID is empty
        o[0] = new Integer(0);
        counter++;

        for (int i = 0; i < initAxes.length; i++) {
            o[counter] = new Float(initAxes[i]);
            counter++;
        }
        for (int i = 0; i < initHats.length; i++) {
            o[counter] = new Integer(initHats[i]);
            counter++;
        }
        for (int i = 0; i < initButtons.length; i++) {
            o[counter] = new Integer(initButtons[i]);
            counter++;
        }
        for (int i = 0; i < axesMask.length; i++) {
            o[counter] = new Integer(axesMask[i]);
            counter++;
        }
        for (int i = 0; i < hatsMask.length; i++) {
            o[counter] = new Integer(hatsMask[i]);
            counter++;
        }

        for (int i = 0; i < buttonsMask.length; i++) {
            o[counter] = new Integer(buttonsMask[i]);
            counter++;
        }

        OSCMessage msg = new OSCMessage(hidInitAllString, o);
        sender.send(msg);

    /*  Object[] o1 = new Object[initAxes.length];
    for (int i = 0; i < initAxes.length; i++) {
    o1[i] = new Float(initAxes[i]);
    }
    OSCMessage msg1 = new OSCMessage(hidInitAxesString, o1);
    sender.send(msg1);

    Object[] o2 = new Object[initHats.length];
    for (int i = 0; i < initHats.length; i++) {
    o2[i] = new Integer(initHats[i]);
    }
    OSCMessage msg2 = new OSCMessage(hidInitHatsString, o2);
    sender.send(msg2);

    Object[] o3 = new Object[initButtons.length];
    for (int i = 0; i < initButtons.length; i++) {
    o3[i] = new Integer(initButtons[i]);
    }
    OSCMessage msg3 = new OSCMessage(hidInitButtonsString, o3);
    sender.send(msg3);

    Object[] o4 = new Object[initAxes.length + initHats.length + initButtons.length];
    int j = 0;
    for (int i = 0; i < axesMask.length; i++) {
    o4[j] = axesMask[i];
    j++;
    }
    for (int i = 0; i < hatsMask.length; i++) {
    o4[j] = hatsMask[i];
    j++;
    }
    for (int i = 0; i < buttonsMask.length; i++) {
    o4[j] = buttonsMask[i];
    j++;
    }
    OSCMessage msg4 = new OSCMessage(hidInitMaskString, o4);
    sender.send(msg4);

    System.out.println("Sent 4 init strings"); */
    }

    public void end() {
        receiver.stopListening();
        receiver.close(); //this line causes errors!!

        sender.close();
        //  oldState = ConnectionState.NOT_CONNECTED;
        //  notifyObservers();
        setConnectionState(ConnectionState.NOT_CONNECTED);
    }

    public void disconnect() {
        // oldState = ConnectionState.NOT_CONNECTED;
        setConnectionState(ConnectionState.NOT_CONNECTED);

    }

    //Initiates extraction of features by chuck, so that chuck will start sending us features*/
    public void initiateRecord() {

        Object[] o = new Object[2];
        o[0] = extractMessageString;
        o[1] = new Integer(0);
        OSCMessage msg = new OSCMessage(sendControlMessageString, o);
        try {
            sender.send(msg);
        } catch (IOException ex) {
            Logger.getLogger(OscHandler.class.getName()).log(Level.SEVERE, null, ex);
            errorHappened(ex);
        }
    }

    public void initiateClassify() throws IOException {
        initiateRecord();
    }

    //Stop extracting features
    public void stopExtractingFeatures()  {
        Object[] o = new Object[2];
        o[0] = stopMessageString;
        o[1] = new Integer(0);
        OSCMessage msg = new OSCMessage(sendControlMessageString, o);
        try {
            sender.send(msg);
            //   System.out.println("h sent stop " + sendControlMessageString + " " + stopMessageString);
        } catch (IOException ex) {
            Logger.getLogger(OscHandler.class.getName()).log(Level.SEVERE, null, ex);
            errorHappened(ex);
        }
     //   System.out.println("h sent stop " + sendControlMessageString + " " + stopMessageString);
    }

    public void sendClass(int c) {
        Object[] o = new Object[1];
        o[0] = new Integer(c);

        OSCMessage msg = new OSCMessage(classLabelString, o);
        try {

            sender.send(msg);
        //System.out.println("sent label... I think");
        } catch (IOException ex) {
            System.out.println("123");

            Logger.getLogger(OscHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    void sendClassMulti(int[] vals) {
        /*    Object[] o = new Object[vals.length];
        for (int i = 0; i < vals.length; i++) {
        o[i] = new Integer(vals[i]);

        }

        OSCMessage msg = new OSCMessage(classLabelString, o);
        try {

        sender.send(msg);
        //System.out.println("sent label... I think");
        } catch (IOException ex) {
        System.out.println("123");

        Logger.getLogger(OscHandler.class.getName()).log(Level.SEVERE, null, ex);
        } */

        float[] fvals = new float[vals.length];
        for (int i = 0; i < vals.length; i++) {
            fvals[i] = vals[i];

        }
        sendRealValueMulti(fvals);

    }

    public void sendRealValue(double r) {
        float[] vals = new float[1];
        vals[0] = (float) r;
        sendRealValueMulti(vals);
    }

    public void requestChuckSettings() {
        Object[] o = new Object[2];
        o[0] = requestChuckSettingsMessageString;
        o[1] = new Integer(0);
        OSCMessage msg = new OSCMessage(sendControlMessageString, o);
        try {

            sender.send(msg);
        //System.out.println("sent label... I think");
        } catch (IOException ex) {
            System.out.println("123");
            Logger.getLogger(OscHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    void requestChuckSettingsArray() {
        logger.log(Level.INFO, "Requesting chuck settings array");
        Object[] o = new Object[2];
        o[0] = requestChuckSettingsArrayMsgString;
        o[1] = new Integer(0);
        OSCMessage msg = new OSCMessage(sendControlMessageString, o);
        try {

            sender.send(msg);
        //System.out.println("sent label... I think");
        } catch (IOException ex) {
            System.out.println("123");
            Logger.getLogger(OscHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    void sendParamsToSynth(double[] params) {
        Object[] o = new Object[params.length];
        try {

            for (int i = 0; i < params.length; i++) {
                o[i] = new Float(params[i]);
            }

            OSCMessage msg = new OSCMessage(paramSendString, o);
            sender.send(msg);
        } catch (IOException ex) {
            Logger.getLogger(OscHandler.class.getName()).log(Level.SEVERE, null, ex);
            errorHappened(ex);
        }

    }

    void sendRealValueMulti(float[] realVals) {
        //   System.out.println("sending real values:");
        Object[] o = new Object[realVals.length];

        try {

            for (int i = 0; i < realVals.length; i++) {
                o[i] = new Float(realVals[i]);
            }

            OSCMessage msg = new OSCMessage(realLabelString, o);
            sender.send(msg);
        //   System.out.println("sent labels... I think, for len=" + realVals.length);
        } catch (IOException ex) {
            System.out.println("123");

            Logger.getLogger(OscHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void sendClass(int c, double[] dist) {
        Object[] o = new Object[1];
        o[0] = new Integer(c);

        OSCMessage msg = new OSCMessage(classLabelString, o);
        try {
            int n = 0;
            for (int i = 0; i < dist.length; i++) {
                msg.addArgument(new Double(dist[i]));
                n++;
            }

            System.out.println("n added: " + n);


            sender.send(msg);
        //System.out.println("sent label... I think");
        } catch (IOException ex) {
            System.out.println("123");

            Logger.getLogger(OscHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //TODO: implement sending method for multi-class distribution
    public void sendDist(double[] dist) {
        OSCMessage msg = new OSCMessage(classDistString);
        try {
            int i = 0;
            for (; i < dist.length; i++) {
                msg.addArgument(new Float((float) dist[i]));
            }
            sender.send(msg);
            System.out.println("sent: " + i);
        //System.out.println("sent label... I think");
        } catch (IOException ex) {
            System.out.println("123");

            Logger.getLogger(OscHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    void sendDistMulti(double[][] dists) {

        OSCMessage msg = new OSCMessage(classDistString);
        try {
            for (int j = 0; j < dists.length; j++) {

                for (int i = 0; i < dists[j].length; i++) {
                    msg.addArgument(new Float((float) dists[j][i]));
                }

            //System.out.println("sent label... I think");
            }
            sender.send(msg);
            System.out.println("sent: " + dists.length * dists[0].length);
        } catch (IOException ex) {
            System.out.println("123");

            Logger.getLogger(OscHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void addFeatureInfoListener() {
        OSCListener listener = new OSCListener() {

            public void acceptMessage(java.util.Date time, OSCMessage message) {
                System.out.println("Feature info received!");
                Object[] o = message.getArguments();
                if (o.length > 0 && (o[0] instanceof java.lang.Integer)) {
                   // w.receivedFeatureInfo((Integer) o[0]);
                }
            }
        };
        receiver.addListener(featureInfoString, listener);
    }

    private void addNumParamsListener() {
        OSCListener listener = new OSCListener() {

            public void acceptMessage(java.util.Date time, OSCMessage message) {
                System.out.println("# params received!");
                Object[] o = message.getArguments();

             //   w.receivedNumParams(o);

            }
        };
        receiver.addListener(numParamsString, listener);
    }

    private void addPlayalongMessageListener() {
        OSCListener listener = new OSCListener() {

            public void acceptMessage(java.util.Date time, OSCMessage message) {
                System.out.println("playalong message received!"); //only gets here after "Play along" executed-- because xmit not ready yet!!
                Object[] o = message.getArguments();
            //    w.receivedPlayalongUpdate((String) o[0]);
                if (bp != null) {
                    bp.updatePlayalongMessage((String) o[0]);
                }
            }
        };
        receiver.addListener(playAlongMessage, listener);
        System.out.println("playalong message listener added");
    }

    private void addChuckSettingsListener() {
        OSCListener listener = new OSCListener() {

            public void acceptMessage(java.util.Date time, OSCMessage message) {
                System.out.println("settings received!");
                Object[] o = message.getArguments();
                System.out.println("class " + o[0].getClass().toString());

             //   w.receivedChuckSettings(o);

            }
        };
        receiver.addListener(chuckSettingsString, listener);
    }

    private void addChuckSettingsArrayListener() {
        OSCListener listener = new OSCListener() {

            public void acceptMessage(java.util.Date time, OSCMessage message) {
                System.out.println("settings received!");
                Object[] o = message.getArguments();
                //   System.out.println("class " + o[0].getClass().toString());

                //w.receivedChuckSettingsArray(o);
                ChuckSystem.getChuckSystem().receivedChuckSettings(o);

            }
        };
        receiver.addListener(chuckSettingsArrayString, listener);
    }

    private void addFeatureListener() {
        OSCListener listener = new OSCListener() {

            public void acceptMessage(java.util.Date time, OSCMessage message) {
                 System.out.println("Feature received!");
                Object[] o = message.getArguments();
                double d[]  = new double[o.length];
                for (int i = 0; i < o.length; i++) {
                    if (o[i] instanceof Float) {
                        d[i] = ((Float) o[i]).floatValue();
                    } else {
                        Logger.getLogger(OscHandler.class.getName()).log(Level.WARNING, "Received feature is not a float");
                    }
                }
                FeatureExtractionController.updateFeatures(d);
            }
        };
        receiver.addListener(featuresString, listener);
    }

    private void addParamFromSynthListener() {
        OSCListener listener = new OSCListener() {

            public void acceptMessage(java.util.Date time, OSCMessage message) {
                try {
                    //    System.out.println("Real value received!");
                    Object[] o = message.getArguments();
                    //  try {
                    //Introduce delay here?
                    // Thread.sleep(500);
                    // } catch (InterruptedException ex) {
                    //    Logger.getLogger(OscHandler.class.getName()).log(Level.SEVERE, null, ex);
                    // }
                   
                    
                    // w.receivedRealValue(o);

                    //Received params from synth:
                   double d[]  = new double[o.length];
                    for (int i = 0; i < o.length; i++) {
                    if (o[i] instanceof Float) {
                        d[i] = ((Float) o[i]).floatValue();
                    } else {
                        Logger.getLogger(OscHandler.class.getName()).log(Level.WARNING, "Received feature is not a float");
                    }
                    }

                    System.out.println("received params from synth");
                    WekinatorLearningManager.getInstance().setParams(d);

                } catch (Exception ex) {
                    Logger.getLogger(OscHandler.class.getName()).log(Level.WARNING, null, ex);
                }

            }
        };
        receiver.addListener(realValueString, listener);
    }

    private void addHidSetupBegunListener() {
        OSCListener listener = new OSCListener() {

            public void acceptMessage(java.util.Date time, OSCMessage message) {
                System.out.println("Hid Setup begun");
                Object[] o = message.getArguments();
                WekinatorInstance.getWekinatorInstance().getCurrentHidSetup().setupBegun();
            }
        };
        receiver.addListener(hidSetupBegunString, listener);

    }

    private void addHidSetupStoppedListener() {
        OSCListener listener = new OSCListener() {

            public void acceptMessage(java.util.Date time, OSCMessage message) {
                try {
                    System.out.println("Hid Setup stopped");
                    Object[] o = message.getArguments();
                    System.out.flush();
                    WekinatorInstance.getWekinatorInstance().getCurrentHidSetup().setupStopped();
                } catch (IOException ex) {
                    Logger.getLogger(OscHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        receiver.addListener(hidSetupStoppedString, listener);
    }

    private void addSendHidInitValuesListener() {
        OSCListener listener = new OSCListener() {

            public void acceptMessage(java.util.Date time, OSCMessage message) {
                System.out.println("Hid received send hid init values message");
                Object[] o = message.getArguments();
                try {
                    WekinatorInstance.getWekinatorInstance().getCurrentHidSetup().receivedSendHidInitValues();
                } catch (IOException ex) {
                    Logger.getLogger(OscHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        receiver.addListener(sendHidInitValuesString, listener);
    }

    /* private void addHidSettingsNumsListener() {
    OSCListener listener = new OSCListener() {

    public void acceptMessage(java.util.Date time, OSCMessage message) {
    System.out.println("Received hid settings nums");
    Object[] o = message.getArguments();
    if (o.length == 3) {
    Integer a = (Integer) o[0];
    Integer b = (Integer) o[1];
    Integer c = (Integer) o[2];
    WekinatorInstance.getWekinatorInstance().getCurrentHidSetup().receivedHidSettingsNums(a, b, c);
    } else {
    System.out.println("Wrong number of nums received");
    }
    }
    };
    receiver.addListener(hidSettingsNumsString, listener);

    } */
    private void addHidSettingsAllListener() {
        OSCListener listener = new OSCListener() {

            public void acceptMessage(java.util.Date time, OSCMessage message) {
                System.out.println("Received hid settings all");
                Object[] o = message.getArguments();

                WekinatorInstance.getWekinatorInstance().getCurrentHidSetup().receivedHidSettingsAll(o);

            }
        };
        receiver.addListener(hidSettingsAllString, listener);

    }

    /*  private void addHidSettingsAxesListener() {
    OSCListener listener = new OSCListener() {

    public void acceptMessage(java.util.Date time, OSCMessage message) {
    System.out.println("Received hid settings axes");
    Object[] o = message.getArguments();
    float f[] = new float[o.length];
    for (int i = 0; i < o.length; i++) {
    f[i] = ((Float) o[i]).floatValue();
    }
    WekinatorInstance.getWekinatorInstance().getCurrentHidSetup().receivedHidAxisSettings(f);
    }
    };
    receiver.addListener(hidSettingsAxesString, listener);

    } */

    /*  private void addHidSettingsHatsListener() {
    OSCListener listener = new OSCListener() {

    public void acceptMessage(java.util.Date time, OSCMessage message) {
    System.out.println("Received hid settings hats");
    Object[] o = message.getArguments();
    int f[] = new int[o.length];
    for (int i = 0; i < o.length; i++) {
    f[i] = ((Integer) o[i]).intValue();
    }
    WekinatorInstance.getWekinatorInstance().getCurrentHidSetup().receivedHidHatsSettings(f);
    }
    };
    receiver.addListener(hidSettingsHatsString, listener);

    } */

    /*  private void addHidSettingsButtonsListener() {
    OSCListener listener = new OSCListener() {

    public void acceptMessage(java.util.Date time, OSCMessage message) {
    System.out.println("Received hid settings buttons");
    Object[] o = message.getArguments();
    int f[] = new int[o.length];
    for (int i = 0; i < o.length; i++) {
    f[i] = ((Integer) o[i]).intValue();
    }
    WekinatorInstance.getWekinatorInstance().getCurrentHidSetup().receivedHidButtonsSettings(f);
    }
    };
    receiver.addListener(hidSettingButtonsString, listener);

    }

    private void addHidSettingsMaskListener() {
    OSCListener listener = new OSCListener() {

    public void acceptMessage(java.util.Date time, OSCMessage message) {
    System.out.println("Received hid settings mask");
    Object[] o = message.getArguments();
    int f[] = new int[o.length];
    for (int i = 0; i < o.length; i++) {
    f[i] = ((Integer) o[i]).intValue();
    }
    WekinatorInstance.getWekinatorInstance().getCurrentHidSetup().receivedHidMaskSettings(f);
    }
    };
    receiver.addListener(hidSettingsMaskString, listener);

    } */
    public void playScore() {
        Object[] o = new Object[2];
        o[0] = startPlaybackMessageString;
        o[1] = new Integer(0);
        OSCMessage msg = new OSCMessage(sendControlMessageString, o);
        try {
            sender.send(msg);
        } catch (IOException ex) {
            Logger.getLogger(OscHandler.class.getName()).log(Level.SEVERE, null, ex);
            errorHappened(ex);
        }
        System.out.println("Asked for start playback");

    }

    public void stopPlayback() {
        Object[] o = new Object[2];
        o[0] = stopPlaybackMessageString;
        o[1] = new Integer(0);
        OSCMessage msg = new OSCMessage(sendControlMessageString, o);
        try {
            sender.send(msg);
        } catch (IOException ex) {
            Logger.getLogger(OscHandler.class.getName()).log(Level.SEVERE, null, ex);
            errorHappened(ex);
        }
        System.out.println("Asked for start playback");
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public int getReceivePort() {
        return receivePort;
    }

    public void setReceivePort(int receivePort) {
        this.receivePort = receivePort;
    }

    public int getSendPort() {
        return sendPort;
    }

    public void setSendPort(int sendPort) {
        this.sendPort = sendPort;
    }

    public void sendFeatureConfiguration(FeatureConfiguration f) throws IOException {
        Object[] o = new Object[16]; //TODO
        o[0] = new Integer((f.getNumAudioFeatures() > 0) ? 1 : 0);
        o[1] = new Integer(f.isUseFFT() ? 1 : 0);
        o[2] = new Integer(f.isUseRMS() ? 1 : 0);
        o[3] = new Integer(f.isUseCentroid() ? 1 : 0);
        o[4] = new Integer(f.isUseRolloff() ? 1 : 0);
        o[5] = new Integer(f.isUseFlux() ? 1 : 0);
        o[6] = new Integer(f.getFftSize());
        o[7] = new Integer(f.getFftWindowSize());
        if (f.getWindowType() == FeatureConfiguration.WindowType.HAMMING) {
            o[8] = new Integer(3);
        } else if (f.getWindowType() == FeatureConfiguration.WindowType.HANN) {
            o[8] = new Integer(2);
        } else {
            o[8] = new Integer(0);
        }
        o[9] = new Integer(f.getAudioExtractionRate());

        o[10] = new Integer(f.isUseTrackpad() ? 1 : 0);
        o[11] = new Integer(f.isUseMotionSensor() ? f.getMotionSensorExtractionRate() : 0);
        o[12] = new Integer(f.isUseOtherHid() ? 1 : 0);
        o[13] = new Integer(f.isUseProcessing() ? f.getNumProcessingFeatures() : 0);
        o[14] = new Integer(f.isUseCustomOscFeatures() ? f.getNumCustomOscFeatures() : 0);
        o[15] = new Integer(f.isUseCustomChuckFeatures() ? f.getNumCustomChuckFeatures() : 0);

        OSCMessage msg = new OSCMessage(setUseAllFeatureString, o);
        sender.send(msg);

    }
      public void setBuildPanel(BuildPanel aThis) {
        bp = aThis;
    }
}
