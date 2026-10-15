package com.salesmanager.test.modules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.InputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.salesmanager.core.business.modules.integration.payment.impl.RazorpayPayment;
import com.salesmanager.core.business.services.payments.PaymentServiceImpl;
import com.salesmanager.core.model.customer.Customer;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.order.Order;
import com.salesmanager.core.model.payments.Payment;
import com.salesmanager.core.model.payments.Transaction;
import com.salesmanager.core.model.payments.TransactionType;
import com.salesmanager.core.model.reference.currency.Currency;
import com.salesmanager.core.model.system.IntegrationConfiguration;
import com.salesmanager.core.modules.integration.IntegrationException;

import com.salesmanager.core.modules.integration.payment.model.PaymentModule;

public class RazorpayPaymentTest {

	private static final String API_URL = "https://api.razorpay.com/v1/";
	private static final String KEY_ID = "rzp_test_key";
	private static final String SECRET = "test_secret";
	private static final String ORDER_ID = "order_test123";
	private static final String PAYMENT_ID = "pay_test123";

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final RestTemplate restTemplate = new RestTemplate();
	private final MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
	private final RazorpayPayment razorpay = new RazorpayPayment();
	private final MerchantStore store = new MerchantStore();
	private final IntegrationConfiguration configuration = new IntegrationConfiguration();

	@Before
	public void setUp() {
		Currency currency = new Currency();
		currency.setCurrency(java.util.Currency.getInstance("INR"));
		store.setCurrency(currency);
		Map<String, String> keys = new HashMap<String, String>();
		keys.put("key_id", KEY_ID);
		keys.put("key_secret", SECRET);
		configuration.setIntegrationKeys(keys);
		razorpay.setRestTemplate(restTemplate);
	}

	@Test
	public void initializesCheckoutWithServerCreatedOrder() throws Exception {
		server.expect(requestTo(API_URL + "orders"))
				.andExpect(method(POST))
				.andExpect(header("Authorization", "Basic " + java.util.Base64.getEncoder()
						.encodeToString((KEY_ID + ":" + SECRET).getBytes(StandardCharsets.UTF_8))))
				.andRespond(withSuccess("{\"id\":\"" + ORDER_ID
						+ "\",\"status\":\"created\",\"amount\":1250,\"currency\":\"INR\"}",
						MediaType.APPLICATION_JSON));

		Transaction transaction = razorpay.initTransaction(store, new Customer(), new BigDecimal("12.50"),
				new Payment(), configuration, null);

		assertEquals(TransactionType.INIT, transaction.getTransactionType());
		Map<?, ?> checkout = objectMapper.readValue(transaction.getDetails(), Map.class);
		assertEquals(KEY_ID, checkout.get("key"));
		assertEquals(ORDER_ID, checkout.get("order_id"));
		assertEquals(1250, ((Number) checkout.get("amount")).intValue());
		server.verify();
	}

	@Test
	public void authorizesOnlySignatureVerifiedGatewayPayment() throws Exception {
		expectVerifiedPayment("authorized");
		Transaction transaction = razorpay.authorize(store, new Customer(), null, new BigDecimal("12.50"),
				checkoutPayment(ORDER_ID, PAYMENT_ID), configuration, null);
		assertEquals(TransactionType.AUTHORIZE, transaction.getTransactionType());
		assertEquals(PAYMENT_ID, transaction.getTransactionDetails().get("PAYMENTID"));
		server.verify();
	}

	@Test
	public void capturesAuthorizedPaymentAfterVerifyingCheckout() throws Exception {
		expectVerifiedPayment("authorized");
		server.expect(requestTo(API_URL + "payments/" + PAYMENT_ID + "/capture"))
				.andExpect(method(POST))
				.andRespond(withSuccess(paymentResponse("captured"), MediaType.APPLICATION_JSON));

		Transaction transaction = razorpay.authorizeAndCapture(store, new Customer(), null,
				new BigDecimal("12.50"), checkoutPayment(ORDER_ID, PAYMENT_ID), configuration, null);
		assertEquals(TransactionType.AUTHORIZECAPTURE, transaction.getTransactionType());
		assertEquals("captured", transaction.getTransactionDetails().get("TRNAPPROVED"));
		server.verify();
	}

