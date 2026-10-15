
"use strict";

const test = require("node:test");
const assert = require("node:assert/strict");

const {
    PaymentStrategy,
    CardPayment,
    UPIPayment,
    PaymentProcessor
} = require("./payment_strategy");

// Additional strategy used to verify extensibility.
class RazorpayPayment extends PaymentStrategy {
    get providerKey() {
        return "razorpay";
    }

    pay(amount) {
        const { PaymentResult } = require("./payment_strategy");

        return new PaymentResult(
            this.providerKey,
            amount,
            "SUCCESS",
            "Razorpay payment simulated successfully."
        );
    }
}

test("card payment succeeds", () => {
    const processor = new PaymentProcessor([new CardPayment()]);
    const result = processor.processPayment("card", 100);

    assert.equal(result.status, "SUCCESS");
    assert.equal(result.provider, "card");
    assert.equal(result.amount, 100);
});

test("UPI payment succeeds", () => {
    const processor = new PaymentProcessor([new UPIPayment()]);
    const result = processor.processPayment("upi", 250.50);

    assert.equal(result.status, "SUCCESS");
    assert.equal(result.provider, "upi");
    assert.equal(result.amount, 250.50);
});

test("provider selection is case-insensitive", () => {
    const processor = new PaymentProcessor([new CardPayment()]);

    assert.equal(
        processor.processPayment("CARD", 50).provider,
        "card"
    );
});

test("a new Razorpay strategy can be registered", () => {
    const processor = new PaymentProcessor([
        new CardPayment(),
        new UPIPayment()
    ]);

    processor.registerStrategy(new RazorpayPayment());

    assert.equal(
        processor.processPayment("razorpay", 500).provider,
        "razorpay"
    );

    assert.equal(
        processor.processPayment("card", 100).provider,
        "card"
    );
});

test("duplicate provider registration is rejected", () => {
    const processor = new PaymentProcessor([new CardPayment()]);

    assert.throws(
        () => processor.registerStrategy(new CardPayment()),
        /already registered/
    );
});

test("unknown provider is rejected", () => {
    const processor = new PaymentProcessor([new CardPayment()]);

    assert.throws(
        () => processor.processPayment("bitcoin", 100),
        /Unknown payment provider/
    );
});

test("zero and negative amounts are rejected", () => {
    const processor = new PaymentProcessor([new CardPayment()]);

    for (const amount of [0, -10]) {
        assert.throws(
            () => processor.processPayment("card", amount),
            /greater than zero/
        );
    }
});

test("invalid amount types are rejected", () => {
    const processor = new PaymentProcessor([new CardPayment()]);

    for (const amount of ["100", NaN, Infinity, -Infinity, null]) {
        assert.throws(
            () => processor.processPayment("card", amount),
            /finite number/
        );
    }
});

test("empty provider names are rejected", () => {
    const processor = new PaymentProcessor([new CardPayment()]);

    for (const provider of ["", "   ", null]) {
        assert.throws(
            () => processor.processPayment(provider, 100),
            /Provider must/
        );
    }
});

test("invalid strategy registration is rejected", () => {
    const processor = new PaymentProcessor();

    assert.throws(
        () => processor.registerStrategy({}),
        /Invalid payment strategy/
    );
});
