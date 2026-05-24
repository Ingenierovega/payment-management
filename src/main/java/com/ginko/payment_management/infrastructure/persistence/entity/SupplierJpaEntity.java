package com.ginko.payment_management.infrastructure.persistence.entity;

import com.ginko.payment_management.domain.model.SupplierStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;

@Entity
@Table(
		name = "suppliers",
		uniqueConstraints = @UniqueConstraint(
				name = "uk_suppliers_tax_identification_number",
				columnNames = "tax_identification_number"
		)
)
public class SupplierJpaEntity {

	@Id
	@Column(nullable = false, updatable = false)
	private UUID id;

	@Column(name = "business_name", nullable = false, length = 160)
	private String businessName;

	@Column(name = "tax_identification_number", nullable = false, length = 60)
	private String taxIdentificationNumber;

	@Column(nullable = false, length = 160)
	private String email;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private SupplierStatus status;

	protected SupplierJpaEntity() {
	}

	public SupplierJpaEntity(
			UUID id,
			String businessName,
			String taxIdentificationNumber,
			String email,
			SupplierStatus status
	) {
		this.id = id;
		this.businessName = businessName;
		this.taxIdentificationNumber = taxIdentificationNumber;
		this.email = email;
		this.status = status;
	}

	public UUID getId() {
		return id;
	}

	public String getBusinessName() {
		return businessName;
	}

	public String getTaxIdentificationNumber() {
		return taxIdentificationNumber;
	}

	public String getEmail() {
		return email;
	}

	public SupplierStatus getStatus() {
		return status;
	}
}
