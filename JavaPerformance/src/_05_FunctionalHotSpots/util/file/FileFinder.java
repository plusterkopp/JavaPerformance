package _05_FunctionalHotSpots.util.file;


import java.util.*;
import java.io.*;

public final class FileFinder {
	public static final int HTML_FILES = 0;
	public static final int JSP_FILES  = 1;
	
	private Collection fileList = new ArrayList();
	private int fileType = -1;
	
	public FileFinder(int fileType) {
		this.fileType = fileType;
	}
	
	private class SourceFileFilter implements FilenameFilter {
		public boolean accept(File dir, String name) {
			StringTokenizer tok = new StringTokenizer(name,".");
			String suffix = null;
			while ( tok.hasMoreTokens() ) {
		      		suffix = tok.nextToken();
		      	}
		      	switch (fileType) {
		      	case HTML_FILES:
		      		return suffix.equals("html") || suffix.equals("htm");		
			case JSP_FILES:
		      		return suffix.equals("jsp");
		      	default:
		      		System.err.println(">>> error: unknown file type");
		      		return false;
		      	}
		      	
		}
	}
	private static class DirectoryFilter implements FileFilter {
		public boolean accept(File pathname) {
			return pathname.isDirectory();
		}
	}
	private void addFiles(File dir) {
		if (!dir.isDirectory()) System.err.println(">>> error: "+dir+" is not a directory");
		FilenameFilter filter = new SourceFileFilter();
		File[] files = dir.listFiles(filter);
		if (files == null) System.err.println(">>> error: cannot read directory: "+dir);
		for(int i=0; i<files.length; i++) {
//			System.err.println(files[i].getName());
			fileList.add(files[i]);
		}
	}
	private void scanDirectory(File currentDir) {
		addFiles(currentDir);
	
		File[] directories = currentDir.listFiles(new DirectoryFilter());
		for(int i=0; i<directories.length; i++) {
//			System.err.println(directories[i].getName());
			scanDirectory(directories[i]);
		}
	}
       /**
        * Provides a Collection<File> containing the absolute path names in Unix notation 
        * of all HTML files found in the specified directory and its subdirectories.
        *
        * @param absolute path name of source directory in Windows notation
        * @return Collection<File> of absolute path names in Unix notation of files to be generated
        */	
	public Collection getAllFilesInDirectory(String srcdirName) {

		File rootDirectory = new File(srcdirName);
		scanDirectory(rootDirectory);
		
//		Iterator iter = fileList.iterator();
//		while (iter.hasNext()) 	System.err.println(iter.next());
		return fileList;
	}

}