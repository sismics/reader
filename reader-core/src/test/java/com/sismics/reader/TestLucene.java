package com.sismics.reader;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.fr.FrenchAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

/**
 * Lucene testing.
 * 
 * @author bgamard
 */
public class TestLucene {
    @Test
    public void test() throws Exception {
        // Configuring language detection
        DetectorFactory.loadProfile(new File(getClass().getResource("/lucene/profiles").getFile()));
        
        // RAM index for testing
        Directory index = new RAMDirectory();
        // Directory index = new SimpleFSDirectory(new File("/var/lucene"); for production

        // Add some data
        indexData("korben1", "/lucene/korben_data.xml", index);
        indexData("techcrunch1", "/lucene/techcrunch_data.xml", index);
        
        search("préféré", getAnalyzerWrapper(new FrenchAnalyzer(Version.LUCENE_42)), index); // "preférées" in korben data
        search("vidéo chaton", getAnalyzerWrapper(new FrenchAnalyzer(Version.LUCENE_42)), index); // "vidéos" "chatons" in korben data
        search("youtube", getAnalyzerWrapper(null), index); // "YouTube" in korben data
        search("windows", getAnalyzerWrapper(null), index); // "Windows" in techcrunch data
        search("similarity", getAnalyzerWrapper(new EnglishAnalyzer(Version.LUCENE_42)), index); // "similarities" in techcrunch data
    }
    
    public void indexData(String id, String data, Directory index) throws Exception {
        // Reading test data
        String korbenData = Files.toString(new File(getClass().getResource(data).getFile()), Charsets.UTF_8);
        
        // Extracting text from HTML
        Document doc = Jsoup.parse(korbenData);
        String content = doc.body().text();
        
        // Detecting language
        Detector detector = DetectorFactory.create();
        detector.append(content);
        String lang = detector.detect();
        
        // Choose Lucene analyzer according to language
        Analyzer analyzer = null;
        if (lang.equals("fr")) {
            analyzer = new FrenchAnalyzer(Version.LUCENE_42);
        } else if (lang.equals("en")) {
            analyzer = new EnglishAnalyzer(Version.LUCENE_42);
        }
        
        // Specific and standard analyzer for each document
        PerFieldAnalyzerWrapper analyzerWrapper = getAnalyzerWrapper(analyzer);
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_42, analyzerWrapper);
        
        // Add document to Lucene index
        IndexWriter indexWriter = new IndexWriter(index, config);
        org.apache.lucene.document.Document luceneDocument = new org.apache.lucene.document.Document();
        luceneDocument.add(new StringField("id", id, Field.Store.YES));
        luceneDocument.add(new TextField("description_std", content, Field.Store.NO));
        luceneDocument.add(new TextField("description", content, Field.Store.NO));
        indexWriter.addDocument(luceneDocument);
        indexWriter.close();
    }
    
    private PerFieldAnalyzerWrapper getAnalyzerWrapper(Analyzer defaultAnalyzer) {
        Map<String, Analyzer> analyzers = new HashMap<String, Analyzer>();
        Analyzer standardAnalyzer = new StandardAnalyzer(Version.LUCENE_42);
        if (defaultAnalyzer != null) {
            analyzers.put("description", defaultAnalyzer);
        }
        analyzers.put("description_std", standardAnalyzer);
        return new PerFieldAnalyzerWrapper(standardAnalyzer, analyzers);
    }

    public void search(String search, Analyzer analyzer, Directory index) throws Exception {
        // Build search query
        StandardQueryParser qpHelper = new StandardQueryParser(analyzer);
        Query query1 = qpHelper.parse(search, "description");
        Query query2 = qpHelper.parse(search, "description_std");
        BooleanQuery query = new BooleanQuery();
        query.add(query1, Occur.SHOULD);
        query.add(query2, Occur.SHOULD);
        
        // Search
        IndexReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);
        TopDocs topDocs = searcher.search(query, 10);
        ScoreDoc[] docs = topDocs.scoreDocs;
        
        // Displaying results
        System.out.println("results for \"" + search + "\": " + docs.length);
        for (int i = 0; i < docs.length; i++) {
            System.out.println(searcher.doc(docs[i].doc).get("id"));
        }
    }
}
