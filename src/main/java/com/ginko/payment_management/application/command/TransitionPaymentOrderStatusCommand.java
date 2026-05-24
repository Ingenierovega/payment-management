package com.ginko.payment_management.application.command;

import com.ginko.payment_management.domain.model.PaymentOrderStatus;

public record TransitionPaymentOrderStatusCommand(PaymentOrderStatus status) {
}
