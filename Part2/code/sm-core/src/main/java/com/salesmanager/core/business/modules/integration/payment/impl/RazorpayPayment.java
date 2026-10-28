package com.salesmanager.core.business.modules.integration.payment.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.salesmanager.core.model.customer.Customer;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.order.Order;
import com.salesmanager.core.model.payments.Payment;
import com.salesmanager.core.model.payments.PaymentType;
import com.salesmanager.core.model.payments.Transaction;
import com.salesmanager.core.model.payments.TransactionType;
import com.salesmanager.core.model.shoppingcart.ShoppingCartItem;
import com.salesmanager.core.model.system.IntegrationConfiguration;
import com.salesmanager.core.model.system.IntegrationModule;
import com.salesmanager.core.modules.integration.IntegrationException;
import com.salesmanager.core.modules.integration.payment.model.PaymentModule;

/**
 * Razorpay Standard Checkout integration using the existing payment module contract.
 */
public class RazorpayPayment implements PaymentModule {

	private static final String API_URL = "https://api.razorpay.com/v1/";
	private static final String PAYMENT_ID = "PAYMENTID";
	private static final String ORDER_ID = "ORDERID";
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private RestTemplate restTemplate = new RestTemplate();

	@Override
	public void validateModuleConfiguration(IntegrationConfiguration configuration, MerchantStore store)
			throws IntegrationException {
		List<String> missingFields = new ArrayList<String>();
		Map<String, String> keys = configuration == null ? null : configuration.getIntegrationKeys();
		if (keys == null || StringUtils.isBlank(keys.get("key_id"))) {
			missingFields.add("key_id");
		}
		if (keys == null || StringUtils.isBlank(keys.get("key_secret"))) {
			missingFields.add("key_secret");
		}
		if (!missingFields.isEmpty()) {
			IntegrationException exception = new IntegrationException(IntegrationException.ERROR_VALIDATION_SAVE);
			exception.setErrorFields(missingFields);
			throw exception;
		}
	}

	@Override
	public Transaction initTransaction(MerchantStore store, Customer customer, BigDecimal amount, Payment payment,
			IntegrationConfiguration configuration, IntegrationModule module) throws IntegrationException {
		validateModuleConfiguration(configuration, store);
		long minorAmount = toMinorUnits(amount, store);
		String currency = store.getCurrency().getCode().toUpperCase(Locale.ROOT);

		Map<String, Object> request = new HashMap<String, Object>();
		request.put("amount", minorAmount);
		request.put("currency", currency);
		request.put("receipt", UUID.randomUUID().toString().replace("-", ""));

		Map<String, Object> response = post("orders", request, configuration);
		String orderId = requiredString(response, "id");
		if (!"created".equals(response.get("status"))
				|| number(response.get("amount")) != minorAmount
				|| !currency.equals(response.get("currency"))) {
			throw transactionException("Razorpay returned an invalid order response");
		}

		Map<String, Object> checkout = new HashMap<String, Object>();
		checkout.put("key", configuration.getIntegrationKeys().get("key_id"));
		checkout.put("order_id", orderId);
		checkout.put("amount", minorAmount);
		checkout.put("currency", currency);

		Transaction transaction = new Transaction();
		transaction.setAmount(amount);
		transaction.setTransactionDate(new Date());
		transaction.setTransactionType(TransactionType.INIT);
		transaction.setPaymentType(PaymentType.CREDITCARD);
		try {
			transaction.setDetails(OBJECT_MAPPER.writeValueAsString(checkout));
		} catch (IOException e) {
			throw transactionException("Cannot prepare Razorpay checkout details", e);
		}
		transaction.getTransactionDetails().put(ORDER_ID, orderId);
		return transaction;
	}

	@Override
	public Transaction authorize(MerchantStore store, Customer customer, List<ShoppingCartItem> items,
			BigDecimal amount, Payment payment, IntegrationConfiguration configuration, IntegrationModule module)
			throws IntegrationException {
		Map<String, Object> verifiedPayment = verifyCheckout(store, amount, payment, configuration);
		String status = requiredString(verifiedPayment, "status");
		if (!"authorized".equals(status) && !"captured".equals(status)) {
			throw transactionException("Razorpay payment is not authorized");
		}
		return createTransaction(amount, TransactionType.AUTHORIZE, verifiedPayment);
	}

