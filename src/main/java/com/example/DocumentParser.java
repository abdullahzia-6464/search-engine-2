package com.example;

import org.apache.lucene.index.IndexWriter;
import java.io.IOException;

public interface DocumentParser {

    void parse(IndexWriter iwriter) throws IOException;
}
