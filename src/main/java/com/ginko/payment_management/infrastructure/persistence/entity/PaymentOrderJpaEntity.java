package com.ginko.payment_management.infrastructure.persistence.entity;

import com.ginko.payment_management.domain.model.PaymentOrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
		name = "payment_orders",
		indexes = {
				@Index(name = "idx_payment_orders_status", columnList = "status"),
				@Index(name = "idx_payment_orders_supplier", columnList = "supplier_id"),
				@Index(name = "idx_payment_orders_created_at", columnList = "created_at"),
				@Index(name = "idx_payment_orders_paid_at", columnList = "paid_at")
		}
)
public class PaymentOrderJpaEntity {

	@Id
	@Column(nullable = false, updatable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "supplier_id", nullable = false, foreignKey = @ForeignKey(name = "fk_payment_orders_supplier"))
	private SupplierJpaEntity supplier;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal amount;

	@Column(nullable = false, length = 250)
	private String concept;

	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private PaymentOrderStatus status;

	@Column(name = "paid_at")
	private OffsetDateTime paidAt;

	@Version
	@Column(nullable = false)
	private Long version;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public SupplierJpaEntity getSupplier() {
		return supplier;
	}

	public void setSupplier(SupplierJpaEntity supplier) {
		this.supplier = supplier;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public String getConcept() {
		return concept;
	}

	public void setConcept(String concept) {
		this.concept = concept;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(OffsetDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public PaymentOrderStatus getStatus() {
		return status;
	}

	public void setStatus(PaymentOrderStatus status) {
		this.status = status;
	}

	public OffsetDateTime getPaidAt() {
		return paidAt;
	}

	public void setPaidAt(OffsetDateTime paidAt) {
		this.paidAt = paidAt;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}
}
