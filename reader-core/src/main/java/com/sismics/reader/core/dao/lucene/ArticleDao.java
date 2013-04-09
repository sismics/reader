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
import org.apache.lucene.index.IndexWriterConfig;
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
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.sismics.reader.core.dao.jpa.dto.UserArticleDto;
import com.sismics.reader.core.model.context.AppContext;
import com.sismics.reader.core.model.jpa.Article;
import com.sismics.reader.core.util.jpa.PaginatedList;

/**
 * Lucene Article DAO.
 * 
 * @author bgamard
 */
public class ArticleDao {

    /**
     * Add articles to the index.
     * @param articleList
     * @throws IOException
     */
    public void create(List<Article> articleList) throws IOException {
        // Standard analyzer
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_42, new StandardAnalyzer(Version.LUCENE_42));
        
        // Creating index writer
        Directory directory = AppContext.getInstance().getLuceneDirectory();
        IndexWriter indexWriter = new IndexWriter(directory, config);
        
        // Unlock index if needed
        if (IndexWriter.isLocked(directory)) {
            IndexWriter.unlock(directory);
        }
        
        for (Article article : articleList) {
            // Extracting text from HTML
            Document doc = Jsoup.parse(article.getDescription());
            String content = doc.body().text();
            
            // Adding document to Lucene index
            org.apache.lucene.document.Document luceneDocument = new org.apache.lucene.document.Document();
            luceneDocument.add(new StringField("id", article.getId(), Field.Store.YES));
            luceneDocument.add(new StringField("feed_id", article.getFeedId(), Field.Store.YES));
            luceneDocument.add(new TextField("title", article.getTitle(), Field.Store.YES));
            luceneDocument.add(new TextField("description", content, Field.Store.YES));
            indexWriter.addDocument(luceneDocument);
        }
        
        indexWriter.close();
    }

    /**
     * Search articles.
     * @param paginatedList
     * @param feedList
     * @param searchQuery
     * @return List of articles
     * @throws Exception
     */
    public List<String> search(PaginatedList<UserArticleDto> paginatedList, List<String> feedList, String searchQuery) throws Exception {
        // Escape query
        searchQuery = QueryParserUtil.escape(searchQuery);
        
        // Build search query
        StandardQueryParser qpHelper = new StandardQueryParser(new StandardAnalyzer(Version.LUCENE_42));
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
}
