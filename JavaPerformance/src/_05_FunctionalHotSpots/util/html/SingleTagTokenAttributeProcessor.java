/*
 * Created on 22.10.2003
 *
 */
package _05_FunctionalHotSpots.util.html;

import _05_FunctionalHotSpots.cvu.html.*;

/**
 * @author Angelika Langer
 *
 */
public interface SingleTagTokenAttributeProcessor {
    public TagToken process(TagToken token);
    public String getTagName();
    public String getAttributeName();
}
