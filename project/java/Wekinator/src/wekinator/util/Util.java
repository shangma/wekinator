/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package wekinator.util;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rebecca
 */
public class Util {

    public static String getCanonicalPath(File f) {
            String s;
        try {
            s = f.getCanonicalPath();
        } catch (IOException ex) {
            s = f.getAbsolutePath();
        }
            return s;
    }

    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }


}
