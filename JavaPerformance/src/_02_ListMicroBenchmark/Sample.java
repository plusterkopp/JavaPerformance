package _02_ListMicroBenchmark;

import org.apache.commons.math.*;
import org.apache.commons.math.stat.descriptive.*;
import org.apache.commons.math.stat.inference.*;


public final class Sample implements Comparable {
    private final int id;
    private final String description;
    private final double[] samples;
    private int idx = 0;

    public Sample(int identification, String desc, int sampleSize) {
        id = identification;
        description = desc;
        samples = new double[sampleSize];
    }
    public String getDescription() {
        return description.toString();
    }
    public int getId() {
        return id;
    }
    public double[] getSamples() {
        double[] result = new double[samples.length];
        System.arraycopy(samples,0,result,0,samples.length);
        return result;
    }
    public void addMeasurement(double data) {
        if (idx == samples.length)
            throw new IllegalArgumentException("samples array is full; capacity: "+samples.length);
        samples[idx++] = data;
    }
    public String toString() {
        SummaryStatistics stats = new SummaryStatistics();
        for( int i = 0; i < samples.length; i++) {
            stats.addValue(samples[i]);
        }
        double mean = stats.getMean();
        double stddeviation = stats.getStandardDeviation();
        double min = stats.getMin();
        double max = stats.getMax();
        long   n = stats.getN();

        StringBuffer buf = new StringBuffer("(#"+id+") "+description + "\n");
        buf.append("[mean: "+mean+" / stddev: "+stddeviation
                 +" / min: "+min+" / max: "+max+"]\n");
        buf.append(n+" values: ");
        for (int i=0;i<samples.length;i++) {
            buf.append(samples[i]);
            buf.append(" ");
        }
        return buf.toString();
    }
    public int compareTo(Object other) {
        if (id < ((Sample)other).getId()) return -1;
        if (id == ((Sample)other).getId()) return 0;
        if (id > ((Sample)other).getId()) return 1;
        return 0;
    }
    public boolean differsFrom(Sample other,double alpha)
        throws IllegalArgumentException, MathException {
        return (new TTestImpl()).tTest(samples,other.samples,alpha);
    }
}