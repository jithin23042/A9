# Change 1 — Test result

Command from the modified `shopizer-3.2.7/` root:

```powershell
.\mvnw.cmd -pl sm-core -am "-Dtest=RazorpayPaymentTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
```

Recorded result: **BUILD SUCCESS; 9 tests passed, 0 failures, 0 errors, 0 skipped.** Tests use Spring's mock HTTP server and do not call Razorpay or initiate a real payment. See `Part2/evidence/test_results.md` for environment notes.
