# Common Test Specification — Strategy and Observer

The same behavior is implemented independently in Java, Python, JavaScript, and C++.

## Strategy — Payment Module Selection

Verify successful simulated Card and UPI payments; case-insensitive provider selection; registering an additional strategy; rejecting duplicate registrations; rejecting unknown or empty provider keys; rejecting zero, negative, non-finite amounts; and rejecting invalid strategy registrations.

## Observer — Order Status SMS Notification

Verify valid status transitions; event publication only for qualifying transitions to SHIPPED or DELIVERED; no notification for unchanged/irrelevant statuses; observers receive the event; SMS notification uses a configured test/recording provider; customer phone selection and absent-phone behavior; provider failure handling; duplicate-event handling; and invalid status transitions/order IDs.

## Scope

These are standalone simulations. No real payment gateway or SMS service is contacted. The exact implementation-specific checks are documented in each language folder's README and individual test file.
