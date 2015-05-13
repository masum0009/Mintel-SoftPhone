

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

package net.sourceforge.peers.scheduler;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Clock implementation wich uses the default OS clock.
 * 
 * @author kulikov
 */
public class DefaultClock implements Clock {

    /**
     * The default time unit: nanoseconds.
     */
    private TimeUnit timeUnit = TimeUnit.NANOSECONDS;

    /**
     * (Non Java-doc.)
     *
     * @see org.mobicents.media.server.scheduler.Clock.getTime().
     */
    public long getTime() {
        return System.nanoTime();
    }

    /**
     * (Non Java-doc.)
     *
     * @see org.mobicents.media.server.scheduler.Clock.getTimeUnit().
     */
    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    /**
     * (Non Java-doc.)
     *
     * @see org.mobicents.media.server.scheduler.Clock.getTime().
     */
    public long getTime(TimeUnit timeUnit) {
        return timeUnit.convert(System.nanoTime(), this.timeUnit);
    }
}

