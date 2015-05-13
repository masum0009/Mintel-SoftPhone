

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

import net.sourceforge.peers.scheduler.Clock;
import net.sourceforge.peers.scheduler.DefaultClock;

/**
 *
 * @author kulikov
 */
public class Memory {
    public static Clock clock = new DefaultClock();
    public final static int PARTITIONS = 100;

    private static Partition[] partitions = new Partition[PARTITIONS];
    private static int sCount;

    private static int findPartition(int size) {
        for (int i = 0; i < sCount; i++) {
            if (partitions[i].size >= size) {
                return i;
            }
        }
        return -1;
    }

    public synchronized static Frame allocate(int size) {
        int i = findPartition(size);

        if (i < 0) {
            i = sCount;
            partitions[sCount++] = new Partition(size);
            //allocate new segment
        }

        return partitions[i].allocate();
    }

}
