# A9 — Design Pattern Identification and Evolution in Shopizer

**Course:** 23CSE455 Design Patterns Case Study  
**Application:** [Shopizer](https://github.com/shopizer-ecommerce/shopizer), an open-source Spring Boot e-commerce application

## Case Study Overview

This repository documents design-pattern discovery in Shopizer, three software-evolution scenarios, and standalone cross-language implementations of two patterns.

The work covers:
- **Part 1 — Pattern Discovery:** source-backed pattern inventory, pattern counts, architecture, and source evidence.
- **Part 2 — Software Evolution:** Razorpay payment support, category-specific Electronics warranty validation, and order-status SMS notifications.
- **Part 3 — Cross-Language Analysis:** Strategy and Observer implementations in Java, Python, JavaScript, and C++.

## Repository Structure

```text
A9/
├── README.md
├── data/
│   ├── code/                       Original Shopizer source (Git submodule)
│   └── test_result/                Test logs, commands, and test specification
├── Part1/
│   ├── patterns.csv
│   ├── Pattern_count.csv
│   ├── pattern_architecture/       Diagram source and image
│   ├── Part1_prompt.txt
│   └── llm.csv
├── Part2/
│   ├── code/                       Evolved Shopizer source
│   ├── changes/
│   │   ├── Change1/                Razorpay payment support
│   │   ├── Change2/                Electronics warranty validation
│   │   └── Change3/                Order-status SMS notification
│   ├── pattern_evolution.csv
│   ├── Pattern_count.csv
│   ├── diagrams/
│   ├── Part2_prompt.txt
│   └── llm.csv
└── Part3/
    ├── Strategy/                   Payment Module Selection
    │   ├── java/
    │   ├── python/
    │   ├── javascript/
    │   └── cpp/
    ├── Observer/                   Order Status SMS Notification
    │   ├── java/
    │   ├── python/
    │   ├── javascript/
    │   └── cpp/
    └── llm/
        ├── part3_prompt.txt
        ├── llm.csv
        └── pattern_crosslanguage.csv
```

Each language folder contains its implementation, test runner, and README with execution instructions. UML and flow diagrams are stored with the corresponding pattern or evolution evidence. The canonical cross-language comparison workbook is included under `Part3/llm/`.

## Original and Evolved Source

The original Shopizer source is linked as a Git submodule in `data/code/`, pinned to commit `6a4a0a65a3408ee8f62597b51d1b3aac24b77dee` from the [Shopizer repository](https://github.com/shopizer-ecommerce/shopizer).

The evolved source snapshot is under `Part2/code/`. The three change directories describe the requirement and implementation, provide a file-level diff summary, and record focused test results.

Clone the repository with the original source submodule initialized:

```bash
git clone --recurse-submodules https://github.com/jithin23042/A9.git
```

If the repository was already cloned without submodules, initialize it with:

```bash
git submodule update --init --recursive
```

## Verification Summary

- **Part 3:** eight standalone test logs report 10/10 passing tests each (80/80 total).
- **Part 2 focused tests:** 26 passed, 0 failures, 0 errors, and 0 skipped.
- **Broader Maven tests:** the recorded baseline and evolved-source runs each retain one `UnexpectedRollbackException` in the existing `ProductTest.testCreateProduct` / `testInsertImage` path. See `data/test_result/Part2_test_results.md`; these broader runs are not reported as full-suite passes.
- **Runtime compatibility:** the project targets Java 11, while the recorded Part 2 tests used JDK 25 because JDK 11 was unavailable. Java 11 runtime compatibility was not verified.
- **External services:** Razorpay and SMS behaviors are tested using mocks or simulations. No live payment or actual SMS delivery was performed.
- **Cyclomatic complexity:** `max_function_cc` is blank because a comparable complexity measurement was not completed; a blank value does not mean zero.
- **LLM provenance:** historical raw model outputs that were not preserved are not recreated or presented as original transcripts.

## Key Data Files

- `Part1/patterns.csv` — source-backed pattern inventory.
- `Part1/Pattern_count.csv` — pattern counts for discovery.
- `Part2/pattern_evolution.csv` — pattern-evolution records across the changes.
- `Part2/Pattern_count.csv` — counts associated with the evolution analysis.
- `Part3/llm/pattern_crosslanguage.csv` — cross-language implementation and measurement dataset.
- `Part3/llm/cross_language_comparison.xlsx` — workbook for reviewing the comparison data.
