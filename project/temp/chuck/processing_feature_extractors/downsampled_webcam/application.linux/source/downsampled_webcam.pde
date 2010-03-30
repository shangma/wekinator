//By Tom Lieber
//Sends coarse grid of webcam out via osc
//100 features (ints)

import processing.video.*;
import oscP5.*;
import netP5.*;

int numPixels;
Capture video;

OscP5 oscP5;
NetAddress dest;

void setup() {
  size(640, 480, P2D);
  
  video = new Capture(this, 10, 10, 24);
  numPixels = video.width * video.height;
  loadPixels();
  noStroke();

  /* start oscP5, listening for incoming messages at port 12000 */
  oscP5 = new OscP5(this,12000);
  dest = new NetAddress("127.0.0.1",6453);
}

void draw() {
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

float diff(int p, int off) {
  if(p + off < 0 || p + off >= numPixels)
    return 0;
  return red(video.pixels[p+off]) - red(video.pixels[p]) +
         green(video.pixels[p+off]) - green(video.pixels[p]) +
         blue(video.pixels[p+off]) - blue(video.pixels[p]);
}

void sendOsc(int[] px) {
  OscMessage msg = new OscMessage("/processingFeatures");
  msg.add(px);
  oscP5.send(msg, dest);
}
