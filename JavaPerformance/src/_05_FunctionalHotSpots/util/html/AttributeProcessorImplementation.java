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
public final class AttributeProcessorImplementation implements TagTokenProcessor {
    private SingleTagTokenAttributeProcessor processor;

    public AttributeProcessorImplementation(SingleTagTokenAttributeProcessor processor) {
        this.processor = processor;
    }

	public TagToken process(TagToken token) {
        if ( token.getName().equals(processor.getTagName())
         && !token.isEndTag()
         &&  token.getAttribute(processor.getAttributeName()) != null) {

             token = processor.process((TagToken)token);
//           System.err.println("~~~~~~~~~~~~~ "+token.toString());
             return token;
        }
        else {
             return token;
        }
	}

    public String toString() {
        StringBuffer buf = new StringBuffer("-- attribute processor --\n");
        buf.append("tag       = ").append(processor.getTagName()).append('\n');
        buf.append("attribute = ").append(processor.getAttributeName()).append('\n');
        buf.append("processor = ").append(processor.toString()).append('\n');
        return buf.toString();
    }

}
