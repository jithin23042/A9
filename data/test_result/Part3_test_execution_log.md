# Cross-Language Test Execution Log

This log summarizes the commands and environments recorded in `data/cross_language.csv`. Run commands from the indicated pattern/language directory unless a README gives a platform-specific variant.

## Strategy / Java
- Directory: `Part3/Strategy/java`
- Compile: `javac PaymentStrategy.java TestPaymentStrategy.java`
- Test: `java -ea TestPaymentStrategy`
- Runtime recorded: OpenJDK 21.0.12.1
- Result: 10/10 passed.

## Strategy / Python
- Directory: `Part3/Strategy/python`
- Test: `python -m unittest test_payment_strategy.py`
- Runtime recorded: Python 3.13.5
- Result: 10/10 passed.

## Strategy / JavaScript
- Directory: `Part3/Strategy/javascript`
- Test: `node --test test_payment_strategy.js`
- Runtime recorded: Node.js v22.16.0
- Result: 10/10 passed.

## Strategy / C++
- Directory: `Part3/Strategy/cpp`
- Compile: `g++ -std=c++17 -Wall -Wextra -pedantic test_payment_strategy.cpp -o test_payment_strategy`
- Test: `./test_payment_strategy`
- Runtime recorded: g++ (Debian 14.2.0-19) 14.2.0
- Result: 10/10 passed.

## Observer / Java
- Directory: `Part3/Observer/java`
- Compile: `javac -d build OrderStatusObserver.java TestOrderStatusObserver.java`
- Test: `java -ea -cp build A9.Part3.Observer_Order_SMS.java.TestOrderStatusObserver`
- Runtime recorded: OpenJDK 21.0.12.1
- Result: 10/10 passed.

## Observer / Python
- Directory: `Part3/Observer/python`
- Test: `python -m unittest test_order_status_observer.py`
- Runtime recorded: Python 3.13.5
- Result: 10/10 passed.

## Observer / JavaScript
- Directory: `Part3/Observer/javascript`
- Test: `node --test test_order_status_observer.js`
- Runtime recorded: Node.js v22.16.0
- Result: 10/10 passed.

## Observer / C++
- Directory: `Part3/Observer/cpp`
- Compile and test commands: see `Part3/Observer/cpp/README.md`.
- Runtime recorded: g++ (Debian 14.2.0-19) 14.2.0
- Result: 10/10 passed.

The commands and results are taken from the existing evidence and CSV; tests were not rerun during this folder reorganization. The test outputs are in the individual `Part3_test_*.log` files in this directory.
