# Observer Pattern — Order Status SMS Notification (Java)

## 1. Overview

This project implements the Observer design pattern in Java as part of the 23CSE455 Design Pattern Case Study.

The implementation demonstrates an e-commerce order-management module in which observers are notified when an order reaches a qualifying fulfilment status. An SMS listener requests a notification through an SMS-provider abstraction, while an audit observer records the published events.

SMS notification requests are demonstrated through an unconfigured provider. No real SMS messages are sent, and no SMS gateway, credentials, external service or network connection is required.

## 2. Connection to Shopizer

The implementation is inspired by the Observer pattern used for order-status event handling in Shopizer.

The Java implementation is a standalone demonstration of the same design principle. It does not replace the original Shopizer classes or reproduce its complete event-processing infrastructure.

## 3. Architecture

| Component | Responsibility |
|---|---|
| `Order` | Represents an order and maintains its current status. |
| `OrderStatus` | Defines the supported order statuses. |
| `OrderStatusChangedEvent` | Immutable record containing order-status change information. |
| `OrderStatusListener` | Observer interface defining the update operation. |
| `EventPublisher` | Registers observers and publishes events to them. |
| `OrderStatusSmsListener` | Requests SMS notifications for qualifying status changes. |
| `AuditObserver` | Records published order-status events. |
| `SmsProvider` | Defines the SMS-sending contract. |
| `UnconfiguredSmsProvider` | Default provider that reports that SMS delivery is unavailable. |
| `OrderRepository` | Maintains orders in an in-memory map. |
| `OrderStatusService` | Validates status transitions, updates orders and publishes qualifying events. |

The service publishes events when an order transitions to `SHIPPED` or `DELIVERED`. The publisher notifies its registered observers, which independently handle the event.

## 4. Files

- `OrderStatusObserver.java` — production implementation.
- `TestOrderStatusObserver.java` — automated test harness.
- `README.md` — implementation documentation and execution instructions.

## 5. Environment

- Language: Java.
- JDK used: Java 26.
- IDE: Visual Studio Code.
- Terminal: Windows PowerShell.
- Testing approach: Custom Java test harness with assertions enabled using `java -ea`.
- External dependencies: None.

### 6. Compilation and Execution

Run the following commands from the `Observer_Order_SMS/java` directory in Windows PowerShell.

**Compile the implementation and test files:**

```powershell
New-Item -ItemType Directory -Force build | Out-Null
javac -d build OrderStatusObserver.java TestOrderStatusObserver.java
```

**Run the demonstration:**

```powershell
java -cp build A9.Part3.Observer_Order_SMS.java.OrderStatusObserver
```

Expected output:

```text
SMS not sent: no SMS provider is configured.
Current status: SHIPPED
Audit events: 1
```

**Run the automated tests:**

```powershell
java -ea -cp build A9.Part3.Observer_Order_SMS.java.TestOrderStatusObserver
```

**Verified test results:**
- Tests passed: 10
- Tests failed: 0

The tests verify event publication for `SHIPPED` and `DELIVERED`, unchanged-status handling, invalid transition rejection, multiple observers, unknown orders, duplicate observer registration, missing phone numbers, and unconfigured SMS-provider behavior.

## 7. Test Coverage

The automated tests cover:

1. `SHIPPED` publishes an event and requests SMS.
2. `DELIVERED` publishes an event and requests SMS.
3. An unchanged status publishes no event.
4. `PROCESSING` publishes no notification event.
5. Invalid status transitions are rejected.
6. Multiple observers receive the same event.
7. Unknown orders are rejected.
8. Duplicate observer registrations are rejected.
9. Missing phone numbers prevent SMS requests.
10. An unconfigured SMS provider returns `false`.

The tests validate the standalone implementation. They do not verify actual SMS delivery.

## 8. Design Pattern Analysis

### Pattern classification

- **Pattern:** Observer.
- **Category:** Behavioral design pattern.
- **Intent:** Define a one-to-many dependency so that registered observers are notified when qualifying events occur.

### Publisher and observers

`EventPublisher` maintains a list of `OrderStatusListener` objects. When an event is published, it invokes the `update()` method of each registered observer.

`OrderStatusSmsListener` and `AuditObserver` implement the same interface but perform different responsibilities.

### Extensibility

Additional observers can implement `OrderStatusListener` and register with the publisher without modifying the existing observer implementations.

The SMS provider is independently abstracted through `SmsProvider`, allowing another provider implementation to be introduced without changing the listener's notification logic.

### Status-transition validation

`OrderStatusService` uses an explicit transition map to reject invalid order-status changes. An unchanged status returns `false` without publishing an event.

### Complexity

Observer notification requires O(n) update invocations for n registered observers. The implementation copies the observer list before notification, adding O(n) time and space overhead per publication.

Order lookup and updates use a `HashMap`, which typically provides O(1) average lookup and update operations under normal hash-distribution assumptions.

## 9. Lines of Code (LOC)

The production LOC count excludes blank lines and comment-only lines. Test files are excluded from the production LOC measurement.

| Metric | Verified value |
|---|---:|
| Total physical lines in `OrderStatusObserver.java` | 313 |
| Production LOC | 234 |

**Measurement command** (run from the `Observer_Order_SMS/java` directory in Windows PowerShell):

```powershell
python ..\..\count_java_loc.py OrderStatusObserver.java
```

**Recorded output:**

```text
File: OrderStatusObserver.java
Total physical lines: 313
Production LOC (nonblank, excluding comment-only lines): 234
```
## 10. Limitations

- SMS notification requests are demonstrated, but no real SMS messages are sent.
- The default SMS provider is unconfigured; no external SMS gateway or credentials are integrated.
- Orders and audit events are stored in memory and are not persisted.
- Event publication is synchronous.
- The implementation demonstrates the Observer Pattern in a standalone application, not the complete Shopizer application.
- Test results apply to this standalone Java implementation and do not establish the correctness of the original Shopizer application.

### 11. Verification Status

- Java compilation: Successful on Windows PowerShell.
- Demonstration execution: Successful.
- Automated tests: 10 passed, 0 failed.
- Observer event publication and notification behavior: Verified.
- Invalid transitions and duplicate observer registration: Tested.
- Production LOC: 234.
- Total physical lines: 313.
- External SMS gateway: Not configured; real SMS delivery is not claimed.
