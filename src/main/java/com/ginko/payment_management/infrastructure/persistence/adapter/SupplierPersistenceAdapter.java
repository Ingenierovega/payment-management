package com.ginko.payment_management.infrastructure.persistence.adapter;

import com.ginko.payment_management.application.port.out.SupplierRepositoryPort;
import com.ginko.payment_management.domain.model.Supplier;
import com.ginko.payment_management.domain.model.SupplierStatus;
import com.ginko.payment_management.infrastructure.persistence.entity.SupplierJpaEntity;
import com.ginko.payment_management.infrastructure.persistence.repository.SupplierJpaRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class SupplierPersistenceAdapter implements SupplierRepositoryPort {

	private final SupplierJpaRepository repository;

	public SupplierPersistenceAdapter(SupplierJpaRepository repository) {
		this.repository = repository;
	}

	@Override
	public Supplier save(Supplier supplier) {
		return toDomain(repository.save(toEntity(supplier)));
	}

	@Override
	public Optional<Supplier> findById(UUID id) {
		return repository.findById(id).map(this::toDomain);
	}

	@Override
	public Optional<Supplier> findByTaxIdentificationNumber(String taxIdentificationNumber) {
		return repository.findByTaxIdentificationNumber(taxIdentificationNumber).map(this::toDomain);
	}

	@Override
	public Page<Supplier> findAll(SupplierStatus status, Pageable pageable) {
		if (status == null) {
			return repository.findAll(pageable).map(this::toDomain);
		}
		return repository.findByStatus(status, pageable).map(this::toDomain);
	}

	private SupplierJpaEntity toEntity(Supplier supplier) {
		return new SupplierJpaEntity(
				supplier.id(),
				supplier.businessName(),
				supplier.taxIdentificationNumber(),
				supplier.email(),
				supplier.status()
		);
	}

	private Supplier toDomain(SupplierJpaEntity entity) {
		return new Supplier(
				entity.getId(),
				entity.getBusinessName(),
				entity.getTaxIdentificationNumber(),
				entity.getEmail(),
				entity.getStatus()
		);
	}
}
