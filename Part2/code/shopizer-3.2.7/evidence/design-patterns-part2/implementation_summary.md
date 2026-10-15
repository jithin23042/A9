# Design Patterns Part 2 — implementation summary

## Verified repository

The checkout started at the requested baseline commit
`6a4a0a65a3408ee8f62597b51d1b3aac24b77dee`; the repository working tree also
contained an unrelated untracked `source-skeleton.txt`, which was preserved.
Root and module POMs target Java 11 and Spring Boot 2.5.12, but declare project
version **3.2.5**, not 3.2.7. Tests ran under installed JDK 25 because JDK 11
was unavailable, using the Maven 3.5.2 wrapper.

Implementation source after-code commit:
`e3db423efa1ac7427d9fa419fef052c49034c711`. The five implementation/refinement
commits are:

| Commit | Change |
|---|---|
| `9d4f923e83d0658b4935f24ff1bb61692ae720c3` | Add Razorpay payment module |
| `b896f59d4c770e7df7e13fb297757ab86e223607` | Require attributes for Electronics products |
| `a555c073e5cb475da0a58feab965469ea46638cb` | Add order-status SMS Observer and transition event |
| `1c12b97b4bae4cfde3ff2ac3bba3dd6fe21f6c33` | Refactor warranty-attribute matching |
| `e3db423efa1ac7427d9fa419fef052c49034c711` | Fix and verify seeded Electronics category persistence |

## Implemented changes

- **CH01:** Added Razorpay to the existing `PaymentModule` strategy and Spring
  map. Gateway order creation, signature verification, payment validation,
  capture, and refund use mockable `RestTemplate` requests and existing
  merchant integration credentials. Added Razorpay integration metadata and
  EN/FR payment labels. No dependency upgrade or SDK was introduced.
- **CH02:** Seeds the visible `electronics` category for the default merchant.
  Electronics products require a non-default manufacturer/brand and exactly
  one `warranty_period` option attribute whose selected value name is an
  integer number of months from 1 to 120. Validation is shared by the v1 and
  product-definition write mappings and attribute create/update paths.
  Added validator unit coverage and a core persistence assertion for the
  default category.
- **CH03:** Added `SHIPPED`, then publishes `OrderStatusChangedEvent` after
  successful persisted status changes to `SHIPPED` or `DELIVERED`. The async
  listener uses delivery phone then billing phone, deduplicates recent
  transition IDs, and delegates to `SmsProvider`. Missing phone/provider errors
  are explicit and do not roll back order status.
- **Refactoring:** Removed duplicated warranty-option filtering by extracting
  one named predicate and reusing one filtered list.

## Verification and metrics

The final focused suite passed **26/26** tests. On the same full core-reactor
command, baseline reported 28 cases (14 passed, 1 error, 13 skipped), while
the after tree reported 38 (24 passed, 1 error, 13 skipped). In both, the same
pre-existing `ProductTest.testCreateProduct` / `testInsertImage` error was an
`UnexpectedRollbackException`; the new Electronics category persistence test
and `ProductNextGenTest` passed.

Across the same five modules, production Java LOC changed from 108,191 to
108,917 and test Java LOC from 6,298 to 6,911. The baseline six confirmed
instances remain; payment Strategy is extended, order Facade is modified, and
the new SMS Observer is a seventh instance. Distinct pattern types remain
five. See `metrics.csv`, `pattern_counts.csv`, and `pattern_impact_matrix.md`
for counting rules and per-instance evidence.

## Limitations

The checkout client still needs to consume Razorpay initialization details and
send Razorpay's callback fields in `paymentToken`; no live credentials or real
transactions were available. Shopizer has no specified SMS vendor integration;
`UnconfiguredSmsProvider` fails explicitly until deployment supplies a
`@Primary SmsProvider`. The category is seeded for the default merchant on
initialization only; other or already initialized merchants must create the
category/options through Shopizer's existing administration mechanisms.
There is no full shop-API integration test for Electronics product creation,
no live SMS/Razorpay test, and no JDK 11 execution.
