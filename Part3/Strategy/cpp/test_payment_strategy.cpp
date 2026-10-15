#define PAYMENT_STRATEGY_TESTING
#include "payment_strategy.cpp"
#include <functional>
#include <limits>
#include <vector>

class TestPayment final : public PaymentStrategy {
public:
    std::string providerKey() const override { return "test"; }
    PaymentResult pay(double amount) const override {
        return {providerKey(), amount, "SUCCESS", "Test payment executed."};
    }
};

int main() {
    int passed = 0, failed = 0;
    auto run = [&](const std::string& name, const std::function<void()>& test) {
        try { test(); ++passed; std::cout << "PASS: " << name << '\n'; }
        catch (const std::exception& e) { ++failed; std::cout << "FAIL: " << name << ": " << e.what() << '\n'; }
    };
    auto processor = createPaymentProcessor();

    run("Card strategy succeeds", [&] {
        auto r = processor.processPayment("card", 100.0);
        if (r.provider != "card" || r.status != "SUCCESS" || r.amount != 100.0) throw std::runtime_error("Unexpected result");
    });
    run("UPI strategy succeeds", [&] {
        auto r = processor.processPayment("upi", 250.0);
        if (r.provider != "upi" || r.status != "SUCCESS" || r.amount != 250.0) throw std::runtime_error("Unexpected result");
    });
    run("Provider selection is case-insensitive", [&] {
        if (processor.processPayment("  CARD ", 10.0).provider != "card") throw std::runtime_error("Normalization failed");
    });
    run("Unknown provider is rejected", [&] {
        try { processor.processPayment("unknown", 10.0); } catch (const std::invalid_argument&) { return; }
        throw std::runtime_error("Expected invalid_argument");
    });
    run("Empty provider is rejected", [&] {
        try { processor.processPayment("  ", 10.0); } catch (const std::invalid_argument&) { return; }
        throw std::runtime_error("Expected invalid_argument");
    });
    run("Zero amount is rejected", [&] {
        try { processor.processPayment("card", 0.0); } catch (const std::invalid_argument&) { return; }
        throw std::runtime_error("Expected invalid_argument");
    });
    run("Negative amount is rejected", [&] {
        try { processor.processPayment("upi", -1.0); } catch (const std::invalid_argument&) { return; }
        throw std::runtime_error("Expected invalid_argument");
    });
    run("Non-finite amount is rejected", [&] {
        try { processor.processPayment("card", std::numeric_limits<double>::quiet_NaN()); } catch (const std::invalid_argument&) { return; }
        throw std::runtime_error("Expected invalid_argument");
    });
    run("Custom strategy can be registered", [&] {
        processor.registerStrategy(std::make_unique<TestPayment>());
        if (processor.processPayment("test", 75.0).amount != 75.0) throw std::runtime_error("Custom strategy failed");
    });
    run("Duplicate strategy registration is rejected", [&] {
        try { processor.registerStrategy(std::make_unique<CardPayment>()); } catch (const std::invalid_argument&) { return; }
        throw std::runtime_error("Expected invalid_argument");
    });

    std::cout << "\nTests passed: " << passed << "\nTests failed: " << failed << '\n';
    return failed == 0 ? 0 : 1;
}
