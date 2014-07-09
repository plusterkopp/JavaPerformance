/*
 * Created on 22.10.2003
 *
 */
package _05_FunctionalHotSpots.util.html;

import java.io.*;
import java.util.*;

import _05_FunctionalHotSpots.cvu.html.*;
import _05_FunctionalHotSpots.util.*;
import _05_FunctionalHotSpots.util.file.*;

/**
 * @author Angelika Langer
 * 
 */
public class HmtlFileProcessor {
	private TagTokenProcessor	processor;

	HTMLTokenizer				ht	= new HTMLTokenizer( null);

	public HmtlFileProcessor( TagTokenProcessor processor) {
		this.processor = processor;
	}

	public void processPage( String page) {
		SiteDescription siteDesc = SiteDescription.getSiteDescription();
		String htmlFilename = siteDesc.getBodyFilenameForPage( page);
		String tmpHtmlFilename = siteDesc.getTemporaryBodyFilenameForPage( page);
		FileUtility.makeDirectories( new File( tmpHtmlFilename));

		processHtmlFile( htmlFilename, tmpHtmlFilename);
	}

	public void processHtmlFile( String inputFilename, String outputFilename) {

		PrintWriter output = null;
		if ( outputFilename != null) {
			try {
				output = new PrintWriter( new OutputStreamWriter( new FileOutputStream(
					outputFilename)));
			} catch ( Exception e) {
				System.err.println( e);
			}
		}

		ht.parseFile( inputFilename);
		Enumeration e = ht.getTokens();

		while ( e.hasMoreElements()) {
			Object token = e.nextElement();

			if ( token instanceof TagToken) {
				token = processor.process( ( TagToken) token);
				if ( output != null)
					output.println( token.toString());
			} else {
				if ( output != null)
					output.print( token.toString());
			}
		}
		if ( output != null) {
			output.flush();
			output.close();
		}
	}
}
