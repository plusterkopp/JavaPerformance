/*
 * Created on 28.03.2012
 *
 */
package _07_04_MemoryLeaks;

import java.io.*;
import java.util.concurrent.*;

public final class Test {
	private static final int	serverPort	= 666;

	@SuppressWarnings( "null")
	public static void main( String[] args) {
		AbstractServer theServer = null;
		try {
			theServer = new Server( serverPort);
		} catch ( IOException e) {
			System.out.println( "exception starting server: " + e);
			e.printStackTrace();
			System.exit( -1);
		}

		System.out.println( "server started ... " + theServer);
		theServer.doAccepting();

		ExecutorService pool = new ThreadPoolExecutor( 8, 8, 0, TimeUnit.MILLISECONDS,
			new ArrayBlockingQueue<Runnable>( 64), new ThreadPoolExecutor.CallerRunsPolicy());

		byte[] buffer = new byte[256];
		for ( int i = 0; i < 1024; i++) {
			try {

				// pool.submit( new Client( serverPort));

				/************************************************************************
				 * If you intend to perform a memory leak analysis with an interactive profiler it
				 * will help if you enable the subsequent statements. Otherwise the program might
				 * exit before you can perform the analysis.
				 */
				new Client( serverPort).run();
				if ( i % 128 == 127) {
					try {
						Thread.sleep( 1000);
					} catch ( InterruptedException e) {
					}
					System.gc();
					System.out.println( "Shall we continue? Waiting for input: ");
					System.out.flush();
					try {
						System.in.read( buffer);
					} catch ( IOException e) {
					}
				}
				/************************************************************************/
			} catch ( IOException e) {
				System.out.println( "exception creating client: " + e);
				e.printStackTrace();
			}
		}

		try {
			Thread.sleep( 2000);
		} catch ( InterruptedException e) {
		}

		pool.shutdown();

		/************************************************************************
		 * If you intend to perform a memory leak analysis with an interactive profiler it will help
		 * if you enable the subsequent statements. Otherwise the program might exit before you can
		 * perform the analysis.
		 */
		System.out.println( "Shall we exit? Waiting for input: ");
		System.out.flush();
		try {
			System.in.read( buffer);
		} catch ( IOException e) {
		}
		/************************************************************************/
	}
}
