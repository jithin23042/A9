# Test results

## Environment and baseline comparison

The POMs target Java 11 and Spring Boot 2.5.12. JDK 11 was unavailable, so
commands ran with installed JDK 25 using the repository Maven wrapper (Maven
3.5.2). Java 11 runtime compatibility has not been verified.

Baseline commit:
`6a4a0a65a3408ee8f62597b51d1b3aac24b77dee`.

After-code commit:
`e3db423efa1ac7427d9fa419fef052c49034c711`.

Same-scope baseline and after command:

```text
.\mvnw.cmd -pl sm-core -am test
```

| Tree | Reported cases | Passed | Failures | Errors | Skipped | Result |
|---|---:|---:|---:|---:|---:|---|
| Baseline | 28 | 14 | 0 | 1 | 13 | BUILD FAILURE |
| After | 38 | 24 | 0 | 1 | 13 | BUILD FAILURE |

The sole error in both runs is the existing
`ProductTest.testCreateProduct` / `testInsertImage` rollback:
`UnexpectedRollbackException: Transaction silently rolled back because it has
been marked as rollback-only`. The new category initialization's detached
description issue found during the first after-run was fixed by persisting a
new `CategoryDescription` with an unset generated ID; `ProductNextGenTest`
then passed and now asserts that the seeded `electronics` category and English
description are persisted.

## Focused implementation and regression tests

Final focused command:

```text
.\mvnw.cmd -pl sm-shop -am "-Dtest=RazorpayPaymentTest,ElectronicsProductValidatorTest,OrderStatusSmsListenerTest,OrderStatusTransitionTest,ProductNextGenTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
```

Result: **BUILD SUCCESS; 26 tests run, 26 passed, 0 failures, 0 errors,
0 skipped.** Per-class results: Razorpay 9, Electronics validator 7, SMS
listener 6, status transition 2, and core product/category persistence 2.
External Razorpay calls and SMS sends are mocked/not configured.

The validator refactoring was tested independently before and after with:

```text
.\mvnw.cmd -pl sm-shop -am "-Dtest=ElectronicsProductValidatorTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
```

Both runs: **7 tests passed, 0 failures, 0 errors, 0 skipped.**

## Coverage boundaries

No live Razorpay payment or SMS was performed. The final focused tests exercise
mock gateway/provider boundaries, order transition publication, and unit
validation. `ProductNextGenTest` verifies persisted default-category
initialization and an unrelated existing product persistence flow; a full
Shop API/database test that creates an Electronics product with the new
required attributes was not run. No Java 11 runtime test was possible.
