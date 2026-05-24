package com.ginko.payment_management.infrastructure.web.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record ErrorResponse(
		OffsetDateTime timestamp,
		int status,
		String error,
		String message,
		String path,
		List<FieldErrorResponse> fieldErrors
) {
	public static ErrorResponse of(int status, String error, String message, String path) {
		return new ErrorResponse(OffsetDateTime.now(), status, error, message, path, List.of());
	}
}
