# CH03 — SMS notifications for order-status transitions

## Lifecycle and implementation

The customer-facing/admin status update is
`sm-shop/src/main/java/com/salesmanager/shop/store/api/v1/order/OrderApi.java`
calling `OrderFacadeImpl.updateOrderStatus` in
`sm-shop/src/main/java/com/salesmanager/shop/store/controller/order/facade/OrderFacadeImpl.java`.
That method returns without writing history for an identical status and
persists the new status plus `OrderStatusHistory` through
`OrderService.addOrderStatusHistory` before publishing an event.
`OrderStatusHistoryApi`'s `createOrderStatus` endpoint only appends history; it
does not change `Order.status`, so it does not represent a state transition
and does not publish an SMS event.

`OrderStatus.SHIPPED` was added beside the existing `DELIVERED` status. After a
successful persisted transition to either state, `OrderStatusChangedEvent` is
published. `OrderStatusSmsListener` is a Spring event listener running on the
existing async executor (`AsyncConfig`). It selects the delivery telephone,
then falls back to billing telephone, and sends the order reference and status
through the `SmsProvider` interface. The event's persisted status-history ID
serves as the deduplication key; repeated same-status API updates do not write
another history record or event. The listener remembers recent transition IDs
in a bounded, process-local cache.

Provider errors do not roll back an already-persisted order transition or
escape the async listener. They are logged with order ID, status, and exception
type only; phone numbers and provider exception messages are not logged.
Missing phone numbers are skipped with an order-ID-only warning. The default
`UnconfiguredSmsProvider` explicitly reports that no provider is configured,
so Shopizer does not claim that a text was delivered when no SMS integration
exists. A deployment must provide a `@Primary` `SmsProvider` implementation
configured for its vendor. No provider or credentials were specified in the
repository, so no vendor dependency, network protocol, or credentials were
invented and no real messages are sent by tests.

The shipped-status labels in `shopizer.properties` and
`shopizer_fr.properties` were corrected to “Shipped” and “Expédiée”.

## Verification

Command (JDK 25 was used because JDK 11 is not installed):

```text
.\mvnw.cmd -pl sm-shop -am "-Dtest=OrderStatusSmsListenerTest,OrderStatusTransitionTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
```

Result: **BUILD SUCCESS; 8 tests run, 8 passed, 0 failures, 0 errors,
0 skipped.** `OrderStatusSmsListenerTest` covers shipped/delivered messages,
order reference and status content, delivery/billing phone selection,
irrelevant transitions, absent phone, provider failure, and duplicate history
events. `OrderStatusTransitionTest` verifies publication occurs only after a
status change and repeated status updates persist/publish only once. Tests use
a recording mock provider and do not send SMS.

## Pattern impact

The original Order Processing Coordination Facade remains responsible for
status persistence and is **Modified** only to publish a post-persistence
event. The new `OrderStatusChangedEvent` and `OrderStatusSmsListener` form a
new Observer instance; this was not part of the baseline pattern count.

## Limitations

The SMS provider is intentionally an extension point, not a live vendor
adapter. Until a deployment supplies one, order transitions work and SMS
attempts fail explicitly in the listener log. Deduplication is process-local
and bounded to the most recent 10,000 successful transition IDs; it does not
provide cross-restart exactly-once delivery. Customer SMS content is currently
English and contains only order reference and status.
