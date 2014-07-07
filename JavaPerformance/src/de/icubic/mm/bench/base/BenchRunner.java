/*
 * Created on 13.08.2007
 *
 */
package de.icubic.mm.bench.base;

import static de.icubic.mm.bench.base.BenchLogger.*;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * <p>
 * 
 * @author ralf
 * 
 */
public class BenchRunner implements IBenchRunner {

	static {
		lnf.setMaximumFractionDigits( 0);
		lnf.setGroupingUsed( true);
	};

	static class ResultEntry {
		public ResultEntry( String runName, double rps) {
			name = runName;
			runsPerSecond = rps;
		}

		String	name;
		double	runsPerSecond;
	}

	private static NavigableSet<ResultEntry>	chart				= new TreeSet<ResultEntry>(
																		new Comparator<ResultEntry>() {
																			@Override
																			public int compare(
																					ResultEntry r1,
																					ResultEntry r2) {
																				Double rps1 = r1.runsPerSecond;
																				Double rps2 = r2.runsPerSecond;
																				return rps1
																					.compareTo( rps2);
																			}
																		});

	protected IBenchRunnable					benchRunnable;
	/**
	 * wird durch setRuntime festgelegt
	 */
	protected long								runNanosDesired		= 1000000000;			// 1s
	/**
	 * Produkt aus der Anzahl der Aufrufe von run im Zuge von run( loopCount), wobei loopCount bei
	 * der Kalibrierung festgelegt wird, und der Anzahl der Durchl�ufe in jedem einzelnen Lauf
	 */
	protected long								loopCount;
	protected long								runNanosActual;
	protected double							emptyLoopsPerSecond	= 0;

	private FileWriter							csvWriter			= null;

	private static boolean						firstOffset			= true;
	private static long							lastNanoOrigin;

	private static long							startNanos			= System.nanoTime();

	static public void addToComparisonList( String runName, double runsPerSecond) {
		synchronized ( chart) {
			chart.add( new ResultEntry( runName, runsPerSecond));
		}
	}

	static public void clearComparisonList() {
		chart.clear();
	}

	static public void printComparisonList() {
		if ( chart == null || chart.isEmpty()) {
			return;
		}

		double worst = chart.first().runsPerSecond;
		double best = chart.last().runsPerSecond;
		NumberFormat nf = new DecimalFormat();
		nf.setMaximumFractionDigits( 0);
		for ( ResultEntry re : chart.descendingSet()) {
			StringBuilder sb = new StringBuilder();
			double relPerf = re.runsPerSecond / worst;
			double relTime = best / re.runsPerSecond;
			sb.append( re.name + ": " + nf.format( 100 * relPerf) + "% Performance / "
					+ nf.format( 100 * relTime) + "% Time");
			System.out.println( sb.toString());
		}
	}

