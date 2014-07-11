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

import java.io.*;
import java.text.*;
import java.util.*;

public class TestG1Analyzer {
	private static String	VERSION	= "3.0";

	private static class GeneralAmountManager {
		static enum MemUnit {
			K, M, G
		}

		static enum MemType {
			USED, ALLOCATED
		};

		private static class MemInfo {
			private int[]		amount	= new int[2];
			private MemUnit[]	unit	= new MemUnit[2];

			public MemInfo( int usedMemory, MemUnit usedMemoryUnit, int allocatedMemory,
				MemUnit allocatedMemoryUnit) {
				int i = MemType.USED.ordinal();
				amount[i] = usedMemory;
				unit[i] = usedMemoryUnit;

				i = MemType.ALLOCATED.ordinal();
				amount[i] = allocatedMemory;
				unit[i] = allocatedMemoryUnit;
			}
		}

		private long		lastTsEffective;
		private long		lastTsAppRtRelevant;
		private long		smallestAppRt	= Long.MAX_VALUE;
		private long		smallestAppRtTs;
		private long		largestAppRt;
		private long		largestAppRtTs;
		private int			numOfAppRts;

		private int			pause;
		private int[]		maxMem			= new int[2];
		private MemUnit[]	maxMemUnit		= { MemUnit.K, MemUnit.K };
		private long[]		maxTs			= new long[2];

		public void addPause( int amount) {
			pause += amount;
		}

		public void handleTimestamp( long ts, boolean appRtRelevant) {
			lastTsEffective = ts;

			if ( appRtRelevant) {
				long appRt = ts - lastTsAppRtRelevant;
				lastTsAppRtRelevant = ts;

				if ( appRt < smallestAppRt) {
					smallestAppRt = appRt;
					smallestAppRtTs = ts;
				}

				if ( appRt > largestAppRt) {
					largestAppRt = appRt;
					largestAppRtTs = ts;
				}

				numOfAppRts++;
			}
		}

		public void handelMemInfo( long ts, MemInfo mi) {
			for ( MemType mt : MemType.values()) {
				if ( mi.unit[mt.ordinal()].ordinal() < maxMemUnit[mt.ordinal()].ordinal()) {
					return;
				} else if ( mi.unit[mt.ordinal()].ordinal() > maxMemUnit[mt.ordinal()].ordinal()) {
					maxMem[mt.ordinal()] = mi.amount[mt.ordinal()];
					maxMemUnit[mt.ordinal()] = mi.unit[mt.ordinal()];
					maxTs[mt.ordinal()] = ts;
					return;
				} else if ( mi.amount[mt.ordinal()] > maxMem[mt.ordinal()]) {
					maxMem[mt.ordinal()] = mi.amount[mt.ordinal()];
					maxTs[mt.ordinal()] = ts;
				}
			}
		}

		public void printResult( TestG1Analyzer a) {
			System.out.println( "************************************************");
			System.out.println( "************  GC TRACE INFORMATION  ************");
			System.out.println( "************************************************");
			int i = MemType.USED.ordinal();
			System.out.println( "max memory used: " + maxMem[i] + maxMemUnit[i] + " - at: "
					+ formatTs( maxTs[i]));
			i = MemType.ALLOCATED.ordinal();
			System.out.println( "max memory allocated: " + maxMem[i] + maxMemUnit[i] + " - at: "
					+ formatTs( maxTs[i]));
			System.out.println( "total runtime: " + formatTs( lastTsEffective) + " sec / "
					+ "sum of pauses: " + toSec( pause) + " sec");
			System.out.println( "throughput: " + toThroughput( lastTsEffective, pause) + "% ");

			int numberOfOtherPauses = ( ( GcErecordingProcessor) a.procs[IdString.YOUNG.ordinal()])
				.getCnt()
					+ ( ( GcErecordingProcessor) a.procs[IdString.MIXED.ordinal()]).getCnt()
					+ ( ( GcErecordingProcessor) a.procs[IdString.MARK_REMARK.ordinal()]).getCnt()
					+ ( ( GcErecordingProcessor) a.procs[IdString.CLEANUP.ordinal()]).getCnt();
			System.out.println( "number of full gc pauses: "
					+ ( ( GcErecordingProcessor) a.procs[IdString.FULL.ordinal()]).getCnt()
					+ " / number of other pauses: " + numberOfOtherPauses);

			int maxPause = 0;
			for ( IdString id : IdString.values()) {
				if ( id.ordinal() > lastAppRtRelevantPauseIdx)
					break;

				maxPause = Math.max( maxPause, a.procs[id.ordinal()].getMaxPause());
			}
			System.out.println( "longest pause = " + toMillis( maxPause) + " msec");

			System.out.println( "");
			System.out.println( "******** RUNTIME INTERVAL ********");
			System.out.println( "smallest: " + formatDecString( Double.toString( smallestAppRt))
					+ " msec - before: " + formatTs( smallestAppRtTs));
			System.out.println( "largest: " + formatDecString( Double.toString( largestAppRt))
					+ " msec - before: " + formatTs( largestAppRtTs));
			System.out
				.println( "average: "
						+ formatDecString( Double.toString( ( ( double) lastTsEffective)
								/ numOfAppRts)) + " msec");
		}
	}

