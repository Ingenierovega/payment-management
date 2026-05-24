package com.ginko.payment_management.application.command;

import java.math.BigDecimal;
import java.util.UUID;

public record CreatePaymentOrderCommand(UUID supplierId, BigDecimal amount, String concept) {
}
