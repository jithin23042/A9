# CH02 — Electronics category and required attributes

## Existing model and implementation

The repository models product categories through `Category` and
`Product.categories`; `Product.manufacturer` already represents the product
brand. Product attributes are `ProductAttribute` links to a `ProductOption`
and `ProductOptionValue`, with translated option-value descriptions. They are
not free-form key/value fields. No schema or domain-model changes were needed.

Initialization now creates the visible root category with code `electronics`
and English description `Electronics` alongside the default merchant in
`sm-core/src/main/java/com/salesmanager/core/business/services/reference/init/InitializationDatabaseImpl.java`.
The product write paths use the stable category code and existing
manufacturer/option models:

- `sm-shop/src/main/java/com/salesmanager/shop/mapper/catalog/product/PersistableProductMapper.java`
  checks the v1 product create/update mapping and maps submitted product
  attributes for Electronics before validating.
- `sm-shop/src/main/java/com/salesmanager/shop/mapper/catalog/product/PersistableProductDefinitionMapper.java`
  checks the product-definition mapping.
- `sm-shop/src/main/java/com/salesmanager/shop/store/facade/product/ProductOptionFacadeImpl.java`
  validates individual and batch attribute writes against the persisted
  product's full attribute collection, including existing attributes.
- Shared validation lives in
  `sm-shop/src/main/java/com/salesmanager/shop/validation/ElectronicsProductValidator.java`.

An Electronics product requires a nonblank manufacturer code other than
Shopizer's `DEFAULT` placeholder, and exactly one product option whose code is
`warranty_period`. Its selected option value must have at least one translated
description with a decimal integer name from **1 through 120 inclusive**.
Warranty units are whole months, so the supported range is one month to ten
years. The option must be created through Shopizer's existing product-option
and option-value administration flow using code `warranty_period`; its values
should be month counts such as `12`. There is no fabricated fixed list of
options or new schema model.

Validation is category-scoped; other products are unaffected. It is applied
after category resolution and before the product/attribute write completes,
including attribute-only create/update flows. Validation failures use the
existing `ConversionRuntimeException` response mechanism.

## Verification

Command (JDK 25 was used because JDK 11 is not installed):

```text
.\mvnw.cmd -pl sm-shop -am "-Dtest=ElectronicsProductValidatorTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
```

Result: **BUILD SUCCESS; 7 tests run, 7 passed, 0 failures, 0 errors,
0 skipped.** The test class at
`sm-shop/src/test/java/com/salesmanager/test/shop/validation/ElectronicsProductValidatorTest.java`
covers valid brand and warranty, missing brand, missing warranty, nonnumeric
and out-of-range periods, unrelated categories, and invalid update values.
This is unit coverage of the shared validator, not a database/API integration
test.

`.\mvnw.cmd -pl sm-core -am test` was also run before any changes: the
pre-existing `ProductTest.testCreateProduct` failed with
`UnexpectedRollbackException` (28 executed; 1 error, 0 assertion failures,
13 skipped). It prevents treating full persistence behavior as verified.
The database-backed `ProductNextGenTest` now includes
`testElectronicsCategoryInitialized`, which verifies the default merchant's
persisted `electronics` category, root depth, and English description. The
same test class's existing `testCreateProduct` also persisted a product in an
unrelated category. Command:

```text
.\mvnw.cmd -pl sm-core -am "-Dtest=ProductNextGenTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
```

Result: **BUILD SUCCESS; 2 tests run, 2 passed, 0 failures, 0 errors,
0 skipped.** This verifies category initialization and an existing core
product-persistence path, but it does not persist an Electronics product via
the shop API or verify its option/value validation against a live database.

## Limitations

The root initialization path seeds `electronics` for the default merchant;
other merchants/stores do not receive a category automatically through this
specific seeder. The warranty validation reads the selected option value's
description name, so merchants must configure values as integer month counts
in the store's supported language(s). Existing product API error translation
determines the wire response shape. Electronics-product persistence and HTTP
error status behavior have not been integration-tested.
