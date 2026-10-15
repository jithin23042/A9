
"use strict";

const test = require("node:test");
const assert = require("node:assert/strict");

const {
    OrderStatus,
    OrderStatusObserver,
    EventPublisher,
    Order,
    OrderRepository,
    OrderStatusService,
    OrderStatusSmsListener,
    SmsProvider,
    UnconfiguredSmsProvider,
    AuditObserver
} = require("./order_status_observer");

// Fake provider records SMS requests without sending real messages.
class FakeSmsProvider extends SmsProvider {
    constructor() {
        super();
        this.messages = [];
    }

    sendSms(phone, message) {
        this.messages.push({ phone, message });
        return true;
    }
}

// Records events received by an observer.
class RecordingObserver extends OrderStatusObserver {
    constructor() {
        super();
        this.events = [];
    }

    update(event) {
        this.events.push(event);
    }
}

function createFixture(phone = "+910000000000") {
    const repository = new OrderRepository();
    const publisher = new EventPublisher();
    const provider = new FakeSmsProvider();
    const smsListener = new OrderStatusSmsListener(provider);
    const auditObserver = new AuditObserver();

    publisher.subscribe(smsListener);
    publisher.subscribe(auditObserver);

    repository.add(new Order("ORD-1001", phone));

    const service = new OrderStatusService(repository, publisher);

    return {
        repository,
        publisher,
        provider,
        auditObserver,
        service
    };
}

// Test 1: SHIPPED publishes an event and requests an SMS.
test("SHIPPED publishes an event and requests SMS", () => {
    const fixture = createFixture();

    fixture.service.updateStatus("ORD-1001", OrderStatus.PROCESSING);
    const changed = fixture.service.updateStatus(
        "ORD-1001",
        OrderStatus.SHIPPED
    );

    assert.equal(changed, true);
    assert.equal(fixture.auditObserver.events.length, 1);
    assert.equal(fixture.provider.messages.length, 1);
    assert.match(
        fixture.provider.messages[0].message,
        /SHIPPED/
    );
});

// Test 2: DELIVERED publishes an event and requests an SMS.
test("DELIVERED publishes an event and requests SMS", () => {
    const fixture = createFixture();

    fixture.service.updateStatus("ORD-1001", OrderStatus.PROCESSING);
    fixture.service.updateStatus("ORD-1001", OrderStatus.SHIPPED);
    fixture.service.updateStatus("ORD-1001", OrderStatus.DELIVERED);

    assert.equal(fixture.auditObserver.events.length, 2);
    assert.equal(fixture.provider.messages.length, 2);
    assert.match(
        fixture.provider.messages[1].message,
        /DELIVERED/
    );
});

// Test 3: An unchanged status publishes no event.
test("unchanged status publishes no event", () => {
    const fixture = createFixture();

    const changed = fixture.service.updateStatus(
        "ORD-1001",
        OrderStatus.PENDING
    );

    assert.equal(changed, false);
    assert.equal(fixture.auditObserver.events.length, 0);
    assert.equal(fixture.provider.messages.length, 0);
});

// Test 4: PROCESSING does not publish a notification event.
test("PROCESSING does not publish an event", () => {
    const fixture = createFixture();

    fixture.service.updateStatus("ORD-1001", OrderStatus.PROCESSING);

    assert.equal(fixture.auditObserver.events.length, 0);
    assert.equal(fixture.provider.messages.length, 0);
});

// Test 5: Invalid transitions are rejected without changing status.
test("invalid status transition is rejected", () => {
    const fixture = createFixture();

    assert.throws(
        () => fixture.service.updateStatus(
            "ORD-1001",
            OrderStatus.DELIVERED
        ),
        /Invalid status transition/
    );

    assert.equal(
        fixture.repository.get("ORD-1001").status,
        OrderStatus.PENDING
    );
    assert.equal(fixture.auditObserver.events.length, 0);
});

// Test 6: Multiple observers receive the same event.
test("multiple observers receive the same event", () => {
    const fixture = createFixture();
    const secondObserver = new RecordingObserver();

    fixture.publisher.subscribe(secondObserver);

    fixture.service.updateStatus("ORD-1001", OrderStatus.PROCESSING);
    fixture.service.updateStatus("ORD-1001", OrderStatus.SHIPPED);

    assert.equal(fixture.auditObserver.events.length, 1);
    assert.equal(secondObserver.events.length, 1);
    assert.equal(
        secondObserver.events[0].newStatus,
        OrderStatus.SHIPPED
    );
});

// Test 7: Unknown orders are rejected.
test("unknown order is rejected", () => {
    const fixture = createFixture();

    assert.throws(
        () => fixture.service.updateStatus(
            "UNKNOWN",
            OrderStatus.SHIPPED
        ),
        /Order not found/
    );

    assert.equal(fixture.auditObserver.events.length, 0);
});

// Test 8: Duplicate observer registration is rejected.
test("duplicate observer registration is rejected", () => {
    const fixture = createFixture();
    const observer = new RecordingObserver();

    fixture.publisher.subscribe(observer);

    assert.throws(
        () => fixture.publisher.subscribe(observer),
        /Observer already registered/
    );
});

// Test 9: A missing phone number prevents SMS delivery.
test("missing phone number prevents SMS delivery", () => {
    const fixture = createFixture("   ");

    fixture.service.updateStatus("ORD-1001", OrderStatus.PROCESSING);
    fixture.service.updateStatus("ORD-1001", OrderStatus.SHIPPED);

    assert.equal(fixture.auditObserver.events.length, 1);
    assert.equal(fixture.provider.messages.length, 0);
});

// Test 10: The unconfigured provider does not claim delivery.
test("unconfigured SMS provider returns false", () => {
    const provider = new UnconfiguredSmsProvider();

    const result = provider.sendSms(
        "+910000000000",
        "Test notification"
    );

    assert.equal(result, false);
});
