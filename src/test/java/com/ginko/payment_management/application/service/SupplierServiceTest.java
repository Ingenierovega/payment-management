package com.ginko.payment_management.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ginko.payment_management.application.command.ChangeSupplierStatusCommand;
import com.ginko.payment_management.application.command.CreateSupplierCommand;
import com.ginko.payment_management.application.port.out.SupplierRepositoryPort;
import com.ginko.payment_management.domain.exception.ConflictException;
import com.ginko.payment_management.domain.model.Supplier;
import com.ginko.payment_management.domain.model.SupplierStatus;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SupplierServiceTest {

	@Mock
	private SupplierRepositoryPort suppliers;

	@InjectMocks
	private SupplierService service;

	@Test
	void createRejectsDuplicatedTaxIdentificationNumber() {
		when(suppliers.findByTaxIdentificationNumber("900123456")).thenReturn(Optional.of(new Supplier(
				UUID.randomUUID(),
				"Proveedor Existente",
				"900123456",
				"existente@example.com",
				SupplierStatus.ACTIVO
		)));

		assertThatThrownBy(() -> service.create(new CreateSupplierCommand(
				"Nuevo Proveedor",
				" 900123456 ",
				"nuevo@example.com"
		)))
				.isInstanceOf(ConflictException.class)
				.hasMessageContaining("numero tributario");

		verify(suppliers, never()).save(any());
	}

	@Test
	void changeStatusUpdatesSupplierState() {
		UUID supplierId = UUID.randomUUID();
		Supplier activeSupplier = new Supplier(
				supplierId,
				"Proveedor Activo",
				"901222333",
				"activo@example.com",
				SupplierStatus.ACTIVO
		);
		when(suppliers.findById(supplierId)).thenReturn(Optional.of(activeSupplier));
		when(suppliers.save(any(Supplier.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Supplier result = service.changeStatus(
				supplierId,
				new ChangeSupplierStatusCommand(SupplierStatus.INACTIVO)
		);

		assertThat(result.status()).isEqualTo(SupplierStatus.INACTIVO);
	}
}
