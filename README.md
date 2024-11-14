# How to Run the Project

### Step 1: Index the Cranfield Dataset

To create the index, use the `CreateIndex` class with the desired analyzer. The available analyzers are `standard`, `whitespace`, and `english`.

```bash
mvn exec:java -Dexec.mainClass="com.example.CreateIndex" -Dexec.args="english"
```
Replace `english` with `whitespace` or `standard` depending on the desired analyzer.


### Step 2: Query the Index
- You can query the index using different similarity models (vsm, bm25, boolean, lmd) and analyzers (standard, whitespace, english). You can run the query in batch mode (for running all queries) or interactive mode (for manual input).

```bash
mvn exec:java -Dexec.mainClass="com.example.QueryIndex" -Dexec.args="batch vsm english"
```
**Optional Modifications**
- Replace `interactive` with `batch` for batch mode. 
- Replace `vsm` with `bm25`, `boolean`, or `lmd` for the similarity model.
- Replace `english` with `whitespace` or `standard` for the analyzer.

### Step 3: Running trec_eval
Once the results are converted, you can evaluate the performance using TREC Eval. From within the `trec_eval` folder, use the following command:
```bash
./trec_eval <qrels_file> <results_file>
```
Replace `<qrels_file>` with the path to the qrels file.
Replace `<results_file>` with the path to the results file.

For example:
```bash
trec_eval-9.0.7/trec_eval qrels.assignment2.part1 results/english_vsm_results.txt
```