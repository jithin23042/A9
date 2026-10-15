# Test execution log

## Strategy / Java
Working directory: /mnt/data/person3_work/A9/A9/Part3/Strategy_Payment/java
Compile command: javac PaymentStrategy.java TestPaymentStrategy.java
Test command: java -ea TestPaymentStrategy
Runtime/compiler: openjdk version "21.0.12.1" 2026-08-18
Result recorded in data/cross_language.csv.

## Strategy / Python
Working directory: /mnt/data/person3_work/A9/A9/Part3/Strategy_Payment/python
Compile command: (interpreted/runtime; no separate compile)
Test command: python -m unittest test_payment_strategy.py
Runtime/compiler: Python 3.13.5
Result recorded in data/cross_language.csv.

## Strategy / JavaScript
Working directory: /mnt/data/person3_work/A9/A9/Part3/Strategy_Payment/javascript
Compile command: (interpreted/runtime; no separate compile)
Test command: node --test test_payment_strategy.js
Runtime/compiler: Node.js v22.16.0
Result recorded in data/cross_language.csv.

## Strategy / C++
Working directory: /mnt/data/person3_work/A9/A9/Part3/Strategy_Payment/cpp
Compile command: g++ -std=c++17 -Wall -Wextra -pedantic test_payment_strategy.cpp -o test_payment_strategy
Test command: ./test_payment_strategy
Runtime/compiler: g++ (Debian 14.2.0-19) 14.2.0
Result recorded in data/cross_language.csv.

## Observer / Java
Working directory: /mnt/data/person3_work/A9/A9/Part3/Observer_Order_SMS/java
Compile command: javac -d /mnt/data/A9_Compliant_Revision /mnt/data/person3_work/A9/A9/Part3/Observer_Order_SMS/java/OrderStatusObserver.java /mnt/data/person3_work/A9/A9/Part3/Observer_Order_SMS/java/TestOrderStatusObserver.java
Test command: java -ea -cp /mnt/data/A9_Compliant_Revision A9.Part3.Observer_Order_SMS.java.TestOrderStatusObserver
Runtime/compiler: openjdk version "21.0.12.1" 2026-08-18
Result recorded in data/cross_language.csv.

## Observer / Python
Working directory: /mnt/data/person3_work/A9/A9/Part3/Observer_Order_SMS/python
Compile command: (interpreted/runtime; no separate compile)
Test command: python -m unittest test_order_status_observer.py
Runtime/compiler: Python 3.13.5
Result recorded in data/cross_language.csv.

## Observer / JavaScript
Working directory: /mnt/data/person3_work/A9/A9/Part3/Observer_Order_SMS/javascript
Compile command: (interpreted/runtime; no separate compile)
Test command: node --test test_order_status_observer.js
Runtime/compiler: Node.js v22.16.0
Result recorded in data/cross_language.csv.

## Observer / C++
Working directory: /mnt/data/person3_work/A9/A9/Part3/Observer_Order_SMS/cpp
Compile command: g++ -std=c++17 -Wall -Wextra -pedantic test_order_status_observer.cpp -o test_order_status_observer
Test command: ./test_order_status_observer
Runtime/compiler: g++ (Debian 14.2.0-19) 14.2.0
Result recorded in data/cross_language.csv.
