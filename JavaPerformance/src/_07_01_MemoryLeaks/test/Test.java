package _07_01_MemoryLeaks.test;

import java.io.*;
import java.util.concurrent.*;

public final class Test {
	private static final String	FILENAME	= "data/test.txt";

	// private static class MyBlockingQueue {
	// private int idx = 0;
	// private List list = new ArrayList();
	//
	// synchronized public void put( Object elem) {
	// while ( list.size() > idx) {
	// try {
	// this.wait();
	// } catch ( InterruptedException ie) {
	// }
	// }
	//
	// list.add( idx, elem);
	// this.notifyAll();
	// }
	//
	// synchronized public Object get() {
	// while ( list.size() == idx)
	// try {
	// this.wait();
	// } catch ( InterruptedException ie) {
	// }
	//
	// Object tmp = list.get( idx++);
	//
	// this.notifyAll();
	//
	// return tmp;
	// }
	//
	// }

	private static class LineReader {
		private final int	SAMPLE	= 10;

		public LineReader( final BufferedReader in, final BlockingQueue<String> out) {
			Runnable r = new Runnable() {

				public void run() {
					for ( int i = 0;; i++) {
						if ( i % SAMPLE == 0)
							try {
								System.out.println( "shall we continue? <press any key>");
								System.in.read();
							} catch ( Exception e) {
								e.printStackTrace();
							}
						String tmp = null;
						try {
							tmp = in.readLine();
							if ( tmp == null)
								break;
						} catch ( Exception e) {
							break;
						}

						out.offer( tmp);
					}
				}
			};

			Thread t = new Thread( r);
			t.start();
		}
	}

	private static class Displayer {
		public Displayer( final BlockingQueue<String> in) {
			Runnable r = new Runnable() {
				public void run() {
					for ( int i = 0;; i++) {
						String tmp;
						try {
							tmp = in.take();
							System.out.println( "line " + i + ": " + tmp);
						} catch ( InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			};

			Thread t = new Thread( r);
			t.setDaemon( true);
			t.start();
		}
	}

	public static void main( String[] args) {
		BlockingQueue<String> q = new ArrayBlockingQueue<String>( 100);

		try {
			LineReader lr = new LineReader( new BufferedReader( new FileReader( FILENAME)), q);
			Displayer d = new Displayer( q);
		} catch ( Exception e) {
			e.printStackTrace();
		}
	}
}
