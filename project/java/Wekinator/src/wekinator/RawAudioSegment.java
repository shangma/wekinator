/*
 * Represents a single contiguous segment of audio, no features extracted
 */

package wekinator;

import java.io.Serializable;

/**
 *
 * @author rebecca
 */
public class RawAudioSegment implements Serializable {
    private int numSamples;
    private double numSeconds;
    private double samplesPerSecond;
    private boolean isInFile;
    private String filename;
    private double absStartTime;
    

}