	private static abstract class GcEventProcessor {
		protected static NumberFormat	formatter	= null;

		protected int extractPause( String l) {
			int endIdx = l.indexOf( END_TAG);
			int secIdx = l.lastIndexOf( SEC);
			if ( secIdx > 0)
				endIdx = secIdx - 1;

			int blankIdx = l.lastIndexOf( BLANK, endIdx - 1);

			String numStr = l.substring( blankIdx + 1, endIdx);

			if ( formatter == null) {
				try {
					return ( int) ( Double.parseDouble( numStr) * PAUSE_CONVERT_FACTOR);
				} catch ( NumberFormatException e) {
					formatter = NumberFormat.getInstance();
				}
			}

			try {
				return ( int) ( formatter.parse( numStr).doubleValue() * PAUSE_CONVERT_FACTOR);
			} catch ( ParseException e) {
				throw new NumberFormatException( e.toString());
			}
		}

		protected GeneralAmountManager.MemInfo extractMemInfo( String l) {
			int idx = l.indexOf( "->");
			if ( idx < 0) {
				return null;
			}

			char[] memUnit = new char[1];
			memUnit[0] = l.charAt( idx - 1);
			GeneralAmountManager.MemUnit usedMemUnit = GeneralAmountManager.MemUnit
				.valueOf( new String( memUnit));

			int memValStartIdx = -1;
			for ( int i = 0;; i++) {
				if ( !Character.isDigit( l.charAt( idx - ( 2 + i)))) {
					memValStartIdx = idx - ( 2 + i - 1);
					break;
				}
			}

			int usedMemVal = Integer.parseInt( l.substring( memValStartIdx, idx - 1));

			for ( int i = 0;; i++) {
				if ( l.charAt( idx + 2 + i) == '(') {
					memValStartIdx = idx + 2 + i + 1;
					break;
				}
			}

			for ( int i = 0;; i++) {
				if ( l.charAt( memValStartIdx + 2 + i) == ')') {
					idx = memValStartIdx + 2 + i;
					break;
				}
			}

			memUnit[0] = l.charAt( idx - 1);
			GeneralAmountManager.MemUnit allocatedMemUnit = GeneralAmountManager.MemUnit
				.valueOf( new String( memUnit));
			int allocatedMemVal = Integer.parseInt( l.substring( memValStartIdx, idx - 1));

			return new GeneralAmountManager.MemInfo( usedMemVal, usedMemUnit, allocatedMemVal,
				allocatedMemUnit);
		}

		public int getMaxPause() {
			return 0;
		}

		public abstract void process( long ts, String l);

		public abstract void printResult();
	}

	private static class GcErecordingProcessor extends GcEventProcessor {
		private int		cnt;
		private String	headertxt;
		private String	resulttxt;

		protected int getCnt() {
			return cnt;
		}

		public GcErecordingProcessor( String h, String r) {
			headertxt = h;
			resulttxt = r;
		}

		public void process( long ts, String l) {
			cnt++;
		}

		public void printResult() {
			System.out.println( "******** " + headertxt + " ********");
			System.out.println( "number of " + resulttxt + " = " + cnt);
		}
	}

	private static class GcEwithCcrPauseProcessor extends GcErecordingProcessor {
		private int		minPause	= Integer.MAX_VALUE;
		private long	minPauseTs;
		private int		maxPause;
		private long	maxPauseTs;
		private int		myPause;

		protected int	lastPause;

		public GcEwithCcrPauseProcessor( String h, String r) {
			super( h, r);
		}

		public int getMaxPause() {
			return maxPause;
		}

		public void process( long ts, String l) {
			super.process( ts, l);
			int p = extractPause( l);
			lastPause = p;

			if ( p < minPause) {
				minPause = p;
				minPauseTs = ts;
			}

			if ( p > maxPause) {
				maxPause = p;
				maxPauseTs = ts;
			}
			maxPause = Math.max( maxPause, p);
			myPause += p;
		}

