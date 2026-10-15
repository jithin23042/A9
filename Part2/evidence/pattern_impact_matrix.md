# Pattern impact matrix

## Measurement boundary

Baseline is commit `6a4a0a65a3408ee8f62597b51d1b3aac24b77dee`; implementation
after-code commit is `e3db423efa1ac7427d9fa419fef052c49034c711`. Roles are
counted by source type occurrence in each instance, not by call-site count.
Interfaces and abstract classes are reported separately from concrete Java
classes. Spring/JPA types and method symbols are excluded from participant
counts. Shared source types count once per pattern instance and once globally
after deduplication by fully qualified source type.

## Six baseline instances

| Baseline instance | Before-change structure | After-change structure | Changed participants and evidence | Classification |
|---|---|---|---|---|
| Payment Module Selection — Strategy | `com.salesmanager.core.modules.integration.payment.model.PaymentModule`; `PaymentServiceImpl` selects from the Spring-injected `paymentModules` map; seven existing provider classes are registered in `sm-core/src/main/resources/spring/shopizer-core-modules.xml`. | Same map and selector, now with `RazorpayPayment` in the strategy set and `razorpay` metadata. | Added `sm-core/src/main/java/com/salesmanager/core/business/modules/integration/payment/impl/RazorpayPayment.java`; map/config and metadata in `sm-core/src/main/resources/spring/shopizer-core-modules.xml` and `sm-core/src/main/resources/reference/integrationmodules.json`. Selection in `PaymentServiceImpl` methods `getPaymentModule`/transaction processing remains the extension point. `RazorpayPaymentTest` covers selection and mock gateway responses. | **Extended** |
| Shipping Quote Module Selection — Strategy | `ShippingQuoteModule`; `ShippingServiceImpl` selects a module from the `shippingModules` map; six shipping implementations are Spring registered. | Same module map, selection logic, and providers; no shipping source/configuration changed. | No changed participant. See `sm-core-modules/src/main/java/com/salesmanager/core/modules/integration/shipping/model/ShippingQuoteModule.java`, `sm-core/src/main/java/com/salesmanager/core/business/services/shipping/ShippingServiceImpl.java` (`getShippingQuotes` and module selection), and six implementations under `sm-core/src/main/java/com/salesmanager/core/business/modules/integration/shipping/impl/`. | **Preserved** |
| Product Events to Search Indexing — Observer | `PublishProductAspect` and `ProductImageServiceImpl` publish product events; concrete `ProductEvent` subclasses carry product, image, variant, or attribute changes; `IndexProductEventListener` refreshes and indexes the product. | Identical participants and indexing behavior. The new order event/listener is separate and does not alter this product Observer. | No changed participant. Relevant source: `sm-core/src/main/java/com/salesmanager/core/business/configuration/events/products/PublishProductAspect.java` (`publishEvent` advices); `sm-core/src/main/java/com/salesmanager/core/business/services/catalog/product/image/ProductImageServiceImpl.java` (image event publication); `sm-core/src/main/java/com/salesmanager/core/business/configuration/events/products/ProductEvent.java`; its eight published concrete event subclasses; and `.../listeners/IndexProductEventListener.java` (`onApplicationEvent`). | **Preserved** |
| Order Processing Coordination — Facade | `OrderFacade` / `OrderFacadeImpl` coordinate order creation through `OrderService`, `ProductService`, `PaymentService`, and `ShippingService`. | The same façade continues to coordinate and persist status history. `updateOrderStatus` now publishes an event after successful persistence for `SHIPPED` and `DELIVERED`. | Modified participant: `sm-shop/src/main/java/com/salesmanager/shop/store/controller/order/facade/OrderFacadeImpl.java`, method `updateOrderStatus`. It publishes `OrderStatusChangedEvent` only after `OrderService.addOrderStatusHistory` succeeds and retains the same-status early return. `OrderStatusTransitionTest` verifies duplicate same-status updates write/publish once and unrelated status updates do not publish the SMS event. | **Modified** |
| Security Group Construction — Builder | `InitializationDatabaseImpl.createSecurityGroups` calls `SecurityGroupsBuilder.addGroup` and `addPermission` to assemble `Group`/`Permission` objects. | Unchanged security-group construction. | No changed participant. Sources: `sm-core/src/main/java/com/salesmanager/core/business/services/reference/init/InitializationDatabaseImpl.java`, `sm-core/src/main/java/com/salesmanager/core/business/utils/SecurityGroupsBuilder.java`, `sm-core-model/src/main/java/com/salesmanager/core/model/user/Group.java`, and `.../Permission.java`. | **Preserved** |
| CMS Product Image File Manager — Singleton | The identified local manager holds a static instance, private constructor, and lazy `getInstance()` accessor. | Same singleton implementation; untouched by CH01–CH03 and refactoring. | No changed participant. Source: `sm-core/src/main/java/com/salesmanager/core/business/modules/cms/product/local/CmsImageFileManagerImpl.java`. The separate `.../infinispan/CmsImageFileManagerImpl.java` is not this singleton; it is an alternate `ProductAssetsManager` implementation. | **Preserved** |

## Newly introduced instance

| Instance | Before | After | Participants and evidence | Classification |
|---|---|---|---|---|
| Order Status Changes to SMS — Observer | No order-status Observer existed in the baseline. `OrderStatusHistoryApi.createOrderStatus` only appended history and did not update `Order.status`. | `OrderFacadeImpl.updateOrderStatus` publishes `OrderStatusChangedEvent`; `OrderStatusSmsListener` asynchronously filters shipped/delivered statuses and delegates to `SmsProvider`. | New participants: `sm-shop/src/main/java/com/salesmanager/shop/store/controller/order/events/OrderStatusChangedEvent.java`, `.../listeners/OrderStatusSmsListener.java`, and `sm-shop/src/main/java/com/salesmanager/shop/notification/sms/SmsProvider.java` with `UnconfiguredSmsProvider`. The existing `OrderFacadeImpl` is the publisher and is shared with the Facade instance. `OrderStatusSmsListenerTest` and `OrderStatusTransitionTest` cover behavior and failure handling. A vendor adapter is not present because the repository specifies no SMS provider. | **New instance (not a baseline classification)** |

## Overall change

All six original instances remain. Payment Strategy gains a new provider;
Shipping Strategy, product-indexing Observer, Builder, and Singleton retain
their original collaboration; the Order Facade is modified to publish a new
post-persistence notification event. The new SMS Observer is counted
separately: baseline **5 pattern types / 6 instances**, after **5 pattern
types / 7 instances**. No instance was replaced or removed.

Category creation and Electronics validation add no instance to the six
original patterns and are not counted as a pattern extension merely because
they add new classes.

## Candidate rejections

- `ProductAttributeEvent` is a source class but is not instantiated/published
  in the product event flow; it is excluded from concrete participant counts.
- The two CMS managers have similar names. Only the `local` implementation
  has the stated static-lazy singleton structure; the Infinispan implementation
  is not counted as another singleton.
- `OrderStatusHistoryApi.createOrderStatus` records history without mutating
  the current order status, so it is not evidence of a baseline order-status
  Observer or a notification transition.
- Existing order-status email delivery is not an SMS provider or a baseline
  order-status Observer.
- Category/attribute validation is business validation, not a new Gang of Four
  pattern instance.
