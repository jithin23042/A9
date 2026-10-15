# 23CSE455 Part 3 Technical Deliverables & Datasets

**Case Study Title:** *Design Pattern Identification and Evolution in a Spring Boot-Based E-Commerce Application*  
**Assigned Outputs:**
- **Output 5:** UML Class Diagrams (Strategy & Observer)
- **Output 7:** Three Evolution Scenarios (Razorpay, Electronics Warranty Validation, Order Status SMS)
- **Output 8:** Before/After Evolution Analysis
- **Output 12:** Excel and CSV Datasets

---

## 1. Directory Inventory & Deliverables Mapping

| Filename | Format | Description / Assignment Mapping |
|---|---|---|
| `class_diagrams.md` | Markdown | **Output 5:** Architectural report detailing Strategy & Observer class diagrams, participant mappings, responsibilities, and 4-language realizations. |
| `strategy_payment_class_diagram.mmd` | Mermaid | **Output 5:** Standalone editable Mermaid source for Strategy Pattern (Payment Module Selection). |
| `strategy_payment_class_diagram.svg` | SVG | **Output 5:** Rendered high-definition vector diagram for Strategy Pattern. |
| `observer_order_sms_class_diagram.mmd` | Mermaid | **Output 5:** Standalone editable Mermaid source for Observer Pattern (Order Status SMS Notification). |
| `observer_order_sms_class_diagram.svg` | SVG | **Output 5:** Rendered high-definition vector diagram for Observer Pattern. |
| `class_diagrams_viewer.html` | HTML | **Output 5:** Interactive standalone web viewer for viewing rendered UML diagrams in any browser. |
| `evolution_before_after_analysis.md` | Markdown | **Outputs 7 & 8:** Complete Before/After analysis for Scenarios 1–3, verified provider lists, separated participant counts, and pattern inventory deltas. |
| `cross_language.csv` | CSV | **Output 12 (Template):** Core 4-language comparison dataset containing LOC, physical lines, test counts, runtimes, and max cyclomatic complexity. |
| `cross_language_comparison.csv` | CSV | **Output 12:** Preserved alias of `cross_language.csv` for backward compatibility with existing links. |
| `pattern_counts.csv` | CSV | **Output 12 (Template):** Empirical participant role occurrences and GoF pattern instance counts before/after Part 2 evolution. |
| `patterns.csv` | CSV | **Output 12 (Template):** Complete GoF pattern inventory and source code mapping for all 7 confirmed Shopizer instances. |
| `llm.csv` | CSV | **Output 12 (Template):** Manifest and cross-reference table mapping LLM prompt stages, models, and human verification records (Person 1 scope). |
| `pattern_loc_breakdown.csv` | CSV | **Output 12:** File-by-file accounting of physical lines, nonblank/non-comment LOC, comments, blanks, tests, and max function complexity. |
| `complexity_comparison.csv` | CSV | **Output 12:** Detailed cyclomatic complexity measurements, decision-point breakdowns, methods, and tools. |
| `language_feature_matrix.csv` | CSV | **Output 12:** Idiomatic cross-language qualitative matrix comparing OOP mechanisms, memory, typing, and safety models. |
| `cross_language_comparison.xlsx` | Excel (.xlsx) | **Output 12:** Multi-tab styled Excel workbook containing 6 formatted sheets corresponding to all CSV datasets, with sums, formulas, and headers. |
| `generate_datasets.py` | Python | Reusable script used to regenerate all CSV files and the styled Excel workbook. |
| `CHANGELOG.md` | Markdown | Comprehensive log of all corrections applied across Outputs 5, 7, 8, and 12. |

---

## 2. Dataset Filenames Alignment (Assignment Template)

The assignment template specifies four canonical dataset filenames for Output 12. These are provided directly in this directory with explicit mappings:

