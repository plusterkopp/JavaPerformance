package _05_FunctionalHotSpots.util.file;

import java.io.*;

import _05_FunctionalHotSpots.util.*;

public final class GeneratorOutput {
	private SiteDescription siteDesc;

	public GeneratorOutput() {
		this.siteDesc = SiteDescription.getSiteDescription();
	}


	public PrintWriter getOutputWriter(String page) {
		String outputFileName = siteDesc.getTargetFilenameForPage(page);
	        PrintWriter outputFile = null;
	        try {
	        	FileUtility.makeDirectories(new File(outputFileName));
	        	outputFile = new PrintWriter(new OutputStreamWriter(new FileOutputStream(outputFileName)));
	        }
	        catch (Exception e) {
	        	System.err.println(">>> error: cannot create target file: "+outputFileName);
	        	System.err.println(e);
	        	System.exit(-1);
	        }
	        return outputFile;
	}

}