	@Test
	public void rejectsInvalidSignatureBeforeCallingGateway() throws Exception {
		Payment payment = checkoutPayment(ORDER_ID, PAYMENT_ID);
		payment.getPaymentMetaData().put("paymentToken", objectMapper.writeValueAsString(checkoutResponse(
				ORDER_ID, PAYMENT_ID, "invalid-signature")));
		try {
			razorpay.authorize(store, new Customer(), null, new BigDecimal("12.50"), payment, configuration, null);
			fail("Invalid signature should not authorize a payment");
		} catch (IntegrationException expected) {
			assertTrue(expected.getMessage().contains("signature"));
		}
		server.verify();
	}

	@Test
	public void rejectsGatewayErrorsWithoutCreatingTransaction() throws Exception {
		server.expect(requestTo(API_URL + "orders"))
				.andExpect(method(POST))
				.andRespond(withBadRequest().body("{\"error\":{\"description\":\"rejected\"}}"));
		try {
			razorpay.initTransaction(store, new Customer(), new BigDecimal("12.50"), new Payment(),
					configuration, null);
			fail("Gateway rejection must fail initialization");
		} catch (IntegrationException expected) {
			assertTrue(expected.getMessage().contains("gateway request failed"));
		}
		server.verify();
	}

	@Test
	public void reportsMissingCredentialsAsConfigurationErrors() throws Exception {
		IntegrationConfiguration invalid = new IntegrationConfiguration();
		try {
			razorpay.validateModuleConfiguration(invalid, store);
			fail("Missing credentials must be rejected");
		} catch (IntegrationException expected) {
			assertEquals(IntegrationException.ERROR_VALIDATION_SAVE, expected.getErrorCode());
			assertEquals(2, expected.getErrorFields().size());
		}
	}

	@Test
	public void registersAndSelectsRazorpayUsingTheExistingPaymentStrategy() throws Exception {
		String moduleConfiguration;
		try (InputStream input = new ClassPathResource("spring/shopizer-core-modules.xml").getInputStream()) {
			moduleConfiguration = IOUtils.toString(input, StandardCharsets.UTF_8);
		}
		assertTrue(moduleConfiguration.contains("<beans:entry key=\"razorpay\" value-ref=\"razorpay\" />"));
		assertTrue(moduleConfiguration.contains(
				"class=\"com.salesmanager.core.business.modules.integration.payment.impl.RazorpayPayment\""));

		JsonNode modules;
		try (InputStream input = new ClassPathResource("reference/integrationmodules.json").getInputStream()) {
			modules = objectMapper.readTree(input);
		}
		boolean moduleIsPublished = false;
		for (JsonNode module : modules) {
			if ("razorpay".equals(module.path("code").asText())) {
				moduleIsPublished = module.path("type").asText().equals("creditcard")
						&& module.path("regions").toString().contains("\"IN\"");
			}
		}
		assertTrue(moduleIsPublished);

		PaymentServiceImpl service = new PaymentServiceImpl();
		Map<String, PaymentModule> paymentModules = new HashMap<String, PaymentModule>();
		paymentModules.put("razorpay", razorpay);
		ReflectionTestUtils.setField(service, "paymentModules", paymentModules);
		assertSame(razorpay, service.getPaymentModule("razorpay"));
	}

