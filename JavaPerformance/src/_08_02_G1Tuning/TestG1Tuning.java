/*
  Based on course material for "High-Performance Java", a seminar 
  prepared and owned by Angelika Langer & Klaus Kreft.
  contact: http://www.angelikalanger.com/

  © Copyright 2005-2010 by Angelika Langer & Klaus Kreft.
  Permission to use, copy, and modify this software for any non-profit
  purpose is hereby granted to attendants of the above mentioned seminar 
  without fee, provided that the above copyright notice appears in all 
  copies.  Angelika Langer and Klaus Kreft make no representations about 
  the suitability of this software for any purpose.  It is provided 
  "as is" without express or implied warranty.
 */
package _08_02_G1Tuning;

import java.util.*;

/*
 synthetic memory allocation
 ---------------------------

 This application's memory allocation scheme is parameterized as follows:
 - delay     = period of time between successive memory allocation (in msec)
 - size      = size of allocated memory chunks (in byte)
 - number    = number of memory chunks to be allocated
 (i.e. size x number = amount of memory to be allocated)
 - hold rate = number of delays after which the allocated memory is free
 (i.e. hold rate x delay = period of time for which the memory is alive)

 The application starts several "memory allocators" each of which can have a different
 configuration (see -> allocStarter).

 The application terminated after a specified period of time (see -> stopAfter).
 */

public class TestG1Tuning {

	private static class MemoryAllocator extends TimerTask {

		private int								cycle;
		private final int						size;
		private final int						number;
		private final int						holdRate;
		private final Map<Integer, byte[][]>	chunks	= new HashMap<Integer, byte[][]>();

		public MemoryAllocator( int size, int number, int holdRate) {
			this.size = size;
			this.number = number;
			this.holdRate = holdRate;
		}

		@Override
		public void run() {
			cycle++;

			byte[][] holder = new byte[number][];
			for ( int i = 0; i < number; i++)
				holder[i] = new byte[size];

			chunks.put( cycle + holdRate, holder);
			for ( int i = 0; i < holder.length; i++)
				holder[i] = null;

			holder = chunks.remove( cycle);
			if ( holder != null) {
				for ( int i = 0; i < holder.length; i++)
					holder[i] = null;
			}
		}

	}

	final static Timer	allocTimer	= new Timer();
	final static Timer	stopTimer	= new Timer( true);

	private static void stopAfter( long runTime) {
		stopTimer.schedule( new TimerTask() {
			public void run() {
				allocTimer.cancel();
			}
		}, runTime);
	}

	private static void allocStarter( int delay, int size, int number, int holdRate) {
		allocTimer.scheduleAtFixedRate( new MemoryAllocator( size, number, holdRate), delay, delay);
	}

	public static void main( String[] argv) {
		// delay, size, number, hold rate (multiple of delay), ? free rate (multiple of delay)?, ?
		// free number rate (1 = number) ?

		/*
		 * erster Versuch allocStarter( 50, 8*1024, 16, 10); allocStarter(100, 8*1024, 16, 5*2);
		 * allocStarter(300, 2*1024, 8, 3*30); allocStarter(1000, 1024, 8, 60*5);
		 */

		/*
		 * zweiter Versuch allocStarter( 33, 16*1024, 32, 10); allocStarter( 50, 16*1024, 32, 10);
		 * allocStarter( 66, 16*1024, 32, 10); allocStarter(100, 16*1024, 32, 10); allocStarter(250,
		 * 8*1024, 16, 60); allocStarter(500, 4*1024, 16, 60*2); allocStarter(750, 4*1024, 16,
		 * 60*2); allocStarter(1000, 1024, 8, 60*5);
		 */

		/*
		 * dritter Versuch allocStarter( 33, 16*1024, 256, 10); allocStarter( 50, 16*1024, 256, 10);
		 * allocStarter( 66, 16*1024, 256, 10); allocStarter(100, 16*1024, 256, 10);
		 * allocStarter(250, 8*1024, 128, 60); allocStarter(500, 4*1024, 128, 60*2);
		 * allocStarter(750, 4*1024, 128, 60*2); allocStarter(1000, 1024, 128, 60*5);
		 */

		/*
		 * original lab for CMS and parOld allocStarter( 50, 32*1024, 256, 10); allocStarter(100,
		 * 32*1024, 256, 10); allocStarter(250, 8*1024, 128, 60); allocStarter(500, 4*1024, 128,
		 * 60*2); allocStarter(750, 4*1024, 128, 60*2); allocStarter(1000, 1024, 128, 60*5);
		 */

		allocStarter( 100, 32 * 1024, 8 * 256, 20);
		allocStarter( 200, 32 * 1024, 8 * 256, 20);
		allocStarter( 500, 8 * 1024, 8 * 128, 60 * 2);
		allocStarter( 1000, 4 * 1024, 8 * 128, 60 * 4);
		allocStarter( 1500, 4 * 1024, 8 * 128, 60 * 4);
		allocStarter( 2000, 1024, 8 * 128, 60 * 10);

		stopAfter( 1000 * 60 * 2);
	}
}
