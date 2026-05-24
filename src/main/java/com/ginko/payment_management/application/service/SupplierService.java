package com.ginko.payment_management.application.service;

import com.ginko.payment_management.application.command.ChangeSupplierStatusCommand;
import com.ginko.payment_management.application.command.CreateSupplierCommand;
import com.ginko.payment_management.application.command.UpdateSupplierCommand;
import com.ginko.payment_management.application.port.out.SupplierRepositoryPort;
import com.ginko.payment_management.domain.exception.BusinessValidationException;
import com.ginko.payment_management.domain.exception.ConflictException;
import com.ginko.payment_management.domain.exception.ResourceNotFoundException;
import com.ginko.payment_management.domain.model.Supplier;
import com.ginko.payment_management.domain.model.SupplierStatus;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SupplierService {

	private final SupplierRepositoryPort suppliers;

	public SupplierService(SupplierRepositoryPort suppliers) {
		this.suppliers = suppliers;
	}

	@Transactional
	public Supplier create(CreateSupplierCommand command) {
		Supplier supplier = Supplier.create(
				normalizeRequired(command.businessName(), "El nombre o razon social es obligatorio."),
				normalizeRequired(command.taxIdentificationNumber(), "El numero tributario es obligatorio."),
				normalizeRequired(command.email(), "El correo electronico es obligatorio.")
		);
		ensureTaxIdentificationNumberIsAvailable(supplier.taxIdentificationNumber(), null);
		return suppliers.save(supplier);
	}

	@Transactional(readOnly = true)
	public Page<Supplier> findAll(SupplierStatus status, Pageable pageable) {
		return suppliers.findAll(status, pageable);
	}

	@Transactional(readOnly = true)
	public Supplier getById(UUID id) {
		return suppliers.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado: " + id));
	}

	@Transactional
	public Supplier update(UUID id, UpdateSupplierCommand command) {
		Supplier current = getById(id);
		String taxIdentificationNumber = normalizeRequired(
				command.taxIdentificationNumber(),
				"El numero tributario es obligatorio."
		);
		ensureTaxIdentificationNumberIsAvailable(taxIdentificationNumber, id);
		Supplier updated = current.update(
				normalizeRequired(command.businessName(), "El nombre o razon social es obligatorio."),
				taxIdentificationNumber,
				normalizeRequired(command.email(), "El correo electronico es obligatorio.")
		);
		return suppliers.save(updated);
	}

	@Transactional
	public Supplier changeStatus(UUID id, ChangeSupplierStatusCommand command) {
		if (command.status() == null) {
			throw new BusinessValidationException("El estado del proveedor es obligatorio.");
		}
		return suppliers.save(getById(id).changeStatus(command.status()));
	}

	private void ensureTaxIdentificationNumberIsAvailable(String taxIdentificationNumber, UUID currentSupplierId) {
		suppliers.findByTaxIdentificationNumber(taxIdentificationNumber)
				.filter(existing -> currentSupplierId == null || !existing.id().equals(currentSupplierId))
				.ifPresent(existing -> {
					throw new ConflictException("Ya existe un proveedor con el numero tributario indicado.");
				});
	}

	private static String normalizeRequired(String value, String message) {
		if (value == null || value.isBlank()) {
			throw new BusinessValidationException(message);
		}
		return value.trim();
	}
}