		public void printResult() {
			super.printResult();
			if ( getCnt() > 0) {
				System.out.println( "shortest pause = " + toMillis( minPause) + " msec - at: "
						+ formatTs( minPauseTs));
				System.out.println( "longest pause = " + toMillis( maxPause) + " msec - at: "
						+ formatTs( maxPauseTs));
				System.out.println( "average pause = " + toMillisAverage( myPause, getCnt())
						+ " msec");
				System.out.println( "pause sum = " + toSec( myPause) + " sec");
			}
		}
	}

	private static class GcEwithGlbPauseProcessor extends GcEwithCcrPauseProcessor {
		protected GeneralAmountManager	theMam;

		public GcEwithGlbPauseProcessor( GeneralAmountManager mam, String h, String r) {
			super( h, r);
			theMam = mam;
		}

		public void process( long ts, String l) {
			super.process( ts, l);
			theMam.addPause( lastPause);
		}
	}

	private static class GcEwithMemChangeProcessor extends GcEwithGlbPauseProcessor {

		GcEwithMemChangeProcessor( GeneralAmountManager mam, String h, String r) {
			super( mam, h, r);
		}

		public void process( long ts, String l) {
			super.process( ts, l);
			GeneralAmountManager.MemInfo mi = extractMemInfo( l);
			if ( mi != null)
				theMam.handelMemInfo( ts, mi);
		}
	}

	private static final int	PAUSE_CONVERT_FACTOR	= 10000000;

	private static final char	START_TAG				= '[';
	private static final char	END_TAG					= ']';
	private static final char	TS_TAG					= ':';
	private static final char	BLANK					= ' ';
	private static final String	SEC						= "sec";

	private static enum IdString {

		YOUNG( "GC pause (young)"), MIXED( "GC pause (mixed)"), FULL( "Full GC"),

		MARK_REMARK( "GC remark"), CLEANUP( "GC cleanup"),

		MARK_START( "GC concurrent-mark-start"), MARK_END( "GC concurrent-mark-end"),

		COUNT_END( "GC concurrent-count-end"), CLEANUP_END( "GC concurrent-cleanup-end"),
		MARK_ABORTED( "GC concurrent-mark-abort"),

		// GC activities with no relevance
		COUNT_START( "GC concurrent-count-start"), CLEANUP_START( "GC concurrent-cleanup-start");

		private final String	text;

		IdString( String t) {
			text = t;
		}

		String text() {
			return text;
		}
	};

	private static final int			lastAppRtRelevantPauseIdx	= IdString.FULL.ordinal();
	private static final int			lastAppRtRelevantNonCcrIdx	= IdString.CLEANUP.ordinal();
	private static final int			lastUsedIdx					= IdString.MARK_ABORTED
																		.ordinal();
	private static final int			lastIdx						= IdString.CLEANUP_START
																		.ordinal();

	private final GeneralAmountManager	myMam						= new GeneralAmountManager();
	private final GcEventProcessor[]	procs						= new GcEventProcessor[lastIdx + 1];
	private final BufferedReader		in;
	private final boolean				printAll;

	private static String formatDecString( String s) {
		int idx = s.indexOf( '.');
		if ( idx + 5 < s.length())
			return s.substring( 0, idx + 5);
		else
			return s;
	}

	private static String toMillis( int amount) {
		return formatDecString( Double
			.toString( ( ( ( double) amount) / PAUSE_CONVERT_FACTOR * 1000)));
	}

	private static String toMillisAverage( int amount, int cnt) {
		return formatDecString( Double.toString( ( ( ( ( double) amount) / cnt
				/ PAUSE_CONVERT_FACTOR * 1000))));
	}

	private static String toSec( int amount) {
		return formatDecString( Double.toString( ( ( ( double) amount) / PAUSE_CONVERT_FACTOR)));
	}

	private static String toThroughput( long rt, int pause) {
		// return formatDecString(Double.toString(((double)pause / rt * PAUSE_CONVERT_FACTOR)));
		return formatDecString( Double.toString( 100.0 - ( ( double) pause / rt / 100)));
	}

	private static String formatTs( long l) {
		String s = Long.toString( l);
		int len = s.length();
		if ( len == 3)
			return ( "0." + s);
		else
			return ( s.substring( 0, len - 3) + '.' + s.substring( len - 3, len));
	}

	private static void handleLineError( String s) {
		System.out.println( "WARNING: Cannot unscramble line:");
		System.out.println( s);
		System.out.println( "Information ignored.");
		System.out.println( "");
	}

	private static boolean isOneLine( String s) {
		if ( s.indexOf( TS_TAG) == s.lastIndexOf( TS_TAG))
			return true;
		else
			return false;
	}

