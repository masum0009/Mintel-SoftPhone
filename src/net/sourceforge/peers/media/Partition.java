/*
 * JBoss, Home of Professional Open Source
 * Copyright XXXX, Red Hat Middleware LLC, and individual contributors as indicated
 * by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a full listing
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 */
package net.sourceforge.peers.media;

import java.util.ArrayList;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author kulikov
 */
public class Partition {

    protected int size;
    private static DelayQueue<Frame> queue = new DelayQueue();
    private ArrayList<Frame> heap = new ArrayList();

    protected Partition(int size) {
        this.size = size;
    }
    
    protected Frame allocate() {
       // if (true) return new Frame(this, new byte[size]);
        if (heap.isEmpty()) {
            return new Frame(this, new byte[size]);
        }
        return heap.remove(0);
    }

    protected void recycle(Frame frame) {
        heap.add(frame);
        //queue.offer(frame, frame.getDelay(TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);
    }

}
