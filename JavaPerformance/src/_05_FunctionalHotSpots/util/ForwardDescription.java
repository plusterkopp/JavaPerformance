package _05_FunctionalHotSpots.util;

import java.io.*;
import java.util.*;

/**
 * A wrapper around the forwarding properties file.
 *
 * <p>Retrieves the location of the forwarding description file from the call arguments, or uses the
 *    default location, load the properties, and provides services based on the site description.
 *
 * <p>The forwarding description provides the association of a page name (such as "AboutMe")
 *    with two relative path names: the previous file name and the current file name.
 *    The path names are in Windows notation, i.e. using "/" as a separator.
 *
 * @author Angelika Langer
 */
public final class ForwardDescription {

    private Properties description   = null;
    private Map historicFilesToPages = null;

    private static final String DESC_OPTION = "forw";
    private static final String DESC_FILE_DEFAULT = "D:/HomePageNew/HomepageGenerator/sources/forward.txt";
    private static String forwardingDescriptionFilename = null;
    private static ForwardDescription singleton = null;

   /**
    * Retrieves the filename of the site description file from an array of arguments.
    *
    * @param array of arguments
    *        The option -Dforw (see @link DESC_OPTION) specifies the forwarding description file name.
    * @ returns the the filename of the forwarding description file
    */
	private String retrieveforwardingDescriptionFilename(String[] args) {
	    if (args == null)  return DESC_FILE_DEFAULT;

            String filename = null;

            if (args.length>0 && args[0].equals("?")) {
            	System.out.println("usage: java [-D<option>=value] util.ForwardPageGenerator");
                System.out.println("   "+DESC_OPTION+"=<site description file>");
                System.out.println("   e.g. "+DESC_OPTION+"="+DESC_FILE_DEFAULT);
		System.exit(-1);
            }

            filename = System.getProperty(DESC_OPTION);
            if (filename == null) {
            	filename = DESC_FILE_DEFAULT;
            }
            forwardingDescriptionFilename = filename;
            return filename;
	}
   /**
    * Finds and opens in the forwarding description properties file and loads the properties.
    *
    * @param an array of arguments from which the filename of the forwarding description file is taken
    *        If no filename argument is provided, the file is expected in its default location
    *        (see @link #DESC_FILE_DEFAULT).
    */
    private ForwardDescription(String[] args) {
	String descrFileName = retrieveforwardingDescriptionFilename(args);
//	System.out.println("forwarding description file: "+descrFileName);

	description = new Properties();
        FileInputStream propFile = null;
        try {
        	propFile = new FileInputStream(descrFileName);
        	description.load(propFile);
        }
        catch (Exception e) {System.err.println(e);}
//      description.list(System.out);
        historicFilesToPages = mapHistoricFilesToPages();
//        System.out.println(historicFilesToPages);
    }
   /**
    * Factory method for forwarding description singleton.
    *
    * @param args an array of arguments from which the filename of the forwarding description file is taken
    * @return  the singleton forwarding description
    */
    public static ForwardDescription makeForwardDescription(String[] args) {
        if (singleton == null) {
            singleton = new ForwardDescription(args);
        }
        return singleton;
    }
    public static ForwardDescription makeForwardDescription() {
        if (singleton == null) {
            singleton = new ForwardDescription(null);
        }
        return singleton;
    }
   /**
    * Getter method for forwarding description singleton.
    * This method should only be called after creation of the forwarding description singleton via the
    * makeForwardDescription factory method.  It returns null otherwise.
    *
    * @param args an array of arguments from which the filename of the forwarding description file is taken
    * @return  the singleton site description; returns null if the forwarding descritpion has not yet been created
    */
    public static ForwardDescription getForwardDescription() {
        if (singleton == null) {
            System.err.println(">>> error: forwarding description has not been initialized");
        }
        return singleton;
    }
   /**
    * Provides the absolute path name of the forwarding description file in Windows notation.
    * e.g. D:/HomePageNew/HomepageGenerator/sources/forward.txt
    *
    * @return absolute path name of the forwarding description file in Windows notation
    */
    public String getforwardingDescriptionFilename() {
		return forwardingDescriptionFilename;
    }
   /**
    * The following methods provide the absolute path name in Windows notation
    * of various directories found in the forwarding description file.
    * e.g. D:/HomePage or D:/HomePageNew/Generated
    *
    * @return absolute path name of the respective directory in Windows notation
    */
	public String getHistoricDirectory() {
	        return description.getProperty("HistoricDirectory");
	}
	public String getCurrentDirectory() {
	        return description.getProperty("CurrentDirectory");
	}
	public String getTargetDirectory() {
	        return description.getProperty("TargetDirectory");
	}
	public String getTempDirectory() {
	        return description.getProperty("TempDirectory");
	}
	public String getJspDirectory() {
	        return description.getProperty("JspDirectory");
	}
   /**
    * The following methods provide the relative path name in Windows notation
    * of the JSP files that make up the sources for generation of the forwarding pages
    * as found in the forwarding description file.
    * e.g. XCommon/Header.jsp or XCommon/Forward.jsp
    *
    * @return relative path name of the respective JSP file in Windows notation
    */
	public String getSourceJsp() {
	        return description.getProperty("Forward.Source");
	}
	public String getHeaderJsp() {
	        return description.getProperty("Forward.Header");
	}
	public String getFooterJsp() {
	        return description.getProperty("Forward.Footer");
	}
	public String getSearchJsp() {
	        return description.getProperty("Forward.Search");
	}
	public String getSidebarJsp() {
	        return description.getProperty("Forward.Sidebar");
	}
	public String getNavbarJsp() {
	        return description.getProperty("Forward.Navbar");
	}
	public String getBodyTitleJsp() {
	        return description.getProperty("Forward.BodyTitle");
	}
	public String getBodyJsp() {
	        return description.getProperty("Forward.Body");
	}
   /**
	* Takes a symbolic page name, such as "AboutMe", and produces the full path name
	* in Windows notation of a current file by concatenating the current directory path name
	* with the relative file name.
	* e.g.  ForwardAboutMe => D:/HomePageNew/Generated/AboutMe.htm
	*
	* @param symbolic page name
	* @return absolute path name of a current file in Windows notation
	*/
	public String getTargetFilenameForPage(String page) {
		String filename = getTargetDirectory()+"/"
		                 +description.getProperty(page+".Historic");
        	return filename;
	}
   /**
	* Takes a symbolic page name, such as "AboutMe", and produces the full path name
	* in Windows notation of a current file by concatenating the current directory path name
	* with the relative file name.
	* e.g.  ForwardAboutMe => D:/HomePageNew/sources/HomePage/AboutMe.html
	*
	* @param symbolic page name
	* @return absolute path name of a current file in Windows notation
	*/
	public String getForwardingFilenameForPage(String page) {
		String filename = getCurrentDirectory()+"/"
		                 +description.getProperty(page+".Current");
        	return filename;
	}
   /**
	* Takes a symbolic page name, such as "AboutMe", and produces the full path name
	* in Windows notation of a historic file by concatenating the historic directory path name
	* with the relative file name.
	* e.g.  Forward.AboutMe => D:/HomePage/AboutMe.htm
	*
	* @param symbolic page name
	* @return absolute path name of a historic file in Windows notation
	*/
	public String getOldFilenameForPage(String page) {
    		String filename = getHistoricDirectory()+"/"
    		                       +description.getProperty(page+".Historic");
	        return filename;
	}
       /*
        * Creates a symbolic page name from a historic filename.
        *
        * @param  absolute path name of historic HTML file
        * @return symbolic page name
        */
        public String getSymbolicPagenameForHistoricFilename(String filnam) {
        	return (String)historicFilesToPages.get(filnam);
        }

