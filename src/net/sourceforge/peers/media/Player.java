package net.sourceforge.peers.media;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
public class Player extends Thread {
	AudioTrack at = null;
	public PlayerListner playCallback;
	int user;
	boolean ruunung = true;
	short[] lin2 = new short[160];
	public Player(){
		
		 android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO ); 

		int maxjitter  = AudioTrack.getMinBufferSize(8000,
	                AudioFormat.CHANNEL_OUT_MONO,
	                AudioFormat.ENCODING_PCM_16BIT) ;
	
		 System.out.println("dialer:new  maxjitter is " + maxjitter );
		 
		 at = new AudioTrack(AudioManager.STREAM_MUSIC, 8000,
	                AudioFormat.CHANNEL_OUT_MONO,
	                AudioFormat.ENCODING_PCM_16BIT,  maxjitter,
	                AudioTrack.MODE_STREAM);
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
	  
	  public synchronized void run() {
		 at.play();
	     while(ruunung){
	    	 int head = user - at.getPlaybackHeadPosition();
	    	 //if(head < 250) at.write(lin2, 0, lin2.length);
	    	 
	    	 System.out.println("dialer: running at player " + head);
	    	 short[] lin;
	    	 byte[] buff =  playCallback.fillBuffer();
	    	 if(buff != null)
	    		 lin = byteArrayToShortArray(buff);
	    	 else
	    		 lin = new short[160];
	    	 write(lin,0,lin.length);
	    	 try{
	    		 this.wait((long) 20); // ms
	    	 }  catch (InterruptedException e) {
	    		 System.out.println("dialer:initial noise feed error: " + e);
	             break;
	         }
	     }
		  at.stop();
		  
	  }
	
	 
	 
	 public void close(){
		 at.stop();
		 at.release();
		 ruunung = false;
		 
	 }
	
	
	
	
}
