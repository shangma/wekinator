/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package KyleWrapper;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author rebecca
 */
public class WekinatorBeatboxLogger {

    //protected static final WekinatorBeatboxLogger ref = new WekinatorBeatboxLogger();
    static FileWriter fw = null;
    static PrintWriter actionLog = null; //Used for logging user actions & method calls over time
    static FileWriter rfw = null;
    static PrintWriter dataLog = null; //Used for logging examples + features over time.
    static String filename = null;
    static String rfilename = null;
    public static boolean isLogging = true;
    protected static long sessionID = 0;
    protected static String lastRunStartTime = "0";
    protected static SimpleDateFormat shortFormat = new SimpleDateFormat("HH:mm:ss.SSS");

    private WekinatorBeatboxLogger() {
    }

    public static void log(Msg which) {
        log(which, "");
    }

    //TODO: make sure all of these are getting called (mark with "X")
    public enum Msg {
        LOAD_FROM_FILE, //X
        NEW_SESSION, //X
        RESET, //X
        CLOSE, //X 
        EXAMPLE_LOG_WITH_FEATURES, //X
        CLASSIFICATION_LOG_WITH_FEATURES, //X
        EXAMPLE_RECORDED_INTO_PAD, //X
        EXAMPLE_DELETED_FROM_PAD, //X
        REALTIME_CLASSIFICATION_MADE, // Rebecca: log # training examples somewhere?
        PAD_ENABLED, //X
        PAD_DISABLED, //X
        PAD_STATES_LOGGED, //X
        LOG_SUMMARY_BEGIN, //X
        LOG_SUMMARY_END, //X
        LOG_SUMMARY_PAD_NUM_CORRECT, //X
        LOG_SUMMARY_PAD_DETAIL, //X
        LOG_SUMMARY_STATS, //X
        TRACK_RECORD_STARTED, //X
        TRACK_RECORD_FINISHED, //X
        TRACK_CREATED, //X
        TRACK_DELETED, //X
        TRACK_ELEMENT_CHANGED, //X
        TRACK_PLAYED, //X
        THRESHOLD_ADJUSTED, //TODO: Kyle
        PAD_CREATED, //X
        PAD_NAME_CHANGED, //X
        PAD_AUDIO_FILE_CHANGED, //X
        START_RECORDING_EXAMPLES_CALLED, //X
        STOP_RECORDING_EXAMPLES_CALLED, // X
        START_RUNNING_CALLED, //X
        STOP_RUNNING_CALLED, //X
        DELETE_TRAINING_EXAMPLE_CALLED, // X Could get rid of this if loggging from Kyle's code
        DELETE_TRAINING_EXAMPLES_CALLED, //X
        DELETE_ALL_EXAMPLES_CALLED, //X All examples deleted (logged by Wek)
        SAVED_TO_FILE, //X
        MOUSEOVER_EXAMPLE, // X
        EXPERIMENT_BEGIN, //X
        EXPERIMENT_END //X
    };
    private static boolean isSetup = false;

    public static void setup(String parentDir, BeatboxWekinatorWrapper wrap) throws IOException {
        if (!isSetup) {
            /* wrap.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent pce) {
            }
            });   */ // RF: I don't think we need to listen for property changes on wrapper

            Date d = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            sessionID = Long.parseLong(dateFormat.format(d));
            filename = parentDir + File.separator + dateFormat.format(d) + "actionlog.txt";
            rfilename = parentDir + File.separator + dateFormat.format(d) + "datalog.txt";
            fw = new FileWriter(filename, true);
            actionLog = new PrintWriter(fw);
            rfw = new FileWriter(rfilename, true);
            dataLog = new PrintWriter(rfw);
            log(Msg.NEW_SESSION, "" + sessionID);
            resetDatalog();
            isSetup = true;
        }
    }

    public static void startLogging() {
        try {
            actionLog.close();
            fw.close();
            fw = new FileWriter(filename, false);
            actionLog = new PrintWriter(fw);

            dataLog.close();
            rfw.close();
            rfw = new FileWriter(rfilename, false);
            dataLog = new PrintWriter(rfw);

            log(Msg.RESET);
            resetDatalog();
        } catch (Exception ex) {
            System.out.println("problem starting log.");
            ex.printStackTrace();
        }
    }

