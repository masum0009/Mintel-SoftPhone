package net.sourceforge.peers.media;



import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;

public class SoundRecorder 
//implements Runnable
{

    private int mSampleRate;
    private int mFrameSize;
    private boolean mRunning;
    private RecordListner mlistner;
    AudioRecord recorder;
    long oldtime;
    SoundRecorder(int sampleRate, int frameSize) {
        mSampleRate = sampleRate;
        mFrameSize = frameSize;
    }

    void start(RecordListner listner) {
    /*	mlistner = listner;
    	mRunning = true;
        new Thread(this).start();
    */
    	android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
    	 int bufferSize = AudioRecord.getMinBufferSize(mSampleRate,
                 AudioFormat.CHANNEL_CONFIGURATION_MONO,
                 AudioFormat.ENCODING_PCM_16BIT);// * 3 / 2;
    	 System.out.println("audiorecord: min buffer size " + bufferSize);
    		if (bufferSize == 640) {
    			bufferSize = 4096*3/2;
    		}else if (bufferSize < 4096) {
    		
    			bufferSize = 4096*3/2;
    		} else if (bufferSize == 4096) {
    			bufferSize *= 3/2;
    			
    		}
    		
         recorder = new AudioRecord(
                 MediaRecorder.AudioSource.MIC, mSampleRate,
                 AudioFormat.CHANNEL_CONFIGURATION_MONO,
                 AudioFormat.ENCODING_PCM_16BIT, bufferSize);
        if(recorder.getState() == AudioRecord.STATE_UNINITIALIZED){
     	   System.out.println("audiorecord: thread failed init " + bufferSize + " sample rate " +mSampleRate); 
     	   mRunning = false;
     	   return;
        }
         recorder.startRecording();
         mRunning = true;
    }
    
    
    
    public synchronized byte[] readData(){
    	int recordBufferSize = 160;
    	 short[] recordBuffer = new short[recordBufferSize];
    	 
         byte[] buffer = new byte[320];
         int count = recorder.read(recordBuffer, 0, recordBufferSize);
       
         //adjustMicGain(recordBuffer, count, 32);
         calc2(recordBuffer,0,count);
         buffer = shortArrayToByteArray(recordBuffer);
         
        // long diff = System.currentTimeMillis() - oldtime; 
    	 // System.out.println("Record: diff is " + diff );
    	//oldtime =   System.currentTimeMillis();
         
         return buffer;
    }
    
    void stop(){
    	if(recorder!= null && mRunning == true) 
    		recorder.stop();
    	recorder.release();
    	mRunning = false;
    	//mlistner = null;
    
    }

    private void adjustMicGain(short[] buf, int len, int factor) {
        int i,j;
        for (i = 0; i < len; i++) {
            j = buf[i];
            if (j > 32768/factor) {
                buf[i] = 32767;
            } else if (j < -(32768/factor)) {
                buf[i] = -32767;
            } else {
                buf[i] = (short)(factor*j);
            }
        }
    }
    
    public static byte[] shortToBytes(int myInt) {
        byte[] bytes = new byte[2];
        int hexBase = 0xff;
        bytes[0] = (byte) (hexBase & myInt);
        bytes[1] = (byte) (((hexBase << 8)& myInt) >> 8);
        return bytes;
    }
    
    public static byte[] shortArrayToByteArray(short[] values) {
        byte[] s = new byte[values.length*2];
        for(int q=0; q<values.length;q++) {
                byte[] bytes = shortToBytes(values[q]);
                s[2*q] = bytes[0];
                s[2*q+1] = bytes[1];
        }
        return s;
   }
    
    public static byte[] shortarToByteAr(short[] values){
    	int i=0;
    	ByteBuffer byteBuf = ByteBuffer.allocate(2*values.length);
    	//byteBuf.order(ByteOrder.BIG_ENDIAN);
    	while (values.length > i) {
    	    byteBuf.putShort(values[i]);
    	    i++;
    	}
    	return byteBuf.array();
    } 
    
	void calc1(short[] lin,int off,int len) {
		int i,j;
		
		for (i = 0; i < len; i++) {
			j = lin[i+off];
			lin[i+off] = (short)(j>>2);
		}
	}

	void calc2(short[] lin,int off,int len) {
		int i,j;
		
		for (i = 0; i < len; i++) {
			j = lin[i+off];
			lin[i+off] = (short)(j>>1);
		}
	}

	void calc10(short[] lin,int off,int len) {
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
    	 android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
        long oldtime = 0;
        int recordBufferSize = 160;
        short[] recordBuffer = new short[recordBufferSize];
        
        byte[] buffer = new byte[320];
    //    int offset = sender.getPayloadOffset();
       
       
      // am = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
     //   am.setParameters("noise_suppression=auto"); 
        
        int bufferSize = AudioRecord.getMinBufferSize(mSampleRate,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT) * 3 / 2;

        AudioRecord recorder = new AudioRecord(
                MediaRecorder.AudioSource.MIC, mSampleRate,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufferSize);
       if(recorder.getState() == AudioRecord.STATE_UNINITIALIZED){
    	   System.out.println("audiorecord: thread failed init " + bufferSize + " sample rate " +mSampleRate); 
    	   mRunning = false;
    	   return;
       }
        recorder.startRecording();
        
      //  Log.d(TAG, "start sound recording..." + recorder.getState());

        // skip the first read, kick off read pipeline
        recorder.read(recordBuffer, 0, recordBufferSize);

        long sendCount = 0;
        long startTime = System.currentTimeMillis();
      
        while (mRunning) {
            int count = recorder.read(recordBuffer, 0, recordBufferSize);
           // System.out.println("recorder: read here size " + recordBuffer.length); 
            // TODO: remove the mic gain if the issue is fixed on Passion.
            //adjustMicGain(recordBuffer, count, 32);
            long diff = System.currentTimeMillis() - oldtime; 
            
          //  System.out.println("Record: diff is " + diff + " length  is " + recordBuffer.length + " count is " + count);
            oldtime =  System.currentTimeMillis();
           // calc2(recordBuffer,0,160);
            if(recordBuffer.length > 0)
            mlistner.receivedRecPacket(shortArrayToByteArray(recordBuffer));
           
           /* try {
                this.wait(10);
            } catch (InterruptedException e) {
          //      Log.e(TAG, "initial noise feed error: " + e);
                break;
            }
            */
            /*  int encodeCount =
                    encoder.encode(recordBuffer, count, buffer, offset);
            try {
                sender.send(encodeCount);
                if (mSendDtmf) {
                    recorder.stop();
                    sender.sendDtmf();
                    mSendDtmf = false;
                    recorder.startRecording();
                }
            } catch (IOException e) {
                if (mRunning) Log.e(TAG, "send error, stop sending", e);
                break;
            }*/

            sendCount ++;
        }
      /*  long now = System.currentTimeMillis();
        Log.d(TAG, "     sendCount = " + sendCount);
        Log.d(TAG, "     avg send cycle ="
                + ((double) (now - startTime) / sendCount));
        Log.d(TAG, "stop sound recording..."); */
        recorder.stop();
    }
}