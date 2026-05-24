package com.ginko.payment_management.infrastructure.persistence.repository;

import com.ginko.payment_management.infrastructure.persistence.entity.IdempotencyKeyJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotencyKeyJpaRepository extends JpaRepository<IdempotencyKeyJpaEntity, String> {
}
