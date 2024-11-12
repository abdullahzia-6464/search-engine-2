package com.example;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.store.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class FederalRegisterIndexer {

    private static final String INDEX_DIRECTORY = "fr94_index";
    private static final String DATA_DIRECTORY = "/home/azureuser/FederalRegister/Mydataset/fr94";  // Update with the actual path on VM

    public static void main(String[] args) throws Exception {
        indexDocuments();
    }

    public static void indexDocuments() throws IOException {
        EnglishAnalyzer analyzer = new EnglishAnalyzer();
        Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(directory, config);

        // Traverse all files in DATA_DIRECTORY and index each document
        Files.walkFileTree(Paths.get(DATA_DIRECTORY), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.toString().endsWith(".0") || file.toString().endsWith(".1") || file.toString().endsWith(".2")) {
                    parseAndIndexFile(file, writer);
                }
                return FileVisitResult.CONTINUE;
            }
        });

        writer.close();
        System.out.println("Indexing completed.");
    }

    private static void parseAndIndexFile(Path file, IndexWriter writer) throws IOException {
        try {
            // Read the file content
            String content = new String(Files.readAllBytes(file));
            // Parse using Jsoup to handle SGML-like structure
            org.jsoup.nodes.Document doc = Jsoup.parse(content, "", org.jsoup.parser.Parser.xmlParser());

            // Extract and process each <DOC> element
            Elements documents = doc.select("DOC"); 
            for (Element document : documents) {
                // Extract <DOCNO> (document ID)
                String docId = document.select("DOCNO").text().trim();
                
                // Extract <PARENT> (parent ID if relevant for relationships)
                String parentId = document.select("PARENT").text().trim();

                // Extract <TEXT> and clean it up
                String rawText = document.select("TEXT").html();
                String cleanedText = cleanTextContent(rawText);

                // Create a new Lucene Document and add fields
                Document luceneDoc = new Document();
                luceneDoc.add(new StringField("docId", docId, Field.Store.YES));
                luceneDoc.add(new StringField("parentId", parentId, Field.Store.YES));
                luceneDoc.add(new TextField("content", cleanedText, Field.Store.YES));

                writer.addDocument(luceneDoc);
                System.out.println("Indexed document ID: " + docId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to clean up text content by removing comments and replacing special symbols
    private static String cleanTextContent(String text) {
        // Replace &blank; with a space
        String cleanedText = text.replaceAll("&blank;", " ");
        
        // Remove any HTML-like comments (e.g., <!-- PJG ITAG ... -->)
        cleanedText = cleanedText.replaceAll("<!--.*?-->", "");
        
        // Remove any extra whitespace
        cleanedText = cleanedText.trim();

        return cleanedText;
    }
}

