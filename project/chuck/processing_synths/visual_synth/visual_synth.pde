/* Example of a simple synth in Processing

 Wekinator version 0.2
 Copyright 2009 Rebecca Fiebrink
 http://wekinator.cs.princeton.edu
*/


import processing.opengl.*;
import processing.video.*;
import oscP5.*;
import netP5.*;

OscP5 oscP5;
NetAddress dest;

Circle c = new Circle(75,325,50);
boolean isSendingParams = true;
String sendHostname = "127.0.0.1"; 
int sendPort = 6448; //send directly to wekinator

void setup() {
  size(400,400, OPENGL);
  colorMode(HSB);
  background(0);
  smooth();

  /* start OSC */
  oscP5 = new OscP5(this,12000); //we're listening here: TODO: make sure this matches synthPort in chuck OSC_synth SynthClass file
  dest = new NetAddress(sendHostname,sendPort); //sending here
 //dest = new NetAddress("127.0.0.1",6453);
 println("Set up dest: " + dest);
  frameRate(30);
}


void draw() {
   background(0);
   c.display(); 
   if (isSendingParams && frameCount % 5 == 0) {
  //   println("Sent params");
     sendOscParams();
   }
}

void oscEvent(OscMessage theOscMessage) {
  /* print the address pattern and the typetag of the received OscMessage */
 // print("### received an osc message.");
 // print(" addrpattern: "+theOscMessage.addrPattern());
 // println(" typetag: "+theOscMessage.typetag());
  
  if (theOscMessage.checkAddrPattern("/OSCSynth/setup")==true) {
     setup2(); //setup() is reserved function name in Processing
  } else if (theOscMessage.checkAddrPattern("/OSCSynth/params")==true) {
     if(theOscMessage.checkTypetag("fff")) {
         float f1 = theOscMessage.get(0).floatValue(); 
         float f2 =  theOscMessage.get(1).floatValue(); 
         float f3 =  theOscMessage.get(2).floatValue(); 
         setParams(f1, f2, f3);
      } else {
        println("Error: unexpected params type tag received by Processing");
      }
   } else if (theOscMessage.checkAddrPattern("/OSCSynth/silent")==true) {
      silent();
  } else if (theOscMessage.checkAddrPattern("/OSCSynth/sound")==true) {
      sound();
  }  else if (theOscMessage.checkAddrPattern("/OSCSynth/startSendingParams")==true) {
      if(theOscMessage.checkTypetag("sif")) {
         String s = theOscMessage.get(0).stringValue();
         int i =  theOscMessage.get(1).intValue(); 
         float f =  theOscMessage.get(2).floatValue(); 
         startSendingParams(s,i,f);
      } else {
        println("Typetag is " + theOscMessage.getTypetag());
        println("Error: unexpected startSendingParams type tag received by Processing");
      } 
  } else if (theOscMessage.checkAddrPattern("/OSCSynth/stopParams")==true) {
     stopSendingParams();
  } else if (theOscMessage.checkAddrPattern("/OSCSynth/sendParams")==true) { 
    if(theOscMessage.checkTypetag("si")) {
       String s = theOscMessage.get(0).stringValue(); 
       int i =  theOscMessage.get(1).intValue(); 
       if (! s.equals(sendHostname) || i != sendPort) {
          dest = new NetAddress(s,i);
          sendHostname = s;
          sendPort = i;
        }
       sendParams();
    } else {
       System.out.println("Error: unexpected sendParams type tag received by Processing"); 
    }
  } else {
     System.out.println("Error: Received unknown OSC message");    
  }
}

void startSendingParams(String s, int i, float f) {
  if (! s.equals(sendHostname) || i != sendPort) {
      dest = new NetAddress(s,i);
      println("setting new host " + sendHostname + " port " + sendPort);
      sendHostname = s;
      sendPort = i;
  }
  isSendingParams = true;
}

void sendParams() {
  /*if (! s.equals(sendHostname) && i == sendPort) {
      dest = new NetAddress(s,i);
      sendHostname = s;
      sendPort = i;
  }*/
  println("Sent params");
  sendOscParams();
}

void stopSendingParams() {
  isSendingParams= false;
}

void silent() {
  //TODO: ?? 
  
}

void sound() {
  // TODO ??
}

void setup2() {
  // Anything?  
}

void setParams(float f1, float f2, float f3) {
  c.x = f1;
  c.y = f2;
  c.r = f3;
  //TODO: Check these values are legal!
}



void sendOscParams() {
  //println("Sending OSC params");
  OscMessage msg = new OscMessage("/realValue");
  msg.add((float)c.x);
  msg.add((float)c.y);
  msg.add((float)c.r);
  oscP5.send(msg, dest);
 // println("sent message " + msg);
 // println("Sent vals " + c.x + " " + c.y + " " + c.r);
}

public class Circle {
  // x and y position, radius, x and y velocities
  public float x, y, r;
  // circle color
  color c;
  int h, s, b;

  Circle(float _x, float _y, float _r) {
    x = _x;  
    y = _y;  
    r = _r;  
    float h = random(255);
    float s = random(255);
    b = 255;
    c = color(h, s,b);
  }

  // draw circle
  void display() {
    noStroke();
    fill(c,100);

    ellipse(x, y, r*2, r*2);

  }
  

  void setPosition(float _x, float _y) {
     x = _x; 
      y = _y;
      
  }

 
} 
