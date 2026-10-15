#include <algorithm>
#include <iostream>
#include <memory>
#include <stdexcept>
#include <string>
#include <unordered_map>
#include <unordered_set>
#include <utility>
#include <vector>

enum class OrderStatus { PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED };

inline std::string toString(OrderStatus status) {
    switch (status) {
        case OrderStatus::PENDING: return "PENDING";
        case OrderStatus::PROCESSING: return "PROCESSING";
        case OrderStatus::SHIPPED: return "SHIPPED";
        case OrderStatus::DELIVERED: return "DELIVERED";
        case OrderStatus::CANCELLED: return "CANCELLED";
    }
    throw std::invalid_argument("Invalid order status.");
}

struct OrderStatusChangedEvent {
    std::string orderId;
    OrderStatus previousStatus;
    OrderStatus newStatus;
    std::string customerPhone;
};

class OrderStatusListener {
public:
    virtual ~OrderStatusListener() = default;
    virtual void update(const OrderStatusChangedEvent& event) = 0;
};

class EventPublisher {
private:
    std::vector<OrderStatusListener*> observers;
public:
    void subscribe(OrderStatusListener* observer) {
        if (!observer) throw std::invalid_argument("Observer cannot be null.");
        if (std::find(observers.begin(), observers.end(), observer) != observers.end())
            throw std::invalid_argument("Observer already registered.");
        observers.push_back(observer);
    }
    void unsubscribe(OrderStatusListener* observer) {
        observers.erase(std::remove(observers.begin(), observers.end(), observer), observers.end());
    }
    void publish(const OrderStatusChangedEvent& event) const {
        const auto snapshot = observers;
        for (auto* observer : snapshot) observer->update(event);
    }
    std::size_t observerCount() const { return observers.size(); }
};

class SmsProvider {
public:
    virtual ~SmsProvider() = default;
    virtual bool sendSms(const std::string& phone, const std::string& message) = 0;
};

class UnconfiguredSmsProvider final : public SmsProvider {
public:
    bool sendSms(const std::string&, const std::string&) override {
        std::cout << "SMS not sent: no SMS provider is configured.\n";
        return false;
    }
};

class OrderStatusSmsListener final : public OrderStatusListener {
private:
    SmsProvider& provider;
public:
    explicit OrderStatusSmsListener(SmsProvider& provider) : provider(provider) {}
    void update(const OrderStatusChangedEvent& event) override {
        if (event.newStatus != OrderStatus::SHIPPED && event.newStatus != OrderStatus::DELIVERED) return;
        if (event.customerPhone.find_first_not_of(" \t\n\r") == std::string::npos) return;
        const std::string message = "Order " + event.orderId + " status changed to " + toString(event.newStatus) + ".";
        provider.sendSms(event.customerPhone, message);
    }
};

class AuditObserver final : public OrderStatusListener {
private:
    std::vector<OrderStatusChangedEvent> events;
public:
    void update(const OrderStatusChangedEvent& event) override { events.push_back(event); }
    const std::vector<OrderStatusChangedEvent>& getEvents() const { return events; }
};

struct Order {
    std::string orderId;
    std::string customerPhone;
    OrderStatus status = OrderStatus::PENDING;
};

class OrderRepository {
private:
    std::unordered_map<std::string, Order> orders;
public:
    void add(const Order& order) {
        if (order.orderId.empty()) throw std::invalid_argument("Order ID cannot be empty.");
        if (orders.find(order.orderId) != orders.end()) throw std::invalid_argument("Order already exists: " + order.orderId);
        orders.emplace(order.orderId, order);
    }
    Order& get(const std::string& orderId) {
        auto it = orders.find(orderId);
        if (it == orders.end()) throw std::invalid_argument("Order not found: " + orderId);
        return it->second;
    }
    const Order& get(const std::string& orderId) const {
        auto it = orders.find(orderId);
        if (it == orders.end()) throw std::invalid_argument("Order not found: " + orderId);
        return it->second;
    }
};

class OrderStatusService {
private:
    OrderRepository& repository;
    EventPublisher& publisher;

    static bool isAllowed(OrderStatus oldStatus, OrderStatus newStatus) {
        switch (oldStatus) {
            case OrderStatus::PENDING: return newStatus == OrderStatus::PROCESSING || newStatus == OrderStatus::CANCELLED;
            case OrderStatus::PROCESSING: return newStatus == OrderStatus::SHIPPED || newStatus == OrderStatus::CANCELLED;
            case OrderStatus::SHIPPED: return newStatus == OrderStatus::DELIVERED;
            case OrderStatus::DELIVERED:
            case OrderStatus::CANCELLED: return false;
        }
        return false;
    }
public:
    OrderStatusService(OrderRepository& repository, EventPublisher& publisher)
        : repository(repository), publisher(publisher) {}

    bool updateStatus(const std::string& orderId, OrderStatus newStatus) {
        Order& order = repository.get(orderId);
        const OrderStatus previous = order.status;
        if (previous == newStatus) return false;
        if (!isAllowed(previous, newStatus)) {
            throw std::invalid_argument("Invalid status transition: " + toString(previous) + " -> " + toString(newStatus));
        }
        order.status = newStatus;
        if (newStatus == OrderStatus::SHIPPED || newStatus == OrderStatus::DELIVERED) {
            publisher.publish({order.orderId, previous, newStatus, order.customerPhone});
        }
        return true;
    }
};

class RecordingSmsProvider final : public SmsProvider {
private:
    std::vector<std::string> recipients;
    std::vector<std::string> messages;
    bool shouldSucceed;
public:
    explicit RecordingSmsProvider(bool shouldSucceed = true) : shouldSucceed(shouldSucceed) {}
    bool sendSms(const std::string& phone, const std::string& message) override {
        recipients.push_back(phone);
        messages.push_back(message);
        return shouldSucceed;
    }
    const std::vector<std::string>& getRecipients() const { return recipients; }
    const std::vector<std::string>& getMessages() const { return messages; }
};

#ifndef ORDER_STATUS_OBSERVER_TESTING
int main() {
    OrderRepository repository;
    EventPublisher publisher;
    UnconfiguredSmsProvider provider;
    OrderStatusSmsListener smsListener(provider);
    AuditObserver audit;
    publisher.subscribe(&smsListener);
    publisher.subscribe(&audit);
    repository.add({"ORD-1001", "+910000000000", OrderStatus::PENDING});
    OrderStatusService service(repository, publisher);
    service.updateStatus("ORD-1001", OrderStatus::PROCESSING);
    service.updateStatus("ORD-1001", OrderStatus::SHIPPED);
    std::cout << "Current status: " << toString(repository.get("ORD-1001").status) << '\n';
    std::cout << "Audit events: " << audit.getEvents().size() << '\n';
    return 0;
}
#endif
