package net.sourceforge.peers.media;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import net.sourceforge.peers.Logger;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.*;
public class SourceDataLine extends Thread
{
	
	AudioTrack at = null;
	public PlayerListner playCallback;
	 public static float good, late, lost, loss, loss2;
     double avgheadroom,devheadroom;
     int avgcnt;
     public static int timeout;
     int seq;
     
	int maxjitter,minjitter,minjitteradjust;
    int cnt,cnt2,user,luser,luser2,lserver;
    public static int jitter,mu;
   boolean running = false;
   long oldtime;
	 public static final int BUFFER_SIZE = 1024;
	 short lin2[] = new short[BUFFER_SIZE];
	 short lin1[] = new short[BUFFER_SIZE];

	 public SourceDataLine() {
		 
	
	 }
	 
	 void initAudio(){
		 synchronized (this) {
		 mu  = 1;
		 maxjitter  = AudioTrack.getMinBufferSize(8000,
	                AudioFormat.CHANNEL_OUT_MONO,
	                AudioFormat.ENCODING_PCM_16BIT);
	
		 
		 if (maxjitter < 2*2*BUFFER_SIZE*6*mu)
             maxjitter = 2*2*BUFFER_SIZE*6*mu;
		
		// cbuffer = new CircularBuffer(1024);
		 at = new AudioTrack(AudioManager.STREAM_MUSIC, 8000,
	                AudioFormat.CHANNEL_OUT_MONO,
	                AudioFormat.ENCODING_PCM_16BIT,  maxjitter *2,
	                AudioTrack.MODE_STREAM);
	    
		
		 maxjitter /= 2*2;
         minjitter = minjitteradjust = 500*mu;
         jitter = 875*mu;
         devheadroom = Math.pow(jitter/5, 2);
         timeout = 1;
         luser = luser2 = -8000*mu;
         cnt = cnt2 = user = lserver = 0;
         seq = 0;
		 at.flush();
		 }
		 
	 }
     void newjitter(boolean inc) {

         int newjitter = (int)Math.sqrt(devheadroom)*5 + (inc?minjitteradjust:0);
         if (newjitter < minjitter)
                 newjitter = minjitter;
         if (newjitter > maxjitter)
                 newjitter = maxjitter;
         if (!inc && (Math.abs(jitter-newjitter) < minjitteradjust || newjitter >= jitter))
                 return;
         if (inc && newjitter <= jitter)
                 return;
         jitter = newjitter;
         late = 0;
         avgcnt = 0;
         luser2 = user;
      }

	 
	 public void startPlay(){
		 // at.play();
         running = true;
  
	 }
	 
	
	 
	 public void close(){
		 
		  if(at != null){
	        	 at.stop();
	        	 at.release();
	        	 System.out.println(" Dialer  closed here");
	        }
		  
		  running = false;
	 }
	 
	 
	 public int writeTrack(byte[] buffer, int sequence, int length) throws IOException{
		 android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO ); 
		 int gap;
		 
		// System.out.println(" dialer: got data for seq" + sequence);
		 if (seq == sequence) {
	          //   m++;
	             return 0;
	     }
	     gap = (sequence - seq) & 0xff;
	     if (gap > 240)
	             return 0;
	   timeout = 0;
	   
	 
	   
	   lin1 = byteArrayToShortArray(buffer);
	   
	   playTrack(lin1);
	  
       return length;
		 
	 }
	 
	 public static short bytesToShort(byte byte1, byte byte2) {
         return (short)(0xffff&((0xff&byte1) | ((0xff&byte2)<<8)));
     }
	 
	 public static short[] byteArrayToShortArray(byte[] bytes) {
          short[] s = new short[bytes.length/2];
          for(int q=0; q<s.length;q++) {
                  s[q] = bytesToShort(bytes[2*q],bytes[2*q+1]);
          }
          return s;
      }
	 
	  void write(short a[],int b,int c) {
	         synchronized (this) {
	                user+= at.write(a,b,c);
	         }
	     }


	
	  void calc2(short[] lin,int off,int len) {
          int i,j;
         
          for (i = 0; i < len; i++) {
                  j = lin[i+off];
                  if (j > 16350)
                          lin[i+off] = 16350<<1;
                  else if (j < -16350)
                          lin[i+off] = -16350<<1;
                  else
                          lin[i+off] = (short)(j<<1);
          }
    }
	  
	public void run(){
		
		  initAudio();
	
			System.gc();
			   at.play();
		while(running){
			 if(timeout != 0){
				 
				 at.pause();
					for (int i = maxjitter*4; i > 0; i -= BUFFER_SIZE)
						write(lin2,0,i>BUFFER_SIZE?BUFFER_SIZE:i);
					cnt += maxjitter*2;
					at.play();
			 }
			 timeout = 0;
			 
			 

	         
			 byte[] buf = this.playCallback.fillBuffer();
			 if(buf != null){
				 
				 lin1 = byteArrayToShortArray(buf);
				   
				   playTrack(lin1);
				  
			 }
		
		}
	//	at.stop();
		//at.release();
	}  

	
	 private void playTrack(short buffer[]){
		 synchronized (this) {
		 int server, headroom, todo, len = 0,gap; 

	      
		
		 timeout = 0;
	     
			if(buffer ==null ) len =0;
			else
			 len = buffer.length;
			calc2(buffer,0,buffer.length);
			 server = at.getPlaybackHeadPosition() ;
	         headroom = user-server;
	         long diff = System.currentTimeMillis() - oldtime; 
	         System.out.println(" headroom :" + headroom + " server: " + server + " user:" + user + " jitter:" + jitter + "time diff" + diff);
	         oldtime = System.currentTimeMillis();
	         if (headroom > 2*jitter)
	             cnt += len;
		     else
		            cnt = 0;
		     
		     if (lserver == server)
		             cnt2++;
		     else
		             cnt2 = 0;

		     if (cnt == 0)
	             avgheadroom = avgheadroom * 0.99 + (double)headroom * 0.01;
	          if (avgcnt++ > 300)
	             devheadroom = devheadroom * 0.999 + Math.pow(Math.abs(headroom - avgheadroom),2) * 0.001;
	         
	          if (headroom < 250*mu) {
			    	 System.out.println("dialer: writing  head empty ");
			    	 late++;
		             avgcnt += 10;
		             if (avgcnt > 400)
		                     newjitter(true);
		             todo = jitter - headroom;
		             write(lin2,0,BUFFER_SIZE);
		            
		        }
		    
		     
		     if (cnt > 500*mu && cnt2 < 2) {
		    	// System.out.println("dialer: writing  jitter ");
	             todo = headroom - jitter;
	             if (todo < len)
	                    write(buffer,todo,len-todo);
		     } else{
		    	// System.out.println("dialer: writing  normal ");
		    	 if(len > 0 )  write(buffer,0,len);
		     }
		     lserver = server;
		  //   lin1 = null;
		 }
		 
	 }
	 
    

	
	 }






	
	
	 
	 
	 
