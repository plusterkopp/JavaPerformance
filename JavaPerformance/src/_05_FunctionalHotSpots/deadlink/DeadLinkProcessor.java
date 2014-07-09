/*
 * Created on 25.11.2003
 *
 */
package _05_FunctionalHotSpots.deadlink;

import java.io.*;
import java.util.*;

import _05_FunctionalHotSpots.cvu.html.*;
import _05_FunctionalHotSpots.util.file.*;

final class DeadLinkProcessor extends SupportedSingleTagTokenAttributeProcessor {
	private String	htmlFileName	= null;

	public DeadLinkProcessor( DeadLinkDetector detector) {
		super( detector);
		this.htmlFileName = support.getPageDescription().getFilename();
	}

	public DeadLinkProcessor( DeadLinkDetector detector, File fil) {
		super( detector);
		if ( fil != null)
			this.htmlFileName = FileUtility.changeRealPathToSymbolicPath( fil.getPath());
		else
			this.htmlFileName = support.getPageDescription().getFilename();
	}

	public TagToken process( TagToken token) {
		String hrefAttribute = token.getAttribute( getAttributeName());
		checkForDeadLink( hrefAttribute);
		return token;
	}

	public String getTagName() {
		return "a";
	}

	public String getAttributeName() {
		return "href";
	}

	private void checkForDeadLink( String href) {
		// System.out.println("CHECK - file: "+htmlFileName+" -- "+"checking "+URIChecker.getScheme(href)+": "+href);

		countLinks( href);

		if ( ( LabelChecker.isLocalLabel( href) && !LabelChecker.isAvailableLocalLabel(
			htmlFileName, href))
				|| ( RelativeLinkChecker.isRelativeFileLink( href) && !RelativeLinkChecker
					.isAvailableRelativeLink( htmlFileName, href))
				|| ( URIChecker.isHttpLink( href) && !URIChecker.isAvailableHttpLink( href))) {
			recordDeadLink( htmlFileName, href);
			DeadLinkDetector.getStatistics().incrementNumberOfDeadLinks();
		}
	}

	private void countLinks( String linknam) {
		DeadLinkDetector.getStatistics().incrementTotalNumberOfLinks();
		if ( URIChecker.isHttpLink( linknam)) {
			DeadLinkDetector.getStatistics().incrementNumberOfHttpLinks();
		}
		if ( MailtoChecker.isMailtoLink( linknam)) {
			DeadLinkDetector.getStatistics().incrementNumberOfMailtoLinks();
		}
		if ( !RelativeLinkChecker.isAbsoluteLink( linknam)) {
			if ( LabelChecker.isLocalLabel( linknam))
				DeadLinkDetector.getStatistics().incrementNumberOfLocalLabels();
			else
				DeadLinkDetector.getStatistics().incrementNumberOfRelativeLinks();
		}
	}

	private void recordDeadLink( String filnam, String linknam) {
		// System.out.println("RECORD - file: "+filnam+" -- "+"checking href: "+linknam);
		HashSet filesContainingLink = ( HashSet) support.getDeadLinks().get( linknam);
		if ( filesContainingLink == null) {
			filesContainingLink = new HashSet();
			filesContainingLink.add( filnam);
			support.getDeadLinks().put( linknam, filesContainingLink);
		} else {
			filesContainingLink.add( filnam);
		}
	}
}