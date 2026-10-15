
import unittest
from decimal import Decimal

from payment_strategy import (
    CardPayment,
    UPIPayment,
    PaymentProcessor,
    PaymentStrategy,
    PaymentResult,
)


# Test a new strategy to verify extensibility.
class RazorpayPayment(PaymentStrategy):

    @property
    def provider_key(self) -> str:
        return "razorpay"

    def pay(self, amount: Decimal) -> PaymentResult:
        return PaymentResult(
            provider=self.provider_key,
            amount=amount,
            status="SUCCESS",
            message="Razorpay payment simulated successfully."
        )


class TestPaymentStrategy(unittest.TestCase):

    def setUp(self):
        self.processor = PaymentProcessor([
            CardPayment(),
            UPIPayment()
        ])

    # Test 1: Card payment succeeds.
    def test_card_payment(self):
        result = self.processor.process_payment("card", "100.00")

        self.assertEqual(result.status, "SUCCESS")
        self.assertEqual(result.provider, "card")
        self.assertEqual(result.amount, Decimal("100.00"))

    # Test 2: UPI payment succeeds.
    def test_upi_payment(self):
        result = self.processor.process_payment("upi", "250.50")

        self.assertEqual(result.status, "SUCCESS")
        self.assertEqual(result.provider, "upi")
        self.assertEqual(result.amount, Decimal("250.50"))

    # Test 3: Provider selection is case-insensitive.
    def test_case_insensitive_provider(self):
        result = self.processor.process_payment("CARD", "50")

        self.assertEqual(result.provider, "card")

    # Test 4: A new strategy can be added without changing the context.
    def test_register_new_strategy(self):
        self.processor.register_strategy(RazorpayPayment())

        result = self.processor.process_payment("razorpay", "500")

        self.assertEqual(result.provider, "razorpay")
        self.assertEqual(result.status, "SUCCESS")
        self.assertEqual(result.amount, Decimal("500"))

        # Existing strategies must continue to work.
        existing_result = self.processor.process_payment("card", "100")
        self.assertEqual(existing_result.provider, "card")

    # Test 5: Duplicate provider registration is rejected.
    def test_duplicate_provider_rejected(self):
        with self.assertRaisesRegex(ValueError, "already registered"):
            self.processor.register_strategy(CardPayment())

    # Test 6: Unknown providers are rejected.
    def test_unknown_provider_rejected(self):
        with self.assertRaisesRegex(ValueError, "Unknown payment provider"):
            self.processor.process_payment("bitcoin", "100")

    # Test 7: Zero and negative amounts are rejected.
    def test_non_positive_amount_rejected(self):
        for amount in ["0", "-10", "0.00"]:
            with self.subTest(amount=amount):
                with self.assertRaisesRegex(ValueError, "greater than zero"):
                    self.processor.process_payment("card", amount)

    # Test 8: Invalid numeric input is rejected.
    def test_invalid_amount_rejected(self):
        for amount in ["abc", "", None]:
            with self.subTest(amount=amount):
                with self.assertRaisesRegex(ValueError, "valid number"):
                    self.processor.process_payment("card", amount)

    # Test 9: NaN and infinity are rejected.
    def test_non_finite_amount_rejected(self):
        for amount in ["NaN", "Infinity", "-Infinity"]:
            with self.subTest(amount=amount):
                with self.assertRaisesRegex(ValueError, "greater than zero"):
                    self.processor.process_payment("card", amount)

    # Test 10: Empty and invalid provider names are rejected.
    def test_invalid_provider_rejected(self):
        for provider in ["", "   ", None]:
            with self.subTest(provider=provider):
                with self.assertRaisesRegex(ValueError, "Provider must"):
                    self.processor.process_payment(provider, "100")


if __name__ == "__main__":
    unittest.main(verbosity=2)
