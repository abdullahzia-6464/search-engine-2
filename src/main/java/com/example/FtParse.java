package com.example;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

// Define FtDoc class to hold parsed document data
class FtDoc {
    private String docNo;
    private String textBody;
    private String headline;

    public FtDoc(String docNo, String textBody, String headline) {
        this.docNo = docNo;
        this.textBody = textBody;
        this.headline = headline;
    }

    public String getDocNo() {
        return docNo;
    }

    public String getTextBody() {
        return textBody;
    }

    public String getHeadline() {
        return headline;
    }

    @Override
    public String toString() {
        return "DocNo: " + docNo + "\nHeadline:\n" + headline + "\nTextBody:\n" + textBody + "\n";
    }
}

public class FtParse {
    public static List<FtDoc> parse_ft() throws IOException {

        List<FtDoc> parsedDocs = new ArrayList<>(); // List to store FtDoc objects

        // Locate files for parsing
        File[] file = new File(Constants.DOCS_FILE_PATH + "/ft").listFiles();
        ArrayList<String> files1 = new ArrayList<>();

        // Collect all file paths within subdirectories
        for (File files : file) {
            if (files.isDirectory()) {
                for (File f : files.listFiles()) {
                    files1.add(f.getAbsolutePath());
                }
            }
        }

        // Parse each document and save it as an FtDoc object
        for (String f : files1) {
            try {
                File input = new File(f);
                Document doc = Jsoup.parse(input, "UTF-8", "");
                // doc.select("docid").remove(); // Remove unnecessary tags

                Elements docs = doc.select("doc");

                // Extract information for each <doc> element
                for (Element d : docs) {
                    String docNo = d.getElementsByTag("DOCNO").text();
                    String textBody = d.getElementsByTag("TEXT").text();
                    String headline = d.getElementsByTag("HEADLINE").text();

                    FtDoc parsedDoc = new FtDoc(docNo, textBody, headline);
                    parsedDocs.add(parsedDoc); // Add FtDoc to the list
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Example output of parsed documents
        // int c = 0;
        // for (FtDoc doc : parsedDocs) {
        //     System.out.println(doc);
        //     System.out.println();
        //     c++;
        //     if(c > 4) break;
        // }

        // System.out.println("Docs parsed: " + parsedDocs.size());

        return parsedDocs;
    }
}
