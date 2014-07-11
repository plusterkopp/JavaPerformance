/*
 * Created on 19.02.2007
 *
 */
package _10_01_FinalGC.cache;

public interface Computable<A, R> {
	R compute( A arg) throws InterruptedException;
}
