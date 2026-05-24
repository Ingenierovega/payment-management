package com.ginko.payment_management.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ginko.payment_management.application.command.CreatePaymentOrderCommand;
import com.ginko.payment_management.application.command.TransitionPaymentOrderStatusCommand;
import com.ginko.payment_management.application.port.out.IdempotencyRepositoryPort;
import com.ginko.payment_management.application.port.out.PaymentOrderRepositoryPort;
import com.ginko.payment_management.application.port.out.SupplierRepositoryPort;
import com.ginko.payment_management.application.result.PaymentOrderCreationResult;
import com.ginko.payment_management.domain.exception.BusinessValidationException;
import com.ginko.payment_management.domain.exception.InvalidTransitionException;
import com.ginko.payment_management.domain.model.PaymentOrder;
import com.ginko.payment_management.domain.model.PaymentOrderStatus;
import com.ginko.payment_management.domain.model.Supplier;
import com.ginko.payment_management.domain.model.SupplierStatus;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentOrderServiceTest {

	private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-05-24T10:00:00Z"), ZoneOffset.UTC);

	@Mock
	private PaymentOrderRepositoryPort paymentOrders;

	@Mock
	private SupplierRepositoryPort suppliers;

	@Mock
	private IdempotencyRepositoryPort idempotencyKeys;

	private PaymentOrderService service;

	@BeforeEach
	void setUp() {
		service = new PaymentOrderService(paymentOrders, suppliers, idempotencyKeys, FIXED_CLOCK);
	}

	@Test
	void createRejectsInactiveSupplier() {
		UUID supplierId = UUID.randomUUID();
		when(suppliers.findById(supplierId)).thenReturn(Optional.of(new Supplier(
				supplierId,
				"Proveedor Inactivo",
				"900111222",
				"inactivo@example.com",
				SupplierStatus.INACTIVO
		)));

		assertThatThrownBy(() -> service.create(new CreatePaymentOrderCommand(
				supplierId,
				BigDecimal.valueOf(50000),
				"Servicios"
		), null))
				.isInstanceOf(BusinessValidationException.class)
				.hasMessageContaining("ACTIVO");

		verify(paymentOrders, never()).save(any());
	}

	@Test
	void transitionAllowsDraftToApproved() {
		UUID orderId = UUID.randomUUID();
		UUID supplierId = UUID.randomUUID();
		PaymentOrder draft = paymentOrder(orderId, supplierId, PaymentOrderStatus.BORRADOR);
		when(paymentOrders.findById(orderId)).thenReturn(Optional.of(draft));
		when(paymentOrders.save(any(PaymentOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

		PaymentOrder result = service.transition(
				orderId,
				new TransitionPaymentOrderStatusCommand(PaymentOrderStatus.APROBADA)
		);

		assertThat(result.status()).isEqualTo(PaymentOrderStatus.APROBADA);
	}

	@Test
	void transitionRejectsInvalidStateChange() {
		UUID orderId = UUID.randomUUID();
		UUID supplierId = UUID.randomUUID();
		when(paymentOrders.findById(orderId)).thenReturn(Optional.of(paymentOrder(
				orderId,
				supplierId,
				PaymentOrderStatus.PAGADA
		)));

		assertThatThrownBy(() -> service.transition(
				orderId,
				new TransitionPaymentOrderStatusCommand(PaymentOrderStatus.APROBADA)
		))
				.isInstanceOf(InvalidTransitionException.class)
				.hasMessageContaining("no esta permitida");

		verify(paymentOrders, never()).save(any());
	}

	@Test
	void transitionToPaidRegistersPaymentDate() {
		UUID orderId = UUID.randomUUID();
		UUID supplierId = UUID.randomUUID();
		PaymentOrder approved = paymentOrder(orderId, supplierId, PaymentOrderStatus.APROBADA);
		when(paymentOrders.findById(orderId)).thenReturn(Optional.of(approved));
		when(paymentOrders.save(any(PaymentOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

		PaymentOrder result = service.transition(
				orderId,
				new TransitionPaymentOrderStatusCommand(PaymentOrderStatus.PAGADA)
		);

		assertThat(result.status()).isEqualTo(PaymentOrderStatus.PAGADA);
		assertThat(result.paidAt()).isEqualTo(OffsetDateTime.now(FIXED_CLOCK));
	}

	@Test
	void createWithRepeatedIdempotencyKeyReturnsExistingOrder() {
		UUID orderId = UUID.randomUUID();
		UUID supplierId = UUID.randomUUID();
		PaymentOrder existing = paymentOrder(orderId, supplierId, PaymentOrderStatus.BORRADOR);
		when(idempotencyKeys.findOrderIdByKey("order-key-1")).thenReturn(Optional.of(orderId));
		when(paymentOrders.findById(orderId)).thenReturn(Optional.of(existing));

		PaymentOrderCreationResult result = service.create(new CreatePaymentOrderCommand(
				supplierId,
				BigDecimal.valueOf(100000),
				"Honorarios"
		), " order-key-1 ");

		assertThat(result.created()).isFalse();
		assertThat(result.paymentOrder()).isEqualTo(existing);
		verify(suppliers, never()).findById(any());
		verify(paymentOrders, never()).save(any());
	}

	private static PaymentOrder paymentOrder(UUID orderId, UUID supplierId, PaymentOrderStatus status) {
		return new PaymentOrder(
				orderId,
				supplierId,
				BigDecimal.valueOf(150000),
				"Servicios profesionales",
				OffsetDateTime.now(FIXED_CLOCK),
				status,
				null,
				0L
		);
	}
}
