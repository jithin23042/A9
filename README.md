# A9 — 23CSE455 Design Pattern Case Study

## Submission status

This repository is being organized to match the required A9 submission layout. The combined report source and PDF are **pending** because the report has not been finalized. The required Part 3 slide PDF (`Part3/llm/A9_slide.pdf`) was not present in the supplied repository or ZIPs and is also pending. These files have not been fabricated.

## Application and scope

- Application: [Shopizer e-commerce application](https://github.com/shopizer-ecommerce/shopizer)
- Baseline source: Shopizer 3.2.7, pinned by the submodule in `data/code/shopizer-3.2.7` to commit `6a4a0a65a3408ee8f62597b51d1b3aac24b77dee`.
- Part 2 changes: Razorpay payment support, category-specific Electronics warranty validation, and order-status SMS notification events.
- Part 3 patterns: Strategy (Payment Module Selection) and Observer (Order Status SMS Notification), each implemented in Java, Python, JavaScript, and C++.

## Repository guide

- `data/code/shopizer-3.2.7/`: original Shopizer baseline as a Git submodule. Clone with `git clone --recurse-submodules` or run `git submodule update --init --recursive`.
- `data/test_result/`: consolidated copies of the Part 2 test summary and Part 3 automated test logs. No screenshots were supplied in the current repository snapshot.
- `data/`: canonical pattern inventory, evolution, cross-language CSVs, and comparison workbook.
- `Part1/`: pattern inventory aliases, pattern counts, architecture diagram source/image, prompts, LLM record, and source evidence.
- `Part2/code/shopizer-3.2.7/`: modified Shopizer source snapshot, including its implementation evidence.
- `Part2/changes/Change1`, `Change2`, and `Change3`: each change's requirement, file-level diff summary, and test result.
- `Part2/diagrams/`: editable diagrams for the payment Strategy extension, Electronics validation flow, and order-status Observer flow.
- `Part3/Strategy/` and `Part3/Observer/`: standalone code, tests, and language-specific instructions for all four languages.
- `Part3/evidence/`: individual test logs, UML sources/renderings, timing results, validation checklist, and before/after analysis.
- `Part3/llm/`: prompt record, current revision prompt/output record, LLM CSV, cross-language CSV alias, and prompt-sheet PDF.

## Verification summary and limitations

- The eight standalone Part 3 test suites report 10/10 passing tests each (80/80 in total); the individual logs are included under `Part3/evidence/` and copied to `data/test_result/`.
- Part 2 focused implementation/regression tests report **26 passed, 0 failures, 0 errors, 0 skipped**.
- The broader same-scope `.\mvnw.cmd -pl sm-core -am test` baseline and after-code runs each retain one `UnexpectedRollbackException` in the existing `ProductTest.testCreateProduct` / `testInsertImage` path. Their outcomes are recorded in `Part2/evidence/test_results.md`; they are not described as full-suite passes.
- The project targets Java 11, but the recorded Part 2 tests used JDK 25 because JDK 11 was unavailable. Runtime compatibility on JDK 11 remains unverified.
- Razorpay and SMS integrations were tested with mocks/simulations; no live payment or actual SMS delivery was performed.
- `max_function_cc` remains blank because a common validated complexity measurement was not completed. Blank does not mean zero.
- Historical LLM outputs that were not preserved are not reconstructed or represented as real model outputs.

## Key files

- Pattern inventory: `data/patterns.csv`
- Pattern counts: `data/pattern_counts.csv`
- Evolution records: `data/patterns_evolution.csv`
- Cross-language metrics: `data/cross_language.csv`
- Workbook: `data/cross_language_comparison.xlsx`
- Part 2 test summary: `Part2/evidence/test_results.md`

## Pending items before final submission

1. Add the finalized `A9_Report.tex` and `A9_Report.pdf`.
2. Add `Part3/llm/A9_slide.pdf` if required by the evaluator.
3. If required by the evaluator, add genuine test screenshots and diagram source/image files not included in the materials provided so far.