   /**
	* Creates a Map<String,String> that associates the relative path name in WIndows notation
	* of a historic file with its symbolic page name.
	* e.g.  key:   AboutMe.htm
	*       value: Forward.AboutMe
	*
	* <p>This map is used to look up whether there is a description for a historic file found in the
	*    file system.
	*
	* @return a map of relative path name of historic file in Windoes notation and the corresponding symbolic page name
	*/
	private Map mapHistoricFilesToPages() {
		Map historicFilesToPagename = new HashMap();
		for (Enumeration e = description.propertyNames(); e.hasMoreElements() ;) {
			String propertyKey = (String)e.nextElement();
			String symbolicPagename = getSymbolicPagenameFromHistoricPropertyKey(propertyKey);
         		if (symbolicPagename!=null) {
         			String propertyValue = description.getProperty(propertyKey);
				historicFilesToPagename.put(propertyValue, symbolicPagename);
         		}
         	}
         	return historicFilesToPagename;
	}
	public boolean hasCurrentCounterpart(String pagename) {
		String current = description.getProperty(pagename+".Current");
		return (current.length() > 0);
	}
   /**
	* Extracts the symbolic page name from a property key found in the site description properties file.
	* e.g. Forward.AboutMe => AboutMe.html
	*
	* @param a property key from the site description
	* @return the symbolic page name
	*/
	private static String getSymbolicPagenameFromHistoricPropertyKey(String prop) {
		StringTokenizer tok = new StringTokenizer(prop,".");
		StringBuffer buf = new StringBuffer();
		String suffix = null;
		while ( tok.hasMoreTokens() ) {
	      		suffix = tok.nextToken();
	      		if (tok.countTokens()>0) buf.append(suffix);
	      		if (tok.countTokens()>1) buf.append(".");
	      	}
	      	if ( suffix.equals("Historic") )
	      		return buf.toString();
	      	else
	      		return null;
	}

}