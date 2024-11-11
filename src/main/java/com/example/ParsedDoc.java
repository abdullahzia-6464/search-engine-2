package com.example;

public class ParsedDoc {
    private String docNo;
    private String headline;
    private String textBody;

    public ParsedDoc(String docNo, String headline, String textBody) {
        this.docNo = docNo;
        this.headline = headline;
        this.textBody = textBody;
    }

    public String getDocNo() {
        return docNo;
    }

    public String getHeadline() {
        return headline;
    }

    public String getTextBody() {
        return textBody;
    }
}
