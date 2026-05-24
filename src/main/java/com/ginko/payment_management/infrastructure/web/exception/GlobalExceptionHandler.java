package com.ginko.payment_management.infrastructure.web.exception;

import com.ginko.payment_management.domain.exception.BusinessValidationException;
import com.ginko.payment_management.domain.exception.ConflictException;
import com.ginko.payment_management.domain.exception.ResourceNotFoundException;
import com.ginko.payment_management.infrastructure.web.dto.ErrorResponse;
import com.ginko.payment_management.infrastructure.web.dto.FieldErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(BusinessValidationException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleBusinessValidation(BusinessValidationException exception, HttpServletRequest request) {
		return ErrorResponse.of(
				HttpStatus.BAD_REQUEST.value(),
				HttpStatus.BAD_REQUEST.getReasonPhrase(),
				exception.getMessage(),
				request.getRequestURI()
		);
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ErrorResponse handleNotFound(ResourceNotFoundException exception, HttpServletRequest request) {
		return ErrorResponse.of(
				HttpStatus.NOT_FOUND.value(),
				HttpStatus.NOT_FOUND.getReasonPhrase(),
				exception.getMessage(),
				request.getRequestURI()
		);
	}

	@ExceptionHandler({ConflictException.class, DataIntegrityViolationException.class, OptimisticLockingFailureException.class})
	@ResponseStatus(HttpStatus.CONFLICT)
	public ErrorResponse handleConflict(Exception exception, HttpServletRequest request) {
		String message = exception instanceof ConflictException
				? exception.getMessage()
				: "La operacion no pudo completarse por un conflicto de datos.";
		return ErrorResponse.of(
				HttpStatus.CONFLICT.value(),
				HttpStatus.CONFLICT.getReasonPhrase(),
				message,
				request.getRequestURI()
		);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
		List<FieldErrorResponse> fieldErrors = exception.getBindingResult().getFieldErrors().stream()
				.map(error -> new FieldErrorResponse(error.getField(), error.getDefaultMessage()))
				.toList();
		return new ErrorResponse(
				OffsetDateTime.now(),
				HttpStatus.BAD_REQUEST.value(),
				HttpStatus.BAD_REQUEST.getReasonPhrase(),
				"La solicitud contiene campos invalidos.",
				request.getRequestURI(),
				fieldErrors
		);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleConstraintViolation(ConstraintViolationException exception, HttpServletRequest request) {
		List<FieldErrorResponse> fieldErrors = exception.getConstraintViolations().stream()
				.map(violation -> new FieldErrorResponse(
						violation.getPropertyPath().toString(),
						violation.getMessage()
				))
				.toList();
		return new ErrorResponse(
				OffsetDateTime.now(),
				HttpStatus.BAD_REQUEST.value(),
				HttpStatus.BAD_REQUEST.getReasonPhrase(),
				"La solicitud contiene parametros invalidos.",
				request.getRequestURI(),
				fieldErrors
		);
	}

	@ExceptionHandler({
			HttpMessageNotReadableException.class,
			MethodArgumentTypeMismatchException.class,
			IllegalArgumentException.class
	})
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleBadRequest(Exception exception, HttpServletRequest request) {
		return ErrorResponse.of(
				HttpStatus.BAD_REQUEST.value(),
				HttpStatus.BAD_REQUEST.getReasonPhrase(),
				"Formato de solicitud invalido.",
				request.getRequestURI()
		);
	}
}
