package com.example;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class LaParse implements DocumentParser {

    private static int BATCH_SIZE = 100;

    @Override
    public void parse(IndexWriter iwriter) throws IOException {
        File[] files = new File(Constants.DOCS_FILE_PATH + "/latimes").listFiles();
        if (files == null) {
            throw new IOException("Directory not found or empty: " + Constants.DOCS_FILE_PATH + "/latimes");
        }
  
        System.out.println("Parsing LA times");
        ArrayList<Document> documents = new ArrayList<>();
        for (File file : files) {
            try {
                org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(file, "UTF-8", "");
                jsoupDoc.select("docid").remove();

                Elements docs = jsoupDoc.select("doc");

                for (Element e : docs) {
                    String docNo = e.getElementsByTag("DOCNO").text();
                    String headline = e.getElementsByTag("HEADLINE").text();
                    String textBody = e.getElementsByTag("TEXT").text();

                    Document luceneDoc = new Document();
                    luceneDoc.add(new StringField("docNo", docNo, Field.Store.YES));
                    luceneDoc.add(new TextField("headline", headline, Field.Store.YES));
                    luceneDoc.add(new TextField("textBody", textBody, Field.Store.YES));

                    documents.add(luceneDoc);

                    // batch processing
                    if (documents.size() >= BATCH_SIZE) {
                        iwriter.addDocuments(documents);
                        iwriter.commit(); 
                        documents.clear(); // clear list for the next batch
                        System.out.println("Indexed batch of " + BATCH_SIZE + " documents.");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // index remaining documents 
        if (!documents.isEmpty()) {
            iwriter.addDocuments(documents);
            iwriter.commit();
            System.out.println("Indexed remaining " + documents.size() + " documents.");
        }
    }
}
