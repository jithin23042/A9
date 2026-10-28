# Part 2 — Software Evolution

The modified Shopizer source snapshot is under `code/shopizer-3.2.7/`. Each requested change has its requirement/implementation description in `changes/Change1/`, `Change2/`, or `Change3/`, plus a `diff.md` file-level change summary and a `test_result.md` with the recorded command/results. The full Part 2 test summary and broader-suite limitations remain in `evidence/test_results.md`.

- Change 1: Razorpay payment module — Strategy pattern extended.
- Change 2: Electronics category warranty validation — category-specific business validation; not labelled as a GoF pattern.
- Change 3: order-status SMS — status transition publishes an event consumed by an Observer listener.
- `pattern_evolution.csv` and `Pattern_count.csv` are copies of the corresponding canonical datasets and Part 2 evidence counts.
- Prompt provenance is in `Part2_prompt.txt` and `llm.csv`; historical raw outputs were not reconstructed.
