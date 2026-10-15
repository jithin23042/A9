
package A9.Part3.Observer_Order_SMS.java;

import java.util.ArrayList;
import java.util.List;

class FakeSmsProvider implements SmsProvider {
    private final List<String> messages = new ArrayList<>();

    @Override
    public boolean sendSms(String phone, String message) {
        messages.add(phone + "|" + message);
        return true;
    }

    public List<String> getMessages() {
        return List.copyOf(messages);
    }
}

class RecordingObserver implements OrderStatusListener {
    private final List<OrderStatusChangedEvent> events =
            new ArrayList<>();

    @Override
    public void update(OrderStatusChangedEvent event) {
        events.add(event);
    }

    public List<OrderStatusChangedEvent> getEvents() {
        return List.copyOf(events);
    }
}

public class TestOrderStatusObserver {
    private static int passed = 0;
    private static int failed = 0;

    private static void check(String name, Runnable test) {
        try {
            test.run();
            passed++;
            System.out.println("PASS: " + name);
        } catch (Throwable error) {
            failed++;
            System.out.println(
                    "FAIL: " + name + " - " + error.getMessage());
        }
    }

    private static void expectException(Runnable action) {
        try {
            action.run();
        } catch (IllegalArgumentException exception) {
            return;
        }
        throw new AssertionError(
                "Expected IllegalArgumentException.");
    }

    private static class Fixture {
        final OrderRepository repository = new OrderRepository();
        final EventPublisher publisher = new EventPublisher();
        final FakeSmsProvider provider = new FakeSmsProvider();
        final AuditObserver audit = new AuditObserver();
        final OrderStatusService service;

        Fixture(String phone) {
            publisher.subscribe(new OrderStatusSmsListener(provider));
            publisher.subscribe(audit);
            repository.add(new Order("ORD-1001", phone));
            service = new OrderStatusService(repository, publisher);
        }

        void shipOrder() {
            service.updateStatus("ORD-1001", OrderStatus.PROCESSING);
            service.updateStatus("ORD-1001", OrderStatus.SHIPPED);
        }
    }

    public static void main(String[] args) {
        check("SHIPPED publishes event and requests SMS", () -> {
            Fixture f = new Fixture("+910000000000");
            f.shipOrder();
            assert f.audit.getEvents().size() == 1;
            assert f.provider.getMessages().size() == 1;
        });

        check("DELIVERED publishes event and requests SMS", () -> {
            Fixture f = new Fixture("+910000000000");
            f.shipOrder();
            f.service.updateStatus("ORD-1001", OrderStatus.DELIVERED);
            assert f.audit.getEvents().size() == 2;
            assert f.provider.getMessages().size() == 2;
        });

        check("unchanged status publishes no event", () -> {
            Fixture f = new Fixture("+910000000000");
            assert !f.service.updateStatus(
                    "ORD-1001", OrderStatus.PENDING);
            assert f.audit.getEvents().isEmpty();
        });

        check("PROCESSING publishes no notification event", () -> {
            Fixture f = new Fixture("+910000000000");
            f.service.updateStatus("ORD-1001", OrderStatus.PROCESSING);
            assert f.audit.getEvents().isEmpty();
            assert f.provider.getMessages().isEmpty();
        });

        check("invalid transition is rejected", () -> {
            Fixture f = new Fixture("+910000000000");
            expectException(() -> f.service.updateStatus(
                    "ORD-1001", OrderStatus.DELIVERED));
            assert f.repository.get("ORD-1001").getStatus()
                    == OrderStatus.PENDING;
        });

        check("multiple observers receive the same event", () -> {
            Fixture f = new Fixture("+910000000000");
            RecordingObserver second = new RecordingObserver();
            f.publisher.subscribe(second);
            f.shipOrder();
            assert f.audit.getEvents().size() == 1;
            assert second.getEvents().size() == 1;
            assert second.getEvents().get(0).newStatus()
                    == OrderStatus.SHIPPED;
        });

        check("unknown order is rejected", () -> {
            Fixture f = new Fixture("+910000000000");
            expectException(() -> f.service.updateStatus(
                    "UNKNOWN", OrderStatus.SHIPPED));
        });

        check("duplicate observer registration is rejected", () -> {
            Fixture f = new Fixture("+910000000000");
            RecordingObserver observer = new RecordingObserver();
            f.publisher.subscribe(observer);
            expectException(() -> f.publisher.subscribe(observer));
        });

        check("missing phone prevents SMS", () -> {
            Fixture f = new Fixture("   ");
            f.shipOrder();
            assert f.audit.getEvents().size() == 1;
            assert f.provider.getMessages().isEmpty();
        });

        check("unconfigured provider returns false", () -> {
            SmsProvider provider = new UnconfiguredSmsProvider();
            assert !provider.sendSms("+910000000000", "Test");
        });

        System.out.println();
        System.out.println("Tests passed: " + passed);
        System.out.println("Tests failed: " + failed);

        if (failed > 0) {
            System.exit(1);
        }
    }
}
