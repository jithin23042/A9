# Part 3 UML Class Diagrams and Pattern Realization

This document describes the two **standalone Part 3** implementations across Java, Python, JavaScript and C++. Editable Mermaid sources and rendered SVGs are retained alongside the implementation directories and in this folder.

## 1. Strategy — standalone payment snippet

- Standalone participants: `PaymentProcessor`, the local `PaymentStrategy`/`PaymentMethod` contract, `CardPayment`, `UPIPayment`, and `PaymentResult`.
- Files: `strategy_payment_class_diagram.mmd` (editable source) and `strategy_payment_class_diagram.svg` (rendered diagram).
- The Java implementation names its interface `PaymentMethod`; the other languages use their own language-appropriate abstraction.
- **Separation rule:** Shopizer's production `PaymentModule` and `RazorpayPayment` belong to the original/modified Shopizer application, not the standalone Part 3 Card/UPI snippet. Razorpay is intentionally excluded from this standalone diagram.

## 2. Observer — standalone order-status notification snippet

- Standalone participants include `OrderStatusService`, `OrderStatusChangedEvent`, `EventPublisher`, the observer/listener contract, `OrderStatusSmsListener`, `AuditObserver`, `SmsProvider`, and the unconfigured test provider.
- Editable source and rendered SVG: `observer_order_sms_class_diagram.mmd` and `observer_order_sms_class_diagram.svg`.
- The standalone snippet demonstrates event notification and SMS-provider delegation; it does not claim to be a complete copy of Spring's event infrastructure.

## 3. Relationship to Shopizer

The Shopizer source collaboration is documented separately in `data/patterns.csv` and `data/patterns_evolution.csv`. The modified Shopizer source adds `RazorpayPayment` to the `PaymentModule` Strategy and adds `OrderStatusChangedEvent` / `OrderStatusSmsListener` for the SMS Observer. These application classes are not inserted into the standalone Part 3 UML diagrams.
