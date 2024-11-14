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

public class to_checkparsing {

    private static final String INDEX_DIRECTORY = "fr94_index";
    private static final String DATA_DIRECTORY = "/home/azureuser/FederalRegister/Mydataset/fr94";  // Update with the actual path on your VM
    private static final String PARSING_OUTPUT_FILE = "parsing_output.txt";  // Output file for parsed content

    public static void main(String[] args) throws Exception {
        // Set up the BufferedWriter to write parsing output to a file
        try (BufferedWriter outputWriter = new BufferedWriter(new FileWriter(PARSING_OUTPUT_FILE))) {
            indexDocuments(outputWriter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void indexDocuments(BufferedWriter outputWriter) throws IOException {
        EnglishAnalyzer analyzer = new EnglishAnalyzer();
        Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(directory, config);

        // Traverse all files in DATA_DIRECTORY and index each document
        Files.walkFileTree(Paths.get(DATA_DIRECTORY), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.toString().endsWith(".0") || file.toString().endsWith(".1") || file.toString().endsWith(".2")) {
                    outputWriter.write("Processing file: " + file.toString() + "\n");  // Log file path
                    parseAndIndexFile(file, writer, outputWriter);
                }
                return FileVisitResult.CONTINUE;
            }
        });

        writer.close();
        System.out.println("Indexing completed.");
    }

    private static void parseAndIndexFile(Path file, IndexWriter writer, BufferedWriter outputWriter) throws IOException {
        try {
            // Step 1: Read the file content
            String content = new String(Files.readAllBytes(file));
            
            // Write raw file content to output file (for debugging, optional)
            outputWriter.write("Raw file content:\n" + content + "\n----------\n");

            // Step 2: Parse the file content with Jsoup
            org.jsoup.nodes.Document doc = Jsoup.parse(content, "", org.jsoup.parser.Parser.xmlParser());

            // Step 3: Extract each <DOC> element
            Elements documents = doc.select("DOC");
            outputWriter.write("Number of <DOC> elements found: " + documents.size() + "\n");

            // Step 4: Loop over each <DOC> element and extract data
            for (Element document : documents) {
                String docId = document.select("DOCNO").text().trim();
                String parentId = document.select("PARENT").text().trim();
                String rawText = document.select("TEXT").html();
                String cleanedText = cleanTextContent(rawText);

                // Write extracted fields and cleaned content to output file
                outputWriter.write("Parsed Document:\n");
                outputWriter.write("DocID: " + docId + "\n");
                outputWriter.write("ParentID: " + parentId + "\n");
                outputWriter.write("Raw Text Content: " + rawText + "\n");
                outputWriter.write("Cleaned Text Content: " + cleanedText + "\n");
                outputWriter.write("----------\n");

                // Step 5: Index the document
                Document luceneDoc = new Document();
                luceneDoc.add(new StringField("docId", docId, Field.Store.YES));
                luceneDoc.add(new StringField("parentId", parentId, Field.Store.YES));
                luceneDoc.add(new TextField("content", cleanedText, Field.Store.YES));

                writer.addDocument(luceneDoc);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String cleanTextContent(String text) {
        String cleanedText = text.replaceAll("&blank;", " ");
        cleanedText = cleanedText.replaceAll("<!--.*?-->", "");
        cleanedText = cleanedText.trim();
        return cleanedText;
    }
}

