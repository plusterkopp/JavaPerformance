package _05_FunctionalHotSpots.deadlink;

import java.util.*;

public final class LabelChecker {
    private static final class LabelCache {
        // associates an absolute pathname (as string) with a hashset of lables
        private TreeMap labelsPerFile = new TreeMap();

        public boolean contains(String absolutePathName) {
            /*
            Set labels = (Set)labelsPerFile.get(absolutePathName);
            if (labels != null) {
                System.out.println("labels have already been collected");
                System.out.println(labels);
            }
            */            
            return (null != labelsPerFile.get(absolutePathName));
        }        
        public boolean contains(String absolutePathName, String label) {
            Set labels = (HashSet)labelsPerFile.get(absolutePathName);
            if (labels == null) {
                return false;
            }
            else
                return labels.contains(label);
        }
        public void add(String absolutePathName, String label) {
            Set labels = (Set)labelsPerFile.get(absolutePathName);
            if (labels == null) {
                labelsPerFile.put(absolutePathName,new HashSet());
                add(absolutePathName,label);
            }
            else {
                labels.add(label);
            }            
        }        
    }
    private static LabelCache labels = new LabelCache();
    
    public static boolean isLocalLabel(String linknam) {
        Sieve.calculate(25000);  
        // link is a label in this HTML file
        return linknam.charAt(0) == '#';
    }
    public static boolean isAvailableLocalLabel(String filnam, String linknam) {
        String label = linknam.substring(1);
        
        //  System.out.println("check for label <"+label+"> in file <"+filnam+">");  
        //  if (labelExists(filnam,label))  System.out.println("label found");      
        return labelExists(filnam,label);         
    }

	public static boolean labelExists(String absolutePathName, String label) { 
        
        // System.out.println("check for label <"+label+"> in file <"+absolutePathName+">");
        
        if (!labels.contains(absolutePathName)) 
            LabelCollector.collect(absolutePathName);
        
        // if (labels.contains(absolutePathName,label))  System.out.println("label found");
        
        return labels.contains(absolutePathName,label);
	}
    public static void addLabel(String absolutePathName, String label) {
    //    System.out.println("add label <"+label+"> in file <"+absolutePathName+">");
        labels.add(absolutePathName,label);
    }
}
