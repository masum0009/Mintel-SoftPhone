package net.sourceforge.peers.media;

public class CircularBuffer {
    
    private byte[] buffer;
    private int cursor = 0;
    private int availableData = 0;
    
    public CircularBuffer(int size) {
            buffer = new byte[size];
    }
    
    synchronized public void addData(byte[] data) {
            boolean zeros = false;
            //for(int q=0; q<data.length; q++) if(data[q]!=0) zeros = false;
            //System.out.println("voy a meter " + data.length);
            if(!zeros) {
                    for(int q=0; q<data.length; q++) {
                            buffer[(availableData+q)%buffer.length] = data[q];
                    }
                    availableData += data.length;
                    if(availableData > buffer.length)
                        availableData = buffer.length;
            }
    }
    
    synchronized public byte[] getData(int size) {
            if(availableData<size) return null;
            
            byte[] data = new byte[size];
            for(int q=0; q<data.length; q++) {
                    data[q] = buffer[(cursor+q)%buffer.length];
            }
            cursor = (cursor + data.length)%buffer.length;
            availableData -= size;
            return data;
    }
}


