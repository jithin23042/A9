# Change 3 — File-level diff summary

The source change publishes an order-status event after the status/history write path and adds an asynchronous SMS observer. The modified implementation is under `Part2/code/shopizer-3.2.7/`.

Key changed/added files include:
- `sm-shop/src/main/java/com/salesmanager/shop/store/controller/order/facade/OrderFacadeImpl.java` (publish event after a qualifying transition)
- `sm-shop/src/main/java/com/salesmanager/shop/store/controller/order/listeners/OrderStatusSmsListener.java` (new observer)
- `sm-shop/src/main/java/com/salesmanager/shop/store/controller/order/events/OrderStatusChangedEvent.java` (new event; verify the exact source path in the snapshot if navigating manually)
- `sm-core-model/src/main/java/com/salesmanager/core/model/order/orderstatus/OrderStatus.java` (adds shipped status)
- Relevant language bundles and listener/transition test files.

The SMS provider is an interface with an unconfigured default. Test providers are mocks/recorders; this change does not claim actual message delivery. This is a file-level summary and not a raw unified patch.