1. **`cross_language.csv`** *(Required)*: Maps directly to the four-language implementation metrics (LOC, test results, cyclomatic complexity, and runtime environments). Also retained under `cross_language_comparison.csv`.
2. **`pattern_counts.csv`** *(Required)*: Sourced from Person 2's empirical evidence (`shopizer-3.2.7 2/evidence/design-patterns-part2/pattern_counts.csv`). Details the baseline vs. evolved role counts (36 $\rightarrow$ 41 concrete roles; 6 $\rightarrow$ 7 confirmed instances).
3. **`patterns.csv`** *(Required)*: Full GoF pattern inventory and source code mapping for all confirmed Shopizer instances (Payment Strategy, Shipping Strategy, Product Indexing Observer, Order Facade, Security Builder, CMS Singleton, and Order SMS Observer).
4. **`llm.csv`** *(Required)*: Structured manifest linking prompt IDs to project stages, LLM models used, verification status, and evidence documents. Person 1 maintains the detailed human verification records and transcripts.

---

## 3. Verified Quantitative Metrics Summary

### 3.1 Four-Language Implementation LOC & Test Metrics (100% Pass: 80/80 Tests)

| Language | Paradigm | Dev Runtime | Verification Runtime | Strategy Prod LOC | Observer Prod LOC | Total Prod LOC | Total Test LOC | Grand Total LOC | Tests Passed | Pass Rate | Verbosity Rank | Max Function CC |
|---|---|---|---|---:|---:|---:|---:|---:|:---:|:---:|:---:|:---:|
| **Python** | Multi-paradigm (Dynamic, Typed) | Python 3.14.3 | Python 3.12.10 (Standard Library) | 80 | 151 | **231** | 216 | **447** | 20/20 | 100.0% | **#1 (Most Compact)** | 7 |
| **C++** | Native RAII / Compiled (C++17) | GCC 13.3.0 / C++17 (WSL 2) | Recorded test suite (GCC 13.3.0) | 98 | 170 | **268** | 145 | **413** | 20/20 | 100.0% | **#2** | 8 |
| **JavaScript** | Event-driven / Dynamic (Node.js) | Node.js 24.13.0 | Node.js v24.15.0 (V8) | 107 | 201 | **308** | 265 | **573** | 20/20 | 100.0% | **#3** | 7 |
| **Java** | Class-based Strict OOP (JVM) | OpenJDK 26 | OpenJDK 25.0.3 LTS (Temurin) | 101 | 234 | **335** | 280 | **615** | 20/20 | 100.0% | **#4 (Most Verbose)** | 6 |

*LOC counting strictly excludes blank lines and comment-only lines using the verified counting scripts (`count_java_loc.py`, `count_js_loc.py`, `count_python_loc.py`).*

### 3.2 Maximum Function Cyclomatic Complexity Comparison

| Language | Component / File | Function / Method with Max Complexity | Max CC ($M = E - N + 2P$) | Measurement Tool & Method |
|---|---|---|:---:|---|
| **Python** | `payment_strategy.py` | `PaymentProcessor.process_payment` | **7** | Python `ast` module decision-point counting (McCabe) |
| **JavaScript** | `payment_strategy.js` | `PaymentProcessor.processPayment` | **7** | Control-flow branch / decision-point analysis (McCabe) |
| **Java** | `OrderStatusObserver.java` | `OrderStatusService.updateStatus` | **6** | Branch / decision-point analysis (McCabe) |
| **C++** | `order_status_observer.cpp` | `OrderStatusService::isAllowed` | **8** | Control-flow switch / decision-point analysis (McCabe) |

---

## 3.3 Pattern-count terminology (review clarification)

The pattern-count dataset separates four measures:
- **Concrete role occurrences:** 36 → 41 across all pattern instances; a shared class can be counted in more than one pattern role.
- **Per-instance symbol sums:** 44 → 50 when each pattern instance's participant symbols are summed; these values are not globally deduplicated.
- **Globally unique concrete participant types:** 34 → 38.
- **Globally unique total source types (FQNs):** 42 → 47 after adding interfaces (7 → 8) and abstract classes (1 → 1) to the unique concrete types. Methods and external framework types are excluded.

