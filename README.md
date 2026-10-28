# A9 — 23CSE455 Design Pattern Case Study

## Submission status

This repository follows the required A9 folder layout. The final combined report is not yet complete, so `A9_Report.tex` and `A9_Report.pdf` are intentionally absent for now. The required Part 3 slide `Part3/llm/A9_slide.pdf` was not included in the supplied materials and remains pending; no placeholder PDF has been fabricated.

## Application

- Application: Shopizer e-commerce application — https://github.com/shopizer-ecommerce/shopizer
- Baseline: Shopizer 3.2.7 at commit `6a4a0a65a3408ee8f62597b51d1b3aac24b77dee`, referenced as the Git submodule `data/code/shopizer-3.2.7/`.
- Part 2 changes: Razorpay payment support, Electronics warranty validation, and order-status SMS notification.
- Part 3 patterns: Strategy — Payment Module Selection; Observer — Order Status SMS Notification. Both patterns have Java, Python, JavaScript, and C++ implementations.

## Directory guide

- `data/code/`: original Shopizer 3.2.7 baseline source. The repository uses a pinned submodule, so clone with `git clone --recurse-submodules https://github.com/jithin23042/A9.git`.
- `data/test_result/`: Part 2 test summary, shared cross-language test specification, individual Part 3 test logs, and execution summary.
- `Part1/`: pattern inventory CSV, pattern count CSV, architecture source/image, prompt record, and LLM tracking CSV.
- `Part2/code/`: evolved Shopizer source snapshot.
- `Part2/changes/Change1/`, `Change2/`, `Change3/`: requirement/implementation description, diff summary, and focused test result for each change.
- `Part2/diagrams/`: editable architecture and flow diagrams for the patterns/evolution.
- `Part3/Strategy/` and `Part3/Observer/`: standalone implementations, test runners, and READMEs in Java, Python, JavaScript, and C++.
- `Part3/llm/`: Part 3 prompt record, LLM CSV, cross-language CSV, and comparison workbook.

## Test status and limitations

- Part 3's eight standalone test logs each report 10/10 passing tests (80/80 total).
- Part 2 focused implementation/regression tests report 26 passed, 0 failures, 0 errors, and 0 skipped.
- The broader baseline and after-code `.\mvnw.cmd -pl sm-core -am test` runs each retain one `UnexpectedRollbackException` in the existing `ProductTest.testCreateProduct` / `testInsertImage` path. The test summary is in `data/test_result/Part2_test_results.md`; these runs are not described as full-suite passes.
- The project targets Java 11, but the recorded Part 2 tests used JDK 25 because JDK 11 was unavailable. Java 11 runtime compatibility remains unverified.
- Razorpay and SMS tests use mocks/simulations; no real payment or SMS delivery was performed.
- `max_function_cc` remains blank because a validated comparable complexity measurement was not completed. Blank does not mean zero.
- Historical raw LLM outputs that were not preserved are not reconstructed.

## Pending before final submission

1. Add the final `A9_Report.tex` and `A9_Report.pdf` when complete.
2. Add `Part3/llm/A9_slide.pdf` if required by the evaluator; it was not in the resources supplied for this organization task.
3. Add genuine test screenshots only if they are required and available. The current submission contains saved test logs, not screenshots.
