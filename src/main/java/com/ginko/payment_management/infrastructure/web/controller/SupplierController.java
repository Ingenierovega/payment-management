package com.ginko.payment_management.infrastructure.web.controller;

import com.ginko.payment_management.application.command.ChangeSupplierStatusCommand;
import com.ginko.payment_management.application.command.CreateSupplierCommand;
import com.ginko.payment_management.application.command.UpdateSupplierCommand;
import com.ginko.payment_management.application.result.PaidTotalReport;
import com.ginko.payment_management.application.service.PaymentOrderService;
import com.ginko.payment_management.application.service.SupplierService;
import com.ginko.payment_management.domain.model.SupplierStatus;
import com.ginko.payment_management.infrastructure.web.dto.ChangeSupplierStatusRequest;
import com.ginko.payment_management.infrastructure.web.dto.CreateSupplierRequest;
import com.ginko.payment_management.infrastructure.web.dto.PageResponse;
import com.ginko.payment_management.infrastructure.web.dto.PaidTotalResponse;
import com.ginko.payment_management.infrastructure.web.dto.SupplierResponse;
import com.ginko.payment_management.infrastructure.web.dto.UpdateSupplierRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/suppliers")
@Tag(name = "Proveedores")
public class SupplierController {

	private final SupplierService supplierService;
	private final PaymentOrderService paymentOrderService;

	public SupplierController(SupplierService supplierService, PaymentOrderService paymentOrderService) {
		this.supplierService = supplierService;
		this.paymentOrderService = paymentOrderService;
	}

	@PostMapping
	@Operation(summary = "Crear proveedor")
	public ResponseEntity<SupplierResponse> create(@Valid @RequestBody CreateSupplierRequest request) {
		var created = supplierService.create(new CreateSupplierCommand(
				request.businessName(),
				request.taxIdentificationNumber(),
				request.email()
		));
		return ResponseEntity
				.created(URI.create("/api/v1/suppliers/" + created.id()))
				.body(SupplierResponse.from(created));
	}

	@GetMapping
	@Operation(summary = "Listar proveedores")
	public PageResponse<SupplierResponse> findAll(
			@RequestParam(required = false) SupplierStatus status,
			@PageableDefault(size = 20, sort = "businessName") Pageable pageable
	) {
		return PageResponse.from(supplierService.findAll(status, pageable).map(SupplierResponse::from));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Consultar proveedor por identificador")
	public SupplierResponse getById(@PathVariable UUID id) {
		return SupplierResponse.from(supplierService.getById(id));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Actualizar proveedor")
	public SupplierResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateSupplierRequest request) {
		return SupplierResponse.from(supplierService.update(
				id,
				new UpdateSupplierCommand(
						request.businessName(),
						request.taxIdentificationNumber(),
						request.email()
				)
		));
	}

	@PatchMapping("/{id}/status")
	@Operation(summary = "Cambiar estado de proveedor")
	public SupplierResponse changeStatus(@PathVariable UUID id, @Valid @RequestBody ChangeSupplierStatusRequest request) {
		return SupplierResponse.from(supplierService.changeStatus(
				id,
				new ChangeSupplierStatusCommand(request.status())
		));
	}

	@GetMapping("/{id}/paid-total")
	@Operation(summary = "Reporte agregado de total pagado por proveedor")
	public PaidTotalResponse paidTotal(
			@PathVariable UUID id,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to
	) {
		PaidTotalReport report = paymentOrderService.paidTotal(id, from, to);
		return PaidTotalResponse.from(report);
	}
}
