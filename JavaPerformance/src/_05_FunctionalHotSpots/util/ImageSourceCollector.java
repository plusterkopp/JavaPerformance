/*
 * Created on 22.10.2003
 *
 */
package _05_FunctionalHotSpots.util;

import java.util.*;

import _05_FunctionalHotSpots.cvu.*;
import _05_FunctionalHotSpots.cvu.html.*;
import _05_FunctionalHotSpots.util.html.*;

/**
 * @author Angelika Langer
 *
 */
public final class ImageSourceCollector {
    private Set images = new HashSet();

    public SingleTagTokenAttributeProcessor getImageSourceProcessor() {
        return new imageProcessor();
    }

    private final class imageProcessor implements SingleTagTokenAttributeProcessor {
        public String getTagName() {
            return "img";
        }
        public String getAttributeName() {
            return "src";
        }
        public TagToken process(TagToken token) {
            String srcAttribute = token.getAttribute(getAttributeName());
            images.add(srcAttribute);
            return token;
        }
    }

    public Set getImageFilenames() {
        return images;
    }

}
