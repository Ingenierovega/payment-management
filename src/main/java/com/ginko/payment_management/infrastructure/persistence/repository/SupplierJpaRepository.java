package com.ginko.payment_management.infrastructure.persistence.repository;

import com.ginko.payment_management.domain.model.SupplierStatus;
import com.ginko.payment_management.infrastructure.persistence.entity.SupplierJpaEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierJpaRepository extends JpaRepository<SupplierJpaEntity, UUID> {

	Optional<SupplierJpaEntity> findByTaxIdentificationNumber(String taxIdentificationNumber);

	Page<SupplierJpaEntity> findByStatus(SupplierStatus status, Pageable pageable);
}