    private static void resetDatalog() {
        dataLog.println(ts() + ",#RESET#");
    }

    private static void datalogLogClose() {
        dataLog.println(ts() + ",#CLOSE#");
    }

    public static void close() throws IOException {
        log(Msg.CLOSE);
        actionLog.flush();
        actionLog.close();
        fw.close();

        datalogLogClose();
        dataLog.flush();
        dataLog.close();
        rfw.close();
    }

    public static void flush() {
        actionLog.flush();
        dataLog.flush();
    }

    //Timestamp
    protected static String ts() {
        //Add readable timestamp:
        Date d = new Date();
        return (Long.toString(d.getTime()) + "," + shortFormat.format(d));
        // sessionID = Long.parseLong(dateFormat.format(d));
        // return Long.toString((new Date()).getTime());
        //return Long.toString((new Date()).getTime());
    }

    public static void log(String ts, Msg which, String m) {
        if (actionLog == null) {
            System.out.println("ERROR: logging with null log!");
            return;
        }
        actionLog.println(ts + "," + which.ordinal() + "," + which + "," + m);
    }

    //Kyle: FYI: You can call this anywhere you want a given log message to occur
    //e.g. WekinatorBeatboxLogger.log(Msg.MY_NEW_MESSGE, "" + someDataIcareAbout + "," + 34342 + "," + "etc");
    public static void log(Msg which, String m) {
        if (actionLog == null) {
            System.out.println("ERROR: logging with null log!");
            return;
        }
        actionLog.println(ts() + "," + which.ordinal() + "," + which + "," + m);
    }

    //TODO: Kyle: VERY IMPORTANT: Call this at all points where we want a snapshot of the state of the pads & examples
    // For now, call whenever adding example(s), deleting example(s), enabling or disabling pads
    // here using padIDs so that we can track an individual pad over time
    // Ideally this won't be too expensive that we can do it frequently (ie.., any time an example is added/deleted or pad is enabled/disabled)
    //
    // IMPORTANT: Read closely:
    // Arguments:   padIDs (in no particular order, including both enabled & disabled pads)
    //              isPadEnabled: boolean value for each pad, in same order as padIDs
    //              numExamplesInPad: int value for # examples in pad, in same order as padIDs
    //              exampleInPadClassifiedAs:
    //                  IF pad is enabled, contains one int array per pad, where each int array has one element per example in that pad
    //                      that is, exampleInPadClassifiedAs[i][j] = k  means that the j-th example in the i-th pad (where the ith pad id = padIDs[i]) is classified as padID k
    //                      and exampleInPadClassifiedAs[i][j] == padIDs[i] is true if and only if the j-th example in the i-th pad is classified correctly
    //                  ELSE, i.e. pad is not enabled, exampleInPadClassifiedAs[i] = null or something else (doesn't matter)
    //
    // TODO: Kyle: Is there any reason we want to log individual example IDs in each pad here?
    public static void logPadSummary(int[] padIds, boolean[] isPadEnabled, int[] numExamplesInPad, int[][] exampleInPadClassifiedAsPad) {
        log(Msg.LOG_SUMMARY_BEGIN, "numPads=," + padIds.length); //print a summary begin message
        int numEnabledPads = 0;
        int numTotalExamples = 0;
        int numEnabledExamples = 0;
        int numCorrectEnabledExamples = 0;

        //Print a pad summary line, 1 per pad
        for (int i = 0; i < padIds.length; i++) {
            int correctExamplesInThisPad = 0;
            if (isPadEnabled[i]) {
                numEnabledPads++;
                numEnabledExamples += numExamplesInPad[i];
            }
            numTotalExamples += numExamplesInPad[i];

            //Print a pad example detail line, 1 per pad
            //Print: true pad id, # examples, then 1 pad ID per example
            Msg which2 = Msg.LOG_SUMMARY_PAD_DETAIL;
            actionLog.print(ts() + "," + which2.ordinal() + "," + which2 + "," + i + ",padId=," + padIds[i] + ",isEnabled=," + isPadEnabled[i] + ",numExamples=," + numExamplesInPad[i]);
            if (isPadEnabled[i]) {
                actionLog.print(",ExamplesClassifiedAs=");
                for (int j = 0; j < exampleInPadClassifiedAsPad[i].length; j++) {
                    if (exampleInPadClassifiedAsPad[i][j]==padIds[i]) {
                        correctExamplesInThisPad++;
                    }
                    actionLog.print("," + exampleInPadClassifiedAsPad[i][j]);
                }
            } else {
                correctExamplesInThisPad = -1;
            }
            actionLog.println();


            if (isPadEnabled[i]) {
                log(Msg.LOG_SUMMARY_PAD_NUM_CORRECT, "padnum=," + i + ",padID=," + padIds[i] + ", numCorrect=," + correctExamplesInThisPad);
                numCorrectEnabledExamples += correctExamplesInThisPad;
            }
        }

        //Log numExamples per each pad
        //Do we want to log information about why something is misclassified? (i.e., neighbor counts?)
        log(Msg.LOG_SUMMARY_STATS, "numEnabledPads=," + numEnabledPads + ",numTot=," + numTotalExamples + ",numEnabledEx=," + numEnabledExamples + ",numCorrectEx=," + numCorrectEnabledExamples);
        log(Msg.LOG_SUMMARY_END);
    }

