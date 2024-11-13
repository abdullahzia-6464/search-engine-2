package com.example;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class FtParse implements DocumentParser {

    @Override
    public List<ParsedDoc> parse() throws IOException {
        List<ParsedDoc> parsedDocs = new ArrayList<>();

        File[] file = new File(Constants.DOCS_FILE_PATH + "/ft").listFiles();
        ArrayList<String> files1 = new ArrayList<>();

        for (File files : file) {
            if (files.isDirectory()) {
                for (File f : files.listFiles()) {
                    files1.add(f.getAbsolutePath());
                }
            }
        }

        for (String f : files1) {
            try {
                File input = new File(f);
                Document doc = Jsoup.parse(input, "UTF-8", "");
                doc.select("docid").remove();

                Elements docs = doc.select("doc");

                for (Element e : docs) {
                    String docNo = e.getElementsByTag("DOCNO").text();
                    String headline = e.getElementsByTag("HEADLINE").text();
                    String textBody = e.getElementsByTag("TEXT").text();

                    ParsedDoc parsedDoc = new ParsedDoc(docNo, headline, textBody);
                    parsedDocs.add(parsedDoc);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return parsedDocs;
    }
}
