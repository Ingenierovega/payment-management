package com.ginko.payment_management.infrastructure.web.controller;

import com.ginko.payment_management.application.command.CreatePaymentOrderCommand;
import com.ginko.payment_management.application.command.TransitionPaymentOrderStatusCommand;
import com.ginko.payment_management.application.result.PaymentOrderCreationResult;
import com.ginko.payment_management.application.service.PaymentOrderService;
import com.ginko.payment_management.domain.model.PaymentOrderStatus;
import com.ginko.payment_management.infrastructure.web.dto.CreatePaymentOrderRequest;
import com.ginko.payment_management.infrastructure.web.dto.PageResponse;
import com.ginko.payment_management.infrastructure.web.dto.PaymentOrderResponse;
import com.ginko.payment_management.infrastructure.web.dto.TransitionPaymentOrderStatusRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.net.URI;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/payment-orders")
@Tag(name = "Ordenes de pago")
public class PaymentOrderController {

	private final PaymentOrderService paymentOrderService;

	public PaymentOrderController(PaymentOrderService paymentOrderService) {
		this.paymentOrderService = paymentOrderService;
	}

	@PostMapping
	@Operation(summary = "Crear orden de pago")
	public ResponseEntity<PaymentOrderResponse> create(
			@RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
			@Valid @RequestBody CreatePaymentOrderRequest request
	) {
		PaymentOrderCreationResult result = paymentOrderService.create(
				new CreatePaymentOrderCommand(request.supplierId(), request.amount(), request.concept()),
				idempotencyKey
		);
		PaymentOrderResponse response = PaymentOrderResponse.from(result.paymentOrder());
		if (!result.created()) {
			return ResponseEntity.ok(response);
		}
		return ResponseEntity
				.created(URI.create("/api/v1/payment-orders/" + result.paymentOrder().id()))
				.body(response);
	}

	@GetMapping
	@Operation(summary = "Listar ordenes de pago")
	public PageResponse<PaymentOrderResponse> findAll(
			@RequestParam(required = false) PaymentOrderStatus status,
			@RequestParam(required = false) UUID supplierId,
			@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
	) {
		return PageResponse.from(paymentOrderService.findAll(status, supplierId, pageable)
				.map(PaymentOrderResponse::from));
	}

	@GetMapping("/due-soon")
	@Operation(summary = "Listar ordenes proximas a vencer")
	public PageResponse<PaymentOrderResponse> findDueSoon(
			@RequestParam(defaultValue = "7") @Min(1) @Max(90) Integer days,
			@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable
	) {
		return PageResponse.from(paymentOrderService.findDueSoon(days, pageable)
				.map(PaymentOrderResponse::from));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Consultar orden de pago por identificador")
	public PaymentOrderResponse getById(@PathVariable UUID id) {
		return PaymentOrderResponse.from(paymentOrderService.getById(id));
	}

	@PatchMapping("/{id}/status")
	@Operation(summary = "Transicionar estado de una orden")
	public PaymentOrderResponse transition(
			@PathVariable UUID id,
			@Valid @RequestBody TransitionPaymentOrderStatusRequest request
	) {
		return PaymentOrderResponse.from(paymentOrderService.transition(
				id,
				new TransitionPaymentOrderStatusCommand(request.status())
		));
	}
}
