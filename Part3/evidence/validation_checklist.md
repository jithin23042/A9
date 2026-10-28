# Validation checklist

Status meanings: **Pass** = checked against the supplied artifacts/current execution; **Fail** = known noncompliance; **Not measured** = evidence unavailable or measurement not performed.

| Requirement | Status | Evidence / qualification |
|---|---|---|
| CSV exact headers and order | **Pass** | All five required CSVs use the exact required header names and order. |
| CSV UTF-8 and one header row | **Pass** | Validated UTF-8 decoding and CSV parsing; one header row per file. |
| patterns.csv collaboration granularity | **Pass** | One row per confirmed collaboration in Original code and Modified Part 2 snapshots. |
| patterns.csv source evidence paths | **Pass** | Referenced Java/XML files copied into data/code/shopizer-3.2.7 and Part2/code; source paths are team-relative. |
| pattern_counts.csv summaries | **Pass** | Contains per_pattern, total_pattern_types and total_pattern_instances; totals generated from evolution records. |
| patterns_evolution.csv scenarios | **Pass** | Contains baseline and Change1/Change2/Change3 records, status values from the permitted vocabulary, and three scenario descriptions. |
| Removed-instance handling | **Pass** | No removed pattern collaboration was evidenced in the supplied snapshots; no unsupported removed row was invented. |
| cross_language.csv required rows | **Pass** | Eight rows: Strategy and Observer for Java, Python, JavaScript and C++. |
| cross_language.csv production LOC | **Pass** | LOC measured from the named production file with the documented comment-only-line rule; tests excluded. |
| Cross-language actual tests | **Pass** | Individual logs under `Part3/evidence/test_*.log` report 10/10 passed for Strategy/Java, Strategy/Python, Strategy/JavaScript, Strategy/C++, Observer/Java, Observer/Python, Observer/JavaScript and Observer/C++. The earlier 0/10 summary was a checklist transcription error. |
| cross_language.csv complexity | **Not measured** | max_function_cc is blank because a validated common language-aware complexity tool was not available; blank is not zero. |
| cross_language.csv timing protocol | **Pass** | Compile where applicable, one warm-up, five measured process runs, median reported; raw runs preserved. |
| llm.csv truthful provenance | **Pass** | Added relative paths for the current revision prompt and output summary. Historical prompt/output records remain unavailable and are not fabricated. |
| Excel synchronization | **Pass** | Workbook sheets are generated from corrected CSVs; shared count metrics cross-check against pattern_counts.csv. |
| UML editable sources and renderings | **Pass** | Existing .mmd and .svg files preserved; diagrams are labeled as standalone Card/UPI implementations, not Shopizer Razorpay. |
| Before/after payment provider claim | **Pass** | PayPalRestPayment used; provider classes 7→8 separated from Strategy participant count 8→9. |
| SMS transaction-commit claim | **Pass** | Analysis only claims history-call-before-publication ordering; no post-commit guarantee. |
| Intermediate Change1/Change2 snapshots | **Not measured** | Only baseline and final modified ZIPs supplied; per-change records are scenario-ledger snapshots and cannot be independently rechecked against intermediate source archives. |
| Exact source commit hashes and access date | **Not measured** | Not included in supplied archives; not fabricated. |
| Report PDF, LaTeX source, team roster | **Not measured** | Instructor template requires these team-submission artifacts, but roster details and a final report source were not supplied in this task. |
| patterns.csv header schema | **Pass** | Expected exact ordered columns; found ['source_version', 'pattern_name', 'pattern_type', 'instance_name', 'participant_roles', 'source_locations', 'intent_and_evidence']. |
| patterns.csv UTF-8/CSV parsing | **Pass** | Parsed 13 data rows with one header row. |
| pattern_counts.csv header schema | **Pass** | Expected exact ordered columns; found ['source_version', 'summary', 'pattern_name', 'count']. |
| pattern_counts.csv UTF-8/CSV parsing | **Pass** | Parsed 28 data rows with one header row. |
| patterns_evolution.csv header schema | **Pass** | Expected exact ordered columns; found ['source_version', 'pattern_name', 'pattern_type', 'instance_name', 'participant_roles', 'source_locations', 'intent_and_evidence', 'status_after_change', 'change_description']. |
| patterns_evolution.csv UTF-8/CSV parsing | **Pass** | Parsed 25 data rows with one header row. |
| cross_language.csv header schema | **Pass** | Expected exact ordered columns; found ['pattern_name', 'instance_name', 'language', 'snippet_path', 'source_loc', 'max_function_cc', 'runtime_compiler', 'test_command', 'test_result', 'median_process_ms', 'language_notes']. |
| cross_language.csv UTF-8/CSV parsing | **Pass** | Parsed 8 data rows with one header row. |
| llm.csv header schema | **Pass** | Expected exact ordered columns; found ['part', 'model_and_date', 'prompt_file', 'input_file', 'output_file', 'evaluation', 'verification_file']. |
| llm.csv UTF-8/CSV parsing | **Pass** | Parsed 1 data rows with one header row. |
| patterns.csv source path resolution | **Pass** | All referenced source files exist relative to team folder. |
| patterns_evolution.csv source path resolution | **Pass** | All referenced source files exist relative to team folder. |
| cross_language snippet paths | **Pass** | All eight production snippet paths resolve. |
| Evolution status vocabulary | **Pass** | All statuses use permitted values. |
| Original code totals | **Pass** | Derived types=5, instances=6; CSV totals types=5, instances=6. |
| After Change1 totals | **Pass** | Derived types=5, instances=6; CSV totals types=5, instances=6. |
| After Change2 totals | **Pass** | Derived types=5, instances=6; CSV totals types=5, instances=6. |
| After Change3 totals | **Pass** | Derived types=5, instances=7; CSV totals types=5, instances=7. |
| Eight current test suites | **Pass** | All eight suites passed 10/10 in the current environment. |
| Workbook synchronized sheets | **Pass** | Sheets: ['Summary', 'Pattern Counts', 'Cross Language', 'Pattern Records', 'Evolution', 'Participant Metrics', 'Validation'] |

