package _05_FunctionalHotSpots.util.file;

import java.io.*;
import java.util.*;

import _05_FunctionalHotSpots.util.*;
/**
 * A utility that scans the file system for existing source files
 * and produces a list of symbolic page names for which HTML files will be generated.
 *
 * @author Angelika Langer
 */
public final class FileCrawler {

	private SiteDescription siteDesc;

	private static class Statistics {
		private int numberOfSourceFiles = 0;
		private int numberOfFilesForThisIncrement = 0;

		public void setNumberOfSourcesFiles(int n) {
			numberOfSourceFiles = n;
		}
		public void setNumberOfFilesForThisIncrement(int n) {
			numberOfFilesForThisIncrement = n;
		}
		public String toString() {
			StringBuffer buf = new StringBuffer("-- statistics for HTML file generation --");
			buf.append('\n');
		    buf.append("total number of files                : ");
            buf.append(numberOfSourceFiles);
			buf.append('\n');
			buf.append("number of files in this increment    : ");
            buf.append(numberOfFilesForThisIncrement);
			buf.append('\n');
			return buf.toString();
		}
	}
	private Statistics stats = new Statistics();

	public FileCrawler() {
		this.siteDesc = SiteDescription.getSiteDescription();
	}
	public Statistics getStatistics() {
		return stats;
	}

       /**
        * Provides a list of all symbolic page names for which a page description is available.
        *
        * @param a Collection<File> containing absolute path names in Unix notation of body files available in the source directory
        * @return a Collection<String> containing the symbolic page names of all pages that must and can be generated
        */
	private Collection extractPageNames(Collection htmlFilesInSourceDirectory) {

		// Retrieve association of absolute path name to symbolic page name.
		Map bodyFileNamesInSiteDescription = siteDesc.mapBodyFilesToPages();

		Collection pageNames = new ArrayList();

		// For all path names check whether there is a page description available.
		Iterator iter = htmlFilesInSourceDirectory.iterator();
		while (iter.hasNext()) 	{
			File fil = (File)iter.next();
			if (bodyFileNamesInSiteDescription.containsKey(fil.getPath())) {
				// There is a description available for this path name.
				// Add symbolic page name to list of pages to be generated.
				pageNames.add(bodyFileNamesInSiteDescription.get(fil.getPath()));
			}
			else {
				// There is a description available for this path name.
				System.err.println(">>> error: no description available for body file: "+fil.getPath());
			}
		}
		return pageNames;
	}
   /**
    * Provides a list of all symbolic page names for which a target HTML file must be generated.
    *
    * @return a Collection<String> of absolute path names in Unix notation of files to be generated
    */
    public Collection getPages(boolean modifiedPagesOnly) {

	// Retrieve a list of all HTML files contained in the source directory
	// for which a target file must be generated.
	Collection htmlFiles = getModifiedHtmlFiles(modifiedPagesOnly);
//    	Iterator iter = htmlFiles.iterator();
//	while (iter.hasNext()) 	System.err.println(iter.next());

	// Retrieve a list of symbolic page names for which a site description is available.
	// Print an error message for each page, that is not described in the site description.
	Collection pageNames = extractPageNames(htmlFiles);
	stats.setNumberOfFilesForThisIncrement(pageNames.size());
//	iter = pageNames.iterator();
//	while (iter.hasNext()) 	System.err.println(iter.next());

	return pageNames;
    }

    private Collection getModifiedHtmlFiles(boolean returnModifiedPagesOnly) {
	// Retrieve a list of all HTML files contained in the HTML source directory.
	Collection htmlSourceFiles = new FileFinder(FileFinder.HTML_FILES).
		                       getAllFilesInDirectory(siteDesc.getHtmlSourceDirectory());
	stats.setNumberOfSourcesFiles(htmlSourceFiles.size());
//    	Iterator iter = htmlFiles.iterator();
//	while (iter.hasNext()) 	System.err.println(iter.next());

	// Check whether the JSP source directory or the site description file
	// is younger than the target directory.
	// If so, do a full generation of all target files.
	if (  FileUtility.isYounger(siteDesc.getJspSourceDirectory(), siteDesc.getTargetDirectory())
	   || FileUtility.isYounger(siteDesc.getSiteDescriptionFilename(), siteDesc.getTargetDirectory())
	   ) {
		// Generate all HTML files.
		System.err.println("~~~~~~~~~~~~~~ do full generation");
		return htmlSourceFiles;
	}

	// to be removed later
	if (!returnModifiedPagesOnly) return htmlSourceFiles;

	// Retrieve a list of all HTML files contained in the source directory.
	Collection htmlTargetFiles = new FileFinder(FileFinder.HTML_FILES).
		                       getAllFilesInDirectory(siteDesc.getTargetDirectory());
//    	Iterator iter = htmlFiles.iterator();
//	while (iter.hasNext()) 	System.err.println(iter.next());

	Iterator iter = htmlSourceFiles.iterator();
	while (iter.hasNext()) {
		File srcFil = (File) iter.next();
		// Get corresponding target file.
		File trgFil = new File(siteDesc.getTargetDirectory()+"/"
		                      +FileUtility.relativePath(srcFil.getPath(),siteDesc.getHtmlSourceDirectory()));
		if (htmlTargetFiles.contains(trgFil)) {
			// Corresponding target file already exists; compare last modification dates.
			long srcModDat = srcFil.lastModified();
			long trgModDat = trgFil.lastModified();

//				System.err.println("source: "+srcFil+" last modified at: "+srcModDat);
//				System.err.println("target: "+trgFil+" last modified at: "+trgModDat);

			if (srcModDat < trgModDat) {
				// Target is younger than source; no generation needed; remove.
				iter.remove();
//					System.err.println("target is younger than source; no generation needed");
			}
		}
		else {
			// Corresponding target file does not yet exist; keep target file in list.
		}
        }
	return htmlSourceFiles;
    }

}