	@Override
	public Transaction authorizeAndCapture(MerchantStore store, Customer customer, List<ShoppingCartItem> items,
			BigDecimal amount, Payment payment, IntegrationConfiguration configuration, IntegrationModule module)
			throws IntegrationException {
		Map<String, Object> verifiedPayment = verifyCheckout(store, amount, payment, configuration);
		String status = requiredString(verifiedPayment, "status");
		if ("authorized".equals(status)) {
			String paymentId = requiredString(verifiedPayment, "id");
			Map<String, Object> captureRequest = new HashMap<String, Object>();
			captureRequest.put("amount", toMinorUnits(amount, store));
			captureRequest.put("currency", store.getCurrency().getCode().toUpperCase(Locale.ROOT));
			verifiedPayment = post("payments/" + paymentId + "/capture", captureRequest, configuration);
			status = requiredString(verifiedPayment, "status");
		}
		if (!"captured".equals(status)) {
			throw transactionException("Razorpay payment was not captured");
		}
		return createTransaction(amount, TransactionType.AUTHORIZECAPTURE, verifiedPayment);
	}

	@Override
	public Transaction capture(MerchantStore store, Customer customer, Order order, Transaction capturableTransaction,
			IntegrationConfiguration configuration, IntegrationModule module) throws IntegrationException {
		validateModuleConfiguration(configuration, store);
		String paymentId = transactionDetail(capturableTransaction, PAYMENT_ID);
		Map<String, Object> payment = get("payments/" + paymentId, configuration);
		validatePayment(payment, paymentId, transactionDetail(capturableTransaction, ORDER_ID), order.getTotal(), store);

		String status = requiredString(payment, "status");
		if ("authorized".equals(status)) {
			Map<String, Object> captureRequest = new HashMap<String, Object>();
			captureRequest.put("amount", toMinorUnits(order.getTotal(), store));
			captureRequest.put("currency", store.getCurrency().getCode().toUpperCase(Locale.ROOT));
			payment = post("payments/" + paymentId + "/capture", captureRequest, configuration);
		}
		if (!"captured".equals(requiredString(payment, "status"))) {
			throw transactionException("Razorpay payment was not captured");
		}
		validatePayment(payment, paymentId, transactionDetail(capturableTransaction, ORDER_ID), order.getTotal(), store);
		return createTransaction(order.getTotal(), TransactionType.CAPTURE, payment);
	}

	@Override
	public Transaction refund(boolean partial, MerchantStore store, Transaction transaction, Order order,
			BigDecimal amount, IntegrationConfiguration configuration, IntegrationModule module)
			throws IntegrationException {
		validateModuleConfiguration(configuration, store);
		if (amount == null || amount.signum() <= 0 || (!partial && amount.compareTo(order.getTotal()) != 0)
				|| (partial && amount.compareTo(order.getTotal()) >= 0)) {
			throw transactionException("Invalid Razorpay refund amount");
		}
		String paymentId = transactionDetail(transaction, PAYMENT_ID);
		Map<String, Object> payment = get("payments/" + paymentId, configuration);
		validatePaymentIdentity(payment, paymentId, transactionDetail(transaction, ORDER_ID), store);
		String status = requiredString(payment, "status");
		if (!"captured".equals(status)) {
			throw transactionException("Only captured Razorpay payments can be refunded");
		}

		Map<String, Object> refundRequest = new HashMap<String, Object>();
		refundRequest.put("amount", toMinorUnits(amount, store));
		Map<String, Object> refund = post("payments/" + paymentId + "/refund", refundRequest, configuration);
		String refundId = requiredString(refund, "id");
		if (!paymentId.equals(refund.get("payment_id"))) {
			throw transactionException("Razorpay returned a refund for a different payment");
		}
		if (number(refund.get("amount")) != toMinorUnits(amount, store)) {
			throw transactionException("Razorpay returned a refund for a different amount");
		}
		String refundStatus = requiredString(refund, "status");
		if (!"pending".equals(refundStatus) && !"processed".equals(refundStatus)) {
			throw transactionException("Razorpay did not accept the refund");
		}

		Transaction result = new Transaction();
		result.setAmount(amount);
		result.setOrder(order);
		result.setTransactionDate(new Date());
		result.setTransactionType(TransactionType.REFUND);
		result.setPaymentType(PaymentType.CREDITCARD);
		result.getTransactionDetails().put(PAYMENT_ID, paymentId);
		result.getTransactionDetails().put("REFUNDID", refundId);
		result.getTransactionDetails().put("TRNAPPROVED", refundStatus);
		result.getTransactionDetails().put("TRNORDERNUMBER", refundId);
		return result;
	}

	private Map<String, Object> verifyCheckout(MerchantStore store, BigDecimal amount, Payment payment,
			IntegrationConfiguration configuration) throws IntegrationException {
		validateModuleConfiguration(configuration, store);
		if (payment == null || payment.getPaymentMetaData() == null
				|| StringUtils.isBlank(payment.getPaymentMetaData().get("paymentToken"))) {
			throw transactionException("Missing Razorpay checkout response");
		}

		Map<String, String> checkout;
		try {
			checkout = OBJECT_MAPPER.readValue(payment.getPaymentMetaData().get("paymentToken"),
					new TypeReference<Map<String, String>>() { });
		} catch (IOException e) {
			throw transactionException("Invalid Razorpay checkout response", e);
		}
		String orderId = checkout.get("razorpay_order_id");
		String paymentId = checkout.get("razorpay_payment_id");
		String signature = checkout.get("razorpay_signature");
		if (StringUtils.isAnyBlank(orderId, paymentId, signature)) {
			throw transactionException("Incomplete Razorpay checkout response");
		}
		verifySignature(orderId, paymentId, signature, configuration.getIntegrationKeys().get("key_secret"));

		Map<String, Object> verifiedPayment = get("payments/" + paymentId, configuration);
		validatePayment(verifiedPayment, paymentId, orderId, amount, store);
		return verifiedPayment;
	}

	private void validatePayment(Map<String, Object> payment, String paymentId, String orderId, BigDecimal amount,
			MerchantStore store) throws IntegrationException {
		validatePaymentIdentity(payment, paymentId, orderId, store);
		if (number(payment.get("amount")) != toMinorUnits(amount, store)) {
			throw transactionException("Razorpay payment details do not match the order");
		}
	}

	private void validatePaymentIdentity(Map<String, Object> payment, String paymentId, String orderId,
			MerchantStore store) throws IntegrationException {
		if (!paymentId.equals(payment.get("id")) || !orderId.equals(payment.get("order_id"))
				|| !store.getCurrency().getCode().equalsIgnoreCase(String.valueOf(payment.get("currency")))) {
			throw transactionException("Razorpay payment details do not match the order");
		}
	}

	private void verifySignature(String orderId, String paymentId, String signature, String secret)
			throws IntegrationException {
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
			byte[] digest = mac.doFinal((orderId + "|" + paymentId).getBytes(StandardCharsets.UTF_8));
			StringBuilder expected = new StringBuilder();
			for (byte value : digest) {
				expected.append(String.format("%02x", value & 0xff));
			}
			if (!MessageDigest.isEqual(expected.toString().getBytes(StandardCharsets.US_ASCII),
					signature.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.US_ASCII))) {
				throw transactionException("Invalid Razorpay payment signature");
			}
		} catch (GeneralSecurityException e) {
			throw transactionException("Cannot verify Razorpay payment signature", e);
		}
	}

	private Transaction createTransaction(BigDecimal amount, TransactionType type, Map<String, Object> payment)
			throws IntegrationException {
		Transaction transaction = new Transaction();
		transaction.setAmount(amount);
		transaction.setTransactionDate(new Date());
		transaction.setTransactionType(type);
		transaction.setPaymentType(PaymentType.CREDITCARD);
		transaction.getTransactionDetails().put(PAYMENT_ID, requiredString(payment, "id"));
		transaction.getTransactionDetails().put(ORDER_ID, requiredString(payment, "order_id"));
		transaction.getTransactionDetails().put("TRANSACTIONID", requiredString(payment, "id"));
		transaction.getTransactionDetails().put("TRNAPPROVED", requiredString(payment, "status"));
		transaction.getTransactionDetails().put("TRNORDERNUMBER", requiredString(payment, "id"));
		return transaction;
	}

	private Map<String, Object> get(String path, IntegrationConfiguration configuration)
			throws IntegrationException {
		return request(path, HttpMethod.GET, null, configuration);
	}

	private Map<String, Object> post(String path, Map<String, Object> body, IntegrationConfiguration configuration)
			throws IntegrationException {
		return request(path, HttpMethod.POST, body, configuration);
	}

	private Map<String, Object> request(String path, HttpMethod method, Map<String, Object> body,
			IntegrationConfiguration configuration) throws IntegrationException {
		HttpHeaders headers = new HttpHeaders();
		headers.setBasicAuth(configuration.getIntegrationKeys().get("key_id"),
				configuration.getIntegrationKeys().get("key_secret"));
		headers.setContentType(MediaType.APPLICATION_JSON);
		try {
			ResponseEntity<Map> response = restTemplate.exchange(API_URL + path, method,
					new HttpEntity<Map<String, Object>>(body, headers), Map.class);
			if (response.getBody() == null) {
				throw transactionException("Razorpay returned an empty response");
			}
			return response.getBody();
		} catch (RestClientException e) {
			throw transactionException("Razorpay gateway request failed", e);
		}
	}

	private long toMinorUnits(BigDecimal amount, MerchantStore store) throws IntegrationException {
		if (amount == null || amount.signum() <= 0 || store.getCurrency() == null) {
			throw transactionException("Razorpay payment amount and currency are required");
		}
		try {
			int fractionDigits = java.util.Currency.getInstance(store.getCurrency().getCode()).getDefaultFractionDigits();
			if (fractionDigits < 0) {
				throw transactionException("Unsupported Razorpay currency");
			}
			return amount.setScale(fractionDigits, RoundingMode.UNNECESSARY)
					.movePointRight(fractionDigits).longValueExact();
		} catch (IllegalArgumentException | ArithmeticException e) {
			throw transactionException("Invalid amount or currency precision for Razorpay", e);
		}
	}

	private long number(Object value) throws IntegrationException {
		if (!(value instanceof Number)) {
			throw transactionException("Razorpay response is missing a valid amount");
		}
		try {
			return new BigDecimal(value.toString()).longValueExact();
		} catch (ArithmeticException e) {
			throw transactionException("Razorpay response contains an invalid amount", e);
		}
	}

	private String requiredString(Map<String, Object> values, String key) throws IntegrationException {
		Object value = values == null ? null : values.get(key);
		if (!(value instanceof String) || StringUtils.isBlank((String) value)) {
			throw transactionException("Razorpay response is missing " + key);
		}
		return (String) value;
	}

	private String transactionDetail(Transaction transaction, String key) throws IntegrationException {
		String value = transaction == null || transaction.getTransactionDetails() == null
				? null : transaction.getTransactionDetails().get(key);
		if (StringUtils.isBlank(value)) {
			throw transactionException("Missing Razorpay transaction " + key);
		}
		return value;
	}

	private IntegrationException transactionException(String message) {
		IntegrationException exception = new IntegrationException(message, (Exception) null);
		exception.setExceptionType(IntegrationException.TRANSACTION_EXCEPTION);
		exception.setErrorCode(IntegrationException.TRANSACTION_EXCEPTION);
		exception.setMessageCode("message.payment.error");
		return exception;
	}

	private IntegrationException transactionException(String message, Exception cause) {
		IntegrationException exception = new IntegrationException(message, cause);
		exception.setExceptionType(IntegrationException.TRANSACTION_EXCEPTION);
		exception.setErrorCode(IntegrationException.TRANSACTION_EXCEPTION);
		exception.setMessageCode("message.payment.error");
		return exception;
	}

	public void setRestTemplate(RestTemplate restTemplate) {
		if (restTemplate == null) {
			throw new IllegalArgumentException("RestTemplate cannot be null");
		}
		this.restTemplate = restTemplate;
	}
}
