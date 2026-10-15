package com.salesmanager.shop.store.controller.order.events;

import java.util.UUID;

import org.springframework.context.ApplicationEvent;

import com.salesmanager.core.model.order.Order;
import com.salesmanager.core.model.order.orderstatus.OrderStatus;

public class OrderStatusChangedEvent extends ApplicationEvent {

	private static final long serialVersionUID = 1L;

	private final Order order;
	private final OrderStatus oldStatus;
	private final OrderStatus newStatus;
	private final String transitionId;

	public OrderStatusChangedEvent(Object source, Order order, OrderStatus oldStatus, OrderStatus newStatus,
			Long statusHistoryId) {
		super(source);
		this.order = order;
		this.oldStatus = oldStatus;
		this.newStatus = newStatus;
		this.transitionId = statusHistoryId == null ? UUID.randomUUID().toString() : statusHistoryId.toString();
	}

	public Order getOrder() {
		return order;
	}

	public OrderStatus getOldStatus() {
		return oldStatus;
	}

	public OrderStatus getNewStatus() {
		return newStatus;
	}

	public String getTransitionId() {
		return transitionId;
	}
}
