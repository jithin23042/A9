package com.salesmanager.shop.store.controller.order.listeners;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.salesmanager.core.model.common.Billing;
import com.salesmanager.core.model.common.Delivery;
import com.salesmanager.core.model.order.Order;
import com.salesmanager.core.model.order.orderstatus.OrderStatus;
import com.salesmanager.shop.notification.sms.SmsProvider;
import com.salesmanager.shop.store.controller.order.events.OrderStatusChangedEvent;

@Component
public class OrderStatusSmsListener implements ApplicationListener<OrderStatusChangedEvent> {

	private static final Logger LOGGER = LoggerFactory.getLogger(OrderStatusSmsListener.class);
	private static final int MAX_REMEMBERED_TRANSITIONS = 10000;

	private final SmsProvider smsProvider;
	private final Map<String, Boolean> notifiedTransitions = new LinkedHashMap<>(16, 0.75f, true);
	private final Map<String, Boolean> transitionsInProgress = new LinkedHashMap<>();

	public OrderStatusSmsListener(SmsProvider smsProvider) {
		this.smsProvider = smsProvider;
	}

	@Override
	@Async
	public void onApplicationEvent(OrderStatusChangedEvent event) {
		if (!isSmsStatus(event.getNewStatus())) {
			return;
		}

		Order order = event.getOrder();
		String phoneNumber = phoneNumber(order);
		if (StringUtils.isBlank(phoneNumber)) {
			LOGGER.warn("Order-status SMS skipped because order [{}] has no contact phone number",
					order.getId());
			return;
		}

		String transitionId = event.getTransitionId();
		if (!claimTransition(transitionId)) {
			return;
		}

		try {
			String message = "Your order #" + order.getId() + " is now " + event.getNewStatus().name() + ".";
			smsProvider.sendSms(phoneNumber.trim(), message);
			completeTransition(transitionId);
		} catch (Exception e) {
			releaseTransition(transitionId);
			LOGGER.error("Order-status SMS delivery failed for order [{}] at status [{}] ({})",
					order.getId(), event.getNewStatus(), e.getClass().getSimpleName());
		}
	}

	private boolean isSmsStatus(OrderStatus status) {
		return status == OrderStatus.SHIPPED || status == OrderStatus.DELIVERED;
	}

	private String phoneNumber(Order order) {
		Delivery delivery = order.getDelivery();
		if (delivery != null && StringUtils.isNotBlank(delivery.getTelephone())) {
			return delivery.getTelephone();
		}
		Billing billing = order.getBilling();
		return billing == null ? null : billing.getTelephone();
	}

	private synchronized boolean claimTransition(String transitionId) {
		if (notifiedTransitions.containsKey(transitionId) || transitionsInProgress.containsKey(transitionId)) {
			return false;
		}
		transitionsInProgress.put(transitionId, Boolean.TRUE);
		return true;
	}

	private synchronized void completeTransition(String transitionId) {
		transitionsInProgress.remove(transitionId);
		notifiedTransitions.put(transitionId, Boolean.TRUE);
		if (notifiedTransitions.size() > MAX_REMEMBERED_TRANSITIONS) {
			Iterator<String> oldest = notifiedTransitions.keySet().iterator();
			oldest.next();
			oldest.remove();
		}
	}

	private synchronized void releaseTransition(String transitionId) {
		transitionsInProgress.remove(transitionId);
	}
}
