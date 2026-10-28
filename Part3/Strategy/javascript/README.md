# Strategy Pattern — Payment Module Selection (JavaScript)

## 1. Overview

This project implements the Strategy design pattern in JavaScript as part of the 23CSE455 Design Pattern Case Study.

The implementation demonstrates how an e-commerce application can support multiple payment providers through a common contract. A payment processor selects a registered strategy by provider key and delegates payment processing to that strategy.

All payment operations are simulated. No real payment gateway, credentials, external service or network connection is required.

## 2. Connection to Shopizer

The implementation is inspired by the confirmed Strategy pattern in Shopizer.

- **Original application:** Shopizer 3.2.7.
- **Original abstraction:** `PaymentModule`.
- **Original context/selector:** `PaymentServiceImpl`.
- **Original concrete strategies:** BeanStreamPayment, BraintreePayment, MoneyOrderPayment, PayPalExpressCheckoutPayment, PayPalRestPayment, Stripe3Payment and StripePayment.
- **Relevant evolution:** Part 2 introduced Razorpay as an additional payment strategy.

This JavaScript implementation is a standalone demonstration of the same design principle. It does not replace the original Shopizer classes or reproduce its complete payment infrastructure.

## 3. Architecture

| Component | Responsibility |
|---|---|
| `PaymentStrategy` | Defines the common payment contract. |
| `CardPayment` | Concrete strategy for simulated card payments. |
| `UPIPayment` | Concrete strategy for simulated UPI payments. |
| `PaymentProcessor` | Registers, selects and invokes payment strategies. |
| `PaymentResult` | Represents an immutable payment result. |

`PaymentProcessor` uses a JavaScript `Map` to register strategies by provider key. It delegates processing to the selected strategy without provider-specific conditional branches.

## 4. Files

- `payment_strategy.js` — production implementation.
- `test_payment_strategy.js` — automated tests.
- `README.md` — implementation documentation and execution instructions.

## 5. Environment

- Language: JavaScript (CommonJS).
- Runtime: Node.js 24.13.0.
- Package manager: npm 11.6.2.
- IDE: Visual Studio Code.
- Terminal: Windows PowerShell.
- Testing framework: Node.js built-in test runner and `node:assert/strict`.
- External dependencies: None.

## 6. Execution Instructions

Run commands from:

`A9/Part3/Strategy/javascript/`

### Execute the demonstration

```powershell
node payment_strategy.js
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
node --test test_payment_strategy.js
```

**Recorded test result:** 10 tests passed, 0 failures and 0 skipped tests, according to the captured terminal output.

## 7. Test Coverage

The tests cover:

1. Successful card payment.
2. Successful UPI payment.
3. Case-insensitive provider selection.
4. Registration and use of an additional Razorpay strategy.
5. Rejection of duplicate provider registrations.
6. Rejection of unknown providers.
7. Rejection of zero and negative amounts.
8. Rejection of invalid amount types and non-finite numbers.
9. Rejection of invalid provider names.
10. Rejection of invalid strategy registration.

All payments are simulated.

## 8. Design Pattern Analysis

### Pattern classification

- **Pattern:** Strategy.
- **Category:** Behavioral design pattern.
- **Intent:** Define interchangeable payment implementations behind a common contract and select one at runtime.

### Extensibility

A new provider can extend `PaymentStrategy` and register with `PaymentProcessor`. The existing selection logic does not need to change.

The test-only `RazorpayPayment` class demonstrates this extension without modifying the production implementation.

### Abstraction

JavaScript does not enforce abstract classes natively in the same manner as Python's `ABC`. Here, `PaymentStrategy` uses methods that throw errors when not overridden. Concrete strategies implement the provider key and payment operation.

### Complexity

The strategy registry uses `Map` lookup, which provides average sublinear access according to the JavaScript specification. The precise performance depends on the runtime.

### Monetary representation

This implementation accepts finite positive JavaScript `number` values. Unlike Python's `Decimal`, JavaScript numbers use binary floating-point arithmetic. This example is suitable for demonstrating the pattern, not for production financial calculations.

## 9. Source LOC Measurement

Production LOC was measured from `payment_strategy.js` using the shared `count_js_loc.py` script. The separate test file is excluded.

### Counting rule

- Exclude blank lines.
- Exclude comment-only lines, including JavaScript `//` comments and standalone block-comment lines.
- Count all other non-empty production lines, including declarations and lines containing inline comments.
- Exclude `test_payment_strategy.js` from production LOC.

### Measured Results

| Metric | Measured value |
|---|---:|
| Total physical lines | 141 |
| Production LOC | 107 |

**Measurement command:**

```powershell
python ..\..\count_js_loc.py payment_strategy.js
```

**Recorded terminal output:**

```text
File: ..\Strategy_Payment\javascript\payment_strategy.js
Total physical lines: 141
Production LOC (nonblank, excluding comment-only lines): 107
```

The measurement was obtained using the same JavaScript-specific counting script as the Observer implementation. The same counting principles must be applied to the Python, Java and C++ implementations for a fair cross-language comparison.

## 10. Limitations

- Payments are simulated; no real gateway integration is implemented.
- No database, web server or Shopizer runtime is required.
- The implementation demonstrates the Strategy pattern, not the complete Shopizer payment architecture.
- JavaScript floating-point numbers are not an exact decimal monetary representation.
- Test results apply to this standalone JavaScript implementation and do not establish the correctness of the original Java application.

## 11. Verification Status

- Demonstration execution: successful.
- Automated tests: 10 passed, 0 failures, 0 skipped.
- Node.js version: 24.13.0.
- Production LOC: 107, measured.
- Cross-language comparison: pending.
