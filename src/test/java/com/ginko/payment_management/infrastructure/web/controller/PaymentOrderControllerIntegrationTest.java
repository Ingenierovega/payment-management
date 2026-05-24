package com.ginko.payment_management.infrastructure.web.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentOrderControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void createPaymentOrderForActiveSupplierReturnsDraftOrder() throws Exception {
		String taxIdentificationNumber = "NIT-" + UUID.randomUUID();
		String supplierJson = objectMapper.writeValueAsString(Map.of(
				"businessName", "Proveedor Integracion",
				"taxIdentificationNumber", taxIdentificationNumber,
				"email", "integracion@example.com"
		));

		MvcResult supplierResult = mockMvc.perform(post("/api/v1/suppliers")
						.contentType(MediaType.APPLICATION_JSON)
						.content(supplierJson))
				.andExpect(status().isCreated())
				.andReturn();
		JsonNode supplier = objectMapper.readTree(supplierResult.getResponse().getContentAsString());
		String supplierId = supplier.get("id").asText();

		String orderJson = objectMapper.writeValueAsString(Map.of(
				"supplierId", supplierId,
				"amount", BigDecimal.valueOf(250000),
				"concept", "Servicios de soporte"
		));

		mockMvc.perform(post("/api/v1/payment-orders")
						.header("Idempotency-Key", "integration-" + UUID.randomUUID())
						.contentType(MediaType.APPLICATION_JSON)
						.content(orderJson))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.supplierId").value(supplierId))
				.andExpect(jsonPath("$.amount").value(250000))
				.andExpect(jsonPath("$.concept").value("Servicios de soporte"))
				.andExpect(jsonPath("$.status").value("BORRADOR"));
	}
}