For the SMS event flow, the inspected method establishes that status-history recording precedes `publishEvent`; it does not, by itself, prove publication after transaction commit. The before/after analysis uses this qualified wording.

## 4. Shopizer Part 2 Evolution Summary (Outputs 7 & 8)

1. **Scenario 1 (CH01 — Razorpay Payment Integration):**
   - **Baseline Providers (7):** `BeanStreamPayment`, `BraintreePayment`, `MoneyOrderPayment`, `PayPalExpressCheckoutPayment`, `PayPalRestPayment`, `Stripe3Payment`, `StripePayment`. *(Note: PayPalRestPayment is present; WeChatPayment is absent from baseline).*
   - **Modified Providers (8):** All 7 baseline providers + `RazorpayPayment`.
   - **Strategy Participant Distinction:**
     - Concrete payment providers: **7 before $\rightarrow$ 8 after**.
     - Total Strategy participants (including `PaymentServiceImpl` context): **8 before $\rightarrow$ 9 after**.
   - **Classification:** **Extended** (Strategy pattern). Verified via 9/9 passing tests in `RazorpayPaymentTest`.
   - **Boundary:** **No real payment processing** was performed or verified; tests ran against Spring `MockRestServiceServer`.

2. **Scenario 2 (CH02 — Electronics Mandatory Warranty Validation):**
   - Seeded root category `electronics` for default merchant; requires valid brand and 1–120 months warranty.
   - Refactored duplicate predicate into `isWarrantyAttribute()`.
   - **Classification:** **Business Domain Validation (No GoF Pattern Alteration)**.
   - Verified via 7/7 passing tests in `ElectronicsProductValidatorTest` and 2/2 in `ProductNextGenTest`.

3. **Scenario 3 (CH03 — SMS Notification for Order-Status Changes):**
   - Added `OrderStatus.SHIPPED`; modified `OrderFacadeImpl` to publish `OrderStatusChangedEvent` post-persistence.
   - Asynchronous `OrderStatusSmsListener` filters qualifying transitions (`SHIPPED`, `DELIVERED`), deduplicates transition IDs, and delegates to `SmsProvider`.
   - **Classification:** Order Facade **Modified**; introduced **New Instance** of Observer pattern (4 concrete participants, 1 interface, 5 unique symbols).
   - Confirmed GoF pattern instances increased from **6 to 7**, while distinct pattern types remained **5**.
   - Verified via 8/8 passing tests in `OrderStatusSmsListenerTest` and `OrderStatusTransitionTest`.
   - **Boundary:** **No real SMS delivery** was performed or verified; `UnconfiguredSmsProvider` fails safely, and tests used an in-memory mock.

---

## 5. Execution and Verification Instructions

### 5.1 Regenerating Datasets & Excel Workbook
To regenerate all CSV files and `cross_language_comparison.xlsx`:
```powershell
python generate_datasets.py
```

### 5.2 Running Standalone Unit Tests
- **Python:**
  ```powershell
  python ../Strategy_Payment/python/test_payment_strategy.py
  python ../Observer_Order_SMS/python/test_order_status_observer.py
  ```
- **JavaScript:**
  ```powershell
  node ../Strategy_Payment/javascript/test_payment_strategy.js
  node ../Observer_Order_SMS/javascript/test_order_status_observer.js
  ```
- **Java:**
  ```powershell
  cd ../Strategy_Payment/java; javac -d . PaymentStrategy.java TestPaymentStrategy.java; java -ea TestPaymentStrategy
  cd ../Observer_Order_SMS/java; javac -d . OrderStatusObserver.java TestOrderStatusObserver.java; java -ea -cp . A9.Part3.Observer_Order_SMS.java.TestOrderStatusObserver
  ```
