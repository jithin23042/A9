# Shared Acceptance-Test Specification

## Strategy — Payment Module Selection
Verify that each language implementation:
1. Selects the requested payment strategy at runtime.
2. Supports Card and UPI payment strategies.
3. Handles provider selection according to the implementation's documented case-normalization rules.
4. Rejects invalid payment requests and unsupported methods.
5. Handles missing/invalid input and duplicate registration/selection cases where implemented.
6. Uses a common strategy contract so a new strategy can be added without rewriting the payment context.

## Observer — Order Status SMS Notification
Verify that each language implementation:
1. Allows observers to register with the publisher.
2. Publishes order-status change events.
3. Notifies observers for the supported statuses (including SHIPPED and DELIVERED).
4. Keeps SMS notification logic separate from order-status publication.
5. Supports an audit observer where implemented.
6. Handles missing phone numbers and simulated provider failures where implemented.
7. Does not claim delivery through a live SMS provider; the tests verify the local simulation only.

## Evidence
The per-language test source and README files are stored with each implementation. Actual execution logs are in `Part3/evidence/`. The recorded total is 80/80 across eight suites, subject to rerunning the included commands in the stated environments.
