/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package drawing;
import drawing.TrackSet.ClickState;
import processing.core.*;
import wekinator.SimpleDataset;

/**
 *
 * @author rebecca
 */
public class GraphDataView {
    public int width = 0, height = 0;
    public PApplet p = null;
    public TrackSet ts = null;
    public ClassSelector cs = null;
    Region tsRegion = null;
    Region csRegion = null;
    
    public int vSpace = 10;
    public int hSpace = 10;
   
    public int tsWidth = 100;
    public int tsHeight = 20;
    public int csWidth = 40;
    public int csHeight = 200;
    float[] hues;
    int numClasses = 0;
    SimpleDataset d;


    void processMouseClick(int mouseX, int mouseY, int mouseButton) {
       /* if (clickState == ClickState.NONE && mouseButton == PApplet.LEFT) {
            //look for left click
        } else if (clickState == ClickState.L_LABEL && mouseButto
        *
        * */
        if (tsRegion.inRegion(mouseX, mouseY)) {
            ts.processMouseClick(mouseX - tsRegion.x1, mouseY-tsRegion.y1, mouseButton);
        } else if (csRegion.inRegion(mouseX, mouseY)) {
            int clicked = cs.processMouseClick(mouseX-csRegion.x1, mouseY-csRegion.y1, mouseButton);
            if (clicked != -1 && ts.clickState != ClickState.NONE) {
               changeLabels(ts.selectedTrack, ts.getSelectedMin(), ts.getSelectedMax(), clicked);
            }
        }
    }

    public GraphDataView(int w, int h, SimpleDataset d, PApplet app) {
        p = app;
        this.d = d;
        width = w;
        height = h;
        tsWidth = (int)(w - 3 * hSpace - ClassSelector.getPreferredWidth());
        tsHeight = (int)(h - 2 * vSpace);
        csWidth = ClassSelector.getPreferredWidth();
        csHeight =(int)(h - 2 * vSpace);

        int maxNumClasses = 0;
        int numDiscrete = 0;
        int numContinuous = 0;
        int maxValues[] = d.getMaxLegalDiscreteParamValues();
        for (int i = 0; i < d.getNumParameters(); i++) {
            if (d.isParameterDiscrete(i)) {
                numDiscrete++;
                if (maxValues[i] + 1 > maxNumClasses) {
                    maxNumClasses = maxValues[i] + 1;
                }
            } else {
                numContinuous++;
            }
        }

        numClasses = maxNumClasses; //TODO: may be error, numclasses shoudl be max value + 1
        setColors();


     //   tmpPopulate(numTracks, numLabels, numClasses, numPoints);

        cs = new ClassSelector(csWidth, csHeight, d, hues, app);
        ts = new TrackSet(tsWidth, tsHeight, d, hues, app);

        csRegion = new Region(hSpace, vSpace, hSpace + csWidth, vSpace + csHeight);
        tsRegion = new Region(hSpace*2 + csWidth, vSpace, hSpace*2 + csWidth + tsWidth, vSpace + tsHeight);
        ts.myRegion = tsRegion; //hack
    }

    /*private void tmpPopulate(int numTracks, int numLabels, int numClasses, int numPoints) {
        data = new float[numTracks][numPoints];
        labels = new int[numLabels][numPoints];

        for (int i = 0; i < numTracks; i++) {
            for (int j = 0; j < numPoints; j++) {
                data[i][j] = (float)( Math.random() * 2 - 1);
            }
        }
        for (int i = 0; i < numLabels; i++) {
            for (int j = 0; j < numPoints; j++) {
                labels[i][j] = (int)(Math.random() * numClasses);
            }
        }
    } */


/*    private float getOffsetY(int tNum) {
        return vSpace + (tNum * (trackHeight + vSpace)); 
    } */

    public void draw() {
       p.pushMatrix();
        p.translate(csRegion.x1, csRegion.y1);
        cs.draw();
        p.popMatrix();
        p.pushMatrix();
        p.translate(tsRegion.x1, tsRegion.y1);
        ts.draw();
        p.popMatrix();
    }

    //Data model stuff!!!
    private void changeLabels(int selectedParam, int selectedMin, int selectedMax, int c) {
        System.out.println("changing to class " + c  + " for track " + selectedParam);
        if (selectedMin == -1 || selectedMax == -1) {
            return;
        }
        
        if (c <= d.getMaxLegalDiscreteParamValues()[selectedParam])  {
            for (int i = selectedMin; i <= selectedMax && i < d.getNumDatapoints(); i++) {
                d.setParameterValue(i, selectedParam, c);
            }
            
        } else {
            for (int i = selectedMin; i <= selectedMax  && i < d.getNumDatapoints(); i++) {
                d.setParameterMissing(i, selectedParam);
            }
        }


    }

   private void setColors() {
        hues = new float[numClasses];
        for (int i = 0; i < numClasses; i++) {
            hues[i] = (float)i/numClasses * 256;
        }
    }
}
