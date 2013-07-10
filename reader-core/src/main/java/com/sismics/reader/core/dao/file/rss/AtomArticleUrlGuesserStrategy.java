package com.sismics.reader.core.dao.file.rss;

import java.util.List;

/**
 * Guess the article URL from a set of links.
 * 
 * @author jtremeaux
 */
public class AtomArticleUrlGuesserStrategy {

    /**
     * Guess the correct article URL from a set of links.
     * 
     * @param atomLinkList List of links
     * @return Site URL
     */
    public String guess(List<AtomLink> atomLinkList) {
        if (atomLinkList == null || atomLinkList.isEmpty()) {
            return null;
        }
        
        // 1st try: link from the <item> element
        for (AtomLink atomLink : atomLinkList) {
            if (atomLink.getRel() == null && atomLink.getType() == null) {
                return atomLink.getHref();
            }
        }
        
        // 2nd try: link from the <alternate> element
        for (AtomLink atomLink : atomLinkList) {
            if ("alternate".equals(atomLink.getRel()) && "text/html".equals(atomLink.getType())) {
                return atomLink.getHref();
            }
        }
        
        // Default: return the first link
        return atomLinkList.get(0).getHref();
    }
}
