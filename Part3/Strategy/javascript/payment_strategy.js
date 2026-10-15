
"use strict";

// Represents the result of a simulated payment.
class PaymentResult {
    constructor(provider, amount, status, message) {
        this.provider = provider;
        this.amount = amount;
        this.status = status;
        this.message = message;

        Object.freeze(this);
    }
}

// Defines the common contract for payment strategies.
class PaymentStrategy {
    get providerKey() {
        throw new Error("providerKey must be implemented.");
    }

    pay(amount) {
        throw new Error("pay() must be implemented.");
    }
}

// Concrete strategy for card payments.
class CardPayment extends PaymentStrategy {
    get providerKey() {
        return "card";
    }

    pay(amount) {
        return new PaymentResult(
            this.providerKey,
            amount,
            "SUCCESS",
            "Card payment simulated successfully."
        );
    }
}

// Concrete strategy for UPI payments.
class UPIPayment extends PaymentStrategy {
    get providerKey() {
        return "upi";
    }

    pay(amount) {
        return new PaymentResult(
            this.providerKey,
            amount,
            "SUCCESS",
            "UPI payment simulated successfully."
        );
    }
}

// Context: registers, selects and invokes payment strategies.
class PaymentProcessor {
    constructor(strategies = []) {
        this.strategies = new Map();

        for (const strategy of strategies) {
            this.registerStrategy(strategy);
        }
    }

    registerStrategy(strategy) {
        if (
            !strategy ||
            typeof strategy.providerKey !== "string" ||
            typeof strategy.pay !== "function"
        ) {
            throw new TypeError("Invalid payment strategy.");
        }

        const key = strategy.providerKey.trim().toLowerCase();

        if (!key) {
            throw new Error("Provider key cannot be empty.");
        }

        if (this.strategies.has(key)) {
            throw new Error(`Provider already registered: ${key}`);
        }

        this.strategies.set(key, strategy);
    }

    processPayment(provider, amount) {
        if (typeof provider !== "string" || !provider.trim()) {
            throw new Error("Provider must be a non-empty string.");
        }

        const key = provider.trim().toLowerCase();
        const strategy = this.strategies.get(key);

        if (!strategy) {
            throw new Error(`Unknown payment provider: ${key}`);
        }

        // Accept finite positive numbers only.
        // This avoids JavaScript floating-point and coercion surprises.
        if (
            typeof amount !== "number" ||
            !Number.isFinite(amount) ||
            amount <= 0
        ) {
            throw new Error(
                "Payment amount must be a finite number greater than zero."
            );
        }

        return strategy.pay(amount);
    }
}

// Export the classes for use by the test file.
module.exports = {
    PaymentResult,
    PaymentStrategy,
    CardPayment,
    UPIPayment,
    PaymentProcessor
};

// Demonstration entry point.
if (require.main === module) {
    const processor = new PaymentProcessor([
        new CardPayment(),
        new UPIPayment()
    ]);

    const result = processor.processPayment("card", 100);

    console.log(`Status: ${result.status}`);
    console.log(`Provider: ${result.provider}`);
    console.log(`Amount: ${result.amount.toFixed(2)}`);
    console.log(`Message: ${result.message}`);
}
