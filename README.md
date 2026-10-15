# A9 — 23CSE455 Design Pattern Case Study

## Submission status
This package contains the supporting files and datasets for the A9 team folder. The final combined report is intentionally excluded from this ZIP and must be added separately as `A9_Report.tex` and `A9_Final_Report.pdf`.

## Application
- Application: Shopizer e-commerce application
- Repository: https://github.com/shopizer-ecommerce/shopizer
- Baseline label used in this case study: Shopizer 3.2.7
- Baseline commit recorded for the case study: `6a4a0a6` (verify against the final source checkout before submission)
- Access date recorded in project notes: 2026-10-08
- Part 2 changes: Razorpay payment method, Electronics warranty validation, and order-status SMS notification.
- Part 3 patterns: Strategy (Payment Module Selection) and Observer (Order Status SMS Notification).

## Directory guide
- `data/`: canonical CSV datasets and the cross-language workbook.
- `Part1/code/original_source_evidence/`: selected original-source files used as evidence; paths in pattern datasets are relative to the A9 team folder.
- `Part1/evidence/`: source evidence and Member 2's change log.
- `Part1/pattern_architecture/`: application module architecture diagram.
- `Part1/llm/`: prompt documentation.
- `Part2/code/shopizer-3.2.7/`: modified Shopizer source snapshot. VCS metadata, build outputs, IDE files, generated databases, and compiled binaries were excluded.
- `Part2/changes/`: evidence for the three requested changes.
- `Part2/evidence/`: test summaries and implementation evidence.
- `Part3/Strategy/` and `Part3/Observer/`: standalone implementations, tests, and language-specific READMEs.
- `Part3/test-cases/`: shared test-case/specification material.
- `Part3/evidence/`: test logs, timing data, diagrams, validation checklist, and comparison evidence.
- `Part3/llm/`: current revision prompt/output and the prompt sheet PDF.

## Data files
The canonical datasets are:
- `data/patterns.csv`
- `data/pattern_counts.csv`
- `data/patterns_evolution.csv` (additional evolution dataset)
- `data/cross_language.csv`
- `data/llm.csv`
- `data/cross_language_comparison.xlsx`

Do not duplicate the four required CSV datasets in each part folder.

## Reproduction and tests
Each language folder contains its own README with working directory and test instructions. The supplied test logs report eight suites with 10/10 tests each (80/80 total). Re-run the commands in the language READMEs in the stated working directories to reproduce them.

## Measurement notes and limitations
- Production LOC excludes blank lines, comment-only lines, and tests according to the project's counting method.
- `median_process_ms` is based on five measured runs after compilation and one warm-up; see `Part3/evidence/raw_timing_results.txt` for raw measurements and the language READMEs for execution details.
- `max_function_cc` is intentionally blank because cyclomatic complexity was not measured. Blank values do not mean zero; no complexity result is claimed.
- Payment and SMS implementations are standalone simulations. They do not demonstrate a live Razorpay transaction or real SMS delivery.
- Historical LLM prompts/outputs that were not available are not reconstructed or fabricated.

## Source and version note
The full modified source snapshot is under `Part2/code/shopizer-3.2.7/`. Selected baseline source files are included under `Part1/code/original_source_evidence/`; consult the recorded baseline commit and repository metadata in the supporting evidence for reproducibility.
