package com.ginko.payment_management.infrastructure.web.dto;

import com.ginko.payment_management.domain.model.Supplier;
import com.ginko.payment_management.domain.model.SupplierStatus;
import java.util.UUID;

public record SupplierResponse(
		UUID id,
		String businessName,
		String taxIdentificationNumber,
		String email,
		SupplierStatus status
) {

	public static SupplierResponse from(Supplier supplier) {
		return new SupplierResponse(
				supplier.id(),
				supplier.businessName(),
				supplier.taxIdentificationNumber(),
				supplier.email(),
				supplier.status()
		);
	}
}
