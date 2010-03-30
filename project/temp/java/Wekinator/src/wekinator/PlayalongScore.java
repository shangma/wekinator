/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package wekinator;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.*;

/**
 *
 * @author rebecca
 */
public class PlayalongScore {
    protected List<double[]> paramLists = null;
    protected List<Double> secondLists = null;
    //No masks for now
    protected static boolean isPlaying = false;
    protected transient static Thread myPlayerThread = null;
    protected static final Object lock1 = new Object();
    int numParams = 0;
    protected int playingRow = 0;
    public static final String PROP_PLAYINGROW = "playingRow";
    public static final String PROP_ISPLAYING = "isPlaying";
    protected EventListenerList listenerList = new EventListenerList();
    protected transient PlayalongScoreViewer myViewer = null; //TODO: put elsewhere?

    
    public void view() {
        if (myViewer == null) {
            myViewer = new PlayalongScoreViewer(this);
            myViewer.setVisible(true);
        } 
        myViewer.setVisible(true);
        myViewer.toFront();
    }

        /**
     * Get the value of playingRow
     *
     * @return the value of playingRow
     */
    public boolean getIsPlaying() {
        return isPlaying;
    }

    /**
     * Get the value of playingRow
     *
     * @return the value of playingRow
     */
    public int getPlayingRow() {
        return playingRow;
    }

    /**
     * Set the value of playingRow
     *
     * @param playingRow new value of playingRow
     */
    protected void setPlayingRow(int playingRow) {
        int oldPlayingRow = this.playingRow;
        this.playingRow = playingRow;
        propertyChangeSupport.firePropertyChange(PROP_PLAYINGROW, oldPlayingRow, playingRow);
    }


    protected synchronized void setIsPlaying(boolean isPlaying) {
        boolean oldIsPlaying = PlayalongScore.isPlaying;
        PlayalongScore.isPlaying = isPlaying;
        propertyChangeSupport.firePropertyChange(PROP_ISPLAYING, oldIsPlaying, isPlaying);
    }


    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private ChangeEvent changeEvent = null;


    /**
     * Add PropertyChangeListener.
     *
     * @param listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Remove PropertyChangeListener.
     *
     * @param listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }



    PlayalongScore(int numParams) {
        paramLists = new LinkedList<double[]>();
        secondLists = new LinkedList<Double>();
        this.numParams = numParams;
    }

    public synchronized void play() {
        if (! isPlaying) {
            myPlayerThread = new Thread(new ScorePlayer());
            myPlayerThread.start();
        }
        setIsPlaying(true);
    }

    public synchronized void stop() {
        if (isPlaying) {
            myPlayerThread.interrupt();
            setIsPlaying(false);
        }
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void addParams(double[] p, double time) {
        synchronized(lock1) {
            if (p != null && p.length == numParams && time > 0) {
                double[] newp = new double[p.length];
                for (int i = 0; i < p.length; i++) {
                    newp[i] = p[i];
                }
                paramLists.add(newp);
                secondLists.add(time);
                fireStateChanged();
            }
        }
    }

    public double[] getParamsAt(int index) {
        return paramLists.get(index);
    }

    public int getNumParams() {
        return numParams;
    }

    public double getSecondsAt(int index) {
        return secondLists.get(index);
    }

    public int getScoreLength() {
        return paramLists.size();
    }

    public void setParamsAt(int index, double[] params) {
        if (params != null && params.length == numParams && index >= 0 && index < paramLists.size()) {
            paramLists.set(index, params);
            fireStateChanged();
        }
    }

    public void setSecondsAt(int index, double seconds) {
       if (index >= 0 && index < paramLists.size()) {
            secondLists.set(index, seconds);
            fireStateChanged();
        }
    }

    public void removeAt(int index) {
       synchronized(lock1) {
            paramLists.remove(index);
            secondLists.remove(index);
            fireStateChanged();
        }
    }

    public void setParamsAt(int index, int paramNum, double val) {
        double[] ps = paramLists.get(index);
        if (ps != null && ps.length > paramNum && paramNum >= 0) {
            ps[paramNum] = val;
            fireStateChanged();
        }
        //TODO: re-add? probably not
    }

    private class ScorePlayer implements Runnable {
        public void run() {
            OscHandler.getOscHandler().startSound();
            try {
                int i = 0;
                while (true) {
                    long mySleep = 1000;
                    synchronized(lock1) { //Need to assume that params size is constant in here
                        if (paramLists.size() == 0) {
                            //sleep 1000
                            System.out.println("Size 0");
                        } else if (i < paramLists.size()) {
                            System.out.print("Next: ");
                            double[] next = paramLists.get(i);
                            setPlayingRow(i);
                            for (int j =0 ; j < numParams; j++) {
                                System.out.print(next[j] + " ");
                            }
                            System.out.println("");
                           
                                //float f[] = new float[numParams];
                                WekinatorLearningManager.getInstance().setParams(next);
                                OscHandler.getOscHandler().sendParamsToSynth(next); //TODO: hack: get out of here!
                            

                            mySleep = (long) (secondLists.get(i) * 1000);
                            i++;
                        } else {
                            System.out.println("no sleep");
                           mySleep  = 0;
                           i = 0;
                        }
                    }
                    System.out.println("sleeping " + mySleep);
                    Thread.sleep(mySleep);
                }

            } catch (InterruptedException ex) {
                System.out.println("I was interrupted");
            }
            System.out.println("I finished.");
        }
    }

    public static void main(String[] args) {
        try {
            PlayalongScore ps = new PlayalongScore(3);
            double[] d = {1.0, 2.0, 3.0};
            ps.addParams(d, 1.0);
            double[] d2 = {4.0, 5.0, 6.0};
            ps.addParams(d2, 1.0);

            System.out.println("Playing");
            ps.play();
                Thread.sleep(2999);
            
            System.out.println("removing");
            //double[] d3 = {10, 11, 12};
            //ps.addParams(d3, 2.0);
            ps.removeAt(1);
            Thread.sleep(1001);
            ps.removeAt(0);
            Thread.sleep(10000);
            System.out.println("Stopping");
            ps.stop();
            System.out.println("Stopped");
        } catch (InterruptedException ex) {
            Logger.getLogger(PlayalongScore.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

        public void addChangeListener(ChangeListener l) {
        listenerList.add(ChangeListener.class, l);
    }

    public void removeChangeListener(ChangeListener l) {
        listenerList.remove(ChangeListener.class, l);
    }

    protected void fireStateChanged() {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -=2 ) {
            if (listeners[i] == ChangeListener.class) {
                if (changeEvent == null) {
                    changeEvent = new ChangeEvent(this);
                }
                ((ChangeListener)listeners[i+1]).stateChanged(changeEvent);
            }
        }
    }


}
