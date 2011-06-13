/*
 * WekinatorRunner: Runs the Wekinator, optionally with command-line arguments
 *
 */
package wekinator;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import wekinator.ChuckRunner.ChuckRunnerState;
import wekinator.util.Util;

/**
 *
 * @author Rebecca Fiebrink
 */
public class WekinatorRunner {

    //Optionally specified by arguments:
    protected static File featureFile = null;
    protected static File lsFile = null;
    protected static File chuckFile = null;
    protected static OptionParser parser = null;
    protected static boolean runAutomatically = false;
    protected static boolean connectAutomatically = false;
    protected static boolean minimizeOnRun = false;
    
    protected static boolean isLogging = false; //Can change default here and recompile
    protected static boolean isPlork = false;
    protected static boolean isKbow = false;

    //The argument strings:
    protected static OptionSpec<String> feat;
    protected static OptionSpec<String> ls;
    protected static OptionSpec<String> ck;
    protected static OptionSpec<Void> run;
    protected static OptionSpec<Void> connect;
    protected static OptionSpec<Void> min;
    protected static OptionSpec<Void> isp;

    //Other relevant stuff to know at run time
    //True only if launched from OSX installed app:
    protected static boolean isLaunchedOsxApp = false;

    //Singleton:
    private static final WekinatorRunner ref = new WekinatorRunner();

    public static boolean isLogging() {
        return isLogging;
    }

    public static boolean isPlork() {
        return isPlork;
    }

    public static boolean isKbow() {
        return isKbow;
    }

    public static boolean isMinimizeOnRun() {
        return minimizeOnRun;
    }

    /**
     * Get the value of connectAutomatically
     *
     * @return the value of connectAutomatically
     */
    public boolean isConnectAutomatically() {
        return connectAutomatically;
    }

    /**
     * Get the value of runAutomatically
     *
     * @return the value of runAutomatically
     */
    public boolean isRunAutomatically() {
        return runAutomatically;
    }

    public static boolean isLaunchedOsxApp() {
        return isLaunchedOsxApp;
    }

    public static String getWekinatorDirectory() {
        String projectDirectory = "";
         File f = new File(Util.getCanonicalPath(new File(""))); //launch location
         if (isLaunchedOsxApp) {
            //Working dir will be /Applications/Wekinator/Wekinator.app/Contents/Resources/Java
            //
            String wekPath = f.getParentFile().getParentFile().getParentFile().getParentFile().getAbsolutePath();
            projectDirectory = Util.getCanonicalPath(new File(wekPath));
             System.out.println("Set project directory to " + projectDirectory);
         } else {
            //Assume working directory is project/java/Wekinator/dist/.
            String projectPath = f.getParentFile().getParentFile().getParentFile().getAbsolutePath();
            //Gives us [projects/wekinator]/project/ or 1 level above in non-OSX install

            projectDirectory = "";

            File f2 = new File(projectPath);
            if (f2.exists() && f2.getName().equals("project")) {
               projectDirectory = Util.getCanonicalPath(f2);
            } else if (f2.exists()) {
               File f3 = new File(Util.getCanonicalPath(f2) + File.separator + "project");
               if (f3.exists()) {
                   projectDirectory = Util.getCanonicalPath(f3);
               }
            }
            //A chance that wekinatorProjectDirectory still == ""; user will have to address later.
         }
         return projectDirectory;
    }

    /**
     * Get the value of featureLoadFilename
     *
     * @return the value of featureLoadFilename
     */
    public static File getFeatureFile() {
        return featureFile;
    }

    /**
     * Get the value of learningSystemLoadFilename
     *
     * @return the value of learningSystemLoadFilename
     */
    public static File getLearningSystemFile() {
        return lsFile;
    }

    /**
     * Get the value of chuckConfigLoadFilename
     *
     * @return the value of chuckConfigLoadFilename
     */
    public static File getChuckConfigFile() {
        return chuckFile;
    }
    
    private WekinatorRunner() {
        setupParser();
    }

    private void setupParser() {
        parser = new OptionParser();
        parser.accepts("help", "prints this help message");
        feat = parser.accepts("feat", "followed by feature configuration file").withRequiredArg().ofType(String.class);
        ls = parser.accepts("ls", "followed by learning system file").withRequiredArg().ofType(String.class);
        ck = parser.accepts("ck", "followed by chuck configuration file (only if not running chuck separately)").withRequiredArg().ofType(String.class);
        run = parser.accepts("run", "start running on load (no argument necessary)"); //run automatically
        connect = parser.accepts("connect", "start OSC connection on load (used only when running chuck separately");
        min = parser.accepts("min", "minimize after running (no argument; used only with --run)");
        isp = parser.accepts("p", "plork student special build with logging (no argument necessary)");
    }

