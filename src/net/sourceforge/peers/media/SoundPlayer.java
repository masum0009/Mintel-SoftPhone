package net.sourceforge.peers.media;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import net.sourceforge.peers.Logger;


public class SoundPlayer {
	AudioTrack at = null;
	
	int user, server,seq=0;
	int maxjitter;
	long oldtime = 0;
	boolean isRunning;
	 AudioPlayer player;
	 int writeHead;
	 int bufferHighMark;
	public SoundPlayer(){
		
		 android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO ); 

		 maxjitter  = AudioTrack.getMinBufferSize(8000,
	                AudioFormat.CHANNEL_OUT_MONO,
	                AudioFormat.ENCODING_PCM_16BIT) * 6;
	
		 System.out.println("dialer:new  maxjitter is " + maxjitter );
		 
		 at = new AudioTrack(AudioManager.STREAM_MUSIC, 8000,
	                AudioFormat.CHANNEL_OUT_MONO,
	                AudioFormat.ENCODING_PCM_16BIT,  maxjitter,
	                AudioTrack.MODE_STREAM);
		 
		 bufferHighMark = maxjitter / 2 * 8 / 10;
		 System.out.println("dialer1: play buffer = " + maxjitter + ", high water mark="
                 + bufferHighMark);
		player = new AudioPlayer(at, maxjitter, 160); 
		
		 //player.flush();
		 writeHead = player.getPlaybackHeadPosition();
	}
	
	 public int writeTrack(byte[] buffer,int sequence,  int length){
		// at.play();
		 //if(seq == 0 ) seq = sequence;
		// if(sequence < seq ) return 0;
		// Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);
		 int playState =  player.getPlayState();
		 if (playState == AudioTrack.PLAYSTATE_STOPPED) {
             player.play();
             playState = player.getPlayState();
         }
		 short[] shortMedia = byteArrayToShortArray(buffer);
		 calc2(shortMedia,0,shortMedia.length);
		// player.write(shortMedia, 0, shortMedia.length);
		 seq = sequence;

		 
		 int buffered =    writeHead - player.getPlaybackHeadPosition() ;
		 //System.out.println("dialer: bufer is " + buffered + " write head " + writeHead);
         if (buffered > bufferHighMark) {
             player.flush();
             buffered = 0;
             writeHead = player.getPlaybackHeadPosition();
            // if (LogRateLimiter.allowLogging(now)) {
             System.out.println( "dialer: set writeHead to "
                        + writeHead);
            // }
         }

         writeHead += player.write(shortMedia, 0, length/2);
		 
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
	 

	
	
	 public void start(){
		  player.play();
		  isRunning = true;
        
	 }
	 
	 
	 public void stop(){
		
		  isRunning = false;
		  if(at != null){
			  player.stop();
	        	 at.stop();
	        	 at.release();
	        	 System.out.println(" Dialer  closed here");
	        	  
	        }
		  
	 }
	 

}


class NoiseGenerator {
    private static final int AMP = 1000;
    private static final int TURN_DOWN_RATE = 80;
    private static final int NOISE_LENGTH = 160;

    private short[] mNoiseBuffer = new short[NOISE_LENGTH];
    private int mMeasuredVolume = 0;

    short[] makeNoise() {
        final int len = NOISE_LENGTH;
        short volume = (short) (mMeasuredVolume / TURN_DOWN_RATE / AMP);
        double volume2 = volume * 2.0;
        int m = 8;
        for (int i = 0; i < len; i+=m) {
            short v = (short) (Math.random() * volume2);
            v -= volume;
            for (int j = 0, k = i; (j < m) && (k < len); j++, k++) {
                mNoiseBuffer[k] = v;
            }
        }
        return mNoiseBuffer;
    }

    void measureVolume(short[] audioData, int offset, int count) {
        for (int i = 0, j = offset; i < count; i++, j++) {
            mMeasuredVolume = (mMeasuredVolume * 9
                    + Math.abs((int) audioData[j]) * AMP) / 10;
        }
    }

    int getNoiseLength() {
        return mNoiseBuffer.length;
    }
}


