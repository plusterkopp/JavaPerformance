/*
 * Created on 25.11.2003
 *
 */
package _05_FunctionalHotSpots.deadlink;

import java.util.HashMap;


final class Statistics {
	    private int totalNumberOfLinks = 0;
	    private int numberOfDeadLinks = 0;
	    private int numberOfRelativeLinks = 0;
	    private int numberOfLocalLabels = 0;
	    private int numberOfHttpLinks = 0;
        private int numberOfMailtoLinks = 0;
        private HashMap deadLinks = null;
        
        public Statistics(HashMap deadLinks) {
            this.deadLinks = deadLinks;
        }
        public void incrementTotalNumberOfLinks() {
            totalNumberOfLinks++;
        }
        public void incrementNumberOfDeadLinks() {
            numberOfDeadLinks++;
        }
        public void incrementNumberOfRelativeLinks() {
            numberOfRelativeLinks++;
        }
        public void incrementNumberOfLocalLabels() {
            numberOfLocalLabels++;
        }
        public void incrementNumberOfHttpLinks() {
            numberOfHttpLinks++;
        }
        public void incrementNumberOfMailtoLinks() {
            numberOfMailtoLinks++;
        }     
	    public String toString() {
	    	StringBuffer buf = new StringBuffer("-- statistics for dead link detection --");
	    	buf.append('\n');
        	buf.append("total number of links    : "+totalNumberOfLinks);
        	buf.append('\n');
        	buf.append("number of local labels   : "+numberOfLocalLabels);
        	buf.append('\n');
        	buf.append("number of http links     : "+numberOfHttpLinks);
        	buf.append('\n');
            buf.append("number of mailto links   : "+numberOfMailtoLinks);
            buf.append('\n');
        	buf.append("number of relative links : "+numberOfRelativeLinks);
        	buf.append('\n');
        	buf.append("number of dead links     : "+deadLinks.size());
        	buf.append('\n');
//      	buf.append("number of dead links     : "+numberOfDeadLinks);
		    buf.append('\n');
		    if (numberOfDeadLinks != deadLinks.size()) 
	    		buf.append(">>> dead link appears multiply:  counter="+numberOfDeadLinks+"  listsize="+deadLinks.size());
		    return buf.toString();
       }
}