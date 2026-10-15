# Evolution Before/After Analysis (source-qualified)

## Source snapshots and caveat
The source baseline is the supplied `shopizer-3.2.7_Initial.zip`; the modified snapshot is `after_part_2.zip`. Exact Git commit hashes were not supplied, so this analysis identifies archive snapshots rather than invented commit IDs. Source paths in this document are relative to the `shopizer-3.2.7/` root within those archives.

## CH01 — Razorpay payment integration
- **Requirement:** Add another payment provider without rewriting provider selection.
- **Before:** `PaymentModule` is the common contract; `PaymentServiceImpl` selects a provider from the `paymentModules` map. Seven Java provider classes implement `PaymentModule`; the explicit map contains six entries.
- **After:** `RazorpayPayment` implements `PaymentModule` and is registered in the Spring XML bean/map. The source counts are **7 → 8 concrete provider classes**. The explicit `paymentModules` map counts are **6 → 7 entries**.
- **Strategy participants:** count the shared collaboration once, not once per provider. If `PaymentServiceImpl` is included as a participant in a participant-symbol inventory, the count convention can be **8 → 9 Strategy participants**; this is not the same as concrete providers.
- **Original provider example:** `PayPalRestPayment` is present in the baseline. `WeChatPayment` is not used as the baseline example.
- **Pattern effect:** existing Payment Module Selection Strategy collaboration is `extended`, not a separate new Strategy instance.

## CH02 — Electronics warranty validation
- **Requirement:** Apply brand and warranty-period rules to products in the electronics category.
- **Before/after:** the modified source introduces `ElectronicsProductValidator` and wires it into product mapping/facade workflows.
- **Pattern effect:** this is business validation logic, not a GoF design pattern by itself. It is deliberately excluded from `patterns.csv` pattern-instance totals.
- **Evidence limitation:** source existence and wiring are not equivalent to full end-to-end application validation; no claim of complete Shopizer integration tests is made here.

## CH03 — Order-status SMS notification
- **Requirement:** Notify through an SMS-provider abstraction when an order reaches qualifying fulfilment states.
- **Before:** the baseline has the product-indexing event/listener flow, but does not have `OrderStatusChangedEvent` or `OrderStatusSmsListener`.
- **After:** `OrderFacadeImpl` publishes `OrderStatusChangedEvent` for qualifying `SHIPPED`/`DELIVERED` status changes; `OrderStatusSmsListener` consumes the event asynchronously and delegates delivery through `SmsProvider`.
- **Pattern effect:** a separate Observer/event-listener collaboration is `newly_introduced`. It is distinct from the original product-indexing Observer.
- **Transaction qualification:** the method calls `orderService.addOrderStatusHistory` before `eventPublisher.publishEvent`. This proves call ordering in that method only. It does **not** establish that the database transaction has committed before event publication; no post-commit guarantee is claimed.

## Pattern-count consistency
`data/pattern_counts.csv` is derived from `data/patterns.csv`/the source-snapshot collaboration inventory. Each collaboration is counted once per snapshot. The Electronics warranty validation component is not counted as a pattern. See `data/patterns_evolution.csv` for the snapshot/status ledger.
