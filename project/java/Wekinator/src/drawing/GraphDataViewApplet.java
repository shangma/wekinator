package drawing;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import processing.core.*;
import processing.*;
import wekinator.SimpleDataset;

//TODO: add all appropriate listeners to dataset for things that aren't updated
// in draw loop: e.g. param/feat names!
//Also see if we can keep this from looping every time... only loop
//on change or when mouse is in view
//definitely need to update scroll bar characteristics whhen amt of data changes.


/**
 *
 * @author rebecca
 */
public class GraphDataViewApplet extends PApplet {
   // PlotTrack tr;
    GraphDataView dv = null;
   // SimpleDataset myDataset;

    public void setDataset(SimpleDataset d) {
      //  myDataset = d;
        dv = new GraphDataView(600, 400, d, this);

    }

    public static void main(String[] args) {
       // must match the name of your class ie "letsp5.Main" = packageName.className
       PApplet.main( new String[]{"drawing.GraphDataViewApplet"} );
   }

  

    @Override
   public void setup () {
       size( 600, 400);
       colorMode(HSB);
       smooth();
       background(50, 0, 256);
   }

    @Override
   public void draw () {
       background(255);
       if (dv != null) {
            dv.draw();
       }
   }

   @Override
   public void mouseClicked() {
       dv.processMouseClick(mouseX, mouseY, mouseButton);
   }

}
