package com.ginko.payment_management.infrastructure.persistence.adapter;

import com.ginko.payment_management.application.port.out.IdempotencyRepositoryPort;
import com.ginko.payment_management.infrastructure.persistence.entity.IdempotencyKeyJpaEntity;
import com.ginko.payment_management.infrastructure.persistence.repository.IdempotencyKeyJpaRepository;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class IdempotencyPersistenceAdapter implements IdempotencyRepositoryPort {

	private final IdempotencyKeyJpaRepository repository;

	public IdempotencyPersistenceAdapter(IdempotencyKeyJpaRepository repository) {
		this.repository = repository;
	}

	@Override
	public Optional<UUID> findOrderIdByKey(String key) {
		return repository.findById(key).map(IdempotencyKeyJpaEntity::getOrderId);
	}

	@Override
	public void save(String key, UUID orderId, OffsetDateTime createdAt) {
		repository.save(new IdempotencyKeyJpaEntity(key, orderId, createdAt));
	}
}
