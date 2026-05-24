package com.ginko.payment_management.application.command;

public record UpdateSupplierCommand(String businessName, String taxIdentificationNumber, String email) {
}
