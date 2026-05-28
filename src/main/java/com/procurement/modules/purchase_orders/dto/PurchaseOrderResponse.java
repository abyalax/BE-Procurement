package com.procurement.modules.purchase_orders.dto;

import com.procurement.modules.purchase_orders.entities.PurchaseOrder;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PurchaseOrderResponse(
  Long id,
  Long qcfId,
  Long rfqId,
  Long vendorId,
  String vendorName,
  String poNumber,
  BigDecimal amount,
  BigDecimal quantity,
  BigDecimal remainingQuantity,
  String status,
  String stage,
  LocalDateTime createdAt,
  LocalDateTime updatedAt
) {
  public static PurchaseOrderResponse from(PurchaseOrder po) {
    return new PurchaseOrderResponse(
      po.getId(),
      po.getQcf().getId(),
      po.getRfq().getId(),
      po.getVendor().getId(),
      po.getVendor().getName(),
      po.getPoNumber(),
      po.getAmount(),
      po.getQuantity(),
      po.getRemainingQuantity(),
      po.getStatus(),
      po.getStage(),
      po.getCreatedAt(),
      po.getUpdatedAt()
    );
  }
}