    //TODO: Kyle: Call this when example added to pad
    //I'm having your code call this and not wekinator because here we can gets padID directly
    public static void logNewExampleRecorded(int exampleID, int padID, int numExamplesInPad) {
        log(Msg.EXAMPLE_RECORDED_INTO_PAD, exampleID + "," + padID + "," + numExamplesInPad);
    }

    //TODO: Kyle: Call this when example deleted from pad
    public static void logExampleDeleted(int exampleID, int padID, int numExamplesInPad) {
        log(Msg.EXAMPLE_DELETED_FROM_PAD, exampleID + "," + padID + "," + numExamplesInPad);
    }

    //TODO: Kyle; Call this when pad enabled
    public static void logPadEnabled(int padID, String padName, String padAudioFileName) {
        log(Msg.PAD_ENABLED, padID + "," + padName + "," + padAudioFileName);
    }

    //TODO: Kyle: Call this
    public static void logPadDisabled(int padID, String padName, String padAudioFileName) {
        log(Msg.PAD_DISABLED, padID + "," + padName + "," + padAudioFileName);
    }

    //TODO: Kyle; Might want to call this whenever any pad enable/disable state changes, so that it's easy
    //for us to reconstruct state of pads at any point.
    //**** Don't need to do this if you're calling logPadSummary, though
    //Prints num pads, each id/name/enabled, total num pads enabled.
    public static void logPadStates(int[] padIDs, String[] padNames, boolean[] padEnableds) {
        Msg which = Msg.PAD_STATES_LOGGED;
        actionLog.print(ts() + "," + which.ordinal() + "," + which + "," + padIDs.length);
        int numEnabled = 0;
        for (int i = 0; i < padIDs.length; i++) {
            actionLog.print("," + padIDs[i] + "," + padNames[i] + "," + padEnableds[i]);
            if (padEnableds[i]) {
                numEnabled++;
            }
        }
        actionLog.println(numEnabled);
    }

    //TODO: Kyle: Call this when new pad created
    public static void padCreated(int padID, String padName, String audioFileName) {
        log(Msg.PAD_CREATED, padID + "," + padName + "," + audioFileName);
    }

    //TODO: Kyle: Call this when pad name changed (if ever? if we care?)
    public static void padNameChanged(int padID, String oldName, String newName) {
        log(Msg.PAD_NAME_CHANGED, padID + "," + oldName + "," + newName);
    }

    //TODO: Kyle: Call this when pad audio file changed (if ever? if we care?)
    public static void padFileChanged(int padID, String oldFile, String newFile) {
        log(Msg.PAD_AUDIO_FILE_CHANGED, padID + "," + oldFile + "," + newFile);
    }

    //TODO: Kyle: Call this when user records a new track using record button
    //Not sure if you're using trackIDs; if not, just call with trackID = 0
    public static void logTrackRecordingStarted(int trackID) {
        log(Msg.TRACK_RECORD_STARTED, "" + trackID);
    }

