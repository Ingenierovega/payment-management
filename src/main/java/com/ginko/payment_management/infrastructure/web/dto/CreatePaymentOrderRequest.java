package com.ginko.payment_management.infrastructure.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

public record CreatePaymentOrderRequest(
		@NotNull UUID supplierId,
		@NotNull @DecimalMin(value = "0.00", inclusive = false) BigDecimal amount,
		@NotBlank @Size(max = 250) String concept
) {
}
