/*
 * Created on 22.10.2003
 *
 */
package _05_FunctionalHotSpots.util;

import java.io.*;
import java.util.*;

import _05_FunctionalHotSpots.util.file.*;

/**
 * @author Angelika Langer
 *
 */
public final class BinaryFileCopier {
    private String pageName;
    private SiteDescription siteDesc;

    public BinaryFileCopier(String pageName) {
        this.pageName = pageName;
        this.siteDesc = SiteDescription.getSiteDescription();
    }

    public void copy(Set binaryFileNames) {
        int toBeCopied = binaryFileNames.size();
        int actuallyCopied = 0;

        // retrieve directory where image file is located
        String bodyFilename = siteDesc.getBodyFilenameForPage(pageName);
        File sourceDir = new File(bodyFilename).getParentFile();

        // retrieve target directory to which image file shall be copied
        String targetFilename = siteDesc.getTargetFilenameForPage(pageName);
        File targetDir = new File(targetFilename).getParentFile();

        Iterator iter = binaryFileNames.iterator();
        while (iter.hasNext()) {
            String filename = (String)iter.next();

            // if filename refers to a local file, i.e. without any parent directories
            if (new File(filename).getParentFile() == null) {
                copyFileFromTo(filename,sourceDir, targetDir);
            }
        }

    }
    private void copyFileFromTo(String filename, File sourceDir, File targetDir) {
//        System.err.println("-- copy file: "+filename+" --");
//        System.err.println("   from: "+sourceDir+"   to: "+targetDir+"\n");
        FileUtility.copyBinaryFile(filename, sourceDir, targetDir, false);
    }
}