	private static ArrayList<Integer> containsWhere( String s, char c) {
		ArrayList<Integer> ret = new ArrayList<Integer>();

		int idx = -1;
		for ( ;;) {
			idx = s.indexOf( c, idx + 1);
			if ( idx < 0)
				return ret;
			else
				ret.add( idx);
		}

	}

	private static int findTsStart( int base, int idx, String s) {
		for ( int j = 0;; j++) {
			int testIdx = idx - ( base + j);
			if ( testIdx < 0) {
				System.out.println( "internal error - cannot handle line: " + s);
				return -1;
			}

			if ( !Character.isDigit( s.charAt( testIdx))) {
				return testIdx + 1;
			}
		}
	}

	private String getLine() {
		try {
			return ( in.readLine());
		} catch ( EOFException eofe) {
			return null;
		} catch ( IOException ioe) {
			System.out.println( "IOException " + ioe.getMessage() + " - program aborted");
			return null;
		}
	}

	private String[] unscramble( BufferedReader in, String s) {
		int cnt = -1;
		ArrayList<Integer> starts = null;
		ArrayList<Integer> ends = null;
		ArrayList<Integer> tss = null;

		for ( ;;) {
			String l = getLine();

			if ( l != null)
				s += l;

			starts = containsWhere( s, START_TAG);
			ends = containsWhere( s, END_TAG);
			tss = containsWhere( s, TS_TAG);

			cnt = tss.size();
			if ( cnt == ends.size())
				break;
		}

		String[] ret = new String[cnt];

		if ( cnt == 2 && starts != null && ends != null) {

			int tmpIdx = starts.get( cnt - 1);
			// use format of time stamp: ii.284: [xxxxxxx
			// to determine start of log output line

			if ( s.charAt( tmpIdx - 1) != BLANK && s.charAt( tmpIdx - 2) != TS_TAG) {
				int tmpIdx2 = tss.get( cnt - 1);
				tmpIdx = findTsStart( 6, tmpIdx2, s);

				boolean hasBlank = ( ( s.charAt( tmpIdx2 + 1) == BLANK) ? true : false);
				tmpIdx2 = ( hasBlank ? tmpIdx2 + 2 : tmpIdx2 + 1);

				String firstBegin = s.substring( 0, tmpIdx);
				String secondTs = s.substring( tmpIdx, tmpIdx2);

				int tmpIdx3 = starts.get( cnt - 1);
				if ( !hasBlank)
					tmpIdx3--;
				String firstEnd = s.substring( tmpIdx2, tmpIdx3);
				String secondEnd = s.substring( tmpIdx3, ends.get( cnt - 1) + 1);

				ret[1] = secondTs + secondEnd;
				ret[0] = firstBegin + firstEnd;

				return ret;
			} else {
				tmpIdx = findTsStart( 8, tmpIdx, s);
				if ( tmpIdx == -1)
					return null;

				int tmpIdx2 = ends.get( 0) + 1;
				String begin = s.substring( 0, tmpIdx);
				ret[1] = s.substring( tmpIdx, tmpIdx2);

				String end = s.substring( tmpIdx2);
				s = begin + end;
				ret[0] = s;

				return ret;
			}
		} else {
			return null;
		}

	}

	private void printResult() {
		myMam.printResult( this);
		for ( IdString id : IdString.values()) {
			if ( id.ordinal() > lastAppRtRelevantNonCcrIdx && !printAll)
				break;

			GcEventProcessor p = procs[id.ordinal()];
			if ( p != null) {
				System.out.println( "");
				p.printResult();
			}
		}
	}

	private long getTimestamp( String s) {
		String ts = "";
		long ret = -1L;
		try {
			for ( int i = 0;; i++) {
				char c = s.charAt( i);

				if ( c == ':')
					break;

				if ( Character.isDigit( c)) {
					if ( c != '0' || i != 0)
						ts += c;
				}
			}
			ret = Long.parseLong( ts);
		} catch ( Exception e) {
			// ignore exception return value == -1
		}
		return ret;
	}

	private void processLine( String s) {
		long ts = getTimestamp( s);

		if ( ts < 0) {
			System.out.println( "WARNING: Cannot extract timestamp from line:");
			System.out.println( s);
			System.out.println( "Information ignored.");
			System.out.println( "");
			return;
		}

		for ( IdString id : IdString.values()) {
			int idx = id.ordinal();

			if ( idx > lastUsedIdx) {
				if ( ts > 0)
					myMam.handleTimestamp( ts, false);
				break;
			}

			if ( s.contains( id.text())) {
				if ( ts > 0)
					myMam.handleTimestamp( ts, ( idx <= lastAppRtRelevantPauseIdx));

				GcEventProcessor p = procs[idx];
				if ( p != null)
					p.process( ts, s);
				break;
			}
		}
	}

