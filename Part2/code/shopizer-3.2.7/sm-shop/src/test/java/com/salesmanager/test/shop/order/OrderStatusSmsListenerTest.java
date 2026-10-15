package com.salesmanager.test.shop.order;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.salesmanager.core.model.common.Billing;
import com.salesmanager.core.model.common.Delivery;
import com.salesmanager.core.model.order.Order;
import com.salesmanager.core.model.order.orderstatus.OrderStatus;
import com.salesmanager.shop.notification.sms.SmsProvider;
import com.salesmanager.shop.store.controller.order.events.OrderStatusChangedEvent;
import com.salesmanager.shop.store.controller.order.listeners.OrderStatusSmsListener;

public class OrderStatusSmsListenerTest {

	@Test
	public void sendsOrderReferenceAndStatusForRelevantTransition() {
		RecordingSmsProvider smsProvider = new RecordingSmsProvider();
		OrderStatusSmsListener listener = new OrderStatusSmsListener(smsProvider);
		Order order = orderWithPhones(81L, "+15550001", "+15550002");

		listener.onApplicationEvent(event(order, OrderStatus.PROCESSED, OrderStatus.SHIPPED, 1L));
		listener.onApplicationEvent(event(order, OrderStatus.SHIPPED, OrderStatus.DELIVERED, 2L));

		assertEquals(2, smsProvider.recipients.size());
		assertEquals("+15550001", smsProvider.recipients.get(0));
		assertEquals("Your order #81 is now SHIPPED.", smsProvider.messages.get(0));
		assertEquals("Your order #81 is now DELIVERED.", smsProvider.messages.get(1));
	}

	@Test
	public void prefersBillingPhoneWhenDeliveryPhoneIsMissing() {
		RecordingSmsProvider smsProvider = new RecordingSmsProvider();
		OrderStatusSmsListener listener = new OrderStatusSmsListener(smsProvider);

		listener.onApplicationEvent(event(orderWithPhones(82L, null, "+15550003"),
				OrderStatus.PROCESSED, OrderStatus.DELIVERED, 3L));

		assertEquals("+15550003", smsProvider.recipients.get(0));
	}

	@Test
	public void doesNotSendForIrrelevantTransition() {
		RecordingSmsProvider smsProvider = new RecordingSmsProvider();
		OrderStatusSmsListener listener = new OrderStatusSmsListener(smsProvider);

		listener.onApplicationEvent(event(orderWithPhones(83L, "+15550004", null),
				OrderStatus.ORDERED, OrderStatus.PROCESSED, 4L));

		assertTrue(smsProvider.messages.isEmpty());
	}

	@Test
	public void skipsOrderWithoutPhoneNumber() {
		RecordingSmsProvider smsProvider = new RecordingSmsProvider();
		OrderStatusSmsListener listener = new OrderStatusSmsListener(smsProvider);

		listener.onApplicationEvent(event(orderWithPhones(84L, null, null),
				OrderStatus.PROCESSED, OrderStatus.SHIPPED, 5L));

		assertTrue(smsProvider.messages.isEmpty());
	}

	@Test
	public void logsProviderFailureWithoutFailingOrderStatusHandling() {
		RecordingSmsProvider smsProvider = new RecordingSmsProvider();
		smsProvider.failure = new IllegalStateException("provider unavailable");
		OrderStatusSmsListener listener = new OrderStatusSmsListener(smsProvider);

		listener.onApplicationEvent(event(orderWithPhones(85L, "+15550005", null),
				OrderStatus.PROCESSED, OrderStatus.SHIPPED, 6L));
		assertEquals(1, smsProvider.attempts);
	}

	@Test
	public void suppressesDuplicateDeliveryForOnePersistedTransition() {
		RecordingSmsProvider smsProvider = new RecordingSmsProvider();
		OrderStatusSmsListener listener = new OrderStatusSmsListener(smsProvider);
		Order order = orderWithPhones(86L, "+15550006", null);

		listener.onApplicationEvent(event(order, OrderStatus.PROCESSED, OrderStatus.SHIPPED, 7L));
		listener.onApplicationEvent(event(order, OrderStatus.PROCESSED, OrderStatus.SHIPPED, 7L));

		assertEquals(1, smsProvider.messages.size());
	}

	private OrderStatusChangedEvent event(Order order, OrderStatus oldStatus, OrderStatus newStatus,
			Long historyId) {
		return new OrderStatusChangedEvent(this, order, oldStatus, newStatus, historyId);
	}

	private Order orderWithPhones(Long id, String deliveryPhone, String billingPhone) {
		Order order = new Order();
		order.setId(id);
		Delivery delivery = new Delivery();
		delivery.setTelephone(deliveryPhone);
		order.setDelivery(delivery);
		Billing billing = new Billing();
		billing.setTelephone(billingPhone);
		order.setBilling(billing);
		return order;
	}

	private static class RecordingSmsProvider implements SmsProvider {
		private final List<String> recipients = new ArrayList<>();
		private final List<String> messages = new ArrayList<>();
		private RuntimeException failure;
		private int attempts;

		@Override
		public void sendSms(String phoneNumber, String message) {
			attempts++;
			if (failure != null) {
				throw failure;
			}
			recipients.add(phoneNumber);
			messages.add(message);
		}
	}
}
