package com.sismics.reader.core.dao.file.rss;

import java.util.List;

/**
 * Guess the article comment URL from a set of links.
 * 
 * @author jtremeaux
 */
public class AtomArticleCommentUrlGuesserStrategy {

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
        
        // 1st try: link with "replies" rel and html content type
        for (AtomLink atomLink : atomLinkList) {
            if ("replies".equals(atomLink.getRel()) && "text/html".equals(atomLink.getType())) {
                return atomLink.getHref();
            }
        }
        
        // 2nd try: any link with "replies" rel
        for (AtomLink atomLink : atomLinkList) {
            if ("replies".equals(atomLink.getRel())) {
                return atomLink.getHref();
            }
        }
        
        // Default: no comment link present
        return null;
    }
}
