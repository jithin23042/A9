# Observer Pattern — Order Status SMS Notification (JavaScript)

## 1. Overview

This project implements the Observer design pattern in JavaScript as part of the 23CSE455 Design Pattern Case Study.

The implementation demonstrates how an order-processing component can notify multiple observers when an order transitions to a relevant fulfilment status. An SMS observer receives order-status events and delegates message delivery through an SMS-provider abstraction.

The implementation uses synchronous, in-memory event dispatch and simulated SMS delivery. No real SMS service, credentials, external service or network connection is required.

## 2. Connection to Shopizer

The implementation is inspired by the Observer pattern identified in Shopizer during the earlier parts of the case study.

- **Original application:** Shopizer 3.2.7.
- **Publisher:** `OrderFacadeImpl`.
- **Event:** `OrderStatusChangedEvent`.
- **Observer:** `OrderStatusSmsListener`.
- **Provider abstraction:** `SmsProvider`.
- **Default provider:** `UnconfiguredSmsProvider`.

The original Shopizer implementation publishes an event after a qualifying order-status transition to `SHIPPED` or `DELIVERED`. An asynchronous listener filters relevant events and delegates SMS delivery through the provider abstraction.

This JavaScript implementation is a standalone demonstration of the same design principle. It does not replace the original Shopizer classes or reproduce Spring's asynchronous event infrastructure.

## 3. Architecture

| Component | Responsibility |
|---|---|
| `OrderStatus` | Defines the supported order statuses. |
| `OrderStatusChangedEvent` | Carries order-status change information. |
| `OrderStatusObserver` | Defines the common observer contract. |
| `EventPublisher` | Registers observers and publishes events. |
| `OrderStatusSmsListener` | Receives events and requests SMS delivery for relevant statuses. |
| `AuditObserver` | Records qualifying events for audit and verification purposes. |
| `SmsProvider` | Defines the SMS delivery contract. |
| `UnconfiguredSmsProvider` | Reports that no SMS provider is configured and returns `false`. |
| `Order` | Represents an order and its current status. |
| `OrderRepository` | Stores and retrieves orders in memory. |
| `OrderStatusService` | Validates status transitions, updates orders and publishes qualifying events. |

The order-status service is separated from the notification observers. This allows additional observers to be registered without embedding their notification logic directly into the service.

## 4. Files

- `order_status_observer.js` — production implementation.
- `test_order_status_observer.js` — automated tests.
- `order_status_observer.mmd` — editable Mermaid UML class diagram source.
- `README.md` — implementation documentation and execution instructions.

The shared JavaScript LOC measurement script is stored at the Part 3 level as `count_js_loc.py`.

## 5. Environment

- **Language:** JavaScript (CommonJS).
- **Runtime:** Node.js 24.13.0.
- **Package manager:** npm 11.6.2.
- **IDE:** Visual Studio Code.
- **Terminal:** Windows PowerShell.
- **Testing framework:** Node.js built-in test runner and `node:assert/strict`.
- **External dependencies:** None.

## 6. Execution Instructions

Run commands from:

`A9/Part3/Observer/javascript/`

### Execute the demonstration

```powershell
node order_status_observer.js
```

Expected output:

```text
SMS not sent: no SMS provider is configured.
Current status: SHIPPED
Audit events: 1
```

The demonstration transitions an order from `PENDING` to `PROCESSING` and then to `SHIPPED`. The qualifying transition publishes an event to the registered observers.

### Execute the automated tests

```powershell
node --test test_order_status_observer.js
```

**Recorded test result:** 10 tests passed, 0 failures and 0 skipped tests, according to the terminal output captured during development.

The tests use a fake SMS provider to verify requests without sending real messages.

## 7. Test Coverage

The automated tests cover:

1. A transition to `SHIPPED` publishes an event and requests an SMS.
2. A transition to `DELIVERED` publishes an event and requests an SMS.
3. An unchanged status publishes no event.
4. A transition to `PROCESSING` publishes no notification event.
5. An invalid status transition is rejected without changing the order status.
6. Multiple observers receive the same qualifying event.
7. An unknown order is rejected.
8. Duplicate observer registration is rejected.
9. A missing phone number prevents SMS delivery.
10. The unconfigured SMS provider returns `false` rather than claiming successful delivery.

The tests verify simulated behavior, not real SMS delivery.

## 8. Design Pattern Analysis

### Pattern classification

- **Pattern:** Observer.
- **Category:** Behavioral design pattern.
- **Intent:** Define a one-to-many dependency so that multiple observers can receive notifications when a relevant event occurs.

### Event publication

`OrderStatusService` validates the requested transition and updates the order before publishing an event. Events are published only when the new status is `SHIPPED` or `DELIVERED`.

An unchanged status returns `false` without publishing an event. An invalid transition raises an error before modifying the order.

### Observer extensibility

`EventPublisher` maintains a collection of observers and invokes each observer's `update()` method when an event is published.

A new observer, such as an audit or email observer, can implement the `OrderStatusObserver` contract and register with the publisher without adding its behavior directly to `OrderStatusService`.

### Abstraction

JavaScript does not enforce abstract classes natively in the same manner as Python's `ABC`. The `OrderStatusObserver` and `SmsProvider` classes therefore use methods that throw errors when not overridden.

`OrderStatusSmsListener` and `AuditObserver` implement the observer behavior. `UnconfiguredSmsProvider` implements the SMS-provider contract.

### Complexity

Publishing an event to `n` registered observers requires O(n) notification operations, excluding the work performed by each observer. This follows from traversing the observer collection and invoking each observer.

The exact execution time depends on the runtime, number of observers and the work performed by each observer.

### Dispatch model

This implementation uses synchronous, in-memory event dispatch. Each observer is invoked during the publisher's `publish()` operation.

This differs from Shopizer's asynchronous Spring event listener and must be considered when interpreting the cross-language comparison.

## 9. Source LOC Measurement

Production LOC was measured from `order_status_observer.js` using the shared `count_js_loc.py` script. The separate test file is excluded.

### Counting rule

- Exclude blank lines.
- Exclude comment-only lines, including JavaScript `//` comments and standalone block-comment lines.
- Count all other non-empty production lines, including declarations and lines containing inline comments.
- Exclude `test_order_status_observer.js` from production LOC.

### Measured results

| Metric | Measured value |
|---|---:|
| Total physical lines | 262 |
| Production LOC | 201 |

**Measurement command:**

```powershell
python ..\..\count_js_loc.py order_status_observer.js
```

**Recorded terminal output:**

```text
File: order_status_observer.js
Total physical lines: 262
Production LOC (nonblank, excluding comment-only lines): 201
```

The measurement was obtained in the VS Code PowerShell terminal using the JavaScript-specific counting script.

The same counting rule should be applied to the corresponding Python, Java and C++ implementations for a fair cross-language comparison.

## 10. Limitations

- SMS delivery is simulated; no SMS vendor integration is implemented.
- The default provider does not send real messages.
- Event dispatch is synchronous and in memory, unlike Shopizer's asynchronous Spring event handling.
- Orders are stored in an in-memory repository rather than a database.
- The implementation demonstrates the Observer pattern, not the complete Shopizer application.
- Test results apply to this standalone JavaScript implementation and do not establish the correctness of the original Java application.

## 11. Verification Status

- Demonstration execution: successful.
- Automated tests: 10 passed, 0 failures, 0 skipped.
- Node.js version: 24.13.0.
- Production LOC: 201, measured.
- UML source: included as `order_status_observer.mmd`.
- Real SMS delivery: not implemented.
- Cross-language comparison: pending.