//Use another thread to play back to avoid playback blocks network
//receiving thread
class AudioPlayer implements Runnable,
     AudioTrack.OnPlaybackPositionUpdateListener {
 private short[] mBuffer;
 private int mStartMarker;
 private int mEndMarker;
 private AudioTrack mTrack;
 private int mFrameSize;
 private int mOffset;
 private boolean mIsPlaying = false;
 private boolean mNotificationStarted = false;
 private int noise_length = 160;
 private NoiseGenerator mNoiseGenerator = new NoiseGenerator();
 
 AudioPlayer(AudioTrack track, int bufferSize, int frameSize) {
     mTrack = track;
     mBuffer = new short[bufferSize];
     mFrameSize = frameSize;
 }

 synchronized int write(short[] buffer, int offset, int count) {
     int bufferSize = mBuffer.length;
     while (getBufferedDataSize() + count > bufferSize) {
         try {
             wait();
         } catch (Exception e) {
             //
         }
     }

     int end = mEndMarker % bufferSize;
     if (end + count > bufferSize) {
         int partialSize = bufferSize - end;
         System.arraycopy(buffer, offset, mBuffer, end, partialSize);
         System.arraycopy(buffer, offset + partialSize, mBuffer, 0,
                 count - partialSize);
     } else {
         System.arraycopy(buffer, 0, mBuffer, end, count);
     }
     mEndMarker += count;

     return count;
 }

 synchronized void flush() {
     mEndMarker = mStartMarker;
     notify();
 }

 int getBufferedDataSize() {
     return mEndMarker - mStartMarker;
 }

 synchronized void play() {
     if (!mIsPlaying) {
         mTrack.setPositionNotificationPeriod(mFrameSize);
         mTrack.setPlaybackPositionUpdateListener(this);
         mIsPlaying = true;
         mTrack.play();
         mOffset = mTrack.getPlaybackHeadPosition();

         // start initial noise feed, to kick off periodic notification
         new Thread(this).start();
     }
 }

 synchronized void stop() {
     mIsPlaying = false;
     mTrack.stop();
     mTrack.flush();
     mTrack.setPlaybackPositionUpdateListener(null);
    
 }

 synchronized void release() {
     mTrack.release();
 }

 public synchronized void run() {
   //  Log.d(TAG, "start initial noise feed");
     int count = 0;
     long waitTime = mNoiseGenerator.getNoiseLength() / 8; // ms
     while (!mNotificationStarted && mIsPlaying) {
    	// System.out.println("noise: call for noise");
    	 
         feedNoise();
         count++;
         try {
             this.wait(waitTime);
         } catch (InterruptedException e) {
       //      Log.e(TAG, "initial noise feed error: " + e);
             break;
         }
     }
    // Log.d(TAG, "stop initial noise feed: " + count);
 }

 int getPlaybackHeadPosition() {
     return mStartMarker;
 }

 int getPlayState() {
     return mTrack.getPlayState();
 }

 int getState() {
     return mTrack.getState();
 }

 // callback
 public void onMarkerReached(AudioTrack track) {
 }

 // callback
 public synchronized void onPeriodicNotification(AudioTrack track) {
     if (!mNotificationStarted) {
         mNotificationStarted = true;
   //      Log.d(TAG, " ~~~   notification callback started");
     } else if (!mIsPlaying) {
    //     Log.d(TAG, " ~x~   notification callback quit");
         return;
     }
     try {
         writeToTrack();
     } catch (IllegalStateException e) {
      //   Log.e(TAG, "writeToTrack()", e);
     }
 }

 private void feedNoise() {
	 //System.out.println("noise: call for noise");
     short[] noiseBuffer = mNoiseGenerator.makeNoise();
    // int i;
    // for(i=0;i<noiseBuffer.length;i++)
    //	 noiseBuffer[i] =  Short.MIN_VALUE;
    	 
     mOffset += mTrack.write(noiseBuffer, 0, noiseBuffer.length);
 }

 private synchronized void writeToTrack() {
     if (mStartMarker == mEndMarker) {
         int head = mTrack.getPlaybackHeadPosition() - mOffset;
         if ((mStartMarker - head) <= 320) feedNoise();
         return;
     }

     int count = mFrameSize;
     if (count < getBufferedDataSize()) count = getBufferedDataSize();

     int bufferSize = mBuffer.length;
     int start = mStartMarker % bufferSize;
     if ((start + count) <= bufferSize) {
         mStartMarker += mTrack.write(mBuffer, start, count);
     } else {
         int partialSize = bufferSize - start;
         mStartMarker += mTrack.write(mBuffer, start, partialSize);
         mStartMarker += mTrack.write(mBuffer, 0, count - partialSize);
     }
     notify();
 }
}



