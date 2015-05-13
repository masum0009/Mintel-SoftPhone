/*
    This file is part of Peers, a java SIP softphone.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    
    Copyright 2008, 2009, 2010, 2011 Yohann Martineau 
*/

package net.sourceforge.peers.media;

import java.io.IOException;
import java.io.PipedOutputStream;
import java.util.concurrent.CountDownLatch;

import net.sourceforge.peers.Logger;


public class Capture implements Runnable {
    
    public static final int SAMPLE_SIZE = 16;
    public static final int BUFFER_SIZE = SAMPLE_SIZE * 20;
    
    private PipedOutputStream rawData;
    private boolean isStopped;
    private SoundSource soundSource;
    private Logger logger;
    private CountDownLatch latch;
    
    public Capture(PipedOutputStream rawData, SoundSource soundSource,
            Logger logger, CountDownLatch latch) {
        this.rawData = rawData;
        this.soundSource = soundSource;
        this.logger = logger;
        this.latch = latch;
        isStopped = false;
    }

    public  void run() {
        byte[] buffer;
        long oldtime = 0;
        long sleepTime = 0;
        long offset = 0;
        long lastSentTime = System.nanoTime();
        // indicate if its the first time that we send a packet (dont wait)
        boolean firstTime = true;
        
        while (!isStopped) {
           
            sleepTime = 19500000 - (System.nanoTime() - lastSentTime) + offset;
            if (sleepTime > 0){
            try {
            	 Thread.sleep(Math.round(sleepTime / 1000000f));
              //  this.wait(19);
                
            } catch (InterruptedException e) {
          //      Log.e(TAG, "initial noise feed error: " + e);
                break;
            }
            buffer = soundSource.readData();
            lastSentTime = System.nanoTime();
            offset = 0;
            }else{
            	 buffer = soundSource.readData();
            	  lastSentTime = System.nanoTime();
                  if (sleepTime < -20000000) {
                      offset = sleepTime + 20000000;
                  }
            	
            }
            
         
            try {
                if (buffer == null) {
                    break;
                }
                rawData.write(buffer);
                rawData.flush();
            } catch (IOException e) {
                logger.error("input/output error", e);
                return;
            }
        }
        latch.countDown();
        if (latch.getCount() != 0) {
            try {
                latch.await();
            } catch (InterruptedException e) {
                logger.error("interrupt exception", e);
            }
        }
    }

    public synchronized void setStopped(boolean isStopped) {
        this.isStopped = isStopped;
    }

}
