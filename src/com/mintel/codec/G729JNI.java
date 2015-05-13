package com.mintel.codec;


import net.sourceforge.peers.media.g729.Util;
public class G729JNI {
	
	
	
	private static native void open();
	public native static int decode(byte encoded[], short audio[], int size);
	public native static int encode( short audio[],int offset,byte encoded[], int size);
	public native void close(); 
	
	static {
		try {
			System.loadLibrary("mintelphone-g729");
			open();
			System.out.println("G729 codec init");
		} catch (Throwable e) {
			e.printStackTrace();
			System.out.println("G729 codec init failed");
		}
	}

}


