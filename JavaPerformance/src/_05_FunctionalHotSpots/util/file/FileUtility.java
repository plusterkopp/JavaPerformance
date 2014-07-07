package _05_FunctionalHotSpots.util.file;

import java.io.*;
import java.util.*;

public final class FileUtility {
       /**
        * Converts a path name in Windows notation into a path name in Unix notation, 
        * i.e. replaces '/' by '\'
        *
        * @param path name in Windows notation
        * @return path name in Unix notation
	*/
	public static String changeSymbolicPathToRealPath(String windowsPath) {
		return changePath(windowsPath,"/","\\");		
	}
       /**
        * Converts a path name in Unix notation into a path name in Windows notation, 
        * i.e. replaces '\' by '/'
        *
        * @param path name in Unix notation
        * @return path name in Windows notation
	*/
	public static String changeRealPathToSymbolicPath(String unixPath) {
		return changePath(unixPath,"\\","/");		
	}
	private static String changePath(String path, String replace, String by) {
		StringTokenizer tok = new StringTokenizer(path,replace);
		StringBuffer newPath = new StringBuffer();
		while ( tok.hasMoreTokens() ) {
	      		newPath.append(tok.nextToken());
	      		if (tok.hasMoreTokens())  newPath.append(by);
	    }
		return newPath.toString();		
	}
       /**
        * Turns an absolute pathname into a relative pathname by removing the absolute pathname
        * of a directory.
        *
        * @param  absolute path name of a file in Windows notation
        * @param  absolute path name of a directory in Windows notation
        * @return remainig relative path name in Unix notation
	*/
	public static String relativePath(String path, String directoryToBeRemovedFromPath) {
//		System.err.println("*** substract :"+directoryToBeRemovedFromPath);
//		System.err.println("*** from: "+path);	
		File fil = new File(path);
		File dir = new File(directoryToBeRemovedFromPath);
		StringBuffer buf = new StringBuffer(fil.getName());
		
		File parent = fil.getParentFile();
		while (parent != null && !parent.getName().equals(dir.getName())) {
			buf.insert(0,parent.getName()+"/");
			parent = parent.getParentFile();
		}
	
//		System.err.println("resulting relative path: "+buf.toString());
		return buf.toString();
	}
       /**
        * Turns a collection of File objects into a collection of relative pathnames
        * by removing the absolute pathname of a directory from each file name.
        *
        * @param  collection of File objects
        * @param  absolute path name of a directory in Windows notation
        * @return remainig relative path name in Unix notation
	*/	
	public static Collection removeDirectory(Collection absoluteFiles, String directoryToBeRemovedFromPath) {
	    	Collection relativeFileNames = new HashSet();
	    	Iterator iter = absoluteFiles.iterator();
	    	while (iter.hasNext()) {
	    		relativeFileNames.add(FileUtility.relativePath(((File)iter.next()).getPath(),directoryToBeRemovedFromPath));
	    	}
	    	return relativeFileNames;
        }
	public static void makeDirectories(File fil) {
        	boolean wasCreated = fil.getParentFile().mkdirs();
//        	if (wasCreated) System.out.println("directory: "+fil+" created");		
	}
	public static void deleteEmptyDirectory(File fil) {
		if (fil.exists()) {
			boolean wasDeleted = fil.delete();
	        	if (!wasDeleted) System.out.println(">>>>>>>>>>>>>>>>>>>>>> cannot delete directory: "+fil);
//        		else System.out.println("directory: "+fil+" deleted");
		}
	}
	public static void deleteDirectory(File currentDir) {
		if (currentDir.exists()) {
			File[] files = currentDir.listFiles();
			for(int i=0; i<files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDirectory(files[i]);
				}
				else if (files[i].isFile()) {
					boolean wasDeleted = files[i].delete();
					if (!wasDeleted) System.out.println(">>>>>>>>>>>>>>>>>>>>>> cannot delete file: "+files[i]);
//					else System.out.println("file: "+files[i]+" deleted");
				}
			}
			files = currentDir.listFiles();
			if (files.length == 0) deleteEmptyDirectory(currentDir);
		}
	}
    public static boolean isYounger(String fil1, String fil2) {
	return (new File(fil1)).lastModified() > (new File(fil2)).lastModified();
    }
    public static boolean isYounger(File fil1, File fil2) {
        return (fil1.lastModified() > fil2.lastModified());
    }
    public static void touch(String fil) {
	(new File(fil)).setLastModified((new Date()).getTime());
    }
    public static void copyCharacterFile(Reader source, Writer target) {
        char[] buffer = new char[256];
        int res = -1;
        do {
            try {
                res = source.read(buffer, 0, 256);
                if (res != -1) target.write(buffer,0,res);
            }
            catch (Exception e) { System.err.println(e); e.printStackTrace(); }
        } while (res > -1);
        
        try {
            source.close();
            target.flush();
            target.close();
        }
        catch (Exception e) { System.err.println(e); e.printStackTrace(); }
    }
    public static void copyBinaryFile(FileInputStream source, FileOutputStream target) {
        byte[] buffer = new byte[256];
        int res = -1;
        do {
            try {
                res = source.read(buffer, 0, 256);
                if (res != -1) target.write(buffer,0,res);
            }
            catch (Exception e) { System.err.println(e); e.printStackTrace(); }
        } while (res > -1);
        
        try {
            source.close();
            target.flush();
            target.close();
        }
        catch (Exception e) { System.err.println(e); e.printStackTrace(); }      
    }
    public static void copyBinaryFile(String filename, File sourceDir, File targetDir) {
        copyBinaryFile(filename, sourceDir, targetDir, true);
    }
    public static void copyBinaryFile(String filename, File sourceDir, File targetDir, boolean overwrite) {
        try {
            File inFile  = new File(sourceDir,filename);
            File outFile = new File(targetDir,filename);
            if (isYounger(outFile,inFile) && !overwrite) return;
            
            makeDirectories(outFile); 

            FileInputStream source = new FileInputStream(inFile);
            FileOutputStream target = new FileOutputStream(outFile);
            copyBinaryFile(source,target);
            
            System.err.println("copied binary file: "+outFile.getPath());
        }
        catch (Exception e) { e.printStackTrace(); }
    } 
    public static boolean hasSuffix(String filename, String suffix)   {
        String name = new File(filename).getName();
        StringTokenizer tok = new StringTokenizer(name,".");
        String s = null;
        while ( tok.hasMoreTokens() ) {
            s = tok.nextToken();
            if (tok.countTokens() == 0) {
                return s.equals(suffix);
            }
        }
        return false;
    }
    public static final class RelativePathInfo {
        public String absFilnam;
        public boolean exists;
    }
    public static boolean relativePathExists(String filnam, String linknam) {
        return getRelativePathInfo(filnam,linknam).exists;
    }
       /**
        * Checks whether the link name exists relative to the file name.
        * Example:  
        *   filnam  = D:/HomePageNew/HomepageGenerator/sources/HomePage/Resources/Books/C++.htm
        *   linknam = ../../Articles/Fawcette/C++Primer-Bookreview/review.htm
        *
        *   check whether file Articles/Fawcette/C++Primer-Bookreview/review.htm 
        *   exists in directory  D:/HomePageNew/HomepageGenerator/sources/HomePage
        *
        * @param absolute or relative path name file in Unix notation
        * @param relative link name in Unix notation
        * @return true if file denoted by relative path exists
        *          absolute pathname of file
        */
    public static RelativePathInfo getRelativePathInfo(String filnam, String linknam) {
        RelativePathInfo result = new RelativePathInfo();
    	StringTokenizer tok = null;
    	// extract nesting level from link name, e.g. level 2 from ../../someName
   	    tok = new StringTokenizer(linknam,"/");
    	int nestingLevel = 0;
    	String s = null;
    	StringBuffer remainingPath = new StringBuffer();
    	while (tok.hasMoreTokens()) {
    	   s = tok.nextToken();
    	   if (s.equals("..")) 
    	       nestingLevel++;
    	   else  {
    	       remainingPath.append("/").append(s);
    	   }
    	}
    	
    	// build absolute pathname of file for whose existence we need to check
    	tok = new StringTokenizer(filnam,"/");
    	StringBuffer fileToBeFound = new StringBuffer();
    	int maxDirectoryIndex = tok.countTokens()-nestingLevel-1;
    	int directoryIndex = 0;
    	while (tok.hasMoreTokens() && directoryIndex<maxDirectoryIndex) {
    	   fileToBeFound.append("/").append(tok.nextToken());
    	   directoryIndex++;
    	}
    	fileToBeFound.append(remainingPath);
//    	System.out.println(">>>>>>>>>>>>>>>>> "+fileToBeFound);

        result.absFilnam = fileToBeFound.toString();
        result.exists = (new File(fileToBeFound.toString())).exists();
    	
    	return result;
    }
   /*
    *  Finds all files in one directory that are NOT contained in the other directory.
    *
    *  @param   absolute pathname of this directory in Windows notation
    *  @param   absolute pathname of that directory in Windows notation
    *  @return  collection of relative pathnames in Unix notation
    */ 
    public static Collection directoryDifference(String thisDirectory, String thatDirectory) {
    	FileFinder finder = null;
    	
    	// find all files from this directory
    	finder = new FileFinder(FileFinder.HTML_FILES);
    	Collection theseFiles = finder.getAllFilesInDirectory(thisDirectory);
	Collection theseRelativeFileNames = removeDirectory(theseFiles,thisDirectory);
   	
    	// find all files from that directory
    	finder = new FileFinder(FileFinder.HTML_FILES);
    	Collection thoseFiles = finder.getAllFilesInDirectory(thatDirectory);
    	Collection thoseRelativeFileNames = removeDirectory(thoseFiles,thatDirectory);
    	
    	// find all files in this directory that do not exist in that directory
    	Collection differenceFiles = new TreeSet();
    	Iterator iter = theseRelativeFileNames.iterator();
    	while (iter.hasNext()) {
    		String unmatched = (String)iter.next();
    		if (!thoseRelativeFileNames.contains(unmatched))
    		    differenceFiles.add(unmatched);
    	}    	
    	
    	return differenceFiles;
    }

}