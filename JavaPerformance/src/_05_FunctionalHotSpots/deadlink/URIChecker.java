package _05_FunctionalHotSpots.deadlink;

import java.net.*;


public final class URIChecker {
    public static boolean isAvailableHttpLink(String lnk) {
        try {
			URL url = new URL(lnk);
            URLConnection con = url.openConnection();
            con.connect(); 
		} catch (Exception e) {
            return false;
		}
//      System.out.println("OK   "+lnk);
        return true;
    }
    public static boolean isHttpLink(String lnk) {
        try {
            URI uri = new URI(lnk);
            String scheme = uri.getScheme();
            return (scheme != null && scheme.equals("http"));
        } catch (URISyntaxException e) {
            return false;
        }
    }   
    
    public static String getScheme(String lnk) {
        try {
            URI uri = new URI(lnk);
            String scheme = uri.getScheme();
            if (scheme != null) return scheme;
            else                return "relative file";
        } catch (URISyntaxException e) {
            return (MailtoChecker.mailtoWorkAround(lnk))?"mailto":"???";
        }        
    }
}
