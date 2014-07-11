package _10_01_FinalGC.test;

import java.lang.management.*;
import java.lang.management.GarbageCollectorMXBean;
import java.text.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import javax.management.*;
import javax.management.openmbean.*;

import _10_01_FinalGC.cache.*;

import com.sun.management.*;

public final class Test_FinalGC {

	static int	targetGCDurMS	= 10;

	static void installGCCallback( final Memoizer<Integer, int[]> memoizer) {
		// get readable long numbers
		final NumberFormat nf = NumberFormat.getNumberInstance( Locale.US);
		nf.setGroupingUsed( true);
		nf.setMaximumFractionDigits( 3);
		// keep a count of the total time spent in GCs
		final AtomicLong totalGcDurationMS = new AtomicLong( 0);
		// get all the GarbageCollectorMXBeans - there's one for each heap generation
		// so probably two - the old generation and young generation
		List<GarbageCollectorMXBean> gcbeans = ManagementFactory.getGarbageCollectorMXBeans();
		// Install a notifcation handler for each bean
		for ( GarbageCollectorMXBean gcbean : gcbeans) {
			// LOGGER.system( gcbean);
			NotificationEmitter emitter = ( NotificationEmitter) gcbean;
			// use an anonymously generated listener for this example
			// - proper code should really use a named class
			NotificationListener listener = new NotificationListener() {
				// implement the notifier callback handler
				@Override
				public void handleNotification( Notification notification, Object handback) {
					// we only handle GARBAGE_COLLECTION_NOTIFICATION notifications here
					if ( !notification.getType().equals(
						GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION)) {
						return;
					}
					// get the information associated with this notification
					GarbageCollectionNotificationInfo notificationInfo = GarbageCollectionNotificationInfo
						.from( ( CompositeData) notification.getUserData());

					GcInfo gcInfo = notificationInfo.getGcInfo();
					// System.out.println( notification.getMessage());
					// System.out.println( notificationInfo.getGcName() + " - "
					// + notificationInfo.getGcCause() + " - "
					// + notificationInfo.getGcAction() + " - " + gcInfo.getDuration());
					// 10 -> GC-Zeiten sind zehnmal zu lang!
					double gcFactor = ( ( double) gcInfo.getDuration()) / targetGCDurMS;
					memoizer.adjustToGCFactor( gcFactor);
				}

				private void getMemoryUsageInfo( StringBuilder sb,
					Map<String, MemoryUsage> memBefore, Map<String, MemoryUsage> memAfter) {
					for ( Entry<String, MemoryUsage> entry : memAfter.entrySet()) {
						String name = entry.getKey();
						MemoryUsage memdetail = entry.getValue();
						// long memInit = memdetail.getInit();
						long memCommitted = memdetail.getCommitted();
						long memMax = memdetail.getMax();
						long memUsed = memdetail.getUsed();
						MemoryUsage before = memBefore.get( name);
						final long committedBefore = before.getCommitted();
						sb.append( name
								+ ( memCommitted == memMax ? " (fully expanded) "
										: " (still expandable) "));
						if ( committedBefore != 0) {
							long beforepercent = ( ( before.getUsed() * 1000L) / committedBefore);
							long percent = ( ( memUsed * 1000L) / committedBefore); // >100% when it
																					// gets expanded
							sb.append( "used: " + ( beforepercent / 10) + "."
									+ ( beforepercent % 10) + "% -> " + ( percent / 10) + "."
									+ ( percent % 10) + "% ");
						}
						sb.append( "(" + nf.format( ( memUsed / 1024) + 1) + "kB) / ");
					}
					if ( sb.length() > 2) {
						sb.setLength( sb.length() - 2);
					}
				}
			};

			// Add the listener
			emitter.addNotificationListener( listener, null, null);
		}
	}

	public static void main( String[] args) {
		System.out.println( "starting ...");

		final int MODULO = 32000;
		final long runningTime = 2 * 60 * 1000L;

		int numOfThreads = Math.max( 2, Runtime.getRuntime().availableProcessors());

		ExecutorService pool = new ThreadPoolExecutor( numOfThreads, numOfThreads, 0,
			TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>( 32),
			new ThreadPoolExecutor.CallerRunsPolicy());

		Computable<Integer, int[]> comp = new Computable<Integer, int[]>() {
			public int[] compute( Integer arg) throws InterruptedException {
				int size = Math.min( Math.abs( arg * 100), 10000);
				return new int[size];
			}
		};

		final Memoizer<Integer, int[]> cache = new Memoizer<Integer, int[]>( comp);
		installGCCallback( cache);

		final ThreadLocalRandom rand = ThreadLocalRandom.current();
		final long startTime = System.currentTimeMillis();

		long cnt = 0L;

		while ( true) {
			Callable<int[]> task = new Callable<int[]>() {
				public int[] call() throws Exception {
					cache.compute( rand.nextInt() % MODULO);
					return null;
				}
			};
			pool.submit( task);
			cnt++;

			if ( startTime + runningTime < System.currentTimeMillis()) {
				pool.shutdown();
				try {
					pool.awaitTermination( Long.MAX_VALUE, TimeUnit.DAYS);
				} catch ( InterruptedException ie) { /* ignore */
				}

				long hits = cache.getHits();
				long misses = cache.getMisses();

				System.out.println( "... " + cnt + " requests processed");
				System.out.println( "    " + hits + " hits");
				System.out.println( "    " + misses + " misses");
				System.out.println( "    " + ( hits + misses) + " hits + misses");

				double hitRate = Math.round( ( hits * 100.0 * 10000.0) / cnt) / 10000.0;

				System.out.println( "    hit rate: " + hitRate + '%');

				break;
			}

			if ( cnt % 16 == 0)
				try {
					Thread.sleep( 1);
				} catch ( InterruptedException e) { /* ignore */
				}
		}

	}

}