    //TODO: Kyle: Call this when user finishes recording a track.
    //Want to log contents of the track
    //It's up to you how to do use methods/logging in a way that's easiest given your representation of the track
    //This is just a suggestion:
    public static void logTrackRecordingStopped(int trackID, int numHitsRecorded, double[] beatPositions, int[] beatClassesDetected) {
        Msg which = Msg.TRACK_RECORD_FINISHED;
        actionLog.print(ts() + "," + which.ordinal() + "," + which);
        for (int i = 0; i < beatPositions.length; i++) {
            actionLog.print("," + beatPositions[i] + "," + beatClassesDetected[i]);
        }
        actionLog.println();
    }

    //TODO Kyle: Call when track created
    //Not sure if you have ID #s for tracks that would be useful here?
    public static void logTrackCreated(int id) {
        log(Msg.TRACK_CREATED, "" + id);
    }

    //TODO Kyle: Call if/when track deleted
    //Not sure if you have ID #s for tracks that would be useful here?
    public static void logTrackDeleted(int id) {
        log(Msg.TRACK_DELETED, "" + id);
    }

    //TODO: Kyle: Call when user uses mouse to manually change the label of a hit in a track
    // I am assuming it's not possible to ADD or DELETE examples from a track using the mouse
    // If I'm wrong, we should add a new method for that
    public static void logTrackElementChangedWithMouse(int trackID, int originalClassID, int newClassID) {
        log(Msg.TRACK_ELEMENT_CHANGED, trackID + "," + originalClassID + "," + newClassID);
    }

    //TODO: Kyle:
    //Call this from GUI. (Not sure if tracks have ID or can be played separately; if all tracks played simultaneously, just use id=0
    public static void logTrackPlayed(int id) {
        log(Msg.TRACK_PLAYED, "" + id);
    }

    //TODO: Kyle: Call when someone mouses over example to play it
    public static void logMouseOverExample(int exampleId, int padId) {
        log(Msg.MOUSEOVER_EXAMPLE, exampleId + "," + padId);
    }

    //TODO: Kyle:
    //We definitely don't want to keep logging data around before user begins working with system (e.g., if we're demonstrating stuff to them)
    //Could put a button somewhere on GUI to press to designate that study is starting, then call this method
    //Otherwise, could restart system from scratch for user and ignore this method.
    public static void logExperimentBegin() {
        log(Msg.EXPERIMENT_BEGIN);
        datalogLog(Msg.EXPERIMENT_BEGIN, "");
    }

    protected static void datalogLog(Msg w, String m) {
        dataLog.println(ts() + "," + w.ordinal() + "," + w + "," + m);
    }

    //TODO: Kyle: You should be sure to either shut down software immediately after user is done, or call this method
    // so that we get an accurate measurement of how long it took them (alternatively, you can measure that from video/ audio recording)
    public static void logExperimentEnd() {
        log(Msg.EXPERIMENT_END);
        datalogLog(Msg.EXPERIMENT_END, "");
    }

    //Record id + classified class + k + feature vector into datalog
    //Also record info into action log
    static void logExampleClassified(int id, double[] features, int classifiedAs, int kNN) {
        dataLog.print(ts() + ",CLASSIFICATION_LOG_WITH_FEATURES," + id + "," + classifiedAs + "," + kNN);
        for (int i = 0; i < features.length; i++) {
            dataLog.print("," + features[i]);
        }
        dataLog.println();

        log(Msg.REALTIME_CLASSIFICATION_MADE, id + "," + classifiedAs + "," + kNN);
    }

    //Record id + feature vector into datalog
    static void logNewExampleWithFeatures(int id, double[] features) {
        dataLog.print(ts() + ",EXAMPLE_LOG_WITH_FEATURES," + id);
        for (int i = 0; i < features.length; i++) {
            dataLog.print("," + features[i]);
        }
        dataLog.println();
       
    }

    //TODO: Implement (log to actionLog)
    //Called when new pad enabling/disabling happens (this log will be somewhat redundant, but useful in conjunction with dataLog file)
    static void logSelectedExamplesAndClasses(int[] exampleList, int[] classList) {
        

        //throw new UnsupportedOperationException("Not yet implemented");
    }

    //TODO: want to make sure logs get flushed @ any point we want to quit.
}
