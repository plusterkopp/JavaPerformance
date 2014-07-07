/*
 * Created on 13.08.2007
 *
 */
package de.icubic.mm.bench.base;

/**
 * <p>
 * 
 * @author ralf
 * 
 */
public interface IBenchRunnable extends Runnable {

	/**
	 * loopCount wird benutzt, um auf eine gewünschte Laufzeit zu kommen
	 * 
	 * @param loopCount
	 */
	public void run( long nruns);

	/**
	 * @param loopCount
	 * @return Anzahl der inneren Durchläufe in einem Aufruf von run()
	 */
	public long getRunSize();

	/**
	 * für die Ausgabe, um mehrere IBenchRunnables gegeneinander vergleichen zu können
	 * 
	 * @return name
	 */
	public String getName();

	public void setName( String string);

	/**
	 * prepare for nruns runs in next timing run. There will be no more runs than returned by this
	 * method, which may be less than requested. Uses to prepare a number of data structures to be
	 * consumed by runs.
	 * 
	 * @param nruns
	 * @return
	 */
	public long setup( long nruns);

	public void reset();

	public long getTotalRunSize( long nruns);

	public String getCSVHeader();

	public String getCSVLine();
}
