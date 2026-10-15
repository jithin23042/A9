#include <algorithm>
#include <cctype>
#include <cmath>
#include <iomanip>
#include <iostream>
#include <memory>
#include <sstream>
#include <stdexcept>
#include <string>
#include <unordered_map>
#include <utility>

struct PaymentResult {
    std::string provider;
    double amount;
    std::string status;
    std::string message;
};

class PaymentStrategy {
public:
    virtual ~PaymentStrategy() = default;
    virtual std::string providerKey() const = 0;
    virtual PaymentResult pay(double amount) const = 0;
};

class CardPayment final : public PaymentStrategy {
public:
    std::string providerKey() const override { return "card"; }
    PaymentResult pay(double amount) const override {
        return {providerKey(), amount, "SUCCESS", "Card payment simulated successfully."};
    }
};

class UPIPayment final : public PaymentStrategy {
public:
    std::string providerKey() const override { return "upi"; }
    PaymentResult pay(double amount) const override {
        return {providerKey(), amount, "SUCCESS", "UPI payment simulated successfully."};
    }
};

class PaymentProcessor {
private:
    std::unordered_map<std::string, std::unique_ptr<PaymentStrategy>> strategies;

    static std::string normalize(const std::string& value) {
        std::string result = value;
        std::transform(result.begin(), result.end(), result.begin(),
            [](unsigned char ch) { return static_cast<char>(std::tolower(ch)); });
        return result;
    }

public:
    void registerStrategy(std::unique_ptr<PaymentStrategy> strategy) {
        if (!strategy) {
            throw std::invalid_argument("Strategy cannot be null.");
        }
        const std::string key = normalize(strategy->providerKey());
        if (key.empty()) {
            throw std::invalid_argument("Provider key cannot be empty.");
        }
        if (strategies.find(key) != strategies.end()) {
            throw std::invalid_argument("Provider already registered: " + key);
        }
        strategies.emplace(key, std::move(strategy));
    }

    PaymentResult processPayment(const std::string& provider, double amount) const {
        if (provider.find_first_not_of(" \t\n\r") == std::string::npos) {
            throw std::invalid_argument("Provider must be a non-empty string.");
        }
        if (!std::isfinite(amount) || amount <= 0.0) {
            throw std::invalid_argument("Payment amount must be greater than zero and finite.");
        }
        const std::string key = normalize(
            provider.substr(provider.find_first_not_of(" \t\n\r"),
                provider.find_last_not_of(" \t\n\r") - provider.find_first_not_of(" \t\n\r") + 1));
        const auto it = strategies.find(key);
        if (it == strategies.end()) {
            throw std::invalid_argument("Unknown payment provider: " + key);
        }
        return it->second->pay(amount);
    }
};

PaymentProcessor createPaymentProcessor() {
    PaymentProcessor processor;
    processor.registerStrategy(std::make_unique<CardPayment>());
    processor.registerStrategy(std::make_unique<UPIPayment>());
    return processor;
}

std::string formatPaymentResult(const PaymentResult& result) {
    std::ostringstream output;
    output << "Status: " << result.status
           << "\nProvider: " << result.provider
           << "\nAmount: " << std::fixed << std::setprecision(2) << result.amount
           << "\nMessage: " << result.message;
    return output.str();
}

#ifndef PAYMENT_STRATEGY_TESTING
int main() {
    auto processor = createPaymentProcessor();
    std::cout << formatPaymentResult(processor.processPayment("card", 100.00)) << '\n';
    return 0;
}
#endif
