package com.ginko.payment_management.domain.model;

import com.ginko.payment_management.domain.exception.BusinessValidationException;
import java.util.Objects;
import java.util.UUID;

public record Supplier(
		UUID id,
		String businessName,
		String taxIdentificationNumber,
		String email,
		SupplierStatus status
) {

	public Supplier {
		Objects.requireNonNull(id, "id is required");
		businessName = requireText(businessName, "El nombre o razon social es obligatorio.");
		taxIdentificationNumber = requireText(taxIdentificationNumber, "El numero tributario es obligatorio.");
		email = requireText(email, "El correo electronico es obligatorio.");
		Objects.requireNonNull(status, "status is required");
	}

	public static Supplier create(String businessName, String taxIdentificationNumber, String email) {
		return new Supplier(
				UUID.randomUUID(),
				businessName,
				taxIdentificationNumber,
				email,
				SupplierStatus.ACTIVO
		);
	}

	public Supplier update(String businessName, String taxIdentificationNumber, String email) {
		return new Supplier(id, businessName, taxIdentificationNumber, email, status);
	}

	public Supplier changeStatus(SupplierStatus newStatus) {
		return new Supplier(id, businessName, taxIdentificationNumber, email, newStatus);
	}

	private static String requireText(String value, String message) {
		if (value == null || value.isBlank()) {
			throw new BusinessValidationException(message);
		}
		return value.trim();
	}
}
