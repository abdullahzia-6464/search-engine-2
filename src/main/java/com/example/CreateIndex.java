package com.example;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
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
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        IndexWriter iwriter = new IndexWriter(directory, config);

        // List to hold all document parsers
        List<DocumentParser> parsers = new ArrayList<>();
        parsers.add(new FtParse());
        //parsers.add(new LaParse()); // Adding LaParse, more can be added later

        // Pass the IndexWriter to each parser
        for (DocumentParser parser : parsers) {
            parser.parse(iwriter);
        }

        iwriter.close();
        directory.close();

        System.out.println("Indexing completed for analyzer: " + analyzerType);
    }
}
