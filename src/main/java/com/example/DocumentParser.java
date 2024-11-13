package com.example;

import java.io.IOException;
import java.util.List;

public interface DocumentParser {
    List<ParsedDoc> parse() throws IOException;
}
