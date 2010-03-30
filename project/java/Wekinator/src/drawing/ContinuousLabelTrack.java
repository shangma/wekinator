/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package drawing;

import processing.core.*;
import wekinator.SimpleDataset;

/**
 *
 * @author rebecca
 */
public class ContinuousLabelTrack implements LabelTrack {

    public float width = 0f,  height = 0f;
    public double wzoom = 1.0,  hzoom = 1.0;
    public PApplet p = null;
    // public int x, y;
    float ppHor = 1.0f;
    float ppVert = 10.0f;
    int minX = 0;
    int maxX = 100;
    float minY = -1;
    float maxY = -1;
    int unitWidth = 10;
    SimpleDataset d;
    int myId = 0;
    boolean isParam = false;

    ContinuousLabelTrack(float w, float h, int minInd, int maxInd, SimpleDataset d, boolean isParam, int myId, PApplet app) {
        //   this.x = x;
        //   this.y = y;

        width = w;
        height = h;
        //wzoom = wz;
        //hzoom = hz;
        this.d = d;
        this.myId = myId;
        this.isParam = isParam;
        this.minX = minInd;
        this.maxX = maxInd;
        computeVertRange();

        ppHor = ((float) w) / (maxX - minX + 1);
        
        p = app;
    }

    public int getUnitWidth() {
        return unitWidth;
    }

    public void setUnitWidth(int w) {
        unitWidth = w;
        maxX = minX + w;
        ppHor = width / (maxX - minX + 1);
    }

    private void computeVertRange() {
        float min = Float.MAX_VALUE;
        float max = Float.MIN_VALUE;
        for (int i = 0; i < d.getNumDatapoints(); i++) {
            if (isParam) {
                Double v = d.getParam(i, myId);
                if (!v.isNaN()) {
                    if (v < min) {
                        min = (float) v.doubleValue();
                    }
                    if (v > max) {
                        max = (float) v.doubleValue();
                    }
                }
            } else {
                double v = d.getFeature(i, myId);

                    if (v < min) {
                        min = (float) v;
                    }
                    if (v > max) {
                        max = (float) v;
                    }

            }
        }

        double range = max - min;
        
        if (range < .00005) {
            minY = min - .00005f;
            maxY = min + .00005f; //roundoff! sigh...
        } else {
            minY = min;
            maxY = max;
        }
      //  System.out.println("range " + myId + " updated to " + minY + ", " + maxY);
      //  minY = (float)(min - .05 * range);
      //  maxY = (float)(max + .05 * range);


       /* if (minY == maxY) {
            System.out.println("here with id " + myId + " param " + isParam);
            minY--;
            maxY++;
        } */

        ppVert = ((float) height) / (maxY - minY);
    }

    private float xformx(float x) {
        return ppHor * .5f + (ppHor * (x - minX));
    }

    private float xformy(float y) {
        return (ppVert * (maxY - y));
    }

    public void draw() {
        p.pushMatrix();
        p.pushStyle();
        p.fill(300);
        p.rect(0, 0, width, height);
        //  p.scale((float)ppHor, (float)ppVert);
        //p.translate(x, y);
        //p.sca
        p.stroke(0);
        p.strokeWeight(1.0f);
        // p.line(0, 0, 100, 100);
        boolean updateRange = false;

        if (d.getNumDatapoints() > minX) {
            if (!isParam) {
                float p1 = (float) d.getFeature(0, myId);
                for (int i = minX; (i < maxX && i < (d.getNumDatapoints() - 1)); i++) {
                    float p2 = (float) d.getFeature(i + 1, myId);
                    p.line(xformx(i), xformy(p1), xformx(i + 1), xformy(p2));
                    p1 = p2;
                    if (p1 < minY || p1 > maxY)
                        updateRange = true;
                }

            } else {
                //Must check for NaNs
                Double p1 = d.getParam(0, myId);
                for (int i = minX; (i < maxX && i < (d.getNumDatapoints() - 1)); i++) {
                    Double p2 = d.getParam(i + 1, myId);
                    if (!p1.isNaN() && !p2.isNaN()) {
                        if (p1 < minY || p1 > maxY)
                            updateRange = true;
                        p.line(xformx(i), xformy((float) p1.doubleValue()), xformx(i + 1), xformy((float) p2.doubleValue()));
                    }
                    p1 = p2;
                }
            }
        }
        p.popStyle();
        p.popMatrix();
        if (updateRange) {
            computeVertRange();
        }
    }

    public void setMinInd(int min) {
        minX = min;
    }

    public void setMaxInd(int max) {
        maxX = max;
    }
}
