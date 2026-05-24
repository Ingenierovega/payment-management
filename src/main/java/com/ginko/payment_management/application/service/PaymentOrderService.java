package com.ginko.payment_management.application.service;

import com.ginko.payment_management.application.command.CreatePaymentOrderCommand;
import com.ginko.payment_management.application.command.TransitionPaymentOrderStatusCommand;
import com.ginko.payment_management.application.port.out.IdempotencyRepositoryPort;
import com.ginko.payment_management.application.port.out.PaymentOrderRepositoryPort;
import com.ginko.payment_management.application.port.out.SupplierRepositoryPort;
import com.ginko.payment_management.application.result.PaidTotalReport;
import com.ginko.payment_management.application.result.PaymentOrderCreationResult;
import com.ginko.payment_management.domain.exception.BusinessValidationException;
import com.ginko.payment_management.domain.exception.ResourceNotFoundException;
import com.ginko.payment_management.domain.model.PaymentOrder;
import com.ginko.payment_management.domain.model.PaymentOrderStatus;
import com.ginko.payment_management.domain.model.Supplier;
import com.ginko.payment_management.domain.model.SupplierStatus;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentOrderService {

	private static final int DEFAULT_DUE_SOON_DAYS = 7;
	private static final int MAX_DUE_SOON_DAYS = 90;

	private final PaymentOrderRepositoryPort paymentOrders;
	private final SupplierRepositoryPort suppliers;
	private final IdempotencyRepositoryPort idempotencyKeys;
	private final Clock clock;

	public PaymentOrderService(
			PaymentOrderRepositoryPort paymentOrders,
			SupplierRepositoryPort suppliers,
			IdempotencyRepositoryPort idempotencyKeys,
			Clock clock
	) {
		this.paymentOrders = paymentOrders;
		this.suppliers = suppliers;
		this.idempotencyKeys = idempotencyKeys;
		this.clock = clock;
	}

	@Transactional
	public PaymentOrderCreationResult create(CreatePaymentOrderCommand command, String idempotencyKey) {
		String normalizedKey = normalizeOptionalText(idempotencyKey);
		if (normalizedKey != null) {
			return idempotencyKeys.findOrderIdByKey(normalizedKey)
					.map(orderId -> new PaymentOrderCreationResult(getById(orderId), false))
					.orElseGet(() -> createNewOrder(command, normalizedKey));
		}
		return createNewOrder(command, null);
	}

	@Transactional(readOnly = true)
	public Page<PaymentOrder> findAll(PaymentOrderStatus status, UUID supplierId, Pageable pageable) {
		return paymentOrders.findAll(status, supplierId, pageable);
	}

	@Transactional(readOnly = true)
	public PaymentOrder getById(UUID id) {
		return paymentOrders.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Orden de pago no encontrada: " + id));
	}

	@Transactional
	public PaymentOrder transition(UUID id, TransitionPaymentOrderStatusCommand command) {
		if (command.status() == null) {
			throw new BusinessValidationException("El nuevo estado de la orden es obligatorio.");
		}
		PaymentOrder current = getById(id);
		return paymentOrders.save(current.transitionTo(command.status(), OffsetDateTime.now(clock)));
	}

	@Transactional(readOnly = true)
	public PaidTotalReport paidTotal(UUID supplierId, OffsetDateTime from, OffsetDateTime to) {
		requireExistingSupplier(supplierId);
		if (from == null || to == null) {
			throw new BusinessValidationException("El rango de fechas es obligatorio.");
		}
		if (from.isAfter(to)) {
			throw new BusinessValidationException("La fecha inicial no puede ser posterior a la fecha final.");
		}
		BigDecimal total = paymentOrders.sumPaidBySupplierBetween(supplierId, from, to);
		return new PaidTotalReport(supplierId, from, to, total == null ? BigDecimal.ZERO : total);
	}

	@Transactional(readOnly = true)
	public Page<PaymentOrder> findDueSoon(Integer days, Pageable pageable) {
		int windowDays = days == null ? DEFAULT_DUE_SOON_DAYS : days;
		if (windowDays < 1 || windowDays > MAX_DUE_SOON_DAYS) {
			throw new BusinessValidationException("El rango de vencimiento debe estar entre 1 y 90 dias.");
		}
		OffsetDateTime now = OffsetDateTime.now(clock);
		OffsetDateTime createdFrom = now.minusDays(PaymentOrder.DUE_DAYS_AFTER_CREATION);
		OffsetDateTime createdTo = now.plusDays(windowDays).minusDays(PaymentOrder.DUE_DAYS_AFTER_CREATION);
		return paymentOrders.findDueSoon(createdFrom, createdTo, pageable);
	}

	private PaymentOrderCreationResult createNewOrder(CreatePaymentOrderCommand command, String idempotencyKey) {
		Supplier supplier = requireActiveSupplier(command.supplierId());
		PaymentOrder paymentOrder = PaymentOrder.create(
				supplier.id(),
				command.amount(),
				command.concept(),
				OffsetDateTime.now(clock)
		);
		PaymentOrder saved = paymentOrders.save(paymentOrder);
		if (idempotencyKey != null) {
			idempotencyKeys.save(idempotencyKey, saved.id(), OffsetDateTime.now(clock));
		}
		return new PaymentOrderCreationResult(saved, true);
	}

	private Supplier requireActiveSupplier(UUID supplierId) {
		Supplier supplier = requireExistingSupplier(supplierId);
		if (supplier.status() != SupplierStatus.ACTIVO) {
			throw new BusinessValidationException("Solo se pueden crear ordenes para proveedores ACTIVO.");
		}
		return supplier;
	}

	private Supplier requireExistingSupplier(UUID supplierId) {
		if (supplierId == null) {
			throw new BusinessValidationException("El proveedor es obligatorio.");
		}
		return suppliers.findById(supplierId)
				.orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado: " + supplierId));
	}

	private static String normalizeOptionalText(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim();
	}
}
