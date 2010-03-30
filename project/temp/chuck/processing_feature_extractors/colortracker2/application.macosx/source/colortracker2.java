import processing.core.*; 
import processing.xml.*; 

import processing.video.*; 
import java.util.*; 
import oscP5.*; 
import netP5.*; 

import java.applet.*; 
import java.awt.*; 
import java.awt.image.*; 
import java.awt.event.*; 
import java.io.*; 
import java.net.*; 
import java.text.*; 
import java.util.*; 
import java.util.zip.*; 
import java.util.regex.*; 

public class colortracker2 extends PApplet {






Capture video;
PImage img; 

int numPoints = 2;
OscP5 oscP5;
NetAddress dest;

public void setup() {
  size( 640, 480 );
  video = new Capture( this, width, height, 15 );
  img = loadImage( "yoda-dog.jpg" );
  noStroke();
  smooth();
  oscP5 = new OscP5(this,12000);
  dest = new NetAddress("127.0.0.1",6453);
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

    if (s1 && s2 ) {
      pushMatrix();
      translate( e1.x, e1.y );
      int dx = e2.x - e1.x;
      int dy = e2.y - e1.y;
      rotate( - atan2( e2.x -  + e1.x, e2.y - e1.y) + atan2( img.width, img.height));
      float zoom = sqrt ( dx * dx + dy * dy ) / 
           sqrt(img.width * img.width + img.height * img.height);
      scale(zoom, zoom ); 


      tint( 255, 200 );

      image( img, 0, 0 );
      strokeWeight( 3 );
      stroke( 255 );
      noFill();
      rect( 0,0,img.width, img.height );
      noTint();
      popMatrix();
      
      //if(frameCount % 3 == 0)
      sendOsc(e1, e2);
    }
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
  if (mouseButton == LEFT && mouseCount < 2) {
      searchColor1 = get( mouseX, mouseY );
      s1 = true;
      mouseCount++;
  }
  if (mouseButton == RIGHT && mouseCount < 2) {
     searchColor2 = get( mouseX, mouseY );
     s2 = true;
     mouseCount++;
  }
}

// use the '=' key to reset
public void keyPressed() {
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
  //  System.out.println(p1.x + " " + p1.y + " " + p2.x + " " + p2.y);
  //  System.out.println(dist(p1.x, p2.x, p1.y, p2.y));


 
  msg.add(((int)degrees( atan2(p2.x -  + p1.x, p2.y - p1.y)) + 90 + 360) % 360) ;
  System.out.println(((int)degrees( atan2(p2.x -  + p1.x, p2.y - p1.y)) + 90 + 360) % 360) ;
  
  oscP5.send(msg, dest);
}


  static public void main(String args[]) {
    PApplet.main(new String[] { "--bgcolor=#c0c0c0", "colortracker2" });
  }
}
