/**
 * A dead link detector.
 *
 * <p>Reads in all HTML files in a given directory, checks all HREF entries
 * and reports any dead links.
 * Currently only relative links with the directory are checked.
 * Local labels and external links are considered for future extension.
 *
 * usage: java [-D<option>=value] util.DeadLinkDetector
 * available options:   dir=<site description file>
 * sample usage:        java -Ddir=D:/HomePageNew/Generated util.DeadLinkDetector
 *
 * @author Angelika Langer
 */
package _05_FunctionalHotSpots.deadlink;

import java.io.*;
import java.util.*;

import _05_FunctionalHotSpots.util.file.*;
import _05_FunctionalHotSpots.util.html.*;


public final class DeadLinkDetector {
    private PageDescription pageDescr = null;
    private File htmlFile = null;

    private static HashMap deadLinks = new HashMap();
    private static Statistics stats = new Statistics(deadLinks);

    public static Statistics getStatistics() {
       return stats;
    }

    public final class ProcessorSupport {
        private ProcessorSupport() {} // no public constructors

        public HashMap getDeadLinks() {
            return deadLinks;
        }
        public File GetHtmlFile() {
            return htmlFile;
        }
        public PageDescription getPageDescription() {
            return pageDescr;
        }
    }
    public void giveSupportTo(SupportedSingleTagTokenAttributeProcessor other) {
        other.receiveSupport(new ProcessorSupport());
    }


    public DeadLinkDetector(File htmlFile) {
        this.pageDescr = null;
        this.htmlFile  = htmlFile;
    }
    public DeadLinkDetector(String pageName) {
        this.pageDescr = new PageDescription(pageName);
        this.htmlFile = null;
    }

    public SingleTagTokenAttributeProcessor getDeadLinkProcessor() {
        return new DeadLinkProcessor(this, htmlFile);
    }

    public static void printDeadLinks(PrintStream out) {
    	Set linkNames = deadLinks.keySet();
    	Iterator iter1 = linkNames.iterator();

    	while (iter1.hasNext()) {
    	    String linkName = (String) iter1.next();
            out.println("LINK = "+linkName);
            HashSet filesContainingLink = (HashSet)deadLinks.get(linkName);
            out.println("CONTAINED IN: ");
            Iterator iter2 = filesContainingLink.iterator();
            while (iter2.hasNext()) {
            	out.println(iter2.next());
            }
            out.println();
        }
    }

    private static final String DIR_OPTION = "dir";
    private static final String DIR_DEFAULT = "D:/HomePageNew/Generated";

    private static String retrieveDirectoryName(String[] args) {

	    if (args == null || (args.length>0 && args[0].equals("?"))) {
	    	System.out.println("usage: java [-D<option>=value] util.DeadLinkDetector");
	        System.out.println("   "+DIR_OPTION+"=<absolute directory path>");
	        System.out.println("   e.g. desc=D:/HomePageNew/Generated");
		System.exit(-1);
	    }

	    String dirname = System.getProperty(DIR_OPTION);
	    if (dirname == null) {
	    	dirname = DIR_DEFAULT;
	    }
	    return dirname;
    }
    public static void main(String[] args) {
        // retrieve the directory to be searched and a dummy name of a temporary directory
    	String theDirectory = retrieveDirectoryName(args);

    	System.out.println("DEAD LINK DETECTION started ...\n");
        System.out.println("directory to be examined: \n"+theDirectory);
        System.out.println("\nThis search for dead links will take a while.  Please wait ...\n");

    	// find all files from the directory to be examined
        FileFinder finder = new FileFinder(FileFinder.HTML_FILES);
        Collection theFiles = finder.getAllFilesInDirectory(theDirectory);

        DeadLinkDetector detector = null;

    	for (Iterator iter = theFiles.iterator(); iter.hasNext(); ) {
    		File inputFile = (File)iter.next();
    		String inputFileName = FileUtility.changeRealPathToSymbolicPath(inputFile.getPath());

    	    detector = new DeadLinkDetector(inputFile);

    	    TagTokenVisitor tagProcessor = new TagTokenVisitor();
    	    tagProcessor.register(new AttributeProcessorImplementation(detector.getDeadLinkProcessor()));
            new HmtlFileProcessor(tagProcessor).processHtmlFile(inputFileName,null);
        }

        System.out.println("\n---  DEAD LINK DETECTION ---\n");
        System.out.println("directory to be examined: "+theDirectory);
        System.out.println("\n"+stats+"\n");
	    DeadLinkDetector.printDeadLinks(System.out);
    }
}
