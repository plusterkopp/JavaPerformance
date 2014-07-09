package _07_02_MemoryLeaks.test;

import java.io.*;
import java.util.*;

import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.xml.sax.*;

/**
 * FileProcessor provides methods to parse a JProbe Coverage XML snapshot, extract Java file names
 * and simulate a time and/or resource intensive process that uses these file names.
 */
public class FileProcessor {
	private FileProcessor() {
	}

	/**
	 * Simulated time/resource intensive method to process Java source files.
	 */
	public static void processFiles( ArrayList<String> filenames) {
		String name;
		Iterator<String> it = filenames.iterator();
		while ( it.hasNext()) {
			name = ( it.next());
			System.out.println( name);
			try {
				Thread.sleep( 2500);
			} catch ( InterruptedException ie) {
			}
		}
	}

	/**
	 * Extract Java file names from the Coverage snapshot data
	 */
	private static ArrayList<String> getJavaFiles( Document doc) {
		NodeList classnodelist = doc.getElementsByTagName( "class");
		ArrayList<String> javafiles = new ArrayList<String>( classnodelist.getLength());
		Node nextclass, sourcenode;
		for ( int i = 0; i < classnodelist.getLength(); i++) {
			nextclass = classnodelist.item( i);
			if ( nextclass.hasAttributes()) {
				sourcenode = nextclass.getAttributes().getNamedItem( "source");
				javafiles.add( sourcenode.getNodeValue());
			}
		}

		return javafiles;
	}

	/**
	 * Demonstrate a stalled stack scenario
	 */
	public static ArrayList<String> readXMLFile( String filename) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = null;
		try {
			db = dbf.newDocumentBuilder();
			dbf = null;
		} catch ( ParserConfigurationException e) {
			System.out.println( e.getMessage());
			System.exit( 1);
		}

		Document doc = null;
		try {
			doc = db.parse( new File( filename));
			db = null;
		} catch ( SAXException se) {
			System.out.println( se.getMessage());
			System.exit( 1);
		} catch ( IOException ioe) {
			System.out.println( ioe.getMessage());
			System.exit( 1);
		}

		// Extract the names of the source files.
		ArrayList<String> javafiles = getJavaFiles( doc);
		doc = null;

		// Now process the file names, simulating a time/resource intensive way.

		// Note that the document objects are no longer needed but are
		// pinned until processFiles completes. Most noticeable will be
		// TextNode, AttributeNode, ArrayList, ElementNode and AttributeSet
		// objects. The snapshot taken on the entry to processFiles
		// will show many of these objects.
		processFiles( javafiles);

		return javafiles;
	}
}
