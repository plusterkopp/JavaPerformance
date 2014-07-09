package _05_FunctionalHotSpots.deadlink;

import java.net.*;
import java.util.*;

public final class URIChecker {

	static Set<String>	deadLinks	= new HashSet<String>();
	static Set<String>	liveLinks	= new HashSet<String>();

	public static boolean isAvailableHttpLink( String lnk) {
		URL url;
		try {
			url = new URL( lnk);
			String hostName = url.getHost();
			if ( deadLinks.contains( hostName)) {
				System.err.println( "   cached fail: " + lnk);
				return false;
			}
			if ( liveLinks.contains( lnk)) {
				System.out.println( "   cached hit: " + lnk);
				return true;
			}
			try {
				URLConnection con = url.openConnection();
				System.out.print( "connecting: " + lnk + "…");
				con.connect();
			} catch ( Exception e) {
				System.err.println( "failed: " + e.toString());
				deadLinks.add( hostName);
				return false;
			}
			System.out.println( "connected");
			liveLinks.add( lnk);
			return true;
		} catch ( MalformedURLException e1) {
			System.out.println( "URL malformed: " + lnk);
			return false;
		}
	}

	public static boolean isHttpLink( String lnk) {
		try {
			URI uri = new URI( lnk);
			String scheme = uri.getScheme();
			return ( scheme != null && scheme.equals( "http"));
		} catch ( URISyntaxException e) {
			return false;
		}
	}

	public static String getScheme( String lnk) {
		try {
			URI uri = new URI( lnk);
			String scheme = uri.getScheme();
			if ( scheme != null)
				return scheme;
			else
				return "relative file";
		} catch ( URISyntaxException e) {
			return ( MailtoChecker.mailtoWorkAround( lnk)) ? "mailto" : "???";
		}
	}
}
