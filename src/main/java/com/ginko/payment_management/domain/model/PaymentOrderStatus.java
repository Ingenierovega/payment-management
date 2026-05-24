package com.ginko.payment_management.domain.model;

public enum PaymentOrderStatus {
	BORRADOR,
	APROBADA,
	RECHAZADA,
	PAGADA;

	public boolean canTransitionTo(PaymentOrderStatus target) {
		return switch (this) {
			case BORRADOR -> target == APROBADA || target == RECHAZADA;
			case APROBADA -> target == PAGADA;
			case RECHAZADA, PAGADA -> false;
		};
	}

	public boolean isPendingPaymentLifecycle() {
		return this == BORRADOR || this == APROBADA;
	}
}
