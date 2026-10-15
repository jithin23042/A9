
from abc import ABC, abstractmethod
from dataclasses import dataclass
from decimal import Decimal, InvalidOperation
from typing import Iterable


# Represents the result of a simulated payment.
@dataclass(frozen=True)
class PaymentResult:
    provider: str
    amount: Decimal
    status: str
    message: str


# Defines the common contract for all payment strategies.
class PaymentStrategy(ABC):

    @property
    @abstractmethod
    def provider_key(self) -> str:
        """Return the unique key used to select this strategy."""
        raise NotImplementedError

    @abstractmethod
    def pay(self, amount: Decimal) -> PaymentResult:
        """Process a payment and return its result."""
        raise NotImplementedError


# Concrete strategy for card payments.
class CardPayment(PaymentStrategy):

    @property
    def provider_key(self) -> str:
        return "card"

    def pay(self, amount: Decimal) -> PaymentResult:
        return PaymentResult(
            provider=self.provider_key,
            amount=amount,
            status="SUCCESS",
            message="Card payment simulated successfully."
        )


# Concrete strategy for UPI payments.
class UPIPayment(PaymentStrategy):

    @property
    def provider_key(self) -> str:
        return "upi"

    def pay(self, amount: Decimal) -> PaymentResult:
        return PaymentResult(
            provider=self.provider_key,
            amount=amount,
            status="SUCCESS",
            message="UPI payment simulated successfully."
        )


# Context: selects and invokes the requested payment strategy.
class PaymentProcessor:

    def __init__(self, strategies: Iterable[PaymentStrategy]) -> None:
        self._strategies: dict[str, PaymentStrategy] = {}

        for strategy in strategies:
            self.register_strategy(strategy)

    def register_strategy(self, strategy: PaymentStrategy) -> None:
        key = strategy.provider_key.strip().lower()

        if not key:
            raise ValueError("Provider key cannot be empty.")

        if key in self._strategies:
            raise ValueError(f"Provider already registered: {key}")

        self._strategies[key] = strategy

    def process_payment(
        self, provider: str, amount: Decimal | int | float | str
    ) -> PaymentResult:

        # Validate the provider selection.
        if not isinstance(provider, str) or not provider.strip():
            raise ValueError("Provider must be a non-empty string.")

        key = provider.strip().lower()

        if key not in self._strategies:
            raise ValueError(f"Unknown payment provider: {key}")

        # Convert the amount safely to Decimal.
        try:
            payment_amount = Decimal(str(amount))
        except (InvalidOperation, ValueError, TypeError):
            raise ValueError("Payment amount must be a valid number.") from None

        # Reject zero, negative, NaN and infinite amounts.
        if not payment_amount.is_finite() or payment_amount <= 0:
            raise ValueError("Payment amount must be greater than zero.")

        # Delegate payment processing to the selected strategy.
        strategy = self._strategies[key]
        return strategy.pay(payment_amount)


# Demonstration entry point.
if __name__ == "__main__":
    processor = PaymentProcessor([
        CardPayment(),
        UPIPayment()
    ])

    result = processor.process_payment("card", "100.00")

    print(f"Status: {result.status}")
    print(f"Provider: {result.provider}")
    print(f"Amount: {result.amount:.2f}")
    print(f"Message: {result.message}")
