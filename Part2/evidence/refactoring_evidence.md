# Refactoring evidence — Electronics warranty validation

## Code smell and original location

Before refactoring, `ElectronicsProductValidator.validate(Product)` in
`sm-shop/src/main/java/com/salesmanager/shop/validation/ElectronicsProductValidator.java`
used the same warranty-option predicate twice: once to count matching
`ProductAttribute` entries and again to retrieve the matching entry. Duplicating
that criterion made future changes prone to inconsistent matching behavior.
This is a small, concrete duplicate-logic smell in the newly added validator.

## Refactoring

Extracted the predicate into `isWarrantyAttribute(ProductAttribute)`, filtered
once into a list, then reused that result for both the exact-one check and
period validation. The accepted warranty format/range and all failure messages
remain unchanged. No API, model, or persistence behavior was changed.

## Before/after verification and diff evidence

Before refactoring, command:

```text
.\mvnw.cmd -pl sm-shop -am "-Dtest=ElectronicsProductValidatorTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
```

Result: **BUILD SUCCESS; 7 tests run, 7 passed, 0 failures, 0 errors,
0 skipped.**

The same command was run after the refactoring with the same result:
**BUILD SUCCESS; 7 tests run, 7 passed, 0 failures, 0 errors, 0 skipped.**

The exact source diff is committed separately after CH01, CH02, and CH03. It
replaces two duplicate stream filters with one filtered list and a named
predicate. These tests cover valid periods, missing and invalid values,
category scoping, and updated values; identical results support preserved
behavior for those cases.
