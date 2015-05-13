package net.sourceforge.peers.media;

import net.sourceforge.peers.media.g729.Util;
import com.mintel.codec.G729JNI;

public class G729Decoder extends Decoder {


	
	public G729Decoder() {
   
   
    }
	
	
    
	public byte[] process(byte[] data)
	{
	    short[] audio = new short[data.length * 8];
		int len = G729JNI.decode(data, audio, data.length);
		
    	return Util.shortArrayToByteArray(audio);
	}
	
	
	

	@Override
	public void stopDecoder() {
		
	}
	
//	
//    byte[] concatenateByteArrays(byte[] a, byte[] b) {
//        byte[] result = new byte[a.length + b.length]; 
//        System.arraycopy(a, 0, result, 0, a.length); 
//        System.arraycopy(b, 0, result, a.length, b.length); 
//        return result;
//    }

}
