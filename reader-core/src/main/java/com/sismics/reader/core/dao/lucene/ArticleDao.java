package com.sismics.reader.core.dao.lucene;

import com.sismics.reader.core.dao.jpa.dto.UserArticleDto;
import com.sismics.reader.core.model.context.AppContext;
import com.sismics.reader.core.model.jpa.Article;
import com.sismics.reader.core.util.LuceneUtil;
import com.sismics.reader.core.util.jpa.PaginatedList;
import org.apache.lucene.document.*;
import org.apache.lucene.index.FieldInfo.IndexOptions;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.flexible.standard.QueryParserUtil;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.*;
import org.apache.lucene.search.SortField.Type;
import org.apache.lucene.search.grouping.GroupDocs;
import org.apache.lucene.search.grouping.GroupingSearch;
import org.apache.lucene.search.grouping.TopGroups;
import org.apache.lucene.search.postingshighlight.Passage;
import org.apache.lucene.search.postingshighlight.PassageFormatter;
import org.apache.lucene.search.postingshighlight.PassageScorer;
import org.apache.lucene.search.postingshighlight.PostingsHighlighter;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;

import java.text.BreakIterator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Lucene Article DAO.
 * 
 * @author bgamard
 */
public class ArticleDao {

    /**
     * Destroy and rebuild index.
     * 
     * @param articleList The list of articles
     */
    public void rebuildIndex(final List<Article> articleList) {
        LuceneUtil.handle(indexWriter -> {
            // Empty index
            indexWriter.deleteAll();

            // Add all articles
            for (Article article : articleList) {
                Document document = getDocumentFromArticle(article);
                indexWriter.addDocument(document);
            }
        });
    }

    
    /**
     * Add articles to the index.
     * 
     * @param articleList The list of articles
     */
    public void create(final List<Article> articleList) {
        LuceneUtil.handle(indexWriter -> {
            // Add all articles
            for (Article article : articleList) {
                Document document = getDocumentFromArticle(article);
                indexWriter.addDocument(document);
            }
        });
    }
    
    /**
     * Update index.
     * 
     * @param articleList Article list
     */
    public void update(final List<Article> articleList) {
        LuceneUtil.handle(indexWriter -> {
            // Update all articles
            for (Article article : articleList) {
                Document document = getDocumentFromArticle(article);
                indexWriter.updateDocument(new Term("id", article.getId()), document);
            }
        });
    }

    /**
     * Delete index.
     * 
     * @param articleList Article list
     */
    public void delete(final List<Article> articleList) {
        LuceneUtil.handle(indexWriter -> {
            // Delete all articles
            for (Article article : articleList) {
                indexWriter.deleteDocuments(new Term("id", article.getId()));
            }
        });
    }

    /**
     * Search articles.
     * 
     * @param paginatedList The list of articles
     * @param searchQuery The query
     * @return List of articles
     */
    public Map<String, Article> search(PaginatedList<UserArticleDto> paginatedList, String searchQuery) throws Exception {
        // Escape query and add quotes so QueryParser generate a PhraseQuery
        searchQuery = "\"" + QueryParserUtil.escape(searchQuery) + "\"";
        
        // Build search query
        StandardQueryParser qpHelper = new StandardQueryParser(new ReaderStandardAnalyzer(Version.LUCENE_42));
        qpHelper.setPhraseSlop(100000); // PhraseQuery add terms
        Query titleQuery = qpHelper.parse(searchQuery, "title");
        Query descriptionQuery = qpHelper.parse(searchQuery, "description");
        
        // Search on article content
        BooleanQuery query = new BooleanQuery();
        query.add(titleQuery, Occur.SHOULD);
        query.add(descriptionQuery, Occur.SHOULD);
        
        // Grouping
        GroupingSearch groupingSearch = new GroupingSearch("url");
        groupingSearch.setGroupSort(new Sort(new SortField("date", Type.LONG, true)));
        groupingSearch.setFillSortFields(true);
        groupingSearch.setCachingInMB(20, true);
        groupingSearch.setAllGroups(true);
        
        // Searching
        IndexSearcher searcher = new IndexSearcher(AppContext.getInstance().getIndexingService().getDirectoryReader());
        TopGroups<BytesRef> topGroups = groupingSearch.search(searcher, query, paginatedList.getOffset(), paginatedList.getLimit());
        int total = topGroups.totalGroupCount == null ? 0 : topGroups.totalGroupCount;
        paginatedList.setResultCount(total);
        ScoreDoc[] scoreDocs = new ScoreDoc[topGroups.groups.length];
        int j = 0;
        for (GroupDocs<BytesRef> groupDocs : topGroups.groups) {
            scoreDocs[j++] = groupDocs.scoreDocs[0];
        }
        TopDocs topDocs = new TopDocs(total, scoreDocs, 0);
        
        // Highlighting
        PostingsHighlighter highlighter = new PostingsHighlighter(1000000, BreakIterator.getSentenceInstance(Locale.ROOT), new PassageScorer(), new PassageFormatter() {
            @Override
            public String format(Passage passages[], String content) {
                StringBuilder sb = new StringBuilder();
                int pos = 0;
                for (Passage passage : passages) {
                    for (int i = 0; i < passage.getNumMatches(); i++) {
                        int start = passage.getMatchStarts()[i];
                        int end = passage.getMatchEnds()[i];
                        sb.append(content.substring(pos, start));
                        sb.append("<span class=\"highlight\">");
                        sb.append(content.substring(start, end));
                        sb.append("</span>");
                        pos = end;
                    }
                }
                if (pos < content.length()) {
                    sb.append(content.substring(pos));
                }
                return sb.toString();
            }
        });
        Map<String, String[]> highlights = highlighter.highlightFields(new String[] { "title", "description" }, query, searcher, topDocs, 3);
        
        // Extract article ids
        Map<String, Article> articleList = new HashMap<String, Article>();
        for (int i = 0; i < scoreDocs.length; i++) {
            String id = searcher.doc(scoreDocs[i].doc).get("id");
            String title = highlights.get("title")[i];
            String description = highlights.get("description")[i];
            Article article = new Article();
            article.setId(id);
            article.setTitle(title);
            article.setDescription(description);
            articleList.put(id, article);
        }
        
        return articleList;
    }
    
    /**
     * Build Lucene document from article.
     * 
     * @param article Article
     * @return Document
     */
    private org.apache.lucene.document.Document getDocumentFromArticle(Article article) {
        // Index character offsets for the highlighter
        FieldType fieldType = new FieldType(TextField.TYPE_STORED);
        fieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
        
        // Building document
        org.apache.lucene.document.Document document = new org.apache.lucene.document.Document();
        document.add(new StringField("id", article.getId(), Field.Store.YES));
        document.add(new StringField("url", article.getUrl(), Field.Store.YES));
        document.add(new LongField("date", article.getPublicationDate().getTime(), Field.Store.YES));
        document.add(new Field("title", article.getTitle(), fieldType));
        document.add(new Field("description", article.getDescription(), fieldType));
        
        return document;
    }
}
