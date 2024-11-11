package com.example;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class CreateIndex {

    public static void main(String[] args) throws IOException {
        String analyzerType = args.length > 0 ? args[0] : "standard";  // Default to standard analyzer

        // Initialize appropriate analyzer
        Analyzer analyzer = new StandardAnalyzer();
        if (analyzerType.equalsIgnoreCase("whitespace")) {
            analyzer = new WhitespaceAnalyzer();
        } else if (analyzerType.equalsIgnoreCase("english")) {
            analyzer = new EnglishAnalyzer();
        }

        // Set index directory based on analyzer type
        String indexDirectory = "./" + analyzerType + "_index";
        Directory directory = FSDirectory.open(Paths.get(indexDirectory));

        // Configure the IndexWriter
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        IndexWriter iwriter = new IndexWriter(directory, config);

        // Parse the documents using FtParser and get a list of FtDoc objects
        List<FtDoc> ftDocs = FtParse.parse_ft();

        // Convert FtDoc objects to Lucene Document objects and add to the index
        List<Document> documents = new ArrayList<>();
        for (FtDoc ftDoc : ftDocs) {
            Document doc = new Document();
            doc.add(new StringField("docNo", ftDoc.getDocNo(), Field.Store.YES));          // Document ID
            doc.add(new TextField("headline", ftDoc.getHeadline(), Field.Store.YES));      // Headline
            doc.add(new TextField("textBody", ftDoc.getTextBody(), Field.Store.YES));      // Main text content

            documents.add(doc);
        }

        // Add all documents to the index
        iwriter.addDocuments(documents);

        // Close the writer and directory
        iwriter.close();
        directory.close();

        System.out.println("Indexing completed for analyzer: " + analyzerType);
    }
}
