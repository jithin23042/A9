# Observer Pattern — Order Status SMS Notifications (C++)

## 1. Application Description

This implementation demonstrates the Observer Design Pattern in a standalone order-management module. An order-status service validates transitions and publishes events for qualifying fulfilment statuses. An SMS listener handles notification requests, while an audit observer records published events.

The default SMS provider is unconfigured and does not send real SMS messages.

## 2. Architecture

- **Order:** Stores the order ID, customer phone number and current status.
- **OrderStatus:** `OrderStatus` enum defines the supported statuses.
- **OrderStatusChangedEvent:** Carries the order ID, previous status, new status and customer phone number.
- **OrderStatusListener:** Abstract observer interface defining `update()`.
- **EventPublisher:** Registers observers, removes subscriptions and publishes events to a snapshot of registered observers.
- **OrderStatusSmsListener:** Requests SMS delivery only for `SHIPPED` and `DELIVERED` events when a phone number is present.
- **AuditObserver:** Records published events.
- **SmsProvider:** Abstract contract for SMS delivery.
- **UnconfiguredSmsProvider:** Default provider that reports that no SMS provider is configured and returns `false`.
- **RecordingSmsProvider:** Test provider that records recipients and messages and can simulate success or failure.
- **OrderRepository:** Stores orders in an in-memory map.
- **OrderStatusService:** Validates status transitions, updates order status and publishes qualifying events.

## 3. Design Pattern Implementation

The Observer Pattern establishes a one-to-many dependency between the event publisher and registered listeners. `EventPublisher` invokes each listener's `update()` method without depending on the concrete observer classes. `OrderStatusSmsListener` and `AuditObserver` implement the same observer interface and have separate responsibilities.

C++ features include abstract classes, virtual functions, references, pointers and standard-library containers.

## 4. Notification and Validation Behaviour

- Events are published only for valid transitions to `SHIPPED` or `DELIVERED`.
- An unchanged status returns `false` and publishes no event.
- A transition to `PROCESSING` does not publish a notification event.
- Invalid transitions and unknown order IDs are rejected.
- Multiple observers receive the same event.
- Duplicate observer registration is rejected; observers can unsubscribe.
- Missing phone numbers prevent SMS requests.
- The default provider reports that no provider is configured; no real SMS is sent.

## 5. Testing

The test suite contains 10 tests covering:

1. `SHIPPED` publishes an event and requests SMS.
2. `DELIVERED` publishes an event and requests SMS.
3. An unchanged status publishes no event.
4. `PROCESSING` publishes no notification event.
5. An invalid transition is rejected.
6. Multiple observers receive the same event.
7. An unknown order is rejected.
8. Duplicate observer registration is rejected.
9. A missing phone number prevents an SMS request.
10. The unconfigured SMS provider returns `false`.

**Verified results:** 10 tests passed, 0 failed.

## 6. Compilation and Execution

Verified with GCC 13.3.0 in Ubuntu 24.04 under WSL 2. Run the commands from this directory.

Compile the tests:

```bash
g++ -std=c++17 -Wall -Wextra -pedantic test_order_status_observer.cpp -o test_order_status_observer
```

Run the tests:

```bash
./test_order_status_observer
```

## 7. Lines of Code (LOC)

Production LOC counts nonblank source lines, excluding comment-only lines. The test file is excluded.

| Metric | Verified value |
|---|---:|
| Production LOC (`order_status_observer.cpp`) | 170 |
| Physical lines (`order_status_observer.cpp`) | 186 |

## 8. Limitations

- The default SMS provider is unconfigured; no real SMS messages are sent.
- Orders and audit events are held in memory and are not persisted.
- Event notification is synchronous.
- The implementation demonstrates the Observer Pattern in a standalone module, not the complete Shopizer application.
- Test results apply to this standalone C++ implementation.
