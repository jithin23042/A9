
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

// Immutable result of a simulated payment.
record PaymentResult(
        String provider,
        double amount,
        String status,
        String message) {
}

// Common contract for payment strategies.
interface PaymentMethod {
    String providerKey();

    PaymentResult pay(double amount);
}

// Concrete strategy for card payments.
class CardPayment implements PaymentMethod {
    @Override
    public String providerKey() {
        return "card";
    }

    @Override
    public PaymentResult pay(double amount) {
        return new PaymentResult(
                providerKey(),
                amount,
                "SUCCESS",
                "Card payment simulated successfully.");
    }
}

// Concrete strategy for UPI payments.
class UPIPayment implements PaymentMethod {
    @Override
    public String providerKey() {
        return "upi";
    }

    @Override
    public PaymentResult pay(double amount) {
        return new PaymentResult(
                providerKey(),
                amount,
                "SUCCESS",
                "UPI payment simulated successfully.");
    }
}

// Context: registers, selects and invokes payment strategies.
class PaymentProcessor {
    private final Map<String, PaymentMethod> strategies = new HashMap<>();

    public PaymentProcessor() {
    }

    public PaymentProcessor(PaymentMethod... methods) {
        for (PaymentMethod method : methods) {
            registerStrategy(method);
        }
    }

    public void registerStrategy(PaymentMethod method) {
        if (method == null) {
            throw new IllegalArgumentException(
                    "Invalid payment strategy.");
        }

        String key = normalizeProvider(method.providerKey());

        if (strategies.containsKey(key)) {
            throw new IllegalArgumentException(
                    "Provider already registered: " + key);
        }

        strategies.put(key, method);
    }

    public PaymentResult processPayment(
            String provider, double amount) {

        String key = normalizeProvider(provider);
        PaymentMethod method = strategies.get(key);

        if (method == null) {
            throw new IllegalArgumentException(
                    "Unknown payment provider: " + key);
        }

        if (!Double.isFinite(amount) || amount <= 0) {
            throw new IllegalArgumentException(
                    "Payment amount must be a finite number "
                            + "greater than zero.");
        }

        return method.pay(amount);
    }

    private String normalizeProvider(String provider) {
        if (provider == null || provider.isBlank()) {
            throw new IllegalArgumentException(
                    "Provider must be a non-empty string.");
        }

        return provider.trim().toLowerCase(Locale.ROOT);
    }
}

public class PaymentStrategy {
    public static void main(String[] args) {
        PaymentProcessor processor = new PaymentProcessor(
                new CardPayment(),
                new UPIPayment());

        PaymentResult result =
                processor.processPayment("card", 100.00);

        System.out.println("Status: " + result.status());
        System.out.println("Provider: " + result.provider());
        System.out.printf(
                Locale.ROOT,
                "Amount: %.2f%n",
                result.amount());
        System.out.println("Message: " + result.message());
    }
}
