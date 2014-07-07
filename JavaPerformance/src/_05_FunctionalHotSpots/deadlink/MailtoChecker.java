package _05_FunctionalHotSpots.deadlink;

import java.net.*;
import java.util.StringTokenizer;


public final class MailtoChecker {
    public static boolean isMailtoLink(String lnk) {
        try {
            URI uri = new URI(lnk);
            String scheme = uri.getScheme();
            return (scheme != null && scheme.equals("mailto"));
        } catch (URISyntaxException e) {
            return MailtoChecker.mailtoWorkAround(lnk);
        }
    }

    public static boolean mailtoWorkAround(String lnk) {
        StringTokenizer tok = new StringTokenizer(lnk,":");
        if (tok.nextToken().equals("mailto"))
            return true;
        else
            return false;        
    }
}
