# Strategy Pattern — Payment Processing (C++)

## 1. Application Description

This implementation demonstrates the Strategy Design Pattern in a simulated payment-processing module. The client selects a payment strategy at runtime through a common interface. The built-in strategies are Card and UPI; no external payment gateway is contacted.

## 2. Architecture

- **PaymentStrategy:** Abstract interface defining the provider key and payment operation.
- **CardPayment:** Concrete strategy for simulated card payments.
- **UPIPayment:** Concrete strategy for simulated UPI payments.
- **PaymentProcessor:** Registers strategies, validates inputs, selects a strategy and executes it.
- **PaymentResult:** Holds the provider, amount, status and result message.
- **createPaymentProcessor():** Creates a processor and registers the Card and UPI strategies.
- **TestPayment:** Test-only custom strategy used to verify extensibility.

## 3. Design Pattern Implementation

The Strategy Pattern encapsulates interchangeable payment operations behind the `PaymentStrategy` interface. `PaymentProcessor` delegates the operation to the selected strategy without depending on its concrete implementation.

C++ features include abstract classes, virtual functions, runtime polymorphism, `std::unique_ptr` for strategy ownership and `std::unordered_map` for strategy lookup. A custom strategy can be registered without modifying `PaymentProcessor`.

## 4. Validation and Error Handling

- Provider selection is case-insensitive and trims surrounding whitespace.
- Unknown and empty provider names are rejected.
- Zero, negative, NaN and infinite payment amounts are rejected.
- Null strategies, empty strategy keys and duplicate strategy registrations are rejected.
- Payment results are simulated; no real payment is processed.

## 5. Testing

The test suite contains 10 tests covering:

1. Successful Card payment.
2. Successful UPI payment.
3. Case-insensitive provider selection and surrounding whitespace.
4. Rejection of an unknown provider.
5. Rejection of an empty provider.
6. Rejection of a zero amount.
7. Rejection of a negative amount.
8. Rejection of a non-finite amount.
9. Registration and execution of a custom strategy.
10. Rejection of duplicate strategy registration.

**Verified results:** 10 tests passed, 0 failed.

## 6. Compilation and Execution

Verified with GCC 13.3.0 in Ubuntu 24.04 under WSL 2. Run the commands from this directory.

Compile the tests:

```bash
g++ -std=c++17 -Wall -Wextra -pedantic test_payment_strategy.cpp -o test_payment_strategy
```

Run the tests:

```bash
./test_payment_strategy
```

## 7. Lines of Code (LOC)

Production LOC counts nonblank source lines, excluding comment-only lines. The test file is excluded.

| Metric | Verified value |
|---|---:|
| Production LOC (`payment_strategy.cpp`) | 98 |
| Physical lines (`payment_strategy.cpp`) | 109 |

## 8. Limitations

- Payment processing is simulated; no payment gateway, credentials or network calls are used.
- The example focuses on Strategy selection and validation rather than a complete payment application.
- Test results apply to this standalone C++ implementation.
