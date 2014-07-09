package _05_FunctionalHotSpots.util;

import java.util.*;

import _05_FunctionalHotSpots.cvu.*;
import _05_FunctionalHotSpots.cvu.html.*;
import _05_FunctionalHotSpots.util.html.*;

public final class EmailAddressEncoder  {

    private Map encodings;

    public EmailAddressEncoder() {
        encodings = new TreeMap();
        encodings.put("al@AngelikaLanger.com", "&#097;&#108;&#064;&#097;&#110;&#103;&#101;&#108;&#105;&#107;&#097;&#108;&#097;&#110;&#103;&#101;&#114;&#046;&#099;&#111;&#109;");
        encodings.put("info@AngelikaLanger.com", "&#105;&#110;&#102;&#111;&#064;&#097;&#110;&#103;&#101;&#108;&#105;&#107;&#097;&#108;&#097;&#110;&#103;&#101;&#114;&#046;&#099;&#111;&#109;");
        encodings.put("langer@camelot.de", "&#108;&#097;&#110;&#103;&#101;&#114;&#064;&#099;&#097;&#109;&#101;&#108;&#111;&#116;&#046;&#100;&#101;");
        encodings.put("klaus.kreft@siemens.com", "&#107;&#108;&#097;&#117;&#115;&#046;&#107;&#114;&#101;&#102;&#116;&#064;&#115;&#105;&#101;&#109;&#101;&#110;&#115;&#046;&#099;&#111;&#109;");
    }

    public EmailAddressEncoder(Map encodings) {
     this.encodings = encodings;
    }

    public SingleTagTokenAttributeProcessor getMailtoProcessor() {
        return new mailtoProcessor();
    }

    private final class mailtoProcessor implements SingleTagTokenAttributeProcessor {

        public TagToken process(TagToken token) {
            String hrefAttribute = token.getAttribute(getAttributeName());
            hrefAttribute = encodeEmailAddress(hrefAttribute);
            return makeTokenWithModifiedHref(token, hrefAttribute);
        }
        public String getTagName() {
            return "a";
        }
        public String getAttributeName() {
            return "href";
        }
        public String getUrlProtocolName() {
            return "mailto";
        }
        private TagToken makeTokenWithModifiedHref(TagToken hrefToken, String newHrefAttributeValue) {
            StringBuffer buf = new StringBuffer();
            buf.append(hrefToken.getName());
            AttributeList attr = hrefToken.getAttributes();
            if (attr != null && attr.size() > 0) {
                buf.append(' ');
                Enumeration nameList = attr.names();
                while (nameList.hasMoreElements()) {
                    String name = (String) nameList.nextElement();
                    if (name.equals(getAttributeName())) {
                        buf.append(getAttributeName()+"=\"" + newHrefAttributeValue + '"');
                    }
                    else {
                        buf.append(attr.toString(name));
                    }
                    if (nameList.hasMoreElements())
                        buf.append(' ');
                }
            }
            return new TagToken(buf.toString());
        }
        /*
        private String encodeEmailAddress(String hrefAttribute) {
            StringTokenizer tok = new StringTokenizer(hrefAttribute,":");
            if ( tok.countTokens()>1
              && tok.nextToken().equals(getUrlProtocolName())
              && tok.nextToken().equals(originalEmailAddress)) {
                return getUrlProtocolName()+":"+ encodedEmailAddress;
            }
            return hrefAttribute;
        }
        */
        private String encodeEmailAddress(String hrefAttribute) {
            StringTokenizer tok = new StringTokenizer(hrefAttribute,":");
            if ( tok.countTokens()>1
              && tok.nextToken().equals(getUrlProtocolName())
                ) {
                String emailAddress = tok.nextToken();
                Set entries = encodings.entrySet();
                Iterator iter = entries.iterator();
                while (iter.hasNext()) {
                   Map.Entry entry = (Map.Entry) iter.next();
                   if (emailAddress.equals(entry.getKey())) {
                      return getUrlProtocolName()+":"+ entry.getValue();
                   }
                }
            }
            return hrefAttribute;
        }
    }

    public static void main(String[] args) {
        String originalEmailAddress = args[0];
        String encodedEmailAddress  = args[1];
        if (originalEmailAddress == null || originalEmailAddress.length() == 0 || encodedEmailAddress == null || encodedEmailAddress.length() == 0) {
            System.err.println(">>> error: email address information missing");
            System.err.println("Usage:  java util.EmailAddressEncoder original-email encoded-email source-filename target-filename");
            System.exit(-1);
        }

        String source = args[2];
        String target = args[3];
        if (source == null || source.length() == 0 || target == null || target.length() == 0) {
            System.err.println(">>> error: filename missing");
            System.err.println("Usage:  java util.EmailAddressEncoder original-email encoded-email source-filename target-filename");
            System.exit(-1);
        }
        EmailAddressEncoder encoder = new EmailAddressEncoder();
        new HmtlFileProcessor(new AttributeProcessorImplementation(encoder.getMailtoProcessor())).processHtmlFile(source,target);
    }
}