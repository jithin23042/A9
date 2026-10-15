# CH01 — Razorpay payment support

## Implementation

Added `RazorpayPayment`, implementing the existing `PaymentModule` contract in
`sm-core/src/main/java/com/salesmanager/core/business/modules/integration/payment/impl/RazorpayPayment.java`.
The module is added to the established Spring bean and strategy map in
`sm-core/src/main/resources/spring/shopizer-core-modules.xml`, and published as
the `razorpay` payment integration for India in
`sm-core/src/main/resources/reference/integrationmodules.json`. English and
French payment labels are in `sm-shop/src/main/resources/bundles/payment.properties`
and `payment_fr.properties`.

No SDK or dependency was added. The module uses the Spring `RestTemplate` and
existing Jackson dependencies. Razorpay Key ID and Key Secret are read from the
existing per-merchant integration keys (`key_id`, `key_secret`), whose
configuration is encrypted by Shopizer's existing `PaymentServiceImpl` flow.
No credentials are checked into the repository. Razorpay identifies test and
live accounts by their API keys, so both environments use the documented
`https://api.razorpay.com/v1/` endpoint.

`initTransaction` creates a Razorpay order and returns checkout data as JSON in
the existing transaction `details` field, which the current transaction API
returns to the client. The existing `PersistablePayment.paymentToken` field is
the callback handoff: the checkout client must submit JSON containing
`razorpay_order_id`, `razorpay_payment_id`, and `razorpay_signature`. The module
verifies the HMAC-SHA256 signature with the configured secret, fetches the
payment from Razorpay, and checks payment/order IDs, amount, currency, and
gateway state before creating a Shopizer transaction.

`authorize` accepts only authorized or captured gateway payments.
`authorizeAndCapture` captures authorized payments and requires a captured
gateway response before returning success. `capture` fetches and validates the
stored payment before capture. `refund` permits only a captured payment, sends
the requested amount, and checks the returned payment ID, amount, and accepted
refund status (`pending` or `processed`). The returned transaction preserves
the gateway's actual state. Missing credentials, incomplete callback data,
invalid signatures, mismatches, declined requests, and transport errors raise
the existing `IntegrationException` type; no success-shaped transaction is
returned for those cases.

## Strategy impact

**Extended.** The original collaboration is retained: `PaymentServiceImpl`
selects an implementation by module code from its injected `paymentModules`
map; `RazorpayPayment` is an additional `PaymentModule` implementation
registered in the same map. Existing payment implementations and their
selection behavior were not changed.

## Verification

Command (JDK 25 was used because JDK 11 is not installed):

```text
.\mvnw.cmd -pl sm-core -am "-Dtest=RazorpayPaymentTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
```

Result: **BUILD SUCCESS; 9 tests run, 9 passed, 0 failures, 0 errors, 0 skipped.**
The tests use Spring's `MockRestServiceServer`; they do not call Razorpay or
make real payments. They cover module registration and selection, order
initialization, verified authorization, capture, accepted partial refund,
missing credentials, an invalid signature, and a gateway rejection.

Official API references consulted:

- <https://razorpay.com/docs/api/orders/create/>
- <https://razorpay.com/docs/api/payments/fetch-with-id/>
- <https://razorpay.com/docs/api/payments/capture/>
- <https://razorpay.com/docs/payments/payment-gateway/web-integration/standard/integration-steps/>

## Limitations

The repository does not contain a Razorpay checkout UI or merchant credentials.
An integrating checkout client must consume the `details` JSON returned by the
payment-init endpoint and place Razorpay's three callback values in the
existing `paymentToken` field as JSON. No live or sandbox transaction was
attempted. The payment integration is published only for Indian stores; actual
currency/account eligibility remains subject to Razorpay merchant settings.
