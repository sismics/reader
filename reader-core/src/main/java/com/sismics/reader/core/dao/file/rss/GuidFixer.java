package com.sismics.reader.core.dao.file.rss;

import org.apache.commons.lang.StringUtils;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.sismics.reader.core.model.jpa.Article;

/**
 * Complete the GUID of articles.
 *
 * @author jtremeaux 
 */
public class GuidFixer {

    /**
     * Complete the GUID of an article.
     * 
     * @param article Article to complete (modified by side effect)
     */
    public static void fixGuid(Article article) {
        if (StringUtils.isBlank(article.getGuid())) {
            Hasher hasher = Hashing.sha1().newHasher();
            if (StringUtils.isNotBlank(article.getUrl())) {
                hasher.putString(article.getUrl());
            } else {
                if (StringUtils.isNotBlank(article.getTitle())) {
                    hasher.putString(article.getTitle());
                } else if (StringUtils.isNotBlank(article.getDescription())) {
                    hasher.putString(article.getDescription());
                }
            }
            article.setGuid(hasher.hash().toString());
        }
    }
}
