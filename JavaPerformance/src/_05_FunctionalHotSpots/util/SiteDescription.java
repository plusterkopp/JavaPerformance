package _05_FunctionalHotSpots.util;

import java.io.*;
import java.util.*;

import _05_FunctionalHotSpots.util.file.*;

/**
 * A wrapper around the site description properties file.
 *
 * <p>Retrieves the location of the site description file from the call arguments, or uses the
 *    default location, load the properties, and provides services based on the site description.
 *
 * <p>The site description provides the association of a page name (such as "PreviousConferences")
 *    with the relative path names of the related input and output files.  The path names are in
 *    Windows notation, i.e. using "/" as a separator.  Services provided by this class include
 *    production of full path names, conversion between Windows and Unix notation, etc.
 *
 * @author Angelika Langer
 */
public final class SiteDescription {

    private Properties description = null;
    private static final String DESC_OPTION = "desc";
    private static final String DESC_FILE_DEFAULT = "D:/HomePageNew/HomepageGenerator/sources/sitedescription.txt";
    private static String siteDescriptionFilename = null;
    private static SiteDescription singleton = null;

   /**
    * Retrieves the filename of the site description file from an array of arguments.
    *
    * @param array of arguments
    *        The option -Ddesc (see @link DESC_OPTION) specifies the site description file name.
    * @ returns the the filename of the site description file
    */
	private String retrieveSiteDescriptionFilename(String[] args) {
	    if (args == null)  return DESC_FILE_DEFAULT;

            String filename = null;

            if (args.length>0 && args[0].equals("?")) {
            	System.out.println("usage: java [-D<option>=value] HomepageGenerator");
                	System.out.println("   "+DESC_OPTION+"=<site description file>");
                	System.out.println("   e.g. desc=D:/HomePageNew/HomepageGenerator/sources/sitedescription.txt");
		System.exit(-1);
            }

            filename = System.getProperty(DESC_OPTION);
            if (filename == null) {
            	filename = DESC_FILE_DEFAULT;
            }
            siteDescriptionFilename = filename;
            return filename;
	}
   /**
	* Finds and opens in the site description properties file and loads the properties.
	*
	* @param an array of arguments from which the filename of the site description file is taken
	*        If no filename argument is provided, the file is expected in its default location
	*        (see @link #DESC_FILE_DEFAULT).
    */
    private SiteDescription(String[] args) {
	String descrFileName = retrieveSiteDescriptionFilename(args);
//	System.out.println("page description file: "+descrFileName);

	description = new Properties();
        FileInputStream propFile = null;
        try {
        	propFile = new FileInputStream(descrFileName);
        	description.load(propFile);
        }
        catch (Exception e) {System.err.println(e);}
    }
   /**
    * Factory method for site description singleton.
    *
    * @param args an array of arguments from which the filename of the site description file is taken
    * @return  the singleton site description
    */
    public static SiteDescription makeSiteDescription(String[] args) {
        if (singleton == null) {
            singleton = new SiteDescription(args);
        }
        return singleton;
    }
    public static SiteDescription makeSiteDescription() {
        if (singleton == null) {
            singleton = new SiteDescription(null);
        }
        return singleton;
    }
   /**
    * Getter method for site description singleton.
    * This method should only be called after creation of the site description singleton via the
    * makeSiteDescription factory method.  It returns null otherwise.
    *
    * @param args an array of arguments from which the filename of the site description file is taken
    * @return  the singleton site description; returns null if the site descritpion has not yet been created
    */
    public static SiteDescription getSiteDescription() {
        if (singleton == null) {
            System.err.println(">>> error: site description has not been initialized");
        }
        return singleton;
    }
   /**
    * Provides the absolute path name of the site description file in Windows notation.
    * e.g. D:/HomePageNew/HomepageGenerator/sources/sitedescription.txt
    *
    * @return absolute path name of the site description file in Windows notation
    */
	public String getSiteDescriptionFilename() {
		return siteDescriptionFilename;
	}
   /**
    * Provides the absolute path name of the source directory in Windows notation as found in
    * the site description file.
    * e.g. D:/HomePageNew/HomepageGenerator/sources
    *
    * @return absolute path name of the source directory in Windows notation
    */
	public String getSourceDirectory() {
	        return description.getProperty("SourceDirectory");
	}
	public String getTargetDirectory() {
	        return description.getProperty("TargetDirectory");
	}
	public String getTempDirectory() {
	        return description.getProperty("TempDirectory");
	}
	public String getHtmlSourceDirectory() {
		return description.getProperty("SourceDirectory.Html");
	}
	public String getJspSourceDirectory() {
	        return description.getProperty("SourceDirectory.Jsp");
	}
   /**
	* Takes a symbolic page name, such as "PreviousConferences", and produces the full path name
	* in Windows notation of a target file by concatenating the target directory path name
	* with the relative target file name.
	* e.g.  CV => D:/HomePageNew/Generated/AboutMe/CV.html
	*
	* @param symbolic page name
	* @return absolute path name of a target file in Windows notation
	*/
	public String getTargetFilenameForPage(String page) {
		String filename = description.getProperty("TargetDirectory")+"/"
		                       +description.getProperty(page+".Target");
        return filename;
	}
   /**
	* Takes a symbolic page name, such as "PreviousConferences", and produces the full path name
	* in Windows notation of a body file by concatenating the target directory path name
	* with the relative body file name.
	* e.g.  CV => D:/HomePageNew/HomepageGenerator/sources/HomePage/AboutMe/CV.html
	*
	* @param symbolic page name
	* @return absolute path name of a target file in Windows notation
	*/
	public String getBodyFilenameForPage(String page) {
    		String filename = description.getProperty("SourceDirectory")+"/"
    		                       +description.getProperty(page+".Body");
	        return filename;
	}
   /**
	* Takes a symbolic page name, such as "PreviousConferences", and produces the full path name
	* in Windows notation of a temporary body file by concatenating the temporary directory path name
	* with the relative body file name.
	* e.g.  CV => D:/HomePageNew/Temporary/HomePage/AboutMe/CV.html
	*
	* @param symbolic page name
	* @return absolute path name of a target file in Windows notation
	*/
	public String getTemporaryBodyFilenameForPage(String page) {
    		String filename = description.getProperty("TempDirectory")+"/"
    		                       +description.getProperty(page+".Body");
	        return filename;
	}
   /**
	* Creates a Map<String,String> that associates the absolute path name in Unix notation
	* of a body file with its symbolic page name.
	* e.g.  key:   D:\HomePageNew\HomepageGenerator\sources\HomePage\AboutMe\CV.html
	*       value: CV
	*
	* <p>This map is used to look up whether there is a description for a target file found in the
	*    file system.
	*
	* @return a map of absolute path name of body file in Unix notation and the corresponding symbolic page name
	*/
	public Map mapBodyFilesToPages() {
		Map bodyFilesToPagename = new HashMap();
		for (Enumeration e = description.propertyNames(); e.hasMoreElements() ;) {
			String propertyKey = (String)e.nextElement();
			String symbolicPagename = getSymbolicPagenameFromBodyPropertyKey(propertyKey);
         		if (symbolicPagename!=null) {
         			String propertyValue = description.getProperty(propertyKey);
         			bodyFilesToPagename.put(FileUtility.changeSymbolicPathToRealPath(description.getProperty("SourceDirectory")+"/"+propertyValue),
         			                        symbolicPagename);
         		}
         	}
         	return bodyFilesToPagename;
	}
   /**
	* Extracts the symbolic page name from a property key found in the site description properties file.
	* e.g. CV.Body => CV
	*
	* @param a property key from the site description
	* @return the symbolic page name
	*/
	private static String getSymbolicPagenameFromBodyPropertyKey(String prop) {
		StringTokenizer tok = new StringTokenizer(prop,".");
		StringBuffer buf = new StringBuffer();
		String suffix = null;
		while ( tok.hasMoreTokens() ) {
	      		suffix = tok.nextToken();
	      		if (tok.countTokens()>0) buf.append(suffix);
	      		if (tok.countTokens()>1) buf.append(".");
	      	}
	      	if ( suffix.equals("Target")
	      	  || suffix.equals("Source")
	      	  || suffix.equals("Header")
	      	  || suffix.equals("Footer")
	      	  || suffix.equals("Sidebar")
	      	  || suffix.equals("Navbar")
	      	  || suffix.equals("BodyTitle")
	      	  || suffix.equals("Body")
	      	   )
	      		return buf.toString();
	      	else
	      		return null;
	}

}