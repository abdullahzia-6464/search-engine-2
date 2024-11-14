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
        File[] files = new File(Constants.DOCS_FILE_PATH + "/fr94").listFiles();
        if (files == null) {
            throw new IOException("Directory not found or empty: " + Constants.DOCS_FILE_PATH + "/fr94");
        }
        ArrayList<String> filePaths = new ArrayList<>();

        for (File file : files) {
            if (file.isDirectory()) {
                for (File f : file.listFiles()) {
                    filePaths.add(f.getAbsolutePath());
                }
            }
        }

        System.out.println("Parsing Federal Register documents...");
        ArrayList<Document> documents = new ArrayList<>();

        for (String filePath : filePaths) {
            try {
                File inputFile = new File(filePath);
                org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(inputFile, "UTF-8", "");
                jsoupDoc.select("docid").remove();

                Elements docs = jsoupDoc.select("DOC");

                for (Element e : docs) {
                    String docNo = e.select("DOCNO").text().trim();
                    // String parentId = e.select("PARENT").text().trim();
                    String textBody = e.select("TEXT").text();

                    Document luceneDoc = new Document();
                    luceneDoc.add(new StringField("docNo", docNo, Field.Store.YES));
                    // luceneDoc.add(new TextField("parentId", parentId, Field.Store.YES));
                    luceneDoc.add(new TextField("textBody", textBody, Field.Store.YES));

                    // Add document to batch
                    documents.add(luceneDoc);

                    // If batch size reached, add documents to index
                    if (documents.size() >= BATCH_SIZE) {
                        iwriter.addDocuments(documents);
                        iwriter.commit(); // Commit after each batch
                        documents.clear(); // Clear batch for next set of documents
                        System.out.println("Indexed batch of " + BATCH_SIZE + " documents.");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Add any remaining documents in the final batch
        if (!documents.isEmpty()) {
            iwriter.addDocuments(documents);
            iwriter.commit();
            System.out.println("Indexed remaining " + documents.size() + " documents.");
        }
    }
}