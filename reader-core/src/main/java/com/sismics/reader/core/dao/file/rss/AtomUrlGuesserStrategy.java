package com.sismics.reader.core.dao.file.rss;

import java.util.List;

/**
 * Guess the URL from a set of links.
 * 
 * @author jtremeaux
 */
public class AtomUrlGuesserStrategy {

    /**
     * Guess the correct site URL from a set of links.
     * 
     * @param atomLinkList List of links
     * @return Site URL
     */
    public String guessSiteUrl(List<AtomLink> atomLinkList) {
        if (atomLinkList == null || atomLinkList.isEmpty()) {
            return null;
        }
        
        // Return alternate links first (e.g. Blogspot)
        for (AtomLink atomLink : atomLinkList) {
            if ("alternate".equalsIgnoreCase(atomLink.getRel())) {
                return atomLink.getHref();
            }
        }
        
        // Default: return the first valid link
        for (AtomLink atomLink : atomLinkList) {
            if (!"self".equals(atomLink.getRel())) {
                return atomLink.getHref();
            }
        }

        return null;
    }

    /**
     * Guess the correct Feed URL from a set of links.
     *
     * @param atomLinkList List of links
     * @return Site URL
     */
    public String guessFeedUrl(List<AtomLink> atomLinkList) {
        if (atomLinkList == null || atomLinkList.isEmpty()) {
            return null;
        }

        // Return the self link
        for (AtomLink atomLink : atomLinkList) {
            if ("self".equalsIgnoreCase(atomLink.getRel())) {
                return atomLink.getHref();
            }
        }

        return null;
    }
}
