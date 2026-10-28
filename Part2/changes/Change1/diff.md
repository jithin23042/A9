# Change 1 — File-level diff summary

The source change is represented in the evolved source snapshot at `Part2/code/shopizer-3.2.7/`. It extends the existing `PaymentModule` Strategy with `RazorpayPayment` and registers the provider within Shopizer's existing payment-module selection mechanism.

Key changed/added files include:
- `sm-core/src/main/java/com/salesmanager/core/business/modules/integration/payment/impl/RazorpayPayment.java` (new implementation)
- `sm-core/src/main/resources/spring/shopizer-core-modules.xml` (register provider in the existing map)
- `sm-core/src/main/resources/reference/integrationmodules.json` (publish Razorpay integration metadata)
- `sm-shop/src/main/resources/bundles/payment.properties` and `payment_fr.properties` (labels)
- `sm-core/src/test/java/com/salesmanager/test/modules/RazorpayPaymentTest.java` (focused mocked tests)

The baseline and evolved snapshots are compared in `Part1/evidence/` and `Part2/evidence/refactoring_evidence.md`. Review the source in the baseline submodule against the evolved snapshot to inspect the exact code-level changes. This file is a file-level summary and does not claim to be a raw unified patch.
