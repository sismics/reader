package com.sismics.reader.core.dao.file.html;

import java.util.List;

/**
 * Strategy used to guess the URL of a RSS / Atom feed when several are present.
 *
 * @author jtremeaux
 */
public class FeedChooserStrategy {

    /**
     * Guess the correct feed URL from a set of links.
     * 
     * @param feedList List of links
     * @return Feed URL
     */
    public String guess(List<String> feedList) {
        return feedList.get(0);
    }
}
