package net.sourceforge.peers.media;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.CountDownLatch;
import net.sourceforge.peers.Logger;

import net.sourceforge.peers.media.g729.Util;
import com.mintel.codec.G729JNI;

public class G729Encoder extends Encoder{

	private  EchoFilter echofilter;
	public G729Encoder(PipedInputStream rawData, PipedOutputStream encodedData,
            boolean mediaDebug, Logger logger, String peersHome,CountDownLatch latch) {
        super(rawData, encodedData, mediaDebug, logger, peersHome, latch);
        echofilter = new EchoFilter(160,.5f);
    }
	
	@Override
	public byte[] process(byte[] data) {
		echofilter.filter(data);   
		short[] audio = Util.byteArrayToShortArray(data);		  
		byte[] encoded = new byte[data.length / 16];  	
		int len = G729JNI.encode(audio, 0, encoded, audio.length);
	    return encoded;
	    
	}

	@Override
	public void closeEncoder() {
		// TODO Auto-generated method stub
		//g729.close();
	}
	

	

	
}
