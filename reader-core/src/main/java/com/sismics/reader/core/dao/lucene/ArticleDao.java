package com.sismics.reader.core.dao.lucene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
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
import org.apache.lucene.util.Version;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

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
    public List<String> search(PaginatedList<UserArticleDto> paginatedList, List<String> feedList, String searchQuery) throws Exception {
        // Escape query and add quotes so QueryParser generate a PhraseQuery
        searchQuery = "\"" + QueryParserUtil.escape(searchQuery) + "\"";
        
        // Build search query
        StandardQueryParser qpHelper = new StandardQueryParser(new StandardAnalyzer(Version.LUCENE_42));
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
        
        // Extract article ids
        List<String> articleIdList = new ArrayList<String>();
        for (int i = paginatedList.getOffset(); i < docs.length; i++) {
            articleIdList.add(searcher.doc(docs[i].doc).get("id"));
        }
        
        return articleIdList;
    }
    
    /**
     * Build Lucene document from article.
     * 
     * @param Article
     * @return Document
     */
    private org.apache.lucene.document.Document getDocumentFromArticle(Article article) {
        // Extracting text from HTML
        Document doc = Jsoup.parse(article.getDescription());
        String content = doc.body().text();

        // Building document
        org.apache.lucene.document.Document document = new org.apache.lucene.document.Document();
        document.add(new StringField("id", article.getId(), Field.Store.YES));
        document.add(new StringField("feed_id", article.getFeedId(), Field.Store.YES));
        document.add(new TextField("title", article.getTitle(), Field.Store.YES));
        document.add(new TextField("description", content, Field.Store.YES));
        
        return document;
    }
}
