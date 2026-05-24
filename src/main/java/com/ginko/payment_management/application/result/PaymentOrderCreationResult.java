package com.ginko.payment_management.application.result;

import com.ginko.payment_management.domain.model.PaymentOrder;

public record PaymentOrderCreationResult(PaymentOrder paymentOrder, boolean created) {
}
