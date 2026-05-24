package com.ginko.payment_management.application.command;

public record CreateSupplierCommand(String businessName, String taxIdentificationNumber, String email) {
}
