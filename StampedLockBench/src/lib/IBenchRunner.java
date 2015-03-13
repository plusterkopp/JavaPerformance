package lib;
/*
 * Created on 13.08.2007
 *
 */


import java.util.concurrent.*;

/**
 * <p>@author ralf
 *
 */
public interface IBenchRunner extends Runnable {

	/**
	 * setzt die gew�nschte Laufzeit des Tests. Dazu kommt noch die Laufzeit f�r
	 * die Kalibrierungsl�ufe.
	 *
	 * @param tu
	 * @param units
	 */
	void setRuntime( TimeUnit tu, long units);

	/**
	 * setzt den zu testenden Benchmark
	 * @param runner
	 */
	void setBenchRunner( IBenchRunnable runner);

	/**
	 * erst sinnvoll, nachdem run() aufgerufen wurde
	 * @return Anzahl der Durchl�ufe innerhalb der gew�nschten Laufzeit
	 */
	long getRuns();

	/**
	 * @return Durchl�ufe je Sekunde
	 */
	double getRunsPerSecond();

	/**
	 * @return tats�chliche Laufzeit in Sekunden
	 */
	double getRunSeconds();

	/**
	 * formatierte Ausgabe
	 */
	void printResults();

	/**
	 * wieviele leere Schleifen schaffen wir pro Sekunde (Vorgabe: unendlich viele)
	 * @param eRuns
	 */
	void setEmptyLoops( double eRuns);

	/**
	 * @return Name des letzten IBenchRunnables
	 */
	String getName();

}
