package _02_ListMicroBenchmark;

import java.util.*;
import java.util.concurrent.*;

import de.icubic.mm.bench.base.*;

public final class MicroBenchmark {

	// test data
	private static final int	LOOPSIZE	= 50000;

	private static final int	SMALL		= 2;
	private static final int	MEDIUM		= 20;
	private static final int	LARGE		= 200;

	// dummy method
	// private and final so that it probably gets inlined
	private static void processMessage( String s) {
		int i = s.length();
	}

	private static void processElements_1_remove_with_exception( List l) {
		try {
			for ( ;;) {
				String s = ( String) ( ( LinkedList) l).removeLast();
				processMessage( s);
			}
		} catch ( NoSuchElementException nsee) {
		}
	}

	private static void processElements_2_remove_without_exception( List l) {
		while ( !l.isEmpty()) {
			String s = ( String) ( ( LinkedList) l).removeLast();
			processMessage( s);
		}
	}

	private static void processElements_3_with_index( List l) {
		int len = l.size();

		for ( int i = 0; i < len; i++) {
			String s = ( String) l.get( len - 1 - i);
			processMessage( s);
		}
	}

	private static final void processElements_4_toArray( List l) {
		int len = l.size();
		Object[] array = l.toArray();

		for ( int i = 0; i < len; i++) {
			String s = ( String) array[len - 1 - i];
			processMessage( s);
		}
	}

	private static void processElements_5_iterator( List l) {
		ListIterator liter = l.listIterator( l.size());

		while ( liter.hasPrevious()) {
			String s = ( String) liter.previous();
			processMessage( s);
		}
	}

	private static void processElements_6_reverse( List l) {
		Collections.reverse( l);

		for ( Object s : l) {
			processMessage( ( String) s);
		}
	}

	abstract static class MicroBenchRunnable extends AbstractBenchRunnable {

		List<String>[]	lists;
		private int		size;
		protected int	runCounter;

		public MicroBenchRunnable( int size) {
			super( "MicroBench");
			this.size = size;
		}

		private List<String> createList( int size) {
			List<String> list = new LinkedList<String>();
			for ( int i = 0; i < size; i++) {
				list.add( "");
			}
			return list;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.icubic.mm.bench.base.AbstractBenchRunnable#setup()
		 */
		@Override
		public long setup( long n) {
			if ( size > 0) {
				int ni = ( int) Math.min( 1000000 / size, n); // max 1.000.000
				lists = new List[ni];
				// BenchLogger.sysout( getClass().getSimpleName() + " creating " + ni + " lists");
				for ( int i = 0; i < ni; i++) {
					lists[i] = createList( size);
				}
				// BenchLogger.sysout( ni + " " + getClass().getSimpleName() + " created " + size
				// + " strings");
				// System.gc();
				return ni;
			}
			return n;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.icubic.mm.bench.base.AbstractBenchRunnable#getName()
		 */
		@Override
		public String getName() {
			return getClass().getSimpleName() + "-" + size;
		}

		public long getRunSize() {
			return size;
		}
	}

	static class P1_remove_with_exception extends MicroBenchRunnable {

		public P1_remove_with_exception( int size) {
			super( size);
		}

		@Override
		public void run() {
			processElements_1_remove_with_exception( lists[runCounter++]);
		}
	}

	static class P2_remove_without_exception extends MicroBenchRunnable {

		public P2_remove_without_exception( int size) {
			super( size);
		}

		@Override
		public void run() {
			processElements_2_remove_without_exception( lists[runCounter++]);
		}
	}

	static class P3_with_index extends MicroBenchRunnable {

		public P3_with_index( int size) {
			super( size);
		}

		@Override
		public void run() {
			processElements_3_with_index( lists[runCounter++]);
		}
	}

	static class P4_toArray extends MicroBenchRunnable {

		public P4_toArray( int size) {
			super( size);
		}

		@Override
		public void run() {
			processElements_4_toArray( lists[runCounter++]);
		}
	}

	static class P5_iterator extends MicroBenchRunnable {

		public P5_iterator( int size) {
			super( size);
		}

		@Override
		public void run() {
			processElements_5_iterator( lists[runCounter++]);
		}
	}

	static class P6_reverse extends MicroBenchRunnable {

		public P6_reverse( int size) {
			super( size);
		}

		@Override
		public void run() {
			processElements_6_reverse( lists[runCounter++]);
		}
	}

	public static void main( String[] args) {

		Map<Class, MicroBenchRunnable> benches = new LinkedHashMap();
		// warm-up
		BenchLogger.sysout( "Start warm-up.");
		createBenches( benches, LARGE);
		runBenches( benches, 10, false);
		System.out.println();

		// test with 2
		BenchLogger.sysout( "Start test for small = " + SMALL + " Elements.");
		createBenches( benches, SMALL);
		runBenches( benches, 5, true);
		System.out.println();

		// test with 20
		BenchLogger.sysout( "Start test for medium = " + MEDIUM + " Elements.");
		createBenches( benches, MEDIUM);
		runBenches( benches, 5, true);
		System.out.println();

		// test with 200
		BenchLogger.sysout( "Start test for medium = " + LARGE + " Elements.");
		createBenches( benches, LARGE);
		runBenches( benches, 5, true);
	}

	private static void runBenches( Map<Class, MicroBenchRunnable> benches, int seconds,
			boolean report) {
		if ( report) {
			BenchRunner.clearComparisonList();
		}
		System.gc();

		BenchRunner runner = new BenchRunner( null);
		runner.setRuntime( TimeUnit.SECONDS, seconds);

		for ( MicroBenchRunnable b : benches.values()) {
			runner.setBenchRunner( b);
			runner.run();
			runner.printResults();
			if ( report) {
				BenchRunner.addToComparisonList( b.getName(), runner.getRunsPerSecond());
			}
		}
		if ( report) {
			BenchLogger.sysout( "\nChart: " + seconds + " sec");
			BenchRunner.printComparisonList();
		}
	}

	private static void createBenches( Map<Class, MicroBenchRunnable> benches, int s) {
		P1_remove_with_exception p1 = new P1_remove_with_exception( s);
		benches.put( p1.getClass(), p1);
		P2_remove_without_exception p2 = new P2_remove_without_exception( s);
		benches.put( p2.getClass(), p2);
		P3_with_index p3 = new P3_with_index( s);
		benches.put( p3.getClass(), p3);
		P4_toArray p4 = new P4_toArray( s);
		benches.put( p4.getClass(), p4);
		P5_iterator p5 = new P5_iterator( s);
		benches.put( p5.getClass(), p5);
		P6_reverse p6 = new P6_reverse( s);
		benches.put( p6.getClass(), p6);
	}
}
