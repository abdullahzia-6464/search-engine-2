package com.example;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class FrParse implements DocumentParser {

    @Override
    public List<ParsedDoc> parse() throws IOException {
        List<ParsedDoc> parsedDocs = new ArrayList<>();

        // Directory containing Federal Register files
        File[] file = new File(Constants.DOCS_FILE_PATH + "/fr94").listFiles();
        ArrayList<String> filePaths = new ArrayList<>();

        // Traverse all subdirectories and add file paths
        for (File files : file) {
            if (files.isDirectory()) {
                for (File f : files.listFiles()) {
                    filePaths.add(f.getAbsolutePath());
                }
            }
        }

        // Parse each file in the filePaths list
        for (String filePath : filePaths) {
            try {
                File input = new File(filePath);
                Document doc = Jsoup.parse(input, "UTF-8", "");
                doc.select("docid").remove();

                Elements documents = doc.select("DOC");

                for (Element document : documents) {
                    // Extract fields from each <DOC> element
                    String docNo = document.select("DOCNO").text().trim();
                    String parentId = document.select("PARENT").text().trim();
                    String textContent = document.select("TEXT").text();

                    // Create ParsedDoc object and add it to the list
                    ParsedDoc parsedDoc = new ParsedDoc(docNo, parentId, textContent);
                    parsedDocs.add(parsedDoc);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return parsedDocs;
    }
}
