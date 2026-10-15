#define ORDER_STATUS_OBSERVER_TESTING
#include "order_status_observer.cpp"
#include <functional>

class CountingObserver final : public OrderStatusListener {
public:
    int count = 0;
    OrderStatusChangedEvent lastEvent{"", OrderStatus::PENDING, OrderStatus::PENDING, ""};
    void update(const OrderStatusChangedEvent& event) override { ++count; lastEvent = event; }
};

int main() {
    int passed = 0, failed = 0;
    auto run = [&](const std::string& name, const std::function<void()>& test) {
        try { test(); ++passed; std::cout << "PASS: " << name << '\n'; }
        catch (const std::exception& e) { ++failed; std::cout << "FAIL: " << name << ": " << e.what() << '\n'; }
    };

    run("SHIPPED publishes event and requests SMS", [] {
        OrderRepository repo; EventPublisher publisher; RecordingSmsProvider provider;
        OrderStatusSmsListener sms(provider); AuditObserver audit;
        publisher.subscribe(&sms); publisher.subscribe(&audit);
        repo.add({"ORD-1", "+911234567890", OrderStatus::PROCESSING});
        OrderStatusService service(repo, publisher);
        service.updateStatus("ORD-1", OrderStatus::SHIPPED);
        if (audit.getEvents().size() != 1 || provider.getMessages().size() != 1 || audit.getEvents()[0].newStatus != OrderStatus::SHIPPED)
            throw std::runtime_error("SHIPPED event/SMS not recorded");
    });
    run("DELIVERED publishes event and requests SMS", [] {
        OrderRepository repo; EventPublisher publisher; RecordingSmsProvider provider;
        OrderStatusSmsListener sms(provider); AuditObserver audit;
        publisher.subscribe(&sms); publisher.subscribe(&audit);
        repo.add({"ORD-2", "+911234567890", OrderStatus::SHIPPED});
        OrderStatusService service(repo, publisher);
        service.updateStatus("ORD-2", OrderStatus::DELIVERED);
        if (audit.getEvents().size() != 1 || provider.getMessages().size() != 1 || audit.getEvents()[0].newStatus != OrderStatus::DELIVERED)
            throw std::runtime_error("DELIVERED event/SMS not recorded");
    });
    run("Unchanged status publishes no event", [] {
        OrderRepository repo; EventPublisher publisher; AuditObserver audit; publisher.subscribe(&audit);
        repo.add({"ORD-3", "+911234567890", OrderStatus::PENDING});
        OrderStatusService service(repo, publisher);
        if (service.updateStatus("ORD-3", OrderStatus::PENDING) || !audit.getEvents().empty()) throw std::runtime_error("Unexpected event");
    });
    run("PROCESSING publishes no notification event", [] {
        OrderRepository repo; EventPublisher publisher; AuditObserver audit; publisher.subscribe(&audit);
        repo.add({"ORD-4", "+911234567890", OrderStatus::PENDING});
        OrderStatusService service(repo, publisher); service.updateStatus("ORD-4", OrderStatus::PROCESSING);
        if (!audit.getEvents().empty()) throw std::runtime_error("Non-notifiable status published event");
    });
    run("Invalid transition is rejected", [] {
        OrderRepository repo; EventPublisher publisher; repo.add({"ORD-5", "+911234567890", OrderStatus::PENDING});
        OrderStatusService service(repo, publisher);
        try { service.updateStatus("ORD-5", OrderStatus::DELIVERED); } catch (const std::invalid_argument&) { return; }
        throw std::runtime_error("Expected invalid_argument");
    });
    run("Multiple observers receive the same event", [] {
        OrderRepository repo; EventPublisher publisher; CountingObserver first, second;
        publisher.subscribe(&first); publisher.subscribe(&second);
        repo.add({"ORD-6", "+911234567890", OrderStatus::PROCESSING});
        OrderStatusService service(repo, publisher); service.updateStatus("ORD-6", OrderStatus::SHIPPED);
        if (first.count != 1 || second.count != 1 || first.lastEvent.orderId != second.lastEvent.orderId || first.lastEvent.newStatus != second.lastEvent.newStatus)
            throw std::runtime_error("Observers did not receive the same event");
    });
    run("Unknown order is rejected", [] {
        OrderRepository repo; EventPublisher publisher; OrderStatusService service(repo, publisher);
        try { service.updateStatus("missing", OrderStatus::SHIPPED); } catch (const std::invalid_argument&) { return; }
        throw std::runtime_error("Expected invalid_argument");
    });
    run("Duplicate observer registration is rejected", [] {
        EventPublisher publisher; CountingObserver observer; publisher.subscribe(&observer);
        try { publisher.subscribe(&observer); } catch (const std::invalid_argument&) { return; }
        throw std::runtime_error("Expected invalid_argument");
    });
    run("Missing phone prevents SMS request", [] {
        OrderRepository repo; EventPublisher publisher; RecordingSmsProvider provider; OrderStatusSmsListener sms(provider); publisher.subscribe(&sms);
        repo.add({"ORD-9", "   ", OrderStatus::PROCESSING}); OrderStatusService service(repo, publisher);
        service.updateStatus("ORD-9", OrderStatus::SHIPPED);
        if (!provider.getMessages().empty()) throw std::runtime_error("SMS requested without phone number");
    });
    run("Unconfigured SMS provider returns false", [] {
        UnconfiguredSmsProvider provider;
        if (provider.sendSms("+911234567890", "test")) throw std::runtime_error("Unconfigured provider must return false");
    });

    std::cout << "\nTests passed: " << passed << "\nTests failed: " << failed << '\n';
    return failed == 0 ? 0 : 1;
}
