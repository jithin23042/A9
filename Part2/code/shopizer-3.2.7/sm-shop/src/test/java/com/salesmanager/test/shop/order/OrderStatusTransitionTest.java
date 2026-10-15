package com.salesmanager.test.shop.order;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import com.salesmanager.core.business.services.order.OrderService;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.order.Order;
import com.salesmanager.core.model.order.orderstatus.OrderStatusHistory;
import com.salesmanager.core.model.order.orderstatus.OrderStatus;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.shop.store.controller.order.events.OrderStatusChangedEvent;
import com.salesmanager.shop.store.controller.order.facade.OrderFacadeImpl;
import com.salesmanager.shop.utils.LabelUtils;

public class OrderStatusTransitionTest {

	@Test
	public void repeatedStatusUpdatePersistsAndPublishesOnlyOnce() throws Exception {
		OrderFacadeImpl facade = new OrderFacadeImpl();
		OrderService orderService = mock(OrderService.class);
		LabelUtils labels = mock(LabelUtils.class);
		List<Object> publishedEvents = new ArrayList<>();
		ApplicationEventPublisher publisher = publishedEvents::add;
		ReflectionTestUtils.setField(facade, "orderService", orderService);
		ReflectionTestUtils.setField(facade, "messages", labels);
		ReflectionTestUtils.setField(facade, "eventPublisher", publisher);
		when(labels.getMessage(eq("email.order.status.changed"), any(String[].class), any(Locale.class)))
				.thenReturn("status changed");
		doNothing().when(orderService).addOrderStatusHistory(any(Order.class), any(OrderStatusHistory.class));

		MerchantStore store = new MerchantStore();
		store.setDefaultLanguage(new Language("en"));
		Order order = new Order();
		order.setId(91L);
		order.setStatus(OrderStatus.PROCESSED);

		facade.updateOrderStatus(order, OrderStatus.SHIPPED, store);
		facade.updateOrderStatus(order, OrderStatus.SHIPPED, store);

		verify(orderService, times(1)).addOrderStatusHistory(any(Order.class), any(OrderStatusHistory.class));
		assertEquals(1, publishedEvents.size());
		OrderStatusChangedEvent event = (OrderStatusChangedEvent) publishedEvents.get(0);
		assertEquals(OrderStatus.PROCESSED, event.getOldStatus());
		assertEquals(OrderStatus.SHIPPED, event.getNewStatus());
	}

	@Test
	public void irrelevantStatusTransitionDoesNotPublishSmsEvent() throws Exception {
		OrderFacadeImpl facade = new OrderFacadeImpl();
		OrderService orderService = mock(OrderService.class);
		LabelUtils labels = mock(LabelUtils.class);
		List<Object> publishedEvents = new ArrayList<>();
		ReflectionTestUtils.setField(facade, "orderService", orderService);
		ReflectionTestUtils.setField(facade, "messages", labels);
		ReflectionTestUtils.setField(facade, "eventPublisher", (ApplicationEventPublisher) publishedEvents::add);
		when(labels.getMessage(anyString(), any(String[].class), any(Locale.class))).thenReturn("status changed");
		doNothing().when(orderService).addOrderStatusHistory(any(Order.class), any(OrderStatusHistory.class));

		MerchantStore store = new MerchantStore();
		store.setDefaultLanguage(new Language("en"));
		Order order = new Order();
		order.setStatus(OrderStatus.ORDERED);

		facade.updateOrderStatus(order, OrderStatus.PROCESSED, store);

		assertEquals(0, publishedEvents.size());
	}
}
