
package A9.Part3.Observer_Order_SMS.java;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Supported order statuses.
enum OrderStatus {
    PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED
}

// Represents an order.
class Order {
    private final String orderId;
    private final String customerPhone;
    private OrderStatus status;

    public Order(String orderId, String customerPhone) {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException(
                    "Order ID must not be empty.");
        }

        this.orderId = orderId;
        this.customerPhone = customerPhone;
        this.status = OrderStatus.PENDING;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}

// Immutable event describing an order-status change.
record OrderStatusChangedEvent(
        String orderId,
        OrderStatus previousStatus,
        OrderStatus newStatus,
        String customerPhone) {
}

// Observer contract.
interface OrderStatusListener {
    void update(OrderStatusChangedEvent event);
}

// Publisher that notifies registered observers.
class EventPublisher {
    private final List<OrderStatusListener> observers =
            new ArrayList<>();

    public void subscribe(OrderStatusListener observer) {
        if (observer == null) {
            throw new IllegalArgumentException(
                    "Invalid observer.");
        }

        if (observers.contains(observer)) {
            throw new IllegalArgumentException(
                    "Observer already registered.");
        }

        observers.add(observer);
    }

    public void publish(OrderStatusChangedEvent event) {
        if (event == null) {
            throw new IllegalArgumentException(
                    "Event cannot be null.");
        }

        for (OrderStatusListener observer :
                new ArrayList<>(observers)) {
            observer.update(event);
        }
    }
}

// SMS-provider abstraction.
interface SmsProvider {
    boolean sendSms(String phone, String message);
}

// Default provider: no real SMS service is configured.
class UnconfiguredSmsProvider implements SmsProvider {
    @Override
    public boolean sendSms(String phone, String message) {
        System.out.println(
                "SMS not sent: no SMS provider is configured.");
        return false;
    }
}

// Observer that requests SMS for qualifying status changes.
class OrderStatusSmsListener implements OrderStatusListener {
    private final SmsProvider provider;

    public OrderStatusSmsListener(SmsProvider provider) {
        if (provider == null) {
            throw new IllegalArgumentException(
                    "SMS provider cannot be null.");
        }

        this.provider = provider;
    }

    @Override
    public void update(OrderStatusChangedEvent event) {
        if (event.newStatus() != OrderStatus.SHIPPED
                && event.newStatus() != OrderStatus.DELIVERED) {
            return;
        }

        if (event.customerPhone() == null
                || event.customerPhone().isBlank()) {
            return;
        }

        String message = "Order " + event.orderId()
                + " status changed to "
                + event.newStatus() + ".";

        provider.sendSms(event.customerPhone(), message);
    }
}

// Observer that records qualifying events.
class AuditObserver implements OrderStatusListener {
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

// In-memory order repository.
class OrderRepository {
    private final Map<String, Order> orders = new HashMap<>();

    public void add(Order order) {
        if (order == null) {
            throw new IllegalArgumentException(
                    "Order cannot be null.");
        }

        if (orders.putIfAbsent(order.getOrderId(), order) != null) {
            throw new IllegalArgumentException(
                    "Order already exists: " + order.getOrderId());
        }
    }

    public Order get(String orderId) {
        Order order = orders.get(orderId);

        if (order == null) {
            throw new IllegalArgumentException(
                    "Order not found: " + orderId);
        }

        return order;
    }

    public void updateStatus(
            String orderId, OrderStatus status) {
        get(orderId).setStatus(status);
    }
}

// Coordinates status transitions and event publication.
class OrderStatusService {
    private final OrderRepository repository;
    private final EventPublisher publisher;

    private static final Map<OrderStatus, Set<OrderStatus>>
            ALLOWED_TRANSITIONS = createTransitions();

    private static Map<OrderStatus, Set<OrderStatus>>
    createTransitions() {
        Map<OrderStatus, Set<OrderStatus>> transitions =
                new EnumMap<>(OrderStatus.class);

        transitions.put(
                OrderStatus.PENDING,
                EnumSet.of(
                        OrderStatus.PROCESSING,
                        OrderStatus.CANCELLED));

        transitions.put(
                OrderStatus.PROCESSING,
                EnumSet.of(
                        OrderStatus.SHIPPED,
                        OrderStatus.CANCELLED));

        transitions.put(
                OrderStatus.SHIPPED,
                EnumSet.of(OrderStatus.DELIVERED));

        transitions.put(
                OrderStatus.DELIVERED,
                EnumSet.noneOf(OrderStatus.class));

        transitions.put(
                OrderStatus.CANCELLED,
                EnumSet.noneOf(OrderStatus.class));

        return Map.copyOf(transitions);
    }

    public OrderStatusService(
            OrderRepository repository,
            EventPublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    public boolean updateStatus(
            String orderId, OrderStatus newStatus) {
        Order order = repository.get(orderId);
        OrderStatus previousStatus = order.getStatus();

        if (newStatus == null) {
            throw new IllegalArgumentException(
                    "Invalid order status.");
        }

        if (previousStatus == newStatus) {
            return false;
        }

        if (!ALLOWED_TRANSITIONS.get(previousStatus)
                .contains(newStatus)) {
            throw new IllegalArgumentException(
                    "Invalid status transition: "
                            + previousStatus + " -> " + newStatus);
        }

        // Update the order before publishing the event.
        repository.updateStatus(orderId, newStatus);

        // Publish events only for qualifying fulfilment statuses.
        if (newStatus == OrderStatus.SHIPPED
                || newStatus == OrderStatus.DELIVERED) {
            publisher.publish(new OrderStatusChangedEvent(
                    order.getOrderId(),
                    previousStatus,
                    newStatus,
                    order.getCustomerPhone()));
        }

        return true;
    }
}

// Demonstration entry point.
public class OrderStatusObserver {
    public static void main(String[] args) {
        OrderRepository repository = new OrderRepository();
        EventPublisher publisher = new EventPublisher();

        SmsProvider provider = new UnconfiguredSmsProvider();

        OrderStatusSmsListener smsListener =
                new OrderStatusSmsListener(provider);

        AuditObserver auditObserver = new AuditObserver();

        publisher.subscribe(smsListener);
        publisher.subscribe(auditObserver);

        repository.add(
                new Order("ORD-1001", "+910000000000"));

        OrderStatusService service =
                new OrderStatusService(repository, publisher);

        service.updateStatus(
                "ORD-1001", OrderStatus.PROCESSING);

        service.updateStatus(
                "ORD-1001", OrderStatus.SHIPPED);

        System.out.println(
                "Current status: "
                        + repository.get("ORD-1001").getStatus());

        System.out.println(
                "Audit events: "
                        + auditObserver.getEvents().size());
    }
}
