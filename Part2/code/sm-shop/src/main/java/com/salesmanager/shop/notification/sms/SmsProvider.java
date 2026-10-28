package com.salesmanager.shop.notification.sms;

public interface SmsProvider {

	void sendSms(String phoneNumber, String message) throws Exception;
}
