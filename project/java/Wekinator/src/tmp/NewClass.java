/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tmp;

import java.io.File;

/**
 *
 * @author rebecca
 */
public class NewClass {
    public static void main(String[] args) {

         String currentDir = new File("").getAbsolutePath();
         System.out.println("Current is " + currentDir);
    }
}
