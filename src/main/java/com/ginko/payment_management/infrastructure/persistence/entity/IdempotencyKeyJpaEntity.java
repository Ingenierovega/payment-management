package com.ginko.payment_management.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "idempotency_keys")
public class IdempotencyKeyJpaEntity {

	@Id
	@Column(name = "idempotency_key", nullable = false, length = 120)
	private String key;

	@Column(name = "order_id", nullable = false, updatable = false)
	private UUID orderId;

	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	protected IdempotencyKeyJpaEntity() {
	}

	public IdempotencyKeyJpaEntity(String key, UUID orderId, OffsetDateTime createdAt) {
		this.key = key;
		this.orderId = orderId;
		this.createdAt = createdAt;
	}

	public String getKey() {
		return key;
	}

	public UUID getOrderId() {
		return orderId;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}
}