| Standalone Strategy UML excludes Shopizer Razorpay | **Pass** | Editable Mermaid and SVG now show only standalone Card/UPI participants; Shopizer Razorpay is described separately. |
| Required artifacts present | **Pass** | Missing: none. |
| Workbook pattern counts match CSV | **Pass** | Pattern Counts worksheet was regenerated from the final `data/pattern_counts.csv` bytes. |

| Cyclomatic complexity tool attempt | **Not measured** | Attempted to install `lizard`, a standard complexity tool, but package installation failed because network access was unavailable. `max_function_cc` remains blank; this requirement is outstanding. |

## Follow-up validation (2026-10-14)

| Check | Status | Evidence |
|---|---|---|
| patterns.csv header and UTF-8 parse | **Pass** | 13 data rows; headers=['source_version', 'pattern_name', 'pattern_type', 'instance_name', 'participant_roles', 'source_locations', 'intent_and_evidence'] |
| pattern_counts.csv header and UTF-8 parse | **Pass** | 28 data rows; headers=['source_version', 'summary', 'pattern_name', 'count'] |
| patterns_evolution.csv header and UTF-8 parse | **Pass** | 25 data rows; headers=['source_version', 'pattern_name', 'pattern_type', 'instance_name', 'participant_roles', 'source_locations', 'intent_and_evidence', 'status_after_change', 'change_description'] |
| cross_language.csv header and UTF-8 parse | **Pass** | 8 data rows; headers=['pattern_name', 'instance_name', 'language', 'snippet_path', 'source_loc', 'max_function_cc', 'runtime_compiler', 'test_command', 'test_result', 'median_process_ms', 'language_notes'] |
| llm.csv header and UTF-8 parse | **Pass** | 1 data rows; headers=['part', 'model_and_date', 'prompt_file', 'input_file', 'output_file', 'evaluation', 'verification_file'] |
| llm.csv prompt_file path | **Pass** | Part3/technical_artefacts/current_revision_prompt.txt |
| llm.csv input_file path | **Pass** | Part3/technical_artefacts/README.md |
| llm.csv output_file path | **Pass** | Part3/technical_artefacts/current_revision_output.md |
| llm.csv verification_file path | **Pass** | Part3/evidence/validation_checklist.md |
| Eight individual test logs | **Pass** | [('test_observer_cpp.log', True), ('test_observer_java.log', True), ('test_observer_javascript.log', True), ('test_observer_python.log', True), ('test_strategy_cpp.log', True), ('test_strategy_java.log', True), ('test_strategy_javascript.log', True), ('test_strategy_python.log', True)] |
| Workbook Cross Language sheet matches CSV | **Pass** | Compared all rows and columns. |
| Workbook LLM Records sheet matches CSV | **Pass** | Compared all rows and columns. |
| Maximum function cyclomatic complexity | **Not measured** | lizard install failed due to network unavailable; all max_function_cc cells blank and README/checklist state limitation. |
