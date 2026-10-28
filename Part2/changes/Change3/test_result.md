# Change 3 — Test result

Recorded focused command:

```powershell
.\mvnw.cmd -pl sm-shop -am "-Dtest=OrderStatusSmsListenerTest,OrderStatusTransitionTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
```

Recorded result: **8 tests passed, 0 failures, 0 errors, 0 skipped.** A recording mock SMS provider is used; no real SMS is sent. See `Part2/evidence/test_results.md` for environment, broader-suite results, and limitations.
