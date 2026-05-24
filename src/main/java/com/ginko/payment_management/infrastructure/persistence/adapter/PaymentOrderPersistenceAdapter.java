package com.ginko.payment_management.infrastructure.persistence.adapter;

import com.ginko.payment_management.application.port.out.PaymentOrderRepositoryPort;
import com.ginko.payment_management.domain.model.PaymentOrder;
import com.ginko.payment_management.domain.model.PaymentOrderStatus;
import com.ginko.payment_management.infrastructure.persistence.entity.PaymentOrderJpaEntity;
import com.ginko.payment_management.infrastructure.persistence.entity.SupplierJpaEntity;
import com.ginko.payment_management.infrastructure.persistence.repository.PaymentOrderJpaRepository;
import com.ginko.payment_management.infrastructure.persistence.repository.SupplierJpaRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class PaymentOrderPersistenceAdapter implements PaymentOrderRepositoryPort {

	private final PaymentOrderJpaRepository paymentOrderRepository;
	private final SupplierJpaRepository supplierRepository;

	public PaymentOrderPersistenceAdapter(
			PaymentOrderJpaRepository paymentOrderRepository,
			SupplierJpaRepository supplierRepository
	) {
		this.paymentOrderRepository = paymentOrderRepository;
		this.supplierRepository = supplierRepository;
	}

	@Override
	public PaymentOrder save(PaymentOrder paymentOrder) {
		return toDomain(paymentOrderRepository.save(toEntity(paymentOrder)));
	}

	@Override
	public Optional<PaymentOrder> findById(UUID id) {
		return paymentOrderRepository.findById(id).map(this::toDomain);
	}

	@Override
	public Page<PaymentOrder> findAll(PaymentOrderStatus status, UUID supplierId, Pageable pageable) {
		if (status != null && supplierId != null) {
			return paymentOrderRepository.findByStatusAndSupplier_Id(status, supplierId, pageable).map(this::toDomain);
		}
		if (status != null) {
			return paymentOrderRepository.findByStatus(status, pageable).map(this::toDomain);
		}
		if (supplierId != null) {
			return paymentOrderRepository.findBySupplier_Id(supplierId, pageable).map(this::toDomain);
		}
		return paymentOrderRepository.findAll(pageable).map(this::toDomain);
	}

	@Override
	public BigDecimal sumPaidBySupplierBetween(UUID supplierId, OffsetDateTime from, OffsetDateTime to) {
		return paymentOrderRepository.sumPaidBySupplierBetween(supplierId, from, to);
	}

	@Override
	public Page<PaymentOrder> findDueSoon(OffsetDateTime createdFrom, OffsetDateTime createdTo, Pageable pageable) {
		return paymentOrderRepository.findByStatusInAndCreatedAtBetween(
				List.of(PaymentOrderStatus.BORRADOR, PaymentOrderStatus.APROBADA),
				createdFrom,
				createdTo,
				pageable
		).map(this::toDomain);
	}

	private PaymentOrderJpaEntity toEntity(PaymentOrder paymentOrder) {
		SupplierJpaEntity supplier = supplierRepository.getReferenceById(paymentOrder.supplierId());
		PaymentOrderJpaEntity entity = new PaymentOrderJpaEntity();
		entity.setId(paymentOrder.id());
		entity.setSupplier(supplier);
		entity.setAmount(paymentOrder.amount());
		entity.setConcept(paymentOrder.concept());
		entity.setCreatedAt(paymentOrder.createdAt());
		entity.setStatus(paymentOrder.status());
		entity.setPaidAt(paymentOrder.paidAt());
		entity.setVersion(paymentOrder.version());
		return entity;
	}

	private PaymentOrder toDomain(PaymentOrderJpaEntity entity) {
		return new PaymentOrder(
				entity.getId(),
				entity.getSupplier().getId(),
				entity.getAmount(),
				entity.getConcept(),
				entity.getCreatedAt(),
				entity.getStatus(),
				entity.getPaidAt(),
				entity.getVersion()
		);
	}
}
