# Observer Pattern — Order Status SMS Notification

## 1. Overview

This project implements the Observer design pattern in Python as part of the 23CSE455 Design Pattern Case Study.

The implementation demonstrates how an order-processing component can notify multiple observers when an order transitions to a relevant fulfilment status. The SMS observer delegates message delivery through an SMS-provider abstraction.

The implementation uses in-memory event dispatch and simulated SMS delivery. No real SMS service, credentials, external service or network connection is required.

## 2. Connection to Shopizer

The implementation is inspired by the Observer collaboration introduced during Part 2 of the Shopizer case study.

- **Original application:** Shopizer 3.2.7.
- **Publisher:** `OrderFacadeImpl`.
- **Event:** `OrderStatusChangedEvent`.
- **Observer:** `OrderStatusSmsListener`.
- **Provider abstraction:** `SmsProvider`.
- **Default provider:** `UnconfiguredSmsProvider`.

The original Shopizer implementation publishes an event after a successful order-status transition to `SHIPPED` or `DELIVERED`. An asynchronous listener filters relevant events and delegates SMS delivery through the provider abstraction.

The Python implementation is a standalone demonstration of the same design principle. It does not replace the original Shopizer classes or reproduce Spring's asynchronous event infrastructure.

## 3. Architecture

| Component | Responsibility |
|---|---|
| `OrderStatusService` | Validates status transitions, updates the order and publishes qualifying events. |
| `OrderRepository` | Stores and retrieves orders in memory. |
| `Order` | Represents an order and its current status. |
| `EventPublisher` | Registers observers and publishes events to them. |
| `OrderStatusChangedEvent` | Carries order-status change information. |
| `OrderStatusObserver` | Abstract contract for observers. |
| `OrderStatusSmsListener` | Receives events and requests SMS delivery for relevant statuses. |
| `AuditObserver` | Records events for audit and verification purposes. |
| `SmsProvider` | Abstract contract for SMS delivery. |
| `UnconfiguredSmsProvider` | Reports that no SMS provider is configured and returns `False`. |

## 4. Files

- `order_status_observer.py` — production implementation.
- `test_order_status_observer.py` — automated unit tests.
- `order_status_observer.mmd` — editable Mermaid UML class diagram source.
- `README.md` — implementation documentation and execution instructions.

## 5. Environment

- Language: Python
- Python version used for testing: 3.14.3
- IDE: Visual Studio Code
- Terminal: Windows PowerShell
- Testing framework: Python standard-library `unittest`
- External dependencies: None

## 6. Execution Instructions

Run the commands from:

`A9/Part3/Observer/python/`

### Execute the demonstration

```powershell
python order_status_observer.py
```

Expected output:

```text
SMS not sent: no SMS provider is configured.
Current status: SHIPPED
Audit events: 1
```

### Execute the automated tests

```powershell
python -m unittest -v test_order_status_observer.py
```

**Recorded test result:** 10 tests passed, 0 failures and 0 errors, according to the terminal output captured during development.

## 7. Test Coverage

The automated tests cover:

1. `SHIPPED` publishes an event and requests an SMS.
2. `DELIVERED` publishes an event and requests an SMS.
3. An unchanged status publishes no event.
4. A transition to `PROCESSING` publishes no notification event.
5. An invalid status transition is rejected without changing the order status.
6. Multiple observers receive a qualifying event.
7. Unknown orders are rejected.
8. Duplicate observer registration is rejected.
9. A missing phone number prevents SMS delivery.
10. The unconfigured SMS provider does not claim successful delivery.

The tests verify simulated behavior, not real SMS delivery.

## 8. Design Pattern Analysis

### Pattern classification

- **Pattern:** Observer
- **Category:** Behavioral design pattern
- **Intent:** Define a one-to-many dependency so that multiple observers can receive notifications when a relevant event occurs.

### Event publication

`OrderStatusService` publishes `OrderStatusChangedEvent` only after the status is updated successfully and only when the new status is `SHIPPED` or `DELIVERED`. Unchanged statuses produce no event.

### Observer extensibility

`EventPublisher` maintains a collection of observers and notifies each registered observer when an event is published. A new observer, such as an audit or email observer, can be registered without adding its behavior directly to `OrderStatusService`.

### Abstraction

`OrderStatusObserver` defines the observer contract. `OrderStatusSmsListener` and `AuditObserver` implement that contract. `SmsProvider` separately abstracts SMS delivery.

### Complexity

Publishing an event to `n` registered observers requires O(n) notification operations, excluding the work performed by each observer. This reflects the linear traversal of the observer collection.

### Dispatch limitation

This Python implementation uses synchronous in-memory dispatch. It does not reproduce the asynchronous execution behavior of Shopizer's Spring event listener.

## 9. Source LOC Measurement

Production LOC was measured using the reusable `count_python_loc.py` script on `order_status_observer.py` only. The separate test file is excluded.

**Counting rule:**

- Exclude blank lines.
- Exclude comment-only lines beginning with `#`.
- Count all other non-empty lines, including imports, decorators, declarations, executable statements, docstrings and lines containing inline comments.
- Exclude the separate test file from production LOC.

### Measured results

| Metric | Measured value |
|---|---:|
| Total physical lines | 230 |
| Production LOC | 151 |

**Measurement command:**

```powershell
python ..\..\Strategy_Payment\python\count_python_loc.py order_status_observer.py
```

The command is run from the Observer Python directory and uses the reusable script stored in the Strategy Python directory.

The measurement was executed in the VS Code PowerShell terminal. The same counting rule must be applied to the Java, JavaScript and C++ implementations to support a fair cross-language comparison.

## 10. Limitations

- SMS delivery is simulated; no SMS vendor adapter is implemented.
- The default provider does not send real messages.
- Events are dispatched synchronously in memory, unlike Shopizer's asynchronous Spring event listener.
- The repository is in memory; there is no database integration.
- The implementation demonstrates the Observer pattern, not the complete Shopizer application.
- Test results apply to the standalone Python implementation and do not establish the correctness of the original Java application.

## 11. Verification Status

- Demonstration execution: successful.
- Automated tests: 10 passed, 0 failures, 0 errors.
- Python version: 3.14.3.
- Production LOC: 151, measured.
- UML source: save as `order_status_observer.mmd`.
- Real SMS delivery: not implemented.
- Cross-language comparison: pending.
