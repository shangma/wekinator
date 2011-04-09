import processing.core.*; 
import processing.xml.*; 

import processing.video.*; 
import oscP5.*; 
import netP5.*; 

import java.applet.*; 
import java.awt.Dimension; 
import java.awt.Frame; 
import java.awt.event.MouseEvent; 
import java.awt.event.KeyEvent; 
import java.awt.event.FocusEvent; 
import java.awt.Image; 
import java.io.*; 
import java.net.*; 
import java.text.*; 
import java.util.*; 
import java.util.zip.*; 
import java.util.regex.*; 

public class edgetracker extends PApplet {

// Simple Wekinator edge tracker (feature extractor)
// Identifies edges and downsamples to 10x10 grid
// By Tom Lieber
// Adapted by Rebecca Fiebrink





int numPixels;
Capture video;

OscP5 oscP5;
NetAddress dest;

public void setup() {
  size(640, 480, P2D);
  
  video = new Capture(this, 10, 10, 24);
  numPixels = video.width * video.height;
  loadPixels();
  noStroke();

  /* start oscP5, listening for incoming messages at port 12000 */
  oscP5 = new OscP5(this,12000);
  dest = new NetAddress("127.0.0.1",6453);
}

public void draw() {
  if (video.available()) {
    video.read(); // Read a new video frame
    video.loadPixels(); // Make the pixels of video available
    for (int i = 0; i < numPixels; i++) {
      int x = i % video.width;
      int y = i / video.width;
      float xscl = (float) width / (float) video.width;
      float yscl = (float) height / (float) video.height;
      
      float gradient = diff(i, -1) + diff(i, +1) + diff(i, -video.width) + diff(i, video.width);
      fill(color(gradient, gradient, gradient));
      rect(x * xscl, y * yscl, xscl, yscl);
    }
  }
  
  if(frameCount % 5 == 0)
    sendOsc(video.pixels);
}

public float diff(int p, int off) {
  if(p + off < 0 || p + off >= numPixels)
    return 0;
  return red(video.pixels[p+off]) - red(video.pixels[p]) +
         green(video.pixels[p+off]) - green(video.pixels[p]) +
         blue(video.pixels[p+off]) - blue(video.pixels[p]);
}

public void sendOsc(int[] px) {
  OscMessage msg = new OscMessage("/processingFeatures");
  msg.add(px);
  oscP5.send(msg, dest);
}
  static public void main(String args[]) {
    PApplet.main(new String[] { "--bgcolor=#c0c0c0", "edgetracker" });
  }
}
