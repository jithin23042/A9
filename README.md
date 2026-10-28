# A9 — 23CSE455 Design Pattern Case Study

## Submission status

This repository follows the required A9 directory layout. The final combined report is unfinished; `A9_Report.tex` and `A9_Report.pdf` are intentionally not included yet. The required Part 3 slide `Part3/llm/A9_slide.pdf` was not present in the supplied materials and remains pending. No placeholder report or slide has been fabricated.

## Application

- Application: Shopizer e-commerce application — https://github.com/shopizer-ecommerce/shopizer
- Original source: Shopizer 3.2.7, pinned at commit `6a4a0a65a3408ee8f62597b51d1b3aac24b77dee` as the `data/code/` submodule.
- Part 2 changes: Razorpay payment support, Electronics warranty validation, and order-status SMS notification.
- Part 3 patterns: Strategy (Payment Module Selection) and Observer (Order Status SMS Notification), each implemented in Java, Python, JavaScript, and C++.

## Directory guide

- `data/code/`: original Shopizer source.
- `data/test_result/`: Part 2 test summary, test commands/specification, and the eight Part 3 test logs.
- `Part1/`: pattern inventory/count CSVs, architecture diagram source/image, prompt record, and LLM tracking CSV.
- `Part2/code/`: evolved Shopizer source snapshot.
- `Part2/changes/Change1/`, `Change2/`, `Change3/`: requirement/implementation, diff summary, and test result for each requested change.
- `Part2/pattern_evolution.csv`, `Part2/Pattern_count.csv`, `Part2/diagrams/`, `Part2/Part2_prompt.txt`, and `Part2/llm.csv`: required Part 2 artifacts.
- `Part3/Strategy/` and `Part3/Observer/`: code, test runners, and README files grouped by language.
- `Part3/llm/`: Part 3 prompt, LLM CSV, and cross-language CSV.

## Verification and limits

- The eight supplied Part 3 logs each report 10/10 tests passed (80/80 overall).
- Part 2 focused implementation/regression tests report 26 passed, 0 failures, 0 errors, and 0 skipped.
- The broader baseline and after-code Maven runs each retain one `UnexpectedRollbackException` in the existing `ProductTest.testCreateProduct` / `testInsertImage` path. See `data/test_result/Part2_test_results.md`; these broader runs are not described as full-suite passes.
- The project targets Java 11, but recorded Part 2 tests used JDK 25 because JDK 11 was unavailable. Runtime compatibility on Java 11 remains unverified.
- Razorpay and SMS tests use mocks/simulations; no live payment or actual SMS delivery was performed.
- `max_function_cc` remains blank because a validated comparable complexity measurement was not completed. Blank does not mean zero.
- Historical raw LLM outputs that were not preserved are not reconstructed.

## Pending before final submission

1. Add `A9_Report.tex` and `A9_Report.pdf` when the report is complete.
2. Add `Part3/llm/A9_slide.pdf` if required by the evaluator; it was not supplied for this task.
3. Add genuine test screenshots only if required and available. The current repository contains test logs rather than screenshots.
