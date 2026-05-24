package com.ginko.payment_management.infrastructure.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSupplierRequest(
		@NotBlank @Size(max = 160) String businessName,
		@NotBlank @Size(max = 60) String taxIdentificationNumber,
		@NotBlank @Email @Size(max = 160) String email
) {
}
