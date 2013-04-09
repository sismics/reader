package com.sismics.reader.core.dao.file.rss;

import java.util.List;

/**
 * Guess the URL from a set of links.
 * 
 * @author jtremeaux
 */
public class AtomUrlGuesserStrategy {

    /**
     * Guess the correct Site URL from a set of links.
     * 
     * @param atomLinkList List of links
     * @return Site URL
     */
    public String guess(List<AtomLink> atomLinkList) {
        if (atomLinkList == null || atomLinkList.isEmpty()) {
            return null;
        }
        
        // Return alternate links first (e.g. Blogspot)
        for (AtomLink atomLink : atomLinkList) {
            if ("alternate".equalsIgnoreCase(atomLink.getRel())) {
                return atomLink.getHref();
            }
        }
        
        // Default: return the first link
        return atomLinkList.get(0).getHref();
    }
}