    private static File getFeatureFile(String filename) {
        File f = new File(filename);
        if (f.exists()) {
            return f;
        } else {
            String path = hackyPath() + File.separator + "featureConfigurations";
            f = new File(path, filename);
            if (f.exists()) {
                return f;
            }
        }
        return null;
    }

    private static File getLsFile(String filename) {
        File f = new File(filename);
        if (f.exists()) {
            return f;
        } else {
            String path = hackyPath() + File.separator + "learningSystems";
            f = new File(path, filename);
            if (f.exists()) {
                return f;
            }
        }
        return null;
    }

    private static File getChuckFile(String filename) {
        File f = new File(filename);
        if (f.exists()) {
            return f;
        } else {
            String path = hackyPath() + File.separator + "chuckConfigurations";
            f = new File(path, filename);
            if (f.exists()) {
                return f;
            }
        }
        return null;
    }

    private static String hackyPath() {
        return ".." + File.separator + ".." + File.separator + "mySavedSettings";
    }

    public static void main(String[] args) {
        try {
            configure(args);
            run(true);
        } catch (OptionException ex) {
            System.out.println("Invalid options supplied to Wekinator");
            try {
                parser.printHelpOn(System.out);
            } catch (IOException ex1) {
                Logger.getLogger(WekinatorRunner.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
    }

    public static void configure(String[] args) throws OptionException {
        System.setProperty("apple.laf.useScreenMenuBar", "true");

        if (args == null) {
            return;
        }
        

        //Check if 1st argument is "osxapp"
        if (args.length > 0 && args[0].equals("osxapp")) {
            System.out.println("Launched as OSX app!");
            Logger.getLogger(WekinatorRunner.class.getName()).log(Level.SEVERE, "Launched as OSX ");
            isLaunchedOsxApp = true;
            //No need to do anything else to args at this point
        }

        OptionSet options;

            options = WekinatorRunner.parser.parse(args);
            System.out.println("parsed successfully");

            if(options.has("help")) {
                try {
                    System.out.println("Usage: ");
                    WekinatorRunner.parser.printHelpOn(System.out);
                } catch (IOException ex) {
                    Logger.getLogger(WekinatorRunner.class.getName()).log(Level.SEVERE, null, ex);
                }
                return;
            }

            if (options.has(feat)) {
                String s = options.valueOf(feat);
                File f = getFeatureFile(s);
                if (f == null) {
                    System.out.println("Error: feature configuration file " + s + " is not valid");
                    return;
                }
                featureFile = f;
                System.out.println("Loading feature configuration from file " + Util.getCanonicalPath(f));
            }

            if (options.has(ls)) {
                String s = options.valueOf(ls);
                File f = getLsFile(s);
                if (f == null) {
                    System.out.println("Error: learning system file " + s + " is not valid");
                    return;
                }
                lsFile = f;
                System.out.println("Loading learning system from file " + Util.getCanonicalPath(f));
            }

            if (options.has(ck)) {
                String s = options.valueOf(ck);
                File f = getChuckFile(s);
                if (f == null) {
                    System.out.println("Error: chuck configuration file " + s + " is not valid");
                    return;
                }
                chuckFile = f;
                System.out.println("Loading chuck configuration from file " + Util.getCanonicalPath(f));
            }

            if (options.has(feat) && options.has(ls) && options.has(run)) {
                runAutomatically = true;
                System.out.println("Automatically running");

                if (options.has(min)) {
                    minimizeOnRun = true;
                    System.out.println("Minimizing on run");
                }

            } else if (options.has(run)) {
                System.out.println("Warning: Will not automatically run: no feature configuration and/or learning system files have been specified");
            }

            if (options.has(connect)) {
                connectAutomatically = true;
                System.out.println("Automatically connecting");
            }

            if (options.has(isp)) {
                isPlork = true;
                isLogging = true;
                System.out.println("Running plork student special build");
            }
    }

    //Barebones run function; can use cmd line configure first if desired
    public static void run(boolean launchGUI) {
        try {
            WekinatorInstance w = WekinatorInstance.getWekinatorInstance();
            w.start();

            //Launch GUI
            if (launchGUI) {
                java.awt.EventQueue.invokeLater(new Runnable() {

                    public void run() {
                        MainGUI b = new MainGUI();
                        b.setVisible(true);
                    }
                });
            }
        } catch (IOException ex) {
            Logger.getLogger(WekinatorRunner.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
