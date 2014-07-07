/*
 * Created on 22.10.2003
 *
 */
package _05_FunctionalHotSpots.util.html;

import java.util.*;

import _05_FunctionalHotSpots.cvu.html.*;

/**
 * @author Angelika Langer
 *
 */
public final class TagTokenVisitor implements TagTokenProcessor {
    List processors = new ArrayList();

    public void register(TagTokenProcessor processor) {
        processors.add(processor);
    }
    public TagToken process (TagToken token) {
        if (processors.size() == 0) {
            System.err.println(">>> fatal error: no tag token processors have been registered");
            System.exit(-1);
        }
        for (int i=0; i<processors.size(); i++) {
            token = ((TagTokenProcessor)processors.get(i)).process(token);
        }
        return token;
    }
}
