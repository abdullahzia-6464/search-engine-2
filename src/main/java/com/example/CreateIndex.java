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

        Analyzer analyzer = new StandardAnalyzer();
        if (analyzerType.equalsIgnoreCase("whitespace")) {
            analyzer = new WhitespaceAnalyzer();
        } else if (analyzerType.equalsIgnoreCase("english")) {
            analyzer = new EnglishAnalyzer();
        }

        String indexDirectory = "./" + analyzerType + "_index";
        Directory directory = FSDirectory.open(Paths.get(indexDirectory));

        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        IndexWriter iwriter = new IndexWriter(directory, config);

        // List to hold all document parsers
        List<DocumentParser> parsers = new ArrayList<>();
        parsers.add(new FtParse());
        parsers.add(new FrParse());
        // parsers.add(new AnotherDocParser());  // Add other parsers as needed

        List<Document> documents = new ArrayList<>();

        // Iterate over each parser to parse and index documents
        for (DocumentParser parser : parsers) {
            List<ParsedDoc> parsedDocs = parser.parse();

            for (ParsedDoc parsedDoc : parsedDocs) {
                Document doc = new Document();
                doc.add(new StringField("docNo", parsedDoc.getDocNo(), Field.Store.YES));
                doc.add(new TextField("headline", parsedDoc.getHeadline(), Field.Store.YES));
                doc.add(new TextField("textBody", parsedDoc.getTextBody(), Field.Store.YES));

                documents.add(doc);
            }
        }

        iwriter.addDocuments(documents);

        iwriter.close();
        directory.close();

        System.out.println("Indexing completed for analyzer: " + analyzerType);
    }
}
