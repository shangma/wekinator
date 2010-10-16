//This demo allows wekinator to control the hue of the colored objects, as a discrete classification problem
//0 = red, 1= green, 2= blue
//Includes demo code from http://processing.org/learning/pvector/

//Necessary for OSC communication with Wekinator:
import oscP5.*;
import netP5.*;
OscP5 oscP5;
NetAddress dest;

Mover[] movers = new Mover[20]; //animation objects

//Parameters of sketch
float myX, myY, myHue;

void setup() {
  //Initialize OSC communication
  oscP5 = new OscP5(this,12000); //listen for OSC messages on port 12000 (Wekinator default)
  dest = new NetAddress("127.0.0.1",6448); //send messages back to Wekinator on port 6448, localhost (this machine) (default)
  
  colorMode(HSB);
  size(400,400);
  smooth();
  background(255);

  //Initialize appearance
  myHue = 100; //Start with hue 100
  myX = random(width);  //Start animation objects at a random position
  myY = random(height);
  // Initializing all the elements of the array
  for (int i = 0; i < movers.length; i++) {
    movers[i] = new Mover(); 
  }
}

void draw() {
  noStroke();
  fill(0,10);
  rect(0,0,width,height);

  // Calling functions of all of the objects in the array.
  for (int i = 0; i < movers.length; i++) {
    movers[i].update();
    movers[i].checkEdges();
    movers[i].display(); 
  }
  myX = random(width);  //Assign a new random position to the objects.
  myY = random(height);
}

//Changes sketch hue according to key pressed
void keyPressed() {
    int k = key % 3;
    if (k == 0) {
       myHue = 0; 
    } else if (k == 1) {
       myHue = 100; 
    } else {
       myHue = 150; 
    }
    
  sendOsc(); //Send updated parameter value to wekinator
}

//This is called automatically when OSC message is received, i.e. when Wekinator sends new parameter value for hue
void oscEvent(OscMessage theOscMessage) {
 if (theOscMessage.checkAddrPattern("/OSCSynth/params")==true) {
     if(theOscMessage.checkTypetag("f")) { //looking for 1 parameter
        float receivedValue = theOscMessage.get(0).floatValue(); //get this parameter
        println("Received new params value from Wekinator");
        
        //NOW, receivedValue should be 0, 1, or 2!
        if (receivedValue == 0.0) {
           myHue = 0; 
        } else if (receivedValue == 1.0) {
           myHue = 100; 
        } else if (receivedValue == 2.0) {
           myHue = 150; 
        } else {
           println("Error: Received unexpected hue value"); 
        }
      } else {
        println("Error: unexpected params type tag received by Processing");
      }
   }
}

//Sends current parameter (hue) to Wekinator
void sendOsc() {
  //THIS IS ALSO DIFFERENT: Send category value, not hue itself
    OscMessage msg = new OscMessage("/realValue");
    float category = 0.;
    if (myHue == 0) {
       category = 0.; 
    } else if (myHue == 100) {
      category = 1.;
    } else {
       category = 2.; 
    }
  msg.add(category); //MUST send float!
  oscP5.send(msg, dest);
}


//Animation object class
class Mover {

  PVector location;
  PVector velocity;
  PVector acceleration;
  float topspeed;

  Mover() {
    location = new PVector(random(width),random(height));
    velocity = new PVector(0,0);
    topspeed = 4;
  }

  void update() {

    // Our algorithm for calculating acceleration:
   // PVector mouse = new PVector(mouseX,mouseY);
   PVector loc = new PVector(myX + random(100), myY+random(30));
   PVector dir = PVector.sub(loc,location);  // Find vector pointing towards mouse
    dir.normalize();     // Normalize
    dir.mult(0.5);       // Scale 
    acceleration = dir;  // Set to acceleration

    // Motion 101!  Velocity changes by acceleration.  Location changes by velocity.
    velocity.add(acceleration);
    velocity.limit(topspeed);
    location.add(velocity);
  }

  void display() {
    stroke(0);
    fill(myHue,100,255);
    ellipse(location.x + random(10),location.y + random(10),16 + random(2),16 + random(2));
  }

  void checkEdges() {

    if (location.x > width) {
      location.x = 0;
    } else if (location.x < 0) {
      location.x = width;
    }

    if (location.y > height) {
      location.y = 0;
    }  else if (location.y < 0) {
      location.y = height;
    }

  }

}

