
import unittest

from order_status_observer import (
    AuditObserver,
    EventPublisher,
    Order,
    OrderRepository,
    OrderStatus,
    OrderStatusChangedEvent,
    OrderStatusObserver,
    OrderStatusService,
    OrderStatusSmsListener,
    SmsProvider,
    UnconfiguredSmsProvider,
)


# Records SMS requests without sending real messages.
class FakeSmsProvider(SmsProvider):

    def __init__(self):
        self.messages = []

    def send_sms(self, phone: str, message: str) -> bool:
        self.messages.append((phone, message))
        return True


# Records every event received by this observer.
class RecordingObserver(OrderStatusObserver):

    def __init__(self):
        self.events = []

    def update(self, event: OrderStatusChangedEvent) -> None:
        self.events.append(event)


class TestOrderStatusObserver(unittest.TestCase):

    def setUp(self):
        self.repository = OrderRepository()
        self.publisher = EventPublisher()
        self.sms_provider = FakeSmsProvider()
        self.sms_listener = OrderStatusSmsListener(self.sms_provider)
        self.audit_observer = AuditObserver()

        self.publisher.subscribe(self.sms_listener)
        self.publisher.subscribe(self.audit_observer)

        self.repository.add(
            Order("ORD-1001", "+910000000000")
        )

        self.service = OrderStatusService(
            self.repository, self.publisher
        )

    # Test 1: SHIPPED publishes an event and requests an SMS.
    def test_shipped_publishes_event_and_requests_sms(self):
        self.service.update_status(
            "ORD-1001", OrderStatus.PROCESSING
        )
        changed = self.service.update_status(
            "ORD-1001", OrderStatus.SHIPPED
        )

        self.assertTrue(changed)
        self.assertEqual(len(self.audit_observer.events), 1)
        self.assertEqual(len(self.sms_provider.messages), 1)
        self.assertIn(
            "SHIPPED", self.sms_provider.messages[0][1]
        )

    # Test 2: DELIVERED publishes an event and requests an SMS.
    def test_delivered_publishes_event_and_requests_sms(self):
        self.service.update_status(
            "ORD-1001", OrderStatus.PROCESSING
        )
        self.service.update_status(
            "ORD-1001", OrderStatus.SHIPPED
        )
        self.service.update_status(
            "ORD-1001", OrderStatus.DELIVERED
        )

        self.assertEqual(len(self.audit_observer.events), 2)
        self.assertEqual(len(self.sms_provider.messages), 2)
        self.assertIn(
            "DELIVERED", self.sms_provider.messages[1][1]
        )

    # Test 3: An unchanged status publishes no event.
    def test_unchanged_status_publishes_no_event(self):
        changed = self.service.update_status(
            "ORD-1001", OrderStatus.PENDING
        )

        self.assertFalse(changed)
        self.assertEqual(len(self.audit_observer.events), 0)
        self.assertEqual(len(self.sms_provider.messages), 0)

    # Test 4: PROCESSING does not publish an SMS event.
    def test_processing_does_not_publish_event(self):
        self.service.update_status(
            "ORD-1001", OrderStatus.PROCESSING
        )

        self.assertEqual(len(self.audit_observer.events), 0)
        self.assertEqual(len(self.sms_provider.messages), 0)

    # Test 5: An invalid transition leaves the status unchanged.
    def test_invalid_transition_rejected(self):
        with self.assertRaisesRegex(
            ValueError, "Invalid status transition"
        ):
            self.service.update_status(
                "ORD-1001", OrderStatus.DELIVERED
            )

        self.assertEqual(
            self.repository.get("ORD-1001").status,
            OrderStatus.PENDING
        )
        self.assertEqual(len(self.audit_observer.events), 0)

    # Test 6: Multiple observers receive the same event.
    def test_multiple_observers_receive_event(self):
        second_observer = RecordingObserver()
        self.publisher.subscribe(second_observer)

        self.service.update_status(
            "ORD-1001", OrderStatus.PROCESSING
        )
        self.service.update_status(
            "ORD-1001", OrderStatus.SHIPPED
        )

        self.assertEqual(len(self.audit_observer.events), 1)
        self.assertEqual(len(second_observer.events), 1)
        self.assertEqual(
            second_observer.events[0].new_status,
            OrderStatus.SHIPPED
        )

    # Test 7: An unknown order is rejected.
    def test_unknown_order_rejected(self):
        with self.assertRaisesRegex(ValueError, "Order not found"):
            self.service.update_status(
                "UNKNOWN", OrderStatus.SHIPPED
            )

        self.assertEqual(len(self.audit_observer.events), 0)

    # Test 8: Duplicate observer registration is rejected.
    def test_duplicate_observer_rejected(self):
        with self.assertRaisesRegex(
            ValueError, "Observer already registered"
        ):
            self.publisher.subscribe(self.sms_listener)

    # Test 9: An empty phone number prevents SMS delivery.
    def test_missing_phone_prevents_sms(self):
        repository = OrderRepository()
        publisher = EventPublisher()
        provider = FakeSmsProvider()

        publisher.subscribe(OrderStatusSmsListener(provider))
        repository.add(Order("ORD-1002", "   "))

        service = OrderStatusService(repository, publisher)
        service.update_status(
            "ORD-1002", OrderStatus.PROCESSING
        )
        service.update_status(
            "ORD-1002", OrderStatus.SHIPPED
        )

        self.assertEqual(len(provider.messages), 0)

    # Test 10: The default provider does not claim delivery.
    def test_unconfigured_provider_returns_false(self):
        provider = UnconfiguredSmsProvider()

        result = provider.send_sms(
            "+910000000000", "Test notification"
        )

        self.assertFalse(result)


if __name__ == "__main__":
    unittest.main(verbosity=2)
