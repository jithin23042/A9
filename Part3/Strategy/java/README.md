# Strategy Pattern — Payment Module Selection (Java)

## 1. Overview

This project implements the Strategy design pattern in Java as part of the 23CSE455 Design Pattern Case Study.

The implementation demonstrates how an e-commerce application can support multiple payment providers through a common interface. A payment processor selects a registered strategy by provider key and delegates payment processing to that strategy.

All payment operations are simulated. No real payment gateway, credentials, external service or network connection is required.

## 2. Connection to Shopizer

The implementation is inspired by the confirmed Strategy pattern in Shopizer.

- **Original application:** Shopizer 3.2.7.
- **Original abstraction:** `PaymentModule`.
- **Original context/selector:** `PaymentServiceImpl`.
- **Original concrete strategies:** BeanStreamPayment, BraintreePayment, MoneyOrderPayment, PayPalExpressCheckoutPayment, PayPalRestPayment, Stripe3Payment and StripePayment.
- **Relevant evolution:** Part 2 introduced Razorpay as an additional payment strategy.

This Java implementation is a standalone demonstration of the same design principle. It does not replace the original Shopizer classes or reproduce the complete payment infrastructure.

## 3. Architecture

| Component | Responsibility |
|---|---|
| `PaymentMethod` | Interface defining the common payment contract. |
| `CardPayment` | Concrete strategy for simulated card payments. |
| `UPIPayment` | Concrete strategy for simulated UPI payments. |
| `PaymentProcessor` | Registers, selects and invokes payment strategies. |
| `PaymentResult` | Immutable record representing a payment result. |
| `PaymentStrategy` | Entry-point class containing the demonstration `main()` method. |

`PaymentProcessor` stores strategies in a `HashMap` indexed by normalized provider key. It delegates processing to the selected strategy without provider-specific conditional branches.

## 4. Files

- `PaymentStrategy.java` — production implementation.
- `TestPaymentStrategy.java` — automated test harness.
- `README.md` — implementation documentation and execution instructions.

## 5. Environment

- Language: Java.
- JDK used: Java 26.
- IDE: Visual Studio Code.
- Terminal: Windows PowerShell.
- Testing approach: Custom Java test harness with assertions enabled using `java -ea`.
- External dependencies: None.

## 6. Execution Instructions

Run commands from:

`A9/Part3/Strategy_Payment/java/`

### Compile the implementation

```powershell
javac PaymentStrategy.java
```

### Execute the demonstration

```powershell
java PaymentStrategy
```

Expected output:

```text
Status: SUCCESS
Provider: card
Amount: 100.00
Message: Card payment simulated successfully.
```

### Compile and execute the tests

```powershell
javac PaymentStrategy.java TestPaymentStrategy.java
java -ea TestPaymentStrategy
```

**Recorded test result:** 10 tests passed and 0 failed, according to the terminal output captured during development.

The `-ea` flag enables Java assertions. The test harness also checks exceptions and exits with a nonzero status if any test fails.

## 7. Test Coverage

The automated tests cover:

1. Successful card payment.
2. Successful UPI payment.
3. Case-insensitive provider selection.
4. Registration and use of an additional Razorpay strategy.
5. Rejection of duplicate provider registrations.
6. Rejection of unknown providers.
7. Rejection of zero and negative amounts.
8. Rejection of invalid and non-finite numeric amounts.
9. Rejection of invalid provider names.
10. Rejection of invalid strategy registration.

All payments are simulated.

## 8. Design Pattern Analysis

### Pattern classification

- **Pattern:** Strategy.
- **Category:** Behavioral design pattern.
- **Intent:** Define interchangeable payment implementations behind a common interface and select one at runtime.

### Extensibility

A new provider can implement `PaymentMethod` and register the resulting object with `PaymentProcessor`. The existing selection logic does not need to change.

The test-only `RazorpayPayment` class demonstrates this extension without modifying the production implementation.

### Abstraction

Java's interface mechanism defines the common payment contract. `CardPayment` and `UPIPayment` implement `PaymentMethod`, while `PaymentProcessor` operates on the interface rather than depending directly on concrete strategy classes.

### Complexity

The strategy registry uses `HashMap` lookup. Under typical hash-distribution assumptions, lookup is O(1) on average. This excludes the work performed by the selected payment strategy.

### Monetary representation

The implementation accepts positive finite `double` values. Binary floating-point arithmetic is not an exact representation for decimal monetary values, so this design is intended for pattern demonstration rather than production financial processing.

## 9. Source LOC Measurement

Production LOC was measured from `PaymentStrategy.java` using the shared `count_java_loc.py` script. The separate test file is excluded.

### Counting rule

- Exclude blank lines.
- Exclude comment-only lines, including Java `//` comments and standalone block-comment lines.
- Count all other non-empty production lines, including declarations and lines containing inline comments.
- Exclude `TestPaymentStrategy.java` from production LOC.

### Measured results

| Metric | Measured value |
|---|---:|
| Total physical lines | 131 |
| Production LOC | 101 |

**Measurement command:**

```powershell
python ..\..\count_java_loc.py PaymentStrategy.java
```

**Recorded terminal output:**

```text
File: PaymentStrategy.java
Total physical lines: 131
Production LOC (nonblank, excluding comment-only lines): 101
```

The same counting principles must be applied to the Python, JavaScript and C++ implementations for a fair cross-language comparison.

## 10. Limitations

- Payments are simulated; no real gateway integration is implemented.
- No database, web server or Shopizer runtime is required.
- The implementation demonstrates the Strategy pattern, not the complete Shopizer payment architecture.
- Java `double` is not an exact decimal monetary representation.
- Test results apply to the standalone Java implementation and do not establish the correctness of the original Java application.

## 11. Verification Status

- Demonstration execution: successful.
- Automated tests: 10 passed, 0 failed.
- Java version: 26.
- Production LOC: 101, measured.
- Cross-language comparison: pending.
