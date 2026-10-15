
from abc import ABC, abstractmethod
from dataclasses import dataclass
from enum import Enum


# Defines the order statuses used by this simulation.
class OrderStatus(str, Enum):
    PENDING = "PENDING"
    PROCESSING = "PROCESSING"
    SHIPPED = "SHIPPED"
    DELIVERED = "DELIVERED"
    CANCELLED = "CANCELLED"


# Represents an order-status change after persistence.
@dataclass(frozen=True)
class OrderStatusChangedEvent:
    order_id: str
    previous_status: OrderStatus
    new_status: OrderStatus
    customer_phone: str


# Common contract for event observers.
class OrderStatusObserver(ABC):

    @abstractmethod
    def update(self, event: OrderStatusChangedEvent) -> None:
        """Handle a published order-status event."""
        raise NotImplementedError


# Publishes events to registered observers.
class EventPublisher:

    def __init__(self) -> None:
        self._observers: list[OrderStatusObserver] = []

    def subscribe(self, observer: OrderStatusObserver) -> None:
        if observer in self._observers:
            raise ValueError("Observer already registered.")

        self._observers.append(observer)

    def publish(self, event: OrderStatusChangedEvent) -> None:
        for observer in tuple(self._observers):
            observer.update(event)


# Defines the SMS delivery contract.
class SmsProvider(ABC):

    @abstractmethod
    def send_sms(self, phone: str, message: str) -> bool:
        """Return True if the provider accepts the message."""
        raise NotImplementedError


# Default provider: no real SMS service is configured.
class UnconfiguredSmsProvider(SmsProvider):

    def send_sms(self, phone: str, message: str) -> bool:
        print("SMS not sent: no SMS provider is configured.")
        return False


# Observer that handles relevant order-status events.
class OrderStatusSmsListener(OrderStatusObserver):

    NOTIFIABLE_STATUSES = {
        OrderStatus.SHIPPED,
        OrderStatus.DELIVERED,
    }

    def __init__(self, provider: SmsProvider) -> None:
        self._provider = provider

    def update(self, event: OrderStatusChangedEvent) -> None:
        if event.new_status not in self.NOTIFIABLE_STATUSES:
            return

        if not event.customer_phone.strip():
            return

        message = (
            f"Order {event.order_id} status changed to "
            f"{event.new_status.value}."
        )

        self._provider.send_sms(event.customer_phone, message)


# Additional observer used to demonstrate multiple subscribers.
class AuditObserver(OrderStatusObserver):

    def __init__(self) -> None:
        self.events: list[OrderStatusChangedEvent] = []

    def update(self, event: OrderStatusChangedEvent) -> None:
        self.events.append(event)


# Represents a stored order in the standalone simulation.
@dataclass
class Order:
    order_id: str
    customer_phone: str
    status: OrderStatus = OrderStatus.PENDING


# In-memory repository used instead of a database.
class OrderRepository:

    def __init__(self) -> None:
        self._orders: dict[str, Order] = {}

    def add(self, order: Order) -> None:
        if order.order_id in self._orders:
            raise ValueError(f"Order already exists: {order.order_id}")

        self._orders[order.order_id] = order

    def get(self, order_id: str) -> Order:
        if order_id not in self._orders:
            raise ValueError(f"Order not found: {order_id}")

        return self._orders[order_id]

    def update_status(
        self, order_id: str, new_status: OrderStatus
    ) -> None:
        self.get(order_id).status = new_status


# Coordinates status changes, persistence and event publication.
class OrderStatusService:

    ALLOWED_TRANSITIONS = {
        OrderStatus.PENDING: {
            OrderStatus.PROCESSING,
            OrderStatus.CANCELLED,
        },
        OrderStatus.PROCESSING: {
            OrderStatus.SHIPPED,
            OrderStatus.CANCELLED,
        },
        OrderStatus.SHIPPED: {
            OrderStatus.DELIVERED,
        },
        OrderStatus.DELIVERED: set(),
        OrderStatus.CANCELLED: set(),
    }

    NOTIFIABLE_STATUSES = {
        OrderStatus.SHIPPED,
        OrderStatus.DELIVERED,
    }

    def __init__(
        self,
        repository: OrderRepository,
        publisher: EventPublisher,
    ) -> None:
        self._repository = repository
        self._publisher = publisher

    def update_status(
        self, order_id: str, new_status: OrderStatus
    ) -> bool:
        order = self._repository.get(order_id)

        if not isinstance(new_status, OrderStatus):
            raise ValueError("Invalid order status.")

        previous_status = order.status

        # An unchanged status must not produce an event.
        if previous_status == new_status:
            return False

        # Reject invalid transitions before modifying the order.
        if new_status not in self.ALLOWED_TRANSITIONS[previous_status]:
            raise ValueError(
                f"Invalid status transition: "
                f"{previous_status.value} -> {new_status.value}"
            )

        # Persist the new status before publishing an event.
        self._repository.update_status(order_id, new_status)

        # Publish only for the two relevant fulfilment statuses.
        if new_status in self.NOTIFIABLE_STATUSES:
            event = OrderStatusChangedEvent(
                order_id=order.order_id,
                previous_status=previous_status,
                new_status=new_status,
                customer_phone=order.customer_phone,
            )
            self._publisher.publish(event)

        return True


# Demonstration entry point.
if __name__ == "__main__":
    repository = OrderRepository()
    publisher = EventPublisher()

    sms_provider = UnconfiguredSmsProvider()
    sms_listener = OrderStatusSmsListener(sms_provider)
    audit_observer = AuditObserver()

    publisher.subscribe(sms_listener)
    publisher.subscribe(audit_observer)

    repository.add(
        Order(
            order_id="ORD-1001",
            customer_phone="+910000000000",
        )
    )

    service = OrderStatusService(repository, publisher)

    service.update_status("ORD-1001", OrderStatus.PROCESSING)
    service.update_status("ORD-1001", OrderStatus.SHIPPED)

    print("Current status:", repository.get("ORD-1001").status.value)
    print("Audit events:", len(audit_observer.events))
