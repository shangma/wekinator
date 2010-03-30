/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package drawing;

/**
 *
 * @author rebecca
 */
public interface LabelTrack {
    public void setUnitWidth(int w);
    public int getUnitWidth();
  //  public void leftClick(float x, float y);
  //  public void rightClick(float x, float y);
  //  public void clearClick();
    public void setMinInd(int min);
    public void setMaxInd(int max);
    public void draw();
}
