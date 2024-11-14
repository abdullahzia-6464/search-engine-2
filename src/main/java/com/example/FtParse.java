package com.example;

import java.io.*;
import java.util.ArrayList;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class FtParse implements DocumentParser {

    private static int BATCH_SIZE = 100;

    @Override
    public void parse(IndexWriter iwriter) throws IOException {
        File[] files = new File(Constants.DOCS_FILE_PATH + "/ft").listFiles();
        if (files == null) {
            throw new IOException("Directory not found or empty: " + Constants.DOCS_FILE_PATH + "/ft");
        }

        ArrayList<String> filesList = new ArrayList<>();
        for (File file : files) {
            if (file.isDirectory()) {
                for (File f : file.listFiles()) {
                    filesList.add(f.getAbsolutePath());
                }
            }
        }

        System.out.println("Parsing Financial Times");
        ArrayList<Document> documents = new ArrayList<>();

        for (String filePath : filesList) {
            try {
                File inputFile = new File(filePath);
                org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(inputFile, "UTF-8", "");
                jsoupDoc.select("docid").remove();

                Elements docs = jsoupDoc.select("doc");
                for (Element e : docs) {
                    String docNo = e.getElementsByTag("DOCNO").text();
                    String headline = e.getElementsByTag("HEADLINE").text();
                    String textBody = e.getElementsByTag("TEXT").text();

                    // Create a Lucene Document
                    Document luceneDoc = new Document();
                    luceneDoc.add(new StringField("docNo", docNo, Field.Store.YES));
                    luceneDoc.add(new TextField("headline", headline, Field.Store.YES));
                    luceneDoc.add(new TextField("textBody", textBody, Field.Store.YES));

                    // Add to batch
                    documents.add(luceneDoc);

                    // Check if batch size is reached
                    if (documents.size() >= BATCH_SIZE) {
                        iwriter.addDocuments(documents);
                        iwriter.commit();
                        documents.clear();  // Clear the batch after indexing
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
            System.out.println("Indexed final batch of " + documents.size() + " documents.");
        }
    }
}