	private void doit() {
		for ( ;;) {
			String line = getLine();

			if ( line == null)
				break;

			if ( isOneLine( line)) {
				processLine( line);
			} else {
				String fullLine = null;

				try {
					String[] lines = unscramble( in, line);

					fullLine = "";
					for ( String l : lines)
						fullLine += l;

					if ( lines != null && lines.length > 0) {

						boolean lineError = false;
						for ( int i = 1; i < lines.length; i++) {
							String l = lines[i];

							if ( !Character.isDigit( l.charAt( 0))) {
								lineError = true;
								break;
							}

							int idx = -1;

							for ( int j = 1;; j++) {
								char c = l.charAt( j);

								if ( Character.isDigit( c))
									continue;

								if ( c == '.') {
									idx = j;
									break;
								}

								break;
							}

							if ( idx == -1) {
								lineError = true;
								break;
							}

							if ( !( Character.isDigit( l.charAt( idx + 1))
									&& Character.isDigit( l.charAt( idx + 2))
									&& Character.isDigit( l.charAt( idx + 3)) && l.charAt( idx + 4) == ':')) {
								lineError = true;
								break;
							}
						}

						if ( lineError == true) {
							handleLineError( fullLine != null ? fullLine : line);
							continue;
						}

						for ( String l : lines)
							processLine( l);
					} else {
						System.out.println( "internal error - cannot handle line: " + line);
						break;
					}
				} catch ( Exception e) {
					handleLineError( fullLine != null ? fullLine : line);
				}
			}
		}

		printResult();
	}

	public TestG1Analyzer( BufferedReader br, boolean pa) {
		in = br;
		printAll = pa;

		procs[IdString.YOUNG.ordinal()] = new GcEwithMemChangeProcessor( myMam,
			"FULLY YOUNG COLLECTIONS", "collections");

		procs[IdString.MIXED.ordinal()] = new GcEwithMemChangeProcessor( myMam,
			"MIXED YOUNG COLLECTIONS", "collections");

		procs[IdString.FULL.ordinal()] = new GcEwithMemChangeProcessor( myMam, "FULL COLLECTIONS",
			"collections");

		procs[IdString.MARK_REMARK.ordinal()] = new GcEwithGlbPauseProcessor( myMam, "REMARKS",
			"remarks");

		procs[IdString.CLEANUP.ordinal()] = new GcEwithMemChangeProcessor( myMam, "CLEANUPS",
			"cleanups");

		procs[IdString.MARK_END.ordinal()] = new GcEwithCcrPauseProcessor( "MARK ENDS", "mark ends");

		procs[IdString.COUNT_END.ordinal()] = new GcEwithCcrPauseProcessor( "COUNT ENDS",
			"count ends");
		procs[IdString.CLEANUP_END.ordinal()] = new GcEwithCcrPauseProcessor( "CLEANUP ENDS",
			"cleanup ends");

		procs[IdString.MARK_START.ordinal()] = new GcErecordingProcessor( "MARK STARTS",
			"mark starts");
		procs[IdString.MARK_ABORTED.ordinal()] = new GcErecordingProcessor( "MARK ABORTS",
			"mark aborts");

		// GC activities with no relevance
		procs[IdString.COUNT_START.ordinal()] = null;
		procs[IdString.CLEANUP_START.ordinal()] = null;

		doit();
	}

	public static void main( String[] argv) {
		System.out.println( "G1 Analyzer " + VERSION + " \n");

		int numOfArgs = argv.length;
		String filename = null;
		boolean printAll = false;

		if ( numOfArgs < 1) {
			System.out.println( "argument(s) missing: [-a] gcTraceFileName");
			System.out.println( "                     [-a] = print all information");
			return;
		} else if ( numOfArgs == 1) {
			filename = argv[0];
		} else if ( numOfArgs == 2) {
			if ( argv[0].equals( "-a")) {
				printAll = true;
				filename = argv[1];
			} else {
				System.out.println( "unrecognized invocation paramter: " + argv[0]);
				return;
			}
		} else {
			System.out.println( "too many invocation paramters");
			return;
		}

		try {
			new TestG1Analyzer( new BufferedReader( new FileReader( filename)), printAll);
		} catch ( FileNotFoundException fnfe) {
			System.out.println( "input file " + argv[0] + " does not exist");
		}
	}
}
