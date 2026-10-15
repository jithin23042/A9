
import java.util.Locale;

// Test-only Razorpay strategy used to verify extensibility.
class RazorpayPayment implements PaymentMethod {
    @Override
    public String providerKey() {
        return "razorpay";
    }

    @Override
    public PaymentResult pay(double amount) {
        return new PaymentResult(
                providerKey(),
                amount,
                "SUCCESS",
                "Razorpay payment simulated successfully.");
    }
}

public class TestPaymentStrategy {

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

    private static void expectException(
            Class<? extends Throwable> expected,
            Runnable action) {
        try {
            action.run();
        } catch (Throwable error) {
            if (expected.isInstance(error)) {
                return;
            }
            throw new AssertionError(
                    "Unexpected exception: " + error);
        }
        throw new AssertionError(
                "Expected exception: " + expected.getSimpleName());
    }

    public static void main(String[] args) {
        check("card payment succeeds", () -> {
            PaymentProcessor processor =
                    new PaymentProcessor(new CardPayment());
            PaymentResult result =
                    processor.processPayment("card", 100);

            assert result.status().equals("SUCCESS");
            assert result.provider().equals("card");
            assert result.amount() == 100;
        });

        check("UPI payment succeeds", () -> {
            PaymentProcessor processor =
                    new PaymentProcessor(new UPIPayment());
            PaymentResult result =
                    processor.processPayment("upi", 250.50);

            assert result.status().equals("SUCCESS");
            assert result.provider().equals("upi");
            assert result.amount() == 250.50;
        });

        check("provider selection is case-insensitive", () -> {
            PaymentProcessor processor =
                    new PaymentProcessor(new CardPayment());

            assert processor.processPayment("CARD", 50)
                    .provider().equals("card");
        });

        check("new Razorpay strategy can be registered", () -> {
            PaymentProcessor processor =
                    new PaymentProcessor(
                            new CardPayment(),
                            new UPIPayment());

            processor.registerStrategy(new RazorpayPayment());

            assert processor.processPayment("razorpay", 500)
                    .provider().equals("razorpay");
            assert processor.processPayment("card", 100)
                    .provider().equals("card");
        });

        check("duplicate provider registration is rejected", () -> {
            PaymentProcessor processor =
                    new PaymentProcessor(new CardPayment());

            expectException(
                    IllegalArgumentException.class,
                    () -> processor.registerStrategy(new CardPayment()));
        });

        check("unknown provider is rejected", () -> {
            PaymentProcessor processor =
                    new PaymentProcessor(new CardPayment());

            expectException(
                    IllegalArgumentException.class,
                    () -> processor.processPayment("bitcoin", 100));
        });

        check("zero and negative amounts are rejected", () -> {
            PaymentProcessor processor =
                    new PaymentProcessor(new CardPayment());

            expectException(
                    IllegalArgumentException.class,
                    () -> processor.processPayment("card", 0));
            expectException(
                    IllegalArgumentException.class,
                    () -> processor.processPayment("card", -10));
        });

        check("invalid and non-finite amounts are rejected", () -> {
            PaymentProcessor processor =
                    new PaymentProcessor(new CardPayment());

            expectException(
                    IllegalArgumentException.class,
                    () -> processor.processPayment("card", Double.NaN));
            expectException(
                    IllegalArgumentException.class,
                    () -> processor.processPayment(
                            "card", Double.POSITIVE_INFINITY));
            expectException(
                    IllegalArgumentException.class,
                    () -> processor.processPayment(
                            "card", Double.NEGATIVE_INFINITY));
        });

        check("invalid provider names are rejected", () -> {
            PaymentProcessor processor =
                    new PaymentProcessor(new CardPayment());

            expectException(
                    IllegalArgumentException.class,
                    () -> processor.processPayment(" ", 100));
            expectException(
                    IllegalArgumentException.class,
                    () -> processor.processPayment(null, 100));
        });

        check("invalid strategy registration is rejected", () -> {
            PaymentProcessor processor = new PaymentProcessor();

            expectException(
                    IllegalArgumentException.class,
                    () -> processor.registerStrategy(null));
        });

        System.out.println();
        System.out.println("Tests passed: " + passed);
        System.out.println("Tests failed: " + failed);

        if (failed > 0) {
            System.exit(1);
        }
    }
}
