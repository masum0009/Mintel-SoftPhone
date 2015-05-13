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
    
    Copyright 2010, 2011, 2012 Yohann Martineau 
*/

package net.sourceforge.peers.media;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

//import javax.sound.sampled.AudioFormat;
//import javax.sound.sampled.AudioSystem;
//import javax.sound.sampled.DataLine;
//import javax.sound.sampled.LineUnavailableException;
//import javax.sound.sampled.SourceDataLine;
//import javax.sound.sampled.TargetDataLine;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;



import net.sourceforge.peers.Logger;

public class SoundManager implements SoundSource,PlayerListner,RecordListner {

    public final static String MEDIA_DIR = "media";


    private SourceDataLine sourceDataLine;
    CircularBuffer cbuffer;
    byte[] buffholder;
    byte[] recbuf;
    private SoundPlayer player;
    private SoundRecorder recorder;
    private FileOutputStream microphoneOutput;
    private FileOutputStream speakerInput;
    private boolean mediaDebug;
    private Logger logger;
    private String peersHome;
    long oldtime;
    public SoundManager(boolean mediaDebug, Logger logger, String peersHome) {
        this.mediaDebug = mediaDebug;
        this.logger = logger;
        this.peersHome = peersHome;
    }

    public void openAndStartLines() {
        logger.debug("openAndStartLines");
        if (mediaDebug) {
            SimpleDateFormat simpleDateFormat =
                new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            String date = simpleDateFormat.format(new Date());
            StringBuffer buf = new StringBuffer();
            buf.append(peersHome).append(File.separator);
            buf.append(MEDIA_DIR).append(File.separator);
            buf.append(date).append("_");
            buf.append(8000).append("_");
            buf.append(16).append("_");
            buf.append(1).append("_");
            buf.append(1).append("_");
            buf.append("le");
            try {
                microphoneOutput = new FileOutputStream(buf.toString()
                        + "_microphone.output");
                speakerInput = new FileOutputStream(buf.toString()
                        + "_speaker.input");
            } catch (FileNotFoundException e) {
                logger.error("cannot create file", e);
                return;
            }
        }

  
       player = new SoundPlayer();
       player.start();
       
       recorder = new SoundRecorder(8000,160);
       recorder.start(this);

    }

    public synchronized void closeLines() {
        logger.debug("closeLines");
        if (microphoneOutput != null) {
            try {
                microphoneOutput.close();
            } catch (IOException e) {
                logger.error("cannot close file", e);
            }
            microphoneOutput = null;
        }
        if (speakerInput != null) {
            try {
                speakerInput.close();
            } catch (IOException e) {
                logger.error("cannot close file", e);
            }
            speakerInput = null;
        }
//        if (targetDataLine != null) {
//            targetDataLine.close();
//            targetDataLine = null;
//        }
       
      
        
        /*if (sourceDataLine != null) {
          //  sourceDataLine.drain();
           sourceDataLine.close();
        }*/
        
        if(player != null){
        	
        	player.stop();
            player = null;
          
        }
        
        if(recorder != null){
        	recorder.stop();
        
        }
    }

    /**
     * audio read from microphone, read all available data
     * @return
     */
    
   /* public synchronized byte[] readData() {
    	boolean wait = true;
    	 byte[] buff = null;
    	while(wait){
    	if(recbuf != null){	
    	System.out.println("audiorecord: call for read" + recbuf.length);  
    	buff = recbuf;
    	break;
    	} else
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
       recbuf = null;
        return buff;
    }*/
    
    public synchronized byte[] readData() {
    	byte[] buffer = new byte[320];
    	 
    	buffer= recorder.readData();
    	
    	return buffer;
    	
    }

    /**
     * audio sent to speaker
     * 
     * @param buffer
     * @param offset
     * @param length
     * @return
     */
    public int writeData(byte[] buffer, int seq, long timestamp) {
    	int numberOfBytesWritten =buffer.length;
  		numberOfBytesWritten = player.writeTrack(buffer,seq ,buffer.length);
 
    	return numberOfBytesWritten;

    }

	@Override
	public byte[] fillBuffer() {
		
		// TODO Auto-generated method stub
		//System.out.println("dialer: fill buffer call");
		if(buffholder == null) return null;
		byte[] buff;
		//buff = cbuffer.getData(160);
		buff =buffholder;
		//if(buff != null)
		//	len = buff.length;
		buffholder = null;
		return buff;
		 
	}

	@Override
	public void receivedRecPacket(byte[] recbuff) {
		// TODO Auto-generated method stub
		this.recbuf = recbuff;
	}

}
