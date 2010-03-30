/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package drawing;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import processing.core.*;
import wekinator.SimpleDataset;

/**
 *
 * @author rebecca
 */
public class ClassSelector {
    public float width = 0f, height = 0f;
    public PApplet p = null;
    int selectedClass = -1;
    int numClasses = 0;
    protected EventListenerList listenerList = new EventListenerList();
    private ChangeEvent classClickEvent = null;
    protected Region[] classRegions = null;
    int vSpace = 10;
    int hSpace = 10;
    int cWidth = 20;
    int cHeight = 20;
    float[] hues;
    PFont myFont;

    public ClassSelector(float w, float h, SimpleDataset d, float[] hues, PApplet app) {
        this.hues = hues;
        width = w;
        height = h;
              
        this.numClasses = hues.length;
        p = app;
        classRegions = new Region[numClasses+1];

        int thisH = vSpace;
        for (int i = 0; i < numClasses+1; i++) {
            classRegions[i] = new Region(hSpace+10, thisH +5, hSpace + 10 + cWidth, thisH + 5 +cHeight);
            thisH += cHeight + vSpace;
        }

        myFont = p.createFont("Helvetica", 10);

    }

    public static int getPreferredWidth() {
        return 60;
    }


    public void addClassClickListener(ChangeListener l) {
        listenerList.add(ChangeListener.class, l);
    }

    public void removeClassClickListener(ChangeListener l) {
        listenerList.remove(ChangeListener.class, l);
    }

    protected void fireClassClicked() {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -=2 ) {
            if (listeners[i] == ChangeListener.class) {
                if (classClickEvent == null) {
                    classClickEvent = new ChangeEvent(this);
                }
                ((ChangeListener)listeners[i+1]).stateChanged(classClickEvent);
            }
        }
    }

    public int processMouseClick(float mouseX, float mouseY, int mouseButton) {
        if (mouseButton == PApplet.LEFT) {
            return getClassClicked(mouseX, mouseY);
        }
        return -1;
    }

    public int getClassClicked(float x, float y) {
        for (int i = 0; i < classRegions.length; i++) {
            if (classRegions[i].inRegion(x, y)) {
                System.out.println("click on " + i);
                return i;
            }
        }
        return -1;
    }

    public void draw() {
        p.pushStyle();
        p.textFont(myFont);
       p.fill(0);
       p.text("Select a class", 0, 7);

        p.noStroke();
     //  p.stroke(0);
        p.fill(256);
        p.rect(0, 0, width, height);


        p.fill(0);
        p.stroke(0);
       float thisH = vSpace;
       for (int i = 0; i < numClasses; i++) {
            p.fill(0);
            p.text(Integer.toString(i), hSpace, thisH + 15);
            p.fill(hues[i], 256, 256);
            p.rect(hSpace + 10, thisH + 5, cWidth, cHeight);
            thisH += cHeight + vSpace;
       }
       //Now add "no class"
       p.fill(0);
            p.text("None", hSpace-17, thisH + 15);
            p.fill(0);
            p.rect(hSpace + 10, thisH + 5, cWidth, cHeight);
            thisH += cHeight + vSpace;

       p.popStyle();

    }
}
