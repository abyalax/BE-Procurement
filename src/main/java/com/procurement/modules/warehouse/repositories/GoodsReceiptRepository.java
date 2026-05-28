package com.procurement.modules.warehouse.repositories;

import com.procurement.modules.warehouse.entities.GoodsReceipt;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoodsReceiptRepository extends JpaRepository<GoodsReceipt, Long> {
  List<GoodsReceipt> findByPurchaseOrderId(Long purchaseOrderId);
}
