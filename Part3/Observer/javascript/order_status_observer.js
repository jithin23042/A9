
"use strict";

// Supported order statuses.
const OrderStatus = Object.freeze({
    PENDING: "PENDING",
    PROCESSING: "PROCESSING",
    SHIPPED: "SHIPPED",
    DELIVERED: "DELIVERED",
    CANCELLED: "CANCELLED"
});

// Represents an order-status change event.
class OrderStatusChangedEvent {
    constructor(orderId, previousStatus, newStatus, customerPhone) {
        this.orderId = orderId;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.customerPhone = customerPhone;

        Object.freeze(this);
    }
}

// Common contract for order-status observers.
class OrderStatusObserver {
    update(event) {
        throw new Error("update() must be implemented.");
    }
}

// Publishes events to registered observers.
class EventPublisher {
    constructor() {
        this.observers = [];
    }

    subscribe(observer) {
        if (
            !observer ||
            typeof observer.update !== "function"
        ) {
            throw new TypeError("Invalid observer.");
        }

        if (this.observers.includes(observer)) {
            throw new Error("Observer already registered.");
        }

        this.observers.push(observer);
    }

    publish(event) {
        for (const observer of [...this.observers]) {
            observer.update(event);
        }
    }
}

// Common contract for SMS providers.
class SmsProvider {
    sendSms(phone, message) {
        throw new Error("sendSms() must be implemented.");
    }
}

// Default provider: no real SMS service is configured.
class UnconfiguredSmsProvider extends SmsProvider {
    sendSms(phone, message) {
        console.log("SMS not sent: no SMS provider is configured.");
        return false;
    }
}

// Observer that requests SMS for relevant status changes.
class OrderStatusSmsListener extends OrderStatusObserver {
    constructor(provider) {
        super();
        this.provider = provider;
    }

    update(event) {
        const notifiableStatuses = [
            OrderStatus.SHIPPED,
            OrderStatus.DELIVERED
        ];

        if (!notifiableStatuses.includes(event.newStatus)) {
            return;
        }

        if (
            typeof event.customerPhone !== "string" ||
            !event.customerPhone.trim()
        ) {
            return;
        }

        const message =
            `Order ${event.orderId} status changed to ` +
            `${event.newStatus}.`;

        this.provider.sendSms(event.customerPhone, message);
    }
}

// Additional observer for audit purposes.
class AuditObserver extends OrderStatusObserver {
    constructor() {
        super();
        this.events = [];
    }

    update(event) {
        this.events.push(event);
    }
}

// Represents an order.
class Order {
    constructor(orderId, customerPhone, status = OrderStatus.PENDING) {
        this.orderId = orderId;
        this.customerPhone = customerPhone;
        this.status = status;
    }
}

// In-memory order repository.
class OrderRepository {
    constructor() {
        this.orders = new Map();
    }

    add(order) {
        if (this.orders.has(order.orderId)) {
            throw new Error(`Order already exists: ${order.orderId}`);
        }

        this.orders.set(order.orderId, order);
    }

    get(orderId) {
        const order = this.orders.get(orderId);

        if (!order) {
            throw new Error(`Order not found: ${orderId}`);
        }

        return order;
    }

    updateStatus(orderId, newStatus) {
        this.get(orderId).status = newStatus;
    }
}

// Coordinates status validation, persistence and event publication.
class OrderStatusService {
    constructor(repository, publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    updateStatus(orderId, newStatus) {
        const order = this.repository.get(orderId);
        const previousStatus = order.status;

        if (!Object.values(OrderStatus).includes(newStatus)) {
            throw new Error("Invalid order status.");
        }

        // An unchanged status must not publish an event.
        if (previousStatus === newStatus) {
            return false;
        }

        const allowedTransitions = {
            [OrderStatus.PENDING]: [
                OrderStatus.PROCESSING,
                OrderStatus.CANCELLED
            ],
            [OrderStatus.PROCESSING]: [
                OrderStatus.SHIPPED,
                OrderStatus.CANCELLED
            ],
            [OrderStatus.SHIPPED]: [
                OrderStatus.DELIVERED
            ],
            [OrderStatus.DELIVERED]: [],
            [OrderStatus.CANCELLED]: []
        };

        if (!allowedTransitions[previousStatus].includes(newStatus)) {
            throw new Error(
                `Invalid status transition: ${previousStatus} -> ${newStatus}`
            );
        }

        // Update the order before publishing the event.
        this.repository.updateStatus(orderId, newStatus);

        const notifiableStatuses = [
            OrderStatus.SHIPPED,
            OrderStatus.DELIVERED
        ];

        if (notifiableStatuses.includes(newStatus)) {
            const event = new OrderStatusChangedEvent(
                order.orderId,
                previousStatus,
                newStatus,
                order.customerPhone
            );

            this.publisher.publish(event);
        }

        return true;
    }
}

module.exports = {
    OrderStatus,
    OrderStatusChangedEvent,
    OrderStatusObserver,
    EventPublisher,
    SmsProvider,
    UnconfiguredSmsProvider,
    OrderStatusSmsListener,
    AuditObserver,
    Order,
    OrderRepository,
    OrderStatusService
};

// Demonstration entry point.
if (require.main === module) {
    const repository = new OrderRepository();
    const publisher = new EventPublisher();

    const smsProvider = new UnconfiguredSmsProvider();
    const smsListener = new OrderStatusSmsListener(smsProvider);
    const auditObserver = new AuditObserver();

    publisher.subscribe(smsListener);
    publisher.subscribe(auditObserver);

    repository.add(
        new Order("ORD-1001", "+910000000000")
    );

    const service = new OrderStatusService(repository, publisher);

    service.updateStatus("ORD-1001", OrderStatus.PROCESSING);
    service.updateStatus("ORD-1001", OrderStatus.SHIPPED);

    console.log(
        "Current status:",
        repository.get("ORD-1001").status
    );
    console.log("Audit events:", auditObserver.events.length);
}
