import processing.core.*; import processing.video.*; import oscP5.*; import netP5.*; import java.applet.*; import java.awt.*; import java.awt.image.*; import java.awt.event.*; import java.io.*; import java.net.*; import java.text.*; import java.util.*; import java.util.zip.*; import javax.sound.midi.*; import javax.sound.midi.spi.*; import javax.sound.sampled.*; import javax.sound.sampled.spi.*; import java.util.regex.*; import javax.xml.parsers.*; import javax.xml.transform.*; import javax.xml.transform.dom.*; import javax.xml.transform.sax.*; import javax.xml.transform.stream.*; import org.xml.sax.*; import org.xml.sax.ext.*; import org.xml.sax.helpers.*; public class downsampled_webcam extends PApplet {//By Tom Lieber
//Sends coarse grid of webcam out via osc
//100 features (ints)





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

  static public void main(String args[]) {     PApplet.main(new String[] { "downsampled_webcam" });  }}