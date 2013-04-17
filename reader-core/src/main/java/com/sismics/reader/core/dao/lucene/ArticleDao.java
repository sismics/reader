package com.sismics.reader.core.dao.lucene;

import java.io.IOException;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.FieldInfo.IndexOptions;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.TermsFilter;
import org.apache.lucene.queryparser.flexible.standard.QueryParserUtil;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.postingshighlight.Passage;
import org.apache.lucene.search.postingshighlight.PassageFormatter;
import org.apache.lucene.search.postingshighlight.PassageScorer;
import org.apache.lucene.search.postingshighlight.PostingsHighlighter;
import org.apache.lucene.util.Version;

import com.sismics.reader.core.dao.jpa.dto.UserArticleDto;
import com.sismics.reader.core.model.context.AppContext;
import com.sismics.reader.core.model.jpa.Article;
import com.sismics.reader.core.util.LuceneUtil;
import com.sismics.reader.core.util.LuceneUtil.LuceneRunnable;
import com.sismics.reader.core.util.jpa.PaginatedList;

/**
 * Lucene Article DAO.
 * 
 * @author bgamard
 */
public class ArticleDao {

    /**
     * Destroy and rebuild index.
     * 
     * @param articleList
     * @throws IOException
     */
    public void rebuildIndex(final List<Article> articleList) {
        LuceneUtil.handle(new LuceneRunnable() {
            @Override
            public void run(IndexWriter indexWriter) throws Exception {
             // Empty index
                indexWriter.deleteAll();
                
                // Add all articles
                for (Article article : articleList) {
                    org.apache.lucene.document.Document document = getDocumentFromArticle(article);
                    indexWriter.addDocument(document);
                }
            }
        });
    }

    
    /**
     * Add articles to the index.
     * 
     * @param articleList
     * @throws IOException
     */
    public void create(final List<Article> articleList) {
        LuceneUtil.handle(new LuceneRunnable() {
            @Override
            public void run(IndexWriter indexWriter) throws Exception {
             // Add all articles
                for (Article article : articleList) {
                    org.apache.lucene.document.Document document = getDocumentFromArticle(article);
                    indexWriter.addDocument(document);
                }
            }
        });
    }

    /**
     * Search articles.
     * 
     * @param paginatedList
     * @param feedList
     * @param searchQuery
     * @return List of articles
     * @throws Exception
     */
    public Map<String, Article> search(PaginatedList<UserArticleDto> paginatedList, List<String> feedList, String searchQuery) throws Exception {
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
        
        // Filter on selected feeds
        List<Term> terms = new ArrayList<Term>();
        for (String feed : feedList) {
            terms.add(new Term("feed_id", feed));
        }
        TermsFilter feedsFilter = new TermsFilter(terms);
        
        // Search
        IndexReader reader = DirectoryReader.open(AppContext.getInstance().getLuceneDirectory());
        IndexSearcher searcher = new IndexSearcher(reader);
        TopDocs topDocs = searcher.search(query, feedsFilter, paginatedList.getOffset() + paginatedList.getLimit());
        ScoreDoc[] docs = topDocs.scoreDocs;
        paginatedList.setResultCount(topDocs.totalHits);
        
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
        for (int i = paginatedList.getOffset(); i < docs.length; i++) {
            String id = searcher.doc(docs[i].doc).get("id");
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
     * @param Article
     * @return Document
     */
    private org.apache.lucene.document.Document getDocumentFromArticle(Article article) {
        // Index character offsets for the highlighter
        FieldType fieldType = new FieldType(TextField.TYPE_STORED);
        fieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
        
        // Building document
        org.apache.lucene.document.Document document = new org.apache.lucene.document.Document();
        document.add(new StringField("id", article.getId(), Field.Store.YES));
        document.add(new StringField("feed_id", article.getFeedId(), Field.Store.YES));
        document.add(new Field("title", article.getTitle(), fieldType));
        document.add(new Field("description", article.getDescription(), fieldType));
        
        return document;
    }
}
