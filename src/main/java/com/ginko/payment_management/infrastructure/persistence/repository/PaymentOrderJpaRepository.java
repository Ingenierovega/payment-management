package com.ginko.payment_management.infrastructure.persistence.repository;

import com.ginko.payment_management.domain.model.PaymentOrderStatus;
import com.ginko.payment_management.infrastructure.persistence.entity.PaymentOrderJpaEntity;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentOrderJpaRepository extends JpaRepository<PaymentOrderJpaEntity, UUID> {

	Page<PaymentOrderJpaEntity> findByStatus(PaymentOrderStatus status, Pageable pageable);

	Page<PaymentOrderJpaEntity> findBySupplier_Id(UUID supplierId, Pageable pageable);

	Page<PaymentOrderJpaEntity> findByStatusAndSupplier_Id(PaymentOrderStatus status, UUID supplierId, Pageable pageable);

	Page<PaymentOrderJpaEntity> findByStatusInAndCreatedAtBetween(
			Collection<PaymentOrderStatus> statuses,
			OffsetDateTime createdFrom,
			OffsetDateTime createdTo,
			Pageable pageable
	);

	@Query("""
			select coalesce(sum(po.amount), 0)
			from PaymentOrderJpaEntity po
			where po.supplier.id = :supplierId
				and po.status = com.ginko.payment_management.domain.model.PaymentOrderStatus.PAGADA
				and po.paidAt between :from and :to
			""")
	BigDecimal sumPaidBySupplierBetween(
			@Param("supplierId") UUID supplierId,
			@Param("from") OffsetDateTime from,
			@Param("to") OffsetDateTime to
	);
}
