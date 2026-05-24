package com.ginko.payment_management.infrastructure.web.dto;

import com.ginko.payment_management.domain.model.SupplierStatus;
import jakarta.validation.constraints.NotNull;

public record ChangeSupplierStatusRequest(@NotNull SupplierStatus status) {
}