	public BenchRunner( IBenchRunnable bench) {
		setBenchRunner( bench);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.pkmd.utils.bench.IBenchRunner#setRuntime(java.util.concurrent.TimeUnit, long)
	 */
	@Override
	public void setRuntime( TimeUnit tu, long units) {
		runNanosDesired = tu.toNanos( units);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.pkmd.utils.bench.IBenchRunner#setBenchRunner(de.pkmd.utils.bench.IBenchRunnable)
	 */
	@Override
	public void setBenchRunner( IBenchRunnable runnable) {
		benchRunnable = runnable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		double emptyLoopNanos = 0;
		// Kalibriere
		long nruns = 1;
		runNanosActual = run( nruns);
		// mindestens eine Sekunde, f�r Hotspot Warmup
		while ( runNanosActual < 1e9) {
			long nrunsNew = nruns * 2;
			// aufr�umen
			nrunsNew = benchRunnable.setup( nrunsNew);
			if ( nrunsNew <= nruns) {
				break;
			}
			// starten
			nruns = nrunsNew;
			runNanosActual = run( nruns);
		}
		// jetzt solange laufen lassen, da� gew�nschte Zeit zusammenkommt
		final double f = ( double) runNanosDesired / runNanosActual;
		nruns *= f;
		if ( nruns < 1) {
			nruns = 1;
		}
		// aufr�umen
		benchRunnable.setup( nruns);
		// der Run
		runNanosActual = run( nruns);
		loopCount = benchRunnable.getTotalRunSize( nruns);
		// dann leere Schleife abziehen, wenn bekannt -- und kleiner (JRockit)
		if ( emptyLoopsPerSecond > 0) {
			emptyLoopNanos = Math.round( loopCount * 1e9 / emptyLoopsPerSecond);
		}
		if ( emptyLoopNanos < runNanosActual)
			runNanosActual -= emptyLoopNanos;
	}

	public void writeCSV( String csvLine) {
		if ( csvWriter != null) {
			if ( csvLine != null) {
				try {
					csvWriter.write( csvLine);
					csvWriter.write( '\n');
					csvWriter.flush();
				} catch ( IOException e) {
					System.err.println( "Can not write " + csvLine + "to " + csvWriter);
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public double getRunsPerSecond() {
		return ( 1e9 * loopCount) / runNanosActual;
	}

	protected long run( long nruns) {
		int runs = 0;
		final int maxruns = 3;
		while ( ++runs < maxruns) {
			long start = System.nanoTime();
			benchRunnable.run( nruns);
			long end = System.nanoTime();
			long nanos = end - start;
			// System.out.println( Thread.currentThread() + " run " + benchRunnable.getName() + " "
			// + nruns + " times in " + lnf.format( nanos) + " ns");
			if ( nanos >= 0) {
				return nanos;
			}
			System.out.println( "Nanotime Rollover: " + runs + " of " + maxruns);
		}
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.pkmd.utils.bench.base.IBenchRunner#getRuns()
	 */
	@Override
	public long getRuns() {
		return loopCount;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.pkmd.utils.bench.base.IBenchRunner#getRunSeconds()
	 */
	@Override
	public double getRunSeconds() {
		return runNanosActual / 1e9;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.pkmd.utils.bench.base.IBenchRunner#printResults()
	 */
	@Override
	public void printResults() {
		NumberFormat nf = DecimalFormat.getNumberInstance();
		nf.setMaximumFractionDigits( 3);

		String resultString = "" + lnf.format( getRuns()) + " Runs in "
				+ nf.format( getRunSeconds()) + "s = ";
		double rps = getRunsPerSecond();
		double rrps = 1 / rps;
		String unit = "s";
		String runit = "s";
		if ( rps > 1000000) {
			unit = "�" + unit;
			rps /= 1000000;
			runit = "n" + runit;
			rrps *= 1000000000;
		} else if ( rps > 1000) {
			unit = "m" + unit;
			rps /= 1000;
			runit = "�" + runit;
			rrps *= 1000000;
		} else if ( rps > 1) {
			unit = "" + unit;
			// rps /= 1000;
			runit = "m" + runit;
			rrps *= 1000;
		}
		System.out.println( resultString + nf.format( rps) + "/" + unit + " = " + nf.format( rrps)
				+ runit + "/Run (" + benchRunnable.getName() + ")");
	}

	@Override
	public void setEmptyLoops( double runs) {
		emptyLoopsPerSecond = runs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.icubic.mm.bench.base.IBenchRunner#getName()
	 */
	@Override
	public String getName() {
		if ( benchRunnable != null)
			return benchRunnable.getName();
		return "No Name";
	}

	public void setCSVName( String csvName, String csvHeader) {
		closeCSV(); // den alten zumachen
		try {
			File csvFile = new File( csvName);
			boolean isNewFile = csvFile.createNewFile();
			csvWriter = new FileWriter( csvFile);
		} catch ( IOException ioe) {
			if ( csvWriter != null) {
				try {
					csvWriter.close();
					System.err.println( "Error opening " + csvName);
					ioe.printStackTrace();
				} catch ( IOException ioe2) {
					csvWriter = null;
					System.err.println( "Error closing " + csvName);
					ioe2.printStackTrace();
				}
			}
		}
		writeCSV( csvHeader);
	}

	private void closeCSV() {
		if ( csvWriter != null) {
			try {
				csvWriter.close();
			} catch ( IOException e) {
				System.err.println( "Error closing " + csvWriter);
				e.printStackTrace();
			}
		}
	}

	private static void checkOffset( long nanoTime) {
		long millis = System.currentTimeMillis();
		long nanoMillis = ( long) ( nanoTime / 1e6);
		long nanoOrigin = millis - nanoMillis;
		if ( firstOffset) {
			firstOffset = false;
			lastNanoOrigin = nanoOrigin;
		} else {
			if ( Math.abs( lastNanoOrigin - nanoOrigin) > 500) {
				BenchLogger.syserr(
					nanoTime,
					Thread.currentThread().getName() + " NanoOrigin jump by "
							+ BenchLogger.lnf.format( nanoOrigin - lastNanoOrigin) + " ms");
			}
		}
	}

	public static long getNow() {
		long now = System.nanoTime();
		checkOffset( now);
		return now - startNanos;
	}

}
