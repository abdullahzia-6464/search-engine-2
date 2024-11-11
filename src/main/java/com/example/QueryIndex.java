package com.example;

import java.io.*;
import java.nio.file.Paths;
import java.util.Scanner;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.BooleanSimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class QueryIndex {

    public static void main(String[] args) throws IOException, ParseException {
        String searchMode = args.length > 0 ? args[0] : "interactive";  // "interactive" or "batch"
        String similarityType = args.length > 1 ? args[1] : "vsm";  // "vsm", "bm25", "boolean", "lmd"
        String analyzerType = args.length > 2 ? args[2] : "standard";  // "standard", "english", or "whitespace"
        
        String indexDirectory = "./" + analyzerType + "_index";

        Directory directory = FSDirectory.open(Paths.get(indexDirectory)); // Access index directory
        DirectoryReader ireader = DirectoryReader.open(directory);
        IndexSearcher isearcher = new IndexSearcher(ireader);

        // Choose the relevant analyzer according to argument passed
        Analyzer analyzer = new StandardAnalyzer();
        if(analyzerType.equalsIgnoreCase("whitespace")){
            analyzer = new WhitespaceAnalyzer(); 
        } else if(analyzerType.equalsIgnoreCase("english")){
            analyzer = new EnglishAnalyzer();
        }

        // Set the similarity model for scoring
        if ("bm25".equalsIgnoreCase(similarityType)) {
            isearcher.setSimilarity(new BM25Similarity());
            System.out.println("Using BM25 Similarity for searching");
        } else if ("boolean".equalsIgnoreCase(similarityType)) {
            isearcher.setSimilarity(new BooleanSimilarity());
            System.out.println("Using Boolean Similarity for searching");
        } else if ("lmd".equalsIgnoreCase(similarityType)) {
            isearcher.setSimilarity(new LMDirichletSimilarity());
            System.out.println("Using LMDirichlet Similarity for searching");
        } else {
            isearcher.setSimilarity(new ClassicSimilarity());
            System.out.println("Using Classic (VSM) Similarity for searching");
        }

        // Run relevant search mode
        if ("batch".equalsIgnoreCase(searchMode)) {
            // Placeholder for batch search; implement this later
            runBatchSearch(analyzer, isearcher);
        } else {
            runInteractiveSearch(analyzer, isearcher);
        }

        // Close resources
        ireader.close();
        directory.close();
    }

    private static void runInteractiveSearch(Analyzer analyzer, IndexSearcher isearcher) throws IOException, ParseException {
        Scanner scanner = new Scanner(System.in);
        QueryParser parser = new QueryParser("textBody", analyzer);  // Assuming "textBody" is the main searchable field

        System.out.println("Enter your search query (type 'exit' to quit):");

        while (true) {
            System.out.print("> ");
            String searchTerm = scanner.nextLine();

            if (searchTerm.equalsIgnoreCase("exit")) {
                break;
            }

            // Parse the query and search the index
            Query query = parser.parse(searchTerm);
            TopDocs topDocs = isearcher.search(query, 10);  // Limit to top 10 results

            System.out.println("Found " + topDocs.totalHits + " results.");

            // Display results
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                Document doc = isearcher.doc(scoreDoc.doc);
                System.out.println("DocNo: " + doc.get("docNo"));
                System.out.println("Headline: " + doc.get("headline"));
                System.out.println("Text Body: " + doc.get("textBody"));
                System.out.println("------------------------------");
            }
        }

        scanner.close();
    }

    // Placeholder for batch search, to be implemented later
    public static void runBatchSearch(Analyzer analyzer, IndexSearcher isearcher) throws IOException, ParseException {
        System.out.println("Batch search functionality to be implemented.");
    }
}
