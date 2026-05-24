package com.ginko.payment_management.infrastructure.web.dto;

import com.ginko.payment_management.domain.model.PaymentOrder;
import com.ginko.payment_management.domain.model.PaymentOrderStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PaymentOrderResponse(
		UUID id,
		UUID supplierId,
		BigDecimal amount,
		String concept,
		OffsetDateTime createdAt,
		PaymentOrderStatus status,
		OffsetDateTime paidAt,
		Long version,
		OffsetDateTime dueAt
) {

	public static PaymentOrderResponse from(PaymentOrder paymentOrder) {
		return new PaymentOrderResponse(
				paymentOrder.id(),
				paymentOrder.supplierId(),
				paymentOrder.amount(),
				paymentOrder.concept(),
				paymentOrder.createdAt(),
				paymentOrder.status(),
				paymentOrder.paidAt(),
				paymentOrder.version(),
				paymentOrder.dueAt()
		);
	}
}