	@Test
	public void capturesPreviouslyAuthorizedPayment() throws Exception {
		server.expect(requestTo(API_URL + "payments/" + PAYMENT_ID))
				.andExpect(method(GET))
				.andRespond(withSuccess(paymentResponse("authorized"), MediaType.APPLICATION_JSON));
		server.expect(requestTo(API_URL + "payments/" + PAYMENT_ID + "/capture"))
				.andExpect(method(POST))
				.andRespond(withSuccess(paymentResponse("captured"), MediaType.APPLICATION_JSON));
		Transaction authorized = new Transaction();
		authorized.getTransactionDetails().put("PAYMENTID", PAYMENT_ID);
		authorized.getTransactionDetails().put("ORDERID", ORDER_ID);
		Order order = new Order();
		order.setTotal(new BigDecimal("12.50"));

		Transaction captured = razorpay.capture(store, new Customer(), order, authorized, configuration, null);
		assertEquals(TransactionType.CAPTURE, captured.getTransactionType());
		assertEquals("captured", captured.getTransactionDetails().get("TRNAPPROVED"));
		server.verify();
	}

	@Test
	public void recordsGatewayAcceptedPartialRefund() throws Exception {
		server.expect(requestTo(API_URL + "payments/" + PAYMENT_ID))
				.andExpect(method(GET))
				.andRespond(withSuccess(paymentResponse("captured"), MediaType.APPLICATION_JSON));
		server.expect(requestTo(API_URL + "payments/" + PAYMENT_ID + "/refund"))
				.andExpect(method(POST))
				.andRespond(withSuccess("{\"id\":\"rfnd_test123\",\"payment_id\":\"" + PAYMENT_ID
						+ "\",\"amount\":500,\"status\":\"pending\"}", MediaType.APPLICATION_JSON));
		Transaction captured = new Transaction();
		captured.getTransactionDetails().put("PAYMENTID", PAYMENT_ID);
		captured.getTransactionDetails().put("ORDERID", ORDER_ID);
		Order order = new Order();
		order.setTotal(new BigDecimal("12.50"));

		Transaction refund = razorpay.refund(true, store, captured, order, new BigDecimal("5.00"),
				configuration, null);
		assertEquals(TransactionType.REFUND, refund.getTransactionType());
		assertEquals("pending", refund.getTransactionDetails().get("TRNAPPROVED"));
		assertEquals("rfnd_test123", refund.getTransactionDetails().get("REFUNDID"));
		server.verify();
	}

	private void expectVerifiedPayment(String status) throws IOException {
		server.expect(requestTo(API_URL + "payments/" + PAYMENT_ID))
				.andExpect(method(GET))
				.andRespond(withSuccess(paymentResponse(status), MediaType.APPLICATION_JSON));
	}

	private String paymentResponse(String status) throws IOException {
		Map<String, Object> response = new HashMap<String, Object>();
		response.put("id", PAYMENT_ID);
		response.put("order_id", ORDER_ID);
		response.put("amount", 1250);
		response.put("currency", "INR");
		response.put("status", status);
		return objectMapper.writeValueAsString(response);
	}

	private Payment checkoutPayment(String orderId, String paymentId) throws Exception {
		Payment payment = new Payment();
		Map<String, String> metadata = new HashMap<String, String>();
		metadata.put("paymentToken", objectMapper.writeValueAsString(
				checkoutResponse(orderId, paymentId, signature(orderId, paymentId))));
		payment.setPaymentMetaData(metadata);
		return payment;
	}

	private Map<String, String> checkoutResponse(String orderId, String paymentId, String signature) {
		Map<String, String> response = new HashMap<String, String>();
		response.put("razorpay_order_id", orderId);
		response.put("razorpay_payment_id", paymentId);
		response.put("razorpay_signature", signature);
		return response;
	}

	private String signature(String orderId, String paymentId) throws GeneralSecurityException {
		Mac mac = Mac.getInstance("HmacSHA256");
		mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
		byte[] digest = mac.doFinal((orderId + "|" + paymentId).getBytes(StandardCharsets.UTF_8));
		StringBuilder result = new StringBuilder();
		for (byte value : digest) {
			result.append(String.format("%02x", value & 0xff));
		}
		return result.toString();
	}
}
