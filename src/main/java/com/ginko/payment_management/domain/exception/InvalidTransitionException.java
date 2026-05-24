package com.ginko.payment_management.domain.exception;

public class InvalidTransitionException extends BusinessValidationException {

	public InvalidTransitionException(String message) {
		super(message);
	}
}
