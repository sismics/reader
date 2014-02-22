package com.sismics.reader.core.dao.file.opml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * Utility used to flatten the outline hierarchy to only one root and one category level.
 * 
 * @author jtremeaux
 */
public class OpmlFlattener {

    /**
     * Flattens the outline tree to keep only one level of categories.
     * 
     * @param outlineList Tree to flatten
     * @return Flattened tree
     */
    public static Map<String, List<Outline>> flatten(List<Outline> outlineList) {
        Map<String, List<Outline>> result = new HashMap<String, List<Outline>>();
        flatten(outlineList, result, null);
        return result;
    }

    private static void flatten(List<Outline> outlineTree, Map<String, List<Outline>> outlineMap, String prefix) {
        if (outlineTree == null) {
            return;
        }
        for (Outline outline : outlineTree) {
            if (StringUtils.isBlank(outline.getXmlUrl())) {
                // It's a category
                flatten(outline.getOutlineList(), outlineMap, getPrefix(outline, prefix));
            } else {
                // It's a feed
                List<Outline> outlineList = outlineMap.get(prefix);
                if (outlineList == null) {
                    outlineList = new ArrayList<Outline>();
                    outlineMap.put(prefix, outlineList);
                }
                outlineList.add(outline);
            }
        }
    }
    
    private static String getPrefix(Outline outline, String prefix) {
        String text = outline.getText();
        String title = outline.getTitle();
        String category = null;
        if (StringUtils.isNotBlank(text)) {
            category = text;
        } else if (StringUtils.isNotBlank(title)) {
            category = title;
        }
        if (category != null) {
            if (prefix == null) {
                prefix = category;
            } else {
                prefix += " / " + category;
            }
        }
        return prefix;
    }
}
