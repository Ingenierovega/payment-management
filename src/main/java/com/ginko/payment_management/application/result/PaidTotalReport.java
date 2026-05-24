package com.ginko.payment_management.application.result;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PaidTotalReport(UUID supplierId, OffsetDateTime from, OffsetDateTime to, BigDecimal totalPaid) {
}
