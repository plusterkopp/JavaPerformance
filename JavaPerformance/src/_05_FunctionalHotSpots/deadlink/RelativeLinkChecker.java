/*
 * Created on 24.08.2004
 *
 */
package _05_FunctionalHotSpots.deadlink;

import java.net.*;
import java.util.*;

import _05_FunctionalHotSpots.util.file.*;


public final class RelativeLinkChecker {
    public static boolean isRelativeFileLink(String lnk) {
        return !isAbsoluteLink(lnk) && !LabelChecker.isLocalLabel(lnk);
    }

    public static boolean isAvailableRelativeLink(String filnam, String linknamWithLabel) {
        LinkAndLabel lal = removeLabel(linknamWithLabel);
        if (lal.getLabel() == null) {
            return FileUtility.relativePathExists(filnam,lal.getLink());
        }
        else {
            FileUtility.RelativePathInfo relativePathInfo = FileUtility.getRelativePathInfo(filnam,lal.getLink());
            if (relativePathInfo.exists)
                return LabelChecker.labelExists(relativePathInfo.absFilnam,lal.getLabel());
            else
                return relativePathInfo.exists;
        }
    }

    public final static class UnexpectedLinkNameException extends RuntimeException {
        private String linkName;
        public UnexpectedLinkNameException(String linknam) {
            linkName = linknam;
        }
        public String getLinkName() { return linkName; }
    }
    private static final class LinkAndLabel {
        private String link;
        private String label;
        public String getLink() { return link; }
        public String getLabel() { return label; }
        public void setLink(String l) { link = l; }
        public void setLabel(String l) { label = l; }
    }
    private static LinkAndLabel removeLabel(String linknam) {
        LinkAndLabel result = new LinkAndLabel();
        StringTokenizer tok = new StringTokenizer(linknam,"#");
        if (tok.countTokens() == 1) {
            result.setLink(linknam);
            return result;
        }
        else if (tok.countTokens() == 2) {
            result.setLink(tok.nextToken());
            result.setLabel(tok.nextToken());
            return result;
        }
        else if (tok.countTokens() == 0 || tok.countTokens() > 2) {
            System.err.println(">>> error: unexpected link name  "+linknam);
            throw new UnexpectedLinkNameException(linknam);
        }
        return null;
    }

    public static boolean isAbsoluteLink(String lnk) {
        try {
            URI uri = new URI(lnk);
            return uri.isAbsolute();
        } catch (URISyntaxException e) {
            return MailtoChecker.mailtoWorkAround(lnk);
        }
    }

}
