package com.example;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class FrParse implements DocumentParser {

    private static int BATCH_SIZE = 100;

    @Override
    public void parse(IndexWriter iwriter) throws IOException {
        // Directory containing Federal Register files
        File[] files = new File(Constants.DOCS_FILE_PATH + "/fr94").listFiles();
        if (files == null) {
            throw new IOException("Directory not found or empty: " + Constants.DOCS_FILE_PATH + "/fr94");
        }
        ArrayList<String> filePaths = new ArrayList<>();

        // Traverse all subdirectories and collect file paths
        for (File file : files) {
            if (file.isDirectory()) {
                for (File f : file.listFiles()) {
                    filePaths.add(f.getAbsolutePath());
                }
            }
        }

        System.out.println("Parsing Federal Register documents...");
        ArrayList<Document> luceneDocs = new ArrayList<>();

        // Parse each file in the filePaths list
        for (String filePath : filePaths) {
            try {
                File inputFile = new File(filePath);
                org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(inputFile, "UTF-8", "");
                jsoupDoc.select("docid").remove();

                Elements docs = jsoupDoc.select("DOC");

                for (Element e : docs) {
                    String docNo = e.select("DOCNO").text().trim();
                    String parentId = e.select("PARENT").text().trim();
                    String textBody = e.select("TEXT").text();

                    // Create a Lucene Document and add fields
                    Document luceneDoc = new Document();
                    luceneDoc.add(new StringField("docNo", docNo, Field.Store.YES));
                    luceneDoc.add(new TextField("parentId", parentId, Field.Store.YES));
                    luceneDoc.add(new TextField("textBody", textBody, Field.Store.YES));

                    // Add document to batch
                    luceneDocs.add(luceneDoc);

                    // Check if batch size is reached
                    if (luceneDocs.size() >= BATCH_SIZE) {
                        iwriter.addDocuments(luceneDocs);
                        iwriter.commit(); // Commit after each batch
                        luceneDocs.clear(); // Clear batch for next set of documents
                        System.out.println("Indexed batch of " + BATCH_SIZE + " documents.");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Add any remaining documents in the final batch
        if (!luceneDocs.isEmpty()) {
            iwriter.addDocuments(luceneDocs);
            iwriter.commit();
            System.out.println("Indexed remaining " + luceneDocs.size() + " documents.");
        }
    }
}