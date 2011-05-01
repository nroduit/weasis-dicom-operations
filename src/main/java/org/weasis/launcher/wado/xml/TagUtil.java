package org.weasis.launcher.wado.xml;

import java.util.List;

import org.weasis.launcher.wado.TagElement;

public class TagUtil {

    public static void addXmlAttribute(TagElement tag, String value, StringBuffer result) {
        if (tag != null && value != null) {
            result.append(tag.getTagName());
            result.append("=\"");
            result.append(EscapeChars.forXML(value));
            result.append("\" ");
        }
    }

    public static void addXmlAttribute(String tag, String value, StringBuffer result) {
        if (tag != null && value != null) {
            result.append(tag);
            result.append("=\"");
            result.append(EscapeChars.forXML(value));
            result.append("\" ");
        }
    }

    public static void addXmlAttribute(String tag, Boolean value, StringBuffer result) {
        if (tag != null && value != null) {
            result.append(tag);
            result.append("=\"");
            result.append(value ? "true" : "false");
            result.append("\" ");
        }
    }

    public static void addXmlAttribute(String tag, List<String> value, StringBuffer result) {
        if (tag != null && value != null) {
            result.append(tag);
            result.append("=\"");
            int size = value.size();
            for (int i = 0; i < size - 1; i++) {
                result.append(EscapeChars.forXML(value.get(i)) + ",");
            }
            if (size > 0) {
                result.append(EscapeChars.forXML(value.get(size - 1)));
            }
            result.append("\" ");
        }
    }
}
