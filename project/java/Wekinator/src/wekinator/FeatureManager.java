/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wekinator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rebecca
 */
public class FeatureManager {

    public boolean useAudio = false;
    public boolean useFFT = false;
    public boolean useCentroid = false;
    public boolean useFlux = false;
    public boolean useRMS = false;
    public boolean useRolloff = false;
    private int fftSize = 512;
    
    public boolean useCustomChuck = false;
    public int numCustomChuck = 0;
    public boolean useCustomOsc = false;
    public int numCustomOsc = 0;

    public boolean useTrackpad;
    public boolean useMotionSensor;
    private int motionExtractionRate = 100;

        public boolean useOtherHid = false;
    public HidSetup hidSetup = null;

    public boolean useProcessing = false;
    private int numProcessingFeatures = 0;

    public int getFFTSize() {
        return fftSize;
    }
    private int windowSize = 256;

    public int getWindowSize() {
        return windowSize;
    }

    public boolean setFFTandWindowSize(int fftSize, int windowSize) {
        if (fftSize > 0 && windowSize > 0 && fftSize >= windowSize) {
            this.fftSize = fftSize;
            this.windowSize = windowSize;
            return true;
        }

        return false;
    }

    public enum WindowTypes {

        Hamming, Hann, Rectangular
    };
    public WindowTypes windowType = WindowTypes.Hamming;
    private int audioExtractionRate = 100;

    public int getAudioExtractionRate() {
        return audioExtractionRate;
    }

    public boolean setAudioExtractionRate(int r) {
        if (r > 0) {
            audioExtractionRate = r;
            return true;
        }
        return false;
    }


    public int getMotionExtractionRate() {
        return motionExtractionRate;
    }

    public boolean setMotionExtractionRate(int r) {
        if (r > 0) {
            motionExtractionRate = r;
            return true;
        }
        return false;
    }

//    private File processingFile = null;
    public enum ProcessingOptions { DOWNSAMPLED_100, COLOR_6, OTHER};
    private ProcessingOptions myProcessingOption = ProcessingOptions.DOWNSAMPLED_100;
    
    public void setProcessingOption(ProcessingOptions o, int numIfOther) {
        myProcessingOption = o;
        if (o == ProcessingOptions.DOWNSAMPLED_100) {
            numProcessingFeatures = 100;
        } else if (o == ProcessingOptions.COLOR_6) {
            numProcessingFeatures = 6;
        } else {
            if (numIfOther > 0) {
                numProcessingFeatures = numIfOther;
            } else {
                numProcessingFeatures = 0;
            }
        }
    }
    
    public int getNumAudioFeatures() {
        return ((useFFT?((int)(fftSize/2)):0) + (useCentroid?1:0) + (useFlux?1:0) + (useRMS?1:0) + (useRolloff?1:0));
        
    }
    
    public ProcessingOptions getProcessingOption() {
        return myProcessingOption;
    }
    
    public int getNumFeatures() {
        int s =0;
        if (useAudio) 
              s+= getNumAudioFeatures();
        if (useTrackpad) {
               s+= 2;
        }
        if (useMotionSensor)
            s+= 3;
        if (useOtherHid) 
            s+= hidSetup.getNumFeaturesUsed();
        if (useProcessing)
            s+= getNumProcessingFeatures();
        if (useCustomChuck) {
            s += numCustomChuck;
        }
        if (useCustomOsc) {
            s += numCustomOsc;
        }
        return s;
    }
        
        
        
    
    
    
    public int getNumProcessingFeatures() {
        return numProcessingFeatures;
    }

    public boolean setNumProcessingFeatures(int n) {
        //TODO: verify with Procesisng
        if (n > 0) {
            numProcessingFeatures = n;
            return true;
        }
        return false;
    }

    public int getNumOtherHidFeatures() {
        if (hidSetup != null) {
            return hidSetup.getNumFeaturesUsed();
        } else {
            return 0;
        }
    }

    public boolean setUpOtherHid() {
        //TODO: Interface with chuck to add new hid
        return false;
    }

