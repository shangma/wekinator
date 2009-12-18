/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tmp;

import wekinator.ChuckConfiguration;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rebecca
 */
public class ChuckManager {
    private ChuckConfiguration settings;
    private boolean isRunning = false;


    public ChuckManager() {
    }

    public static void createChuckFileFromSettings(File file, ChuckConfiguration settings) {
     
    }

    public void launch(ChuckConfiguration settings) throws Exception {
        Process p = Runtime.getRuntime().exec("chuck /users/rebecca/tmp.ck");
        BufferedReader stdInput = new BufferedReader(new
                 InputStreamReader(p.getInputStream()));

            BufferedReader stdError = new BufferedReader(new
                 InputStreamReader(p.getErrorStream()));

            // read the output from the command
            System.out.println("Here is the standard output of the command:\n");
            String s = null;
            while ((s = stdInput.readLine()) != null) {
                System.out.println(s);
            }

            // read any errors from the attempted command
            System.out.println("Here is the standard error of the command (if any):\n");
            while ((s = stdError.readLine()) != null) {
                System.out.println(s);
            }

            System.exit(0);


    }

    public static void main(String[] args) {
        try {
            ChuckManager m = new ChuckManager();
            m.launch(new ChuckConfiguration());
        } catch (Exception ex) {
            System.out.println("didn't work");
        }
    }


}
