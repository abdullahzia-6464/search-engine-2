package com.example;

import org.apache.lucene.index.IndexWriter;
import java.io.IOException;

public interface DocumentParser {

    /**
     * Parses documents and adds them to the provided IndexWriter in batches.
     * @param iwriter the IndexWriter used to index documents
     * @throws IOException if an I/O error occurs during parsing or indexing
     */
    void parse(IndexWriter iwriter) throws IOException;
}
