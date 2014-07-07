package _04_JvmProfilingTools;

import java.util.*;

public class MemoryTest {
	private static Random myRandom = new Random();
	private static Set<String> mySet = new HashSet<String>();

	private static void addInteger() {
		mySet.add(new Integer(myRandom.nextInt()).toString());
	}
	public static void addIntegers() {
		for (int i=0; i<100; i++)
			addInteger();
	}

	public static void main(String[] args) {

		// loop for post-mortem data analysis with HPROF + HPjmeter
		for(int i = 0;i<20;i++) {
			addIntegers();
		}


		// loop for monitoring with YourKit, VisualVM, or JProbe
//		for(int i = 0;;i++) {
//			if (i%100 == 0) try {
//                    		System.out.println("shall we continue? <press any key>");
//                    		System.in.read();
//                    	} catch (Exception e) {e.printStackTrace();}
//			addIntegers();
//		}

	}
}