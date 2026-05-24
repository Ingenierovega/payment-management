package com.ginko.payment_management.domain.model;

import com.ginko.payment_management.domain.exception.BusinessValidationException;
import com.ginko.payment_management.domain.exception.InvalidTransitionException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

public record PaymentOrder(
		UUID id,
		UUID supplierId,
		BigDecimal amount,
		String concept,
		OffsetDateTime createdAt,
		PaymentOrderStatus status,
		OffsetDateTime paidAt,
		Long version
) {

	public static final int MAX_CONCEPT_LENGTH = 250;
	public static final int DUE_DAYS_AFTER_CREATION = 30;

	public PaymentOrder {
		Objects.requireNonNull(id, "id is required");
		Objects.requireNonNull(supplierId, "supplierId is required");
		Objects.requireNonNull(amount, "amount is required");
		Objects.requireNonNull(concept, "concept is required");
		Objects.requireNonNull(createdAt, "createdAt is required");
		Objects.requireNonNull(status, "status is required");
		if (amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new BusinessValidationException("El monto debe ser mayor que cero.");
		}
		if (concept.isBlank()) {
			throw new BusinessValidationException("El concepto es obligatorio.");
		}
		if (concept.length() > MAX_CONCEPT_LENGTH) {
			throw new BusinessValidationException("El concepto no puede superar 250 caracteres.");
		}
	}

	public static PaymentOrder create(UUID supplierId, BigDecimal amount, String concept, OffsetDateTime createdAt) {
		return new PaymentOrder(
				UUID.randomUUID(),
				supplierId,
				amount,
				concept.trim(),
				createdAt,
				PaymentOrderStatus.BORRADOR,
				null,
				null
		);
	}

	public PaymentOrder transitionTo(PaymentOrderStatus targetStatus, OffsetDateTime transitionedAt) {
		if (!status.canTransitionTo(targetStatus)) {
			throw new InvalidTransitionException(
					"La transicion de " + status + " a " + targetStatus + " no esta permitida."
			);
		}
		OffsetDateTime newPaidAt = targetStatus == PaymentOrderStatus.PAGADA ? transitionedAt : paidAt;
		return new PaymentOrder(id, supplierId, amount, concept, createdAt, targetStatus, newPaidAt, version);
	}

	public OffsetDateTime dueAt() {
		return createdAt.plusDays(DUE_DAYS_AFTER_CREATION);
	}
}
