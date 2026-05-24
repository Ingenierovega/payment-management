package com.ginko.payment_management.infrastructure.web.dto;

import com.ginko.payment_management.application.result.PaidTotalReport;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PaidTotalResponse(UUID supplierId, OffsetDateTime from, OffsetDateTime to, BigDecimal totalPaid) {

	public static PaidTotalResponse from(PaidTotalReport report) {
		return new PaidTotalResponse(report.supplierId(), report.from(), report.to(), report.totalPaid());
	}
}
