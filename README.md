# How to Run the Project

### Step 1: Place the docs in the project directory

To parse the documents and create the index, we must first place the documents in the correct location, i.e.: In a subdirectory `docs`

### Step 2: Index the Cranfield Dataset

To create the index, use the `CreateIndex` class with the desired analyzer. The available analyzers are `standard`, `whitespace`, and `english`.

```bash
mvn exec:java -Dexec.mainClass="com.example.CreateIndex" -Dexec.args="english"
```
Replace `english` with `whitespace` or `standard` depending on the desired analyzer.


### Step 3: Query the Index
- You can query the index using different similarity models (vsm, bm25, boolean, lmd) and analyzers (standard, whitespace, english). You can run the query in batch mode (for running all queries) or interactive mode (for manual input).

```bash
mvn exec:java -Dexec.mainClass="com.example.QueryIndex" -Dexec.args="interactive vsm english"
```

- Replace `interactive` with `batch` for batch mode. **(TO BE IMPLEMENTED)**
- Replace `vsm` with `bm25`, `boolean`, or `lmd` for the similarity model.
- Replace `english` with `whitespace` or `standard` for the analyzer.
