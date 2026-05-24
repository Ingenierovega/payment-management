package com.ginko.payment_management.application.command;

import com.ginko.payment_management.domain.model.SupplierStatus;

public record ChangeSupplierStatusCommand(SupplierStatus status) {
}
