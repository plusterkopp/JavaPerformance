/*
 * Created on 19.02.2007
 *
 */
package _10_01_FinalGC.cache;

import java.lang.ref.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class Memoizer<A, R> implements Computable<A, R> {

	private class RefQueueProcessor implements Runnable {
		private final Thread	theThread;

		public RefQueueProcessor() {
			theThread = new Thread( this);
			theThread.setDaemon( true);
			theThread.start();
		}

		@Override
		public void run() {
			boolean poll = false;
			boolean pollWait = false;
			int i = 0;
			int total = 0;
			long misses = 0L;
			for ( ;;) {
				try {
					if ( poll) {
						Reference<? extends R> ref = theQ.poll();

						if ( ref != null) {
							i++;
							pollWait = false;
						} else {
							if ( !pollWait) {
								pollWait = true;
								Thread.sleep( 2);
							} else {
								total += i;
								// System.out.println("----> " + i + " / " + total +
								// " SoftReferences cleared at " + misses + " misses");
								i = 0;
								misses = 0L;
								poll = false;
								pollWait = false;
							}
						}
					} else {
						theQ.remove();
						misses = getMisses();
						i++;
						poll = true;
					}
				} catch ( InterruptedException e) {
				}
			}
		}
	}

	private final ReferenceQueue<R>									theQ		= new ReferenceQueue<R>();
	private final RefQueueProcessor									qProcessor	= new RefQueueProcessor();

	private final ConcurrentHashMap<A, Future<SoftReference<R>>>	cache		= new ConcurrentHashMap<A, Future<SoftReference<R>>>();

	private final Computable<A, R>									computable;

	private final AtomicLong										hits		= new AtomicLong();
	private final AtomicLong										misses		= new AtomicLong();

	public Memoizer( Computable<A, R> c) {
		computable = c;
	}

	public long getHits() {
		return hits.get();
	}

	public long getMisses() {
		return misses.get();
	}

	public R compute( final A arg) throws InterruptedException {
		boolean miss = false;

		while ( true) {
			Future<SoftReference<R>> result = cache.get( arg);

			if ( result == null) {
				if ( !miss) {
					miss = true;
					misses.incrementAndGet();
				}

				Callable<SoftReference<R>> eval = new Callable<SoftReference<R>>() {
					public SoftReference<R> call() throws InterruptedException {
						return new SoftReference<R>( computable.compute( arg), theQ);
					}
				};
				FutureTask<SoftReference<R>> ft = new FutureTask<SoftReference<R>>( eval);
				result = ft;
				result = cache.putIfAbsent( arg, ft);
				if ( result == null) { // task war neu?
					result = ft;
					ft.run();
				}
			}
			// ab hier ist result aus dem cache, alt oder neu angelegt
			try {
				SoftReference<R> ref = result.get();
				R ret = ref.get();
				if ( ret != null) { // Softref noch da: HIT
					if ( !miss) {
						hits.incrementAndGet();
					}
					return ret;
				}
				// SoftRef schon GCed: aus Cahce löschen
				cache.remove( arg, result);
			} catch ( ExecutionException e) {
				System.out.println( "aborted by exception: " + e);
				throw launderThrowable( e);
			}
		}
	}

	private static RuntimeException launderThrowable( Throwable t) {
		if ( t instanceof RuntimeException)
			return ( RuntimeException) t;
		else if ( t instanceof Error)
			throw ( Error) t;
		else
			throw new IllegalStateException( "not unchecked", t);
	}

	public void adjustToGCFactor( double gcFactor) {
		if ( gcFactor > 1) {
			int sizeOld = cache.size();
			int sizeNew = ( int) ( sizeOld / gcFactor);
			int remCount = 0;
			Iterator<A> it = cache.keySet().iterator();
			while ( cache.size() > sizeNew) {
				A a = it.next();
				if ( a != null) {
					cache.remove( a);
					remCount++;
				}
			}
			System.out.println( "removed " + remCount + " entries");
		}
	}
}
