/*
 * Created on 07.10.2004
 *
 */
package _05_FunctionalHotSpots.deadlink;

import java.io.*;

import _05_FunctionalHotSpots.util.*;


public final class PageDescription {
    private String pageName;
    private SiteDescription siteDescr;

    public PageDescription(File htmlFile) {
        this.pageName     = null;
        this.siteDescr    = null;
    }
    public PageDescription(String pageName) {
        this.pageName     = pageName;
        this.siteDescr    = SiteDescription.getSiteDescription();
    }
    public String getFilename() {
        return siteDescr.getBodyFilenameForPage(pageName);
    }
}