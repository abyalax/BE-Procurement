package com.procurement.modules.purchase_orders.repositories;

import com.procurement.modules.purchase_orders.entities.PurchaseOrder;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
  boolean existsByQcfId(Long qcfId);
  Optional<PurchaseOrder> findByQcfId(Long qcfId);
  boolean existsByPoNumberAndIdNot(String poNumber, Long id);
  long countByStatus(String status);
}
