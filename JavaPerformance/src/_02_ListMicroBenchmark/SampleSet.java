package _02_ListMicroBenchmark;

import java.util.*;

public class SampleSet {
    private final String description;
    private final int sampleSize;
    private int idGen = 0;
    private ArrayList<Sample> samples = new ArrayList<>();
    public SampleSet(String desc) {
        description = desc;
        sampleSize  = -1; // i.e., the size irrelevant
    }
    public SampleSet(String desc, int size) {
        description = desc;
        sampleSize  = size;
    }
    public void register(Sample sample) {
    	samples.add(sample);
    }
    public Sample newSample(String desc) {
    	if (sampleSize<=0) throw new UnsupportedOperationException();
        return new Sample(idGen++,description+" - "+desc,sampleSize);
    }
    public Sample[] getSamples() {
    	return samples.toArray(new Sample[0]);
    }

    public String getPrintableStatisticsForTestCase() {
        return StatUtils.getPrintableStatisticsForTestCase(samples.toArray(new Sample[0]));
    }
    public String getPrintableSignificanceTestResult(double alpha) {
    	return StatUtils.getPrintableSignificanceTestResult(samples.toArray(new Sample[0]),alpha);
    }
}
