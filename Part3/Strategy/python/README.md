# Strategy Pattern — Payment Module Selection

## 1. Overview

This project implements the Strategy design pattern in Python as part of the 23CSE455 Design Pattern Case Study.

The implementation demonstrates how an e-commerce application can support multiple payment providers through a common abstraction. A payment processor selects the appropriate strategy using a registered provider key and delegates payment processing to that strategy.

All payment operations are simulated. No real payment gateway, credentials, external service, or network connection is required.

## 2. Connection to Shopizer

The implementation is inspired by the confirmed Strategy pattern in Shopizer.

- **Original application:** Shopizer, an open-source e-commerce platform.
- **Original abstraction:** `PaymentModule`.
- **Original context/selector:** `PaymentServiceImpl`.
- **Original concrete strategies:** BeanStreamPayment, BraintreePayment, MoneyOrderPayment, PayPalExpressCheckoutPayment, PayPalRestPayment, Stripe3Payment and StripePayment.
- **Relevant evolution:** Part 2 introduced Razorpay as an additional payment strategy.

The Python implementation is a standalone demonstration of the same design principle. It does not replace the original Shopizer classes or reproduce the complete payment infrastructure.

## 3. Architecture

The implementation contains the following participants:

| Component | Responsibility |
|---|---|
| `PaymentStrategy` | Abstract base class defining the common payment contract. |
| `CardPayment` | Concrete strategy for simulated card payments. |
| `UPIPayment` | Concrete strategy for simulated UPI payments. |
| `PaymentProcessor` | Registers, selects and invokes payment strategies. |
| `PaymentResult` | Immutable data object representing a payment result. |

The `PaymentProcessor` stores strategies in a dictionary indexed by provider key. It delegates processing to the selected strategy without requiring provider-specific conditional branches.

## 4. Files

- `payment_strategy.py` — production implementation.
- `test_payment_strategy.py` — automated unit tests.
- `strategy_payment_uml.mmd` — editable Mermaid UML class diagram source.
- `count_python_loc.py` — reusable Python LOC measurement script.
- `README.md` — implementation documentation and execution instructions.

## 5. Environment

- Language: Python
- Python version used for testing: 3.14.3
- IDE: Visual Studio Code
- Terminal: Windows PowerShell
- Testing framework: Python standard-library `unittest`
- External dependencies: None

## 6. Execution Instructions

Run the commands from this directory:

`A9/Part3/Strategy_Payment/python/`

### Execute the demonstration

```powershell
python payment_strategy.py
```

Expected output:

```text
Status: SUCCESS
Provider: card
Amount: 100.00
Message: Card payment simulated successfully.
```

### Execute the automated tests

```powershell
python -m unittest -v test_payment_strategy.py
```

**Recorded test result:** 10 tests passed, 0 failures and 0 errors, according to the actual terminal output captured during development.

## 7. Test Coverage

The automated tests cover:

1. Successful card payment.
2. Successful UPI payment.
3. Case-insensitive provider selection.
4. Registration and use of an additional Razorpay strategy.
5. Rejection of duplicate provider registrations.
6. Rejection of unknown providers.
7. Rejection of zero and negative amounts.
8. Rejection of invalid numeric inputs.
9. Rejection of NaN and infinite amounts.
10. Rejection of invalid provider names.

The tests verify simulated behavior, not real payment processing.

## 8. Design Pattern Analysis

### Pattern classification

- **Pattern:** Strategy
- **Category:** Behavioral design pattern
- **Intent:** Define interchangeable payment implementations behind a common contract and allow selection at runtime.

### Extensibility

A new payment provider can be implemented by subclassing `PaymentStrategy` and registering the resulting object with `PaymentProcessor`. The existing processor's selection logic does not need to be modified.

The Razorpay strategy in the test file demonstrates this extension without changing the production implementation.

### Abstraction

Python's `ABC` and `abstractmethod` enforce the required strategy operations. Concrete strategies implement the provider key and payment operation.

### Complexity

The strategy registry uses dictionary lookup for provider selection. Under typical hash-table assumptions, lookup is O(1) on average. This excludes the strategy's own payment-processing work.

The design reduces the need for provider-specific branching in the context. Algorithmic complexity alone does not measure coupling, maintainability or extensibility.

## 9. Source LOC Measurement

Production LOC was measured using `count_python_loc.py` on `payment_strategy.py` only. The separate test file is excluded.

**Counting rule:**

- Exclude blank lines.
- Exclude comment-only lines beginning with `#`.
- Count all other non-empty lines, including imports, decorators, declarations, executable statements, docstrings and lines containing inline comments.
- Exclude the separate test file from production LOC.

### Measured results

| Metric | Measured value |
|---|---:|
| Total physical lines | 124 |
| Production LOC | 80 |

**Measurement command:**

```powershell
python count_python_loc.py payment_strategy.py
```

**Recorded terminal output:**

```text
File: payment_strategy.py
Total physical lines: 124
Production LOC (nonblank, excluding comment-only lines): 80
```

The LOC measurement was executed in the VS Code PowerShell terminal. The same counting rule must be applied to the Java, JavaScript and C++ implementations to support a fair cross-language comparison.

## 10. Limitations

- Payments are simulated; no gateway integration is implemented.
- No database, web server or Shopizer runtime is required.
- The example demonstrates the Strategy pattern, not the complete Shopizer payment architecture.
- Test results apply to the standalone Python implementation and do not establish the correctness of the original Java application.
- The LOC measurement applies only to the current Python production file and is not directly comparable with other languages until their implementations are measured using the same rule.

## 11. Verification Status

- Demonstration execution: successful.
- Automated tests: 10 passed, 0 failures, 0 errors.
- Python version: 3.14.3.
- Production LOC: 80, measured.
- UML source: included as `strategy_payment_uml.mmd`.
- Real payment processing: not implemented.
- Cross-language comparison: pending.
