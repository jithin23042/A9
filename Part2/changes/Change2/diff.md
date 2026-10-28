# Change 2 — File-level diff summary

The change introduces category-specific product validation while keeping unrelated product categories on the existing path. The full implementation is in `Part2/code/shopizer-3.2.7/`.

Key files changed/added include:
- `sm-shop/src/main/java/com/salesmanager/shop/validation/ElectronicsProductValidator.java` (shared validation rules)
- `sm-shop/src/main/java/com/salesmanager/shop/mapper/catalog/product/PersistableProductMapper.java` (product create/update path)
- `sm-shop/src/main/java/com/salesmanager/shop/mapper/catalog/product/PersistableProductDefinitionMapper.java` (product-definition path)
- `sm-shop/src/main/java/com/salesmanager/shop/store/facade/product/ProductOptionFacadeImpl.java` (attribute-only write paths)
- `sm-core/src/main/java/com/salesmanager/core/business/services/reference/init/InitializationDatabaseImpl.java` (seed Electronics category)
- Validator and category persistence test files under `sm-shop/src/test/java/` and `sm-core/src/test/java/`.

The existing category/product model is reused; this is business validation, not a newly claimed GoF design pattern. This is a file-level summary and not a raw unified patch.
