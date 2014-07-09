package _07_02_MemoryLeaks.test;

import java.util.*;

/**
 * StalledStack demonstrates loitering objects pinned in the heap. It uses static methods provided by FileProcessor
 * class to parse a JProbe Coverage XML snapshot, extract Java file names and simulate a time and/or resource intensive
 * process that uses these file names.
 */
public class Test {

	public static void main( String[] args) throws Exception {
		// Print out all arguments passed in to this main method.
		System.out.println( "Arguments : ");
		for ( int i = 0; i < args.length; i++) {
			System.out.println( args[ i]);
		}
		System.out.println( "\n");

		if ( args.length < 1) {
			System.out.println( "Usage: StalledStack <xml file>");
			System.exit( 1);
		}

		/* BEGIN YourKit
		 * 
		 * This is specific to using a dynamic profiler such as YourKit.
		 * 
		 * Halt the program by waiting for input in order to allow you to attach YourKit to the application. Otherwise
		 * you would have no chance to attach and set the triggers because the program ends faster than you can set the
		 * triggers. */
		System.out.println( "shall we continue? <press RETURN>");
		try {
			System.in.read();
			System.in.read();
		} catch ( Exception e) {
			e.printStackTrace();
		}
		/* Print the triggers you have been specifying manually.
		 * 
		 * It requires an external library that is located at: <YourKit_Home>\lib\yjp.jar and demonstrates use of the
		 * YourKit API. See YourKit => Help => Profiler API for details and the JavaDoc. */
		// System.out.println( "Triggers: " + new com.yourkit.api.Controller().getTriggers());
		/* END YourKit */

		System.out.println( "Parsing file: " + args[ 0]);

		try {
			ArrayList<String> javafiles = FileProcessor.readXMLFile( args[ 0]);
		} catch ( NoClassDefFoundError e) {
			System.out.println( "An implementation of a SAX Parser is missing from your classpath.");
			System.out.println( "Please add crimson.jar located in the 'lib' folder of the demos directory");
			System.out.println( "to your classpath and try again.\n");
			e.printStackTrace();
			System.exit( 1);
		} catch ( Exception e) {
			e.printStackTrace();
			System.exit( 1);
		}
	}
}
