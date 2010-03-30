/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package drawing;

/**
 *
 * @author rebecca
 */
public class Region {
    public float x1, y1, x2, y2;

    public Region(float x1, float y1, float x2, float y2) {
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
    }

    public boolean inRegion(float x, float y) {
        return (x >= x1 && x <= x2 && y >= y1 && y <= y2);
    }

}
