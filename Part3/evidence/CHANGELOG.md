# Change Log — Instructor-Schema Compliance Revision

- Added canonical team-level `data/` folder with `patterns.csv`, `pattern_counts.csv`, `patterns_evolution.csv`, `cross_language.csv`, and `llm.csv` using exact instructor headers.
- Rebuilt pattern records around confirmed collaborations and generated counts from the evolution record ledger; Strategy collaborations are counted once per version.
- Added source excerpts/files under `data/code/original/` and `Part2/code/` so pattern participant source paths resolve from the team folder.
- Corrected the before/after analysis to use `PayPalRestPayment`, distinguish 7→8 concrete payment providers from 8→9 Strategy participant convention, and avoid unsupported post-commit SMS claims.
- Regenerated `cross_language_comparison.xlsx` from corrected CSVs and added separate participant-role occurrence / globally unique symbol metrics.
- Reran all eight standalone test suites in the current environment and saved stdout/stderr logs under `Part3/evidence/`.
- Recorded one warm-up plus five process timing runs per language/pattern row; raw measurements are in `Part3/evidence/raw_timing_results.txt`.
- Left `max_function_cc` blank because no validated comparable complexity measurement was performed; README explicitly says blank is not zero.
- Added a validation checklist with Pass/Fail/Not measured statuses.
- Preserved existing standalone implementations and editable/rendered UML assets. Existing Strategy UMLs describe standalone Card/UPI snippets and are not presented as Shopizer's Razorpay implementation.
- Historical LLM prompts/raw outputs, exact Git commits, and intermediate Change1/Change2 archives were not supplied and are explicitly marked unavailable rather than invented.

- Revised Strategy UML source and SVG to remove the Razorpay class from the standalone Card/UPI diagram; documented the Shopizer production Strategy separately.

- Corrected the validation checklist's test summary: all eight individual test logs report 10 passed and 0 failed (10/10); the earlier 0/10 row was a transcription error.
- Added current-revision prompt and output-summary paths to `data/llm.csv`. These are current revision records, not reconstructed historical model outputs; historical records remain unavailable.
- Attempted to install the standard `lizard` complexity tool, but network access prevented installation. `max_function_cc` remains blank and is explicitly marked Not measured; the package does not claim full compliance for complexity.
- Regenerated workbook Cross Language, LLM Records, and Validation sheets from the final CSV/checklist data.
