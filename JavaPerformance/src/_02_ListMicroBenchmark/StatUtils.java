package _02_ListMicroBenchmark;

import java.util.*;

import org.apache.commons.math.*;
import org.apache.commons.math.stat.descriptive.*;


 final class StatUtils {

    public static String getPrintableStatisticsForTestCase(Sample[] samples) {
        StringBuffer buf = new StringBuffer("\n~~~ STATISTICS - SAMPLES ~~~\n");
        for (int i=0;i<samples.length;i++) {
            buf.append(samples[i]);
            buf.append("\n");
        }
        buf.append("\n");
        return buf.toString();
    }

    public static String getPrintableSignificanceTestResult(Sample[] samples, double alpha) {
        StringBuffer buf = new StringBuffer();
        buf.append("\n~~~ STATISTICS - T-TEST (confidence level: "+((1-alpha)*100)+"%) ~~~\n\n");
        buf.append("The following samples do NOT differ significantly from each other:\n");
        Set[] equivalenceClasses = new Set[samples.length];

        for (int i=0;i<equivalenceClasses.length;i++) {
            equivalenceClasses[i] = new TreeSet();
        }
        for (int i=0;i<samples.length;i++) {

            for (int j=i+1;j<samples.length;j++) {
                boolean different = false;
                try {
                    different = samples[i].differsFrom(samples[j],alpha);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (MathException e) {
                    e.printStackTrace();
                }
                finally {
                    if(!different) {
                        equivalenceClasses[i].add(samples[i]);
                        equivalenceClasses[i].add(samples[j]);
                        buf.append("["+samples[i].getId()+", "+samples[j].getId()+"]\n");
                    }
                }

            }
        }
        buf.append("\n--------------------------------------------\n\n");
        buf.append(evaluateEquivalenceClasses(equivalenceClasses));
        buf.append("\n");
        buf.append("\n--------------------------------------------\n\n");
        return buf.toString();
    }
    private static boolean intersect(Set s1, Set s2) {
        Iterator iter = s2.iterator();
        while (iter.hasNext()) {
            if (s1.contains(iter.next()))
               return true;
        }
        return false;
    }
    private static String evaluateEquivalenceClasses(Set[] equivalenceClasses) {
        StringBuffer buf = new StringBuffer();
        boolean modified = false;
        do {
            /*
            for (int i=0;i<equivalenceClasses.length-1;i++) {
                System.out.println(equivalenceClasses[i]);
            }
            */
            modified = false;
            for (int i=0;i<equivalenceClasses.length;i++) {
                for (int j=i+1;j<equivalenceClasses.length;j++) {
                    if (intersect(equivalenceClasses[i], equivalenceClasses[j])) {
                        modified = true;
                        equivalenceClasses[i].addAll(equivalenceClasses[j]);
                        equivalenceClasses[j].clear();
                    }
                }
            }
            // System.out.println("같�");
        } while (modified);

        for (int i=0;i<equivalenceClasses.length;i++) {
            if (!equivalenceClasses[i].isEmpty()) {
                buf.append("\nNo significant difference among: \n");
                Iterator iter = equivalenceClasses[i].iterator();
                int cnt=0;
                buf.append("[");
                while (iter.hasNext()) {
                    cnt++;
                    if (cnt<equivalenceClasses[i].size())
                        buf.append(((Sample)iter.next()).getId()+", ");
                    else
                        buf.append(((Sample)iter.next()).getId()+"]\n\n");
                }
                buf.append(getPrintableStatisticsForEquivalenceClass(equivalenceClasses[i]));
                buf.append("\n\n");
                iter = equivalenceClasses[i].iterator();
                while (iter.hasNext()) {
                    buf.append(iter.next());
                    buf.append("\n");
                }
                buf.append("같같같같같같같같같같같같같같�\n");
            }
        }
        return buf.toString();
    }
    public static String getPrintableStatisticsForEquivalenceClass(Set equivClass) {
        SummaryStatistics stats = new SummaryStatistics();
        System.out.println();
        Iterator iter = equivClass.iterator();
        while (iter.hasNext()) {
            Sample sample = (Sample) iter.next();
            for (int j=0;j<sample.getSamples().length;j++) {
                stats.addValue(sample.getSamples()[j]);
            }
        }
        double mean = stats.getMean();
        double stddeviation = stats.getStandardDeviation();
        double min = stats.getMin();
        double max = stats.getMax();
        long   n = stats.getN();

        StringBuffer buf = new StringBuffer("OVERALL STATISTICS FOR EQUIVALENCE CLASS\n");
        buf.append("[mean: "+mean+" / stddev: "+stddeviation
                 +" / min: "+min+" / max: "+max+"]\n");
        buf.append(n+" values: ");
        iter = equivClass.iterator();
        while (iter.hasNext()) {
            Sample sample = (Sample) iter.next();
            for (int j=0;j<sample.getSamples().length;j++) {
                buf.append(sample.getSamples()[j]);
                buf.append(" ");
            }
        }
        return buf.toString();
    }
}