    public boolean saveSettingsToFile(File f) {
        FileOutputStream outstream = null;
        ObjectOutputStream objout = null;
        boolean success = false;
        try {
            outstream = new FileOutputStream(f);
            objout = new ObjectOutputStream(outstream);
            
            objout.writeBoolean(useAudio);
            objout.writeBoolean(useFFT);
            objout.writeBoolean(useCentroid);
            objout.writeBoolean(useFlux);
            objout.writeBoolean(useRMS);
            objout.writeBoolean(useRolloff);
            objout.writeInt(fftSize);
            objout.writeInt(windowSize);
            //objout.writeInt(WindowTypes.valueOf(windowType.name()));
          //  objout.writeObject(windowType);
            int w;
            if (windowType == WindowTypes.Hamming) 
                w = 0;
            else if (windowType == WindowTypes.Hann) 
                w = 1;
            else 
                w = 2;
            objout.writeInt(w);
            objout.writeInt(audioExtractionRate);
            objout.writeBoolean(useTrackpad);
                System.out.println(useTrackpad);
            objout.writeBoolean(useMotionSensor);
            objout.writeInt(motionExtractionRate);
            objout.writeBoolean(useOtherHid);
            if (useOtherHid) {
                objout.writeObject(hidSetup);
                //hidSetup.writeToStream(objout);
            }
            objout.writeBoolean(useProcessing);
           // objout.writeObject(myProcessingOption);
            int p = 0;
            if (myProcessingOption == ProcessingOptions.DOWNSAMPLED_100)
                p = 0;
            else if (myProcessingOption == ProcessingOptions.COLOR_6) 
                p = 1;
            else
                p =2;
            objout.writeInt(p);
            objout.writeInt(numProcessingFeatures);
            objout.writeBoolean(useCustomChuck);
            objout.writeInt(numCustomChuck);
            objout.writeBoolean(useCustomOsc);
            objout.writeInt(numCustomOsc);

            success = true;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FeatureManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(FeatureManager.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                objout.close();
                outstream.close();
            } catch (IOException ex) {
                Logger.getLogger(FeatureManager.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        return success;
    }

    public void readSettingsFromFile(File f) {
        FileInputStream instream = null;
        ObjectInputStream objin = null;
        
        try {
            instream = new FileInputStream(f);
            objin = new ObjectInputStream(instream);
          //  fm = (FeatureManager)objin.readObject();
            useAudio = objin.readBoolean();
            useFFT = objin.readBoolean();
            useCentroid = objin.readBoolean();
            useFlux = objin.readBoolean();
            useRMS = objin.readBoolean();
            useRolloff = objin.readBoolean();
            fftSize = objin.readInt();
            windowSize = objin.readInt();
            //windowType = (WindowTypes)objin.readObject();
            int w = objin.readInt();
            if (w == 0) {
                windowType = WindowTypes.Hamming;
            } else if (w == 1) {
                windowType = WindowTypes.Hann;
            } else {
                windowType = WindowTypes.Rectangular;
                        
            }
            audioExtractionRate = objin.readInt();
            useTrackpad = objin.readBoolean();
            System.out.println(useTrackpad);
            useMotionSensor = objin.readBoolean();
            motionExtractionRate = objin.readInt();
            useOtherHid = objin.readBoolean();
            if (useOtherHid) {
                try {
                    hidSetup = (HidSetup) objin.readObject();
                    WekinatorInstance.getWekinatorInstance().setCurrentHidSetup(hidSetup); //TODO: Do I really want to do this? Or wait until I "apply" this feature set?
                    
                } catch (ClassNotFoundException ex) {
                    System.out.println("Problem loading hidset");
                }

            }
            useProcessing = objin.readBoolean();
          //  myProcessingOption = (ProcessingOptions)objin.readObject();
            int p = objin.readInt();
            if (p == 0) {
                myProcessingOption = ProcessingOptions.DOWNSAMPLED_100;
            } else {
                myProcessingOption =ProcessingOptions.COLOR_6;
            }
            numProcessingFeatures = objin.readInt();
            useCustomChuck = objin.readBoolean();
            numCustomChuck = objin.readInt();
            useCustomOsc = objin.readBoolean();
            numCustomOsc = objin.readInt();
            
            
         } catch (FileNotFoundException ex) {
            Logger.getLogger(FeatureManager.class.getName()).log(Level.SEVERE, null, ex);
        
        } catch (IOException ex) {
            Logger.getLogger(FeatureManager.class.getName()).log(Level.SEVERE, null, ex);
        }  finally {
            try {
                objin.close();
                instream.close();
            } catch (IOException ex) {
                Logger.getLogger(FeatureManager.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        
    }
    
    public void sendToChuck(OscHandler h) throws IOException {
        //TODO: send audio
    //    h.setUseAudio(useAudio, useFFT, useRMS, useCentroid, useRolloff, useFlux, fftSize, windowSize, windowType, audioExtractionRate);
        h.setUseTrackpad(useTrackpad);
        h.setUseMotion(useMotionSensor, motionExtractionRate);
        h.setUseOtherHid(useOtherHid); //ok??
        h.setUseProcessing(useProcessing, numProcessingFeatures);
        h.setUseOscCustom(useCustomOsc, numCustomOsc);
        h.setUseCustom(useCustomChuck, numCustomChuck);
        
    }
        
}
