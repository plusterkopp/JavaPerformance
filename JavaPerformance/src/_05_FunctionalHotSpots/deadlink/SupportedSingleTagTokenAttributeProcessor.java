/*
 * Created on 24.08.2004
 *
 */
package _05_FunctionalHotSpots.deadlink;

import _05_FunctionalHotSpots.util.html.*;

/**
 * @author Angelika Langer
 *
 */
public abstract class SupportedSingleTagTokenAttributeProcessor
    implements SingleTagTokenAttributeProcessor {
        protected DeadLinkDetector.ProcessorSupport support;

        protected SupportedSingleTagTokenAttributeProcessor() {
        }
        protected SupportedSingleTagTokenAttributeProcessor(DeadLinkDetector detector) {
            detector.giveSupportTo(this);
        }
        public void receiveSupport(DeadLinkDetector.ProcessorSupport support) {
            this.support = support;
        }
}
