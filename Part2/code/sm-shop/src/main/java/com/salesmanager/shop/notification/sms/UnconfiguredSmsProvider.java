package com.salesmanager.shop.notification.sms;

import org.springframework.stereotype.Component;
@Component
public class UnconfiguredSmsProvider implements SmsProvider {

	@Override
	public void sendSms(String phoneNumber, String message) {
		throw new IllegalStateException("No SMS provider is configured");
	}
}
