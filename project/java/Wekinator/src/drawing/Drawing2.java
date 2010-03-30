/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package drawing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JComponent;
import javax.swing.JFrame;

/**
 *
 * @author rebecca
 */
public class Drawing2 extends JComponent {

    @Override
    public Dimension getPreferredSize(){
        return new Dimension(450, 125);
    }


    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        Dimension size = getSize();
        Composite origComposite;
        g.setColor(Color.PINK);
        g2.fillRect(0, 0, 100, 100);
        System.out.println("painted");
    }

    public static JFrame openInJFrame(Container content,
                                    int width,
                                    int height,
                                    String title,
                                    Color bgColor) {
    JFrame frame = new JFrame(title);
    frame.setBackground(bgColor);
    content.setBackground(bgColor);
    frame.setSize(width, height);
    frame.setContentPane(content);
    //frame.setC
    //frame.addWindowListener(new ExitListener());
    frame.setVisible(true);
    return(frame);
  }

    public static void main(String[] args) {
        Drawing2.openInJFrame(new Drawing2(), 200, 300, "title", Color.WHITE);
    }
}
