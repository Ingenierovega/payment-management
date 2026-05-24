package com.ginko.payment_management.infrastructure.web.dto;

import com.ginko.payment_management.domain.model.PaymentOrderStatus;
import jakarta.validation.constraints.NotNull;

public record TransitionPaymentOrderStatusRequest(@NotNull PaymentOrderStatus status) {
}
