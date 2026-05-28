package com.procurement.modules.warehouse.dto;

import com.procurement.modules.warehouse.entities.GoodsReceipt;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record GoodsReceiptResponse(
  Long id,
  Long poId,
  BigDecimal receivedQuantity,
  String receivedBy,
  String notes,
  LocalDateTime receivedAt
) {
  public static GoodsReceiptResponse from(GoodsReceipt receipt) {
    return new GoodsReceiptResponse(
      receipt.getId(),
      receipt.getPurchaseOrder().getId(),
      receipt.getReceivedQuantity(),
      receipt.getReceivedBy(),
      receipt.getNotes(),
      receipt.getReceivedAt()
    );
  }
}
