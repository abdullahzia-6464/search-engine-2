package com.example;

import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import java.nio.file.Paths;

public class FederalRegisterQuery {
    public static void main(String[] args) {
        try {
            // Open the index directory
            FSDirectory indexDir = FSDirectory.open(Paths.get("fr94_index"));
            DirectoryReader reader = DirectoryReader.open(indexDir);
            IndexSearcher searcher = new IndexSearcher(reader);
            EnglishAnalyzer analyzer = new EnglishAnalyzer();

            // Create a query parser and search for a sample term
            QueryParser parser = new QueryParser("content", analyzer);
            String searchTerm = "regulation";  // Example search term; replace with relevant terms
            Query query = parser.parse(searchTerm);

            // Execute search
            TopDocs results = searcher.search(query, 10); // Get top 10 results
            System.out.println("Found " + results.totalHits + " hits for query: " + searchTerm);
            for (ScoreDoc scoreDoc : results.scoreDocs) {
                Document doc = searcher.doc(scoreDoc.doc);
                System.out.println("DocID: " + doc.get("docId") + " Score: " + scoreDoc.score);
                System.out.println("Content: " + doc.get("content").substring(0, Math.min(200, doc.get("content").length())) + "...");
                System.out.println("----------");
            }

            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

