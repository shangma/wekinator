/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tmp;

/**
 *
 * @author rebecca
 */
public class NewClass1 {

     static void tester(int[] array1) {
           // array1 = new int[2];
            array1[0] = 2;
            array1[1] = 10;
    }

    public static void main(String[] args) {
        int a[] = new int[5];
        NewClass1.tester(a);
        System.out.println("a == null? " + (a == null));
        System.out.println("a len " + a.length);
        System.out.println("a0 " + a[0] + " a[1]" + a[1]);
    }

}
