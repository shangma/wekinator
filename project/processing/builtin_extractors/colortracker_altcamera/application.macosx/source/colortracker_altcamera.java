import processing.core.*; 
import processing.xml.*; 

import processing.video.*; 
import java.util.*; 
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

public class colortracker_altcamera extends PApplet {

// Simple color tracker (feature extractor)
// Tracks absolute and relative position and angle of two differently-colored objects
// Adapted by Rebecca Fiebrink from code by Nikolaus Gradwohl






Capture video;

PFont myFont;

int numPoints = 2;
OscP5 oscP5;
NetAddress dest;

public void setup() {
   String settings[] = loadStrings("data/camera_num.txt");
   int myDevice = Integer.parseInt(settings[0]);
   String[] devices = Capture.list();
  size( 640, 480 );
   video = new Capture(this, width, height, devices[myDevice], 30);
  noStroke();
  smooth();
  oscP5 = new OscP5(this,12000);
  dest = new NetAddress("127.0.0.1",6453);
  myFont = loadFont("SansSerif-14.vlw");
}

int searchColor1 = color( 128, 255, 0 );
int searchColor2 = color( 255, 0, 0 );

Point e1 = new Point( 0, 0 );
Point e2 = new Point( 640, 480);

boolean s1, s2;


public void draw() {
  if ( video.available()) {
     video.read();
     pushMatrix();
     scale(-1,1);
     image( video, -width, 0, width, height );
     popMatrix();

     int idx = 0;

     ArrayList p1 = new ArrayList();
     ArrayList p2 = new ArrayList();
     for ( int y = 0; y < video.height ; y++ ) {
        for ( int x = video.width; x >0; x-- ) {
          if ( match( searchColor1, video.pixels[idx] )) {
            p1.add( new Point( x, y ));
            //fill( 255, 255, 0, 128 );
            //ellipse( x, y, 10, 10 );
          } else if (match( searchColor2, video.pixels[idx] )){
            p2.add( new Point( x, y ));
            //fill( 255, 0, 0, 128 );
            //ellipse( x, y, 10, 10 );
          }
          idx ++;
        }
     }

    noStroke();
    if (p1.size() > 0) e1 = avg( p1 );
    if (p2.size() > 0) e2 = avg( p2 );

    if (s1) {
      fill( 255, 255, 0, 128 );
      ellipse( e1.x, e1.y, 30, 30 );
    }
    if ( s2 ) {  
      fill( 255, 0, 0, 128 );
      ellipse( e2.x, e2.y, 30, 30 );
    }

    drawtext();
    
    if(frameCount % 5 == 0)
      sendOsc(e1, e2);
   
  }
}

public boolean match( int c1, int c2 ) {
  int limit = 20;
   int sr = c1 >> 16 & 0xFF;
   int sg = c1 >> 8 & 0xFF;
   int sb = c1 & 0xFF;

   int cr = c2 >> 16 & 0xFF;
   int cg = c2 >> 8 & 0xFF;
   int cb = c2 & 0xFF;

   return cr > sr - limit && cr < sr + limit &&
     cg > sg - limit && cg < sg + limit && 
     cb > sb - limit && cb < sb + limit;
}

int mouseCount = 0;

public void mousePressed() {
  if (mouseButton == LEFT && mouseCount <2 && !s1) {
      searchColor1 = get( mouseX, mouseY );
      s1 = true;
      mouseCount++;
  }
  if (mouseButton == RIGHT && mouseCount < 2 && !s2) {
     searchColor2 = get( mouseX, mouseY );
     s2 = true;
     mouseCount++;
  }
}

// use the '=' key to reset
public void keyPressed() {
  println("Key pressed");
  if(key == '=') {
    s1 = false;
    s2 = false;
    mouseCount = 0;
  }
}

public Point avg( ArrayList l ) {
  if (l.size() == 0) {
    return new Point( 0, 0 );
  }
  int x = 0;
  int y = 0;
  for( Iterator i = l.iterator(); i.hasNext(); ) {
      Point p = (Point)i.next();
      x += p.x;
      y += p.y;
  }
  return new Point( x  / l.size(), y / l.size());
}

public class Point {
  int x;
  int y;

  Point( int x, int y ) {
    this.x = x;
    this.y = y;
  }
}

public void sendOsc(Point p1, Point p2) {
  OscMessage msg = new OscMessage("/processingFeatures");
  //Send x1, y1, x2, y2, dist between, angle between (in int degrees)
  msg.add(p1.x);
  msg.add(p1.y);
  msg.add(p2.x);
  msg.add(p2.y);
  msg.add((int)dist(p1.x, p1.y, p2.x, p2.y));
  msg.add(((int)degrees( atan2(p2.x -  + p1.x, p2.y - p1.y)) + 90 + 360) % 360) ;
  System.out.println(((int)degrees( atan2(p2.x -  + p1.x, p2.y - p1.y)) + 90 + 360) % 360) ;
  
  oscP5.send(msg, dest);
}

//Write instructions to screen.
public void drawtext() {
    stroke(255, 0, 0);
    textFont(myFont);
    textAlign(LEFT, TOP); 
    fill(255, 0, 0);

    text("INSTRUCTIONS", 10, 10);
    fill(0);
    text("   Click to set first color", 10, 25);
    text("   Right-click (or control+click on Mac) to set second color", 10, 40);
    text("   Press the '=' key to clear your color selections", 10, 55);
    text("   This color tracker tracks the average position of these colors on screen, as well as the", 10, 70); 
    text("      distance and angle between them.", 10, 85);  
}
  static public void main(String args[]) {
    PApplet.main(new String[] { "--bgcolor=#c0c0c0", "colortracker_altcamera" });
  }
}
