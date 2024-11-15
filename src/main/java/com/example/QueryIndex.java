package com.example;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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

        Directory directory = FSDirectory.open(Paths.get(indexDirectory)); 
        DirectoryReader ireader = DirectoryReader.open(directory);
        IndexSearcher isearcher = new IndexSearcher(ireader);

        // choose analyzer according to argument passed
        Analyzer analyzer = new StandardAnalyzer();
        if(analyzerType.equalsIgnoreCase("whitespace")){
            analyzer = new WhitespaceAnalyzer(); 
        } else if(analyzerType.equalsIgnoreCase("english")){
            analyzer = new EnglishAnalyzer();
        }

        // choose the similarity model
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

        // run relevant search mode
        if ("batch".equalsIgnoreCase(searchMode)) {
            runBatchSearch(analyzer, isearcher, analyzerType, similarityType);
        } else {
            runInteractiveSearch(analyzer, isearcher);
        }

        ireader.close();
        directory.close();
    }

    private static void runInteractiveSearch(Analyzer analyzer, IndexSearcher isearcher) throws IOException, ParseException {
        Scanner scanner = new Scanner(System.in);
        QueryParser parser = new QueryParser("textBody", analyzer);

        System.out.println("Enter your search query (type 'exit' to quit):");

        while (true) {
            System.out.print("> ");
            String searchTerm = scanner.nextLine();

            if (searchTerm.equalsIgnoreCase("exit")) {
                break;
            }

            // parse the query and search the index
            Query query = parser.parse(searchTerm);
            TopDocs topDocs = isearcher.search(query, 10);  

            System.out.println("Found " + topDocs.totalHits + " results.");

            // display results
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

    public static void runBatchSearch(Analyzer analyzer, IndexSearcher isearcher, String analyzerType, String similarityType) throws IOException, ParseException {
        QueryParser parser = new QueryParser("textBody", analyzer);  
        
        String queryFilePath = "./py-script/queries.txt";
        BufferedReader queryReader = new BufferedReader(new FileReader(queryFilePath));
        
        // path for the results file in TREC Eval format
        String resultsPath = "./results/" + analyzerType + "_" + similarityType + "_results.txt";
        BufferedWriter resultsWriter = Files.newBufferedWriter(
            Paths.get(resultsPath), 
            StandardOpenOption.CREATE, 
            StandardOpenOption.TRUNCATE_EXISTING
        );

        String line;
        while ((line = queryReader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue;

            // split query ID and query text 
            String[] parts = line.split("\t", 2);
            int queryId = Integer.parseInt(parts[0]);
            if (queryId > 425) break; // ONLY GENERATE RESULTS FOR FIRST HALF OF QUERIES FOR NOW
            String queryText = parts[1];

            // process queries
            processQuery(queryId, queryText, parser, isearcher, resultsWriter);
        }
        queryReader.close();
        resultsWriter.close();
        System.out.println("Batch search completed. Results written to: " + resultsPath);
    }

    private static void processQuery(int queryId, String queryText, QueryParser parser, IndexSearcher isearcher, BufferedWriter resultsWriter) throws IOException, ParseException {
        // escape query text to handle special characters
        String escapedQueryText = QueryParser.escape(queryText);

        Query query = parser.parse(escapedQueryText);
        TopDocs topDocs = isearcher.search(query, 1000);  // Retrieve top 1000 results

        // Write results in TREC Eval format:
        // <query_id> Q0 <doc_id> <rank> <score> <run_name>
        int rank = 1;
        String prevId = null;
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            Document doc = isearcher.doc(scoreDoc.doc);
            String docId = doc.get("docNo");
            if(docId.equals(prevId)) continue;
            String resultLine = String.format("%d Q0 %s %d %f lucene-v2\n", queryId, docId, rank, scoreDoc.score);
            resultsWriter.write(resultLine);
            prevId = docId;
            rank++;
        }
    }
}
