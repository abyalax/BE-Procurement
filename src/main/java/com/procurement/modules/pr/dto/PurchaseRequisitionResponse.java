package com.procurement.modules.pr.dto;

import com.procurement.modules.pr.entities.PurchaseRequisition;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PurchaseRequisitionResponse(
  Long id,
  String title,
  String description,
  String department,
  String requestedBy,
  BigDecimal amount,
  BigDecimal quantity,
  String status,
  String stage,
  boolean emergency,
  String justification,
  String cancellationReason,
  LocalDateTime createdAt,
  LocalDateTime updatedAt
) {
  public static PurchaseRequisitionResponse from(PurchaseRequisition pr) {
    return new PurchaseRequisitionResponse(
      pr.getId(),
      pr.getTitle(),
      pr.getDescription(),
      pr.getDepartment(),
      pr.getRequestedBy(),
      pr.getAmount(),
      pr.getQuantity(),
      pr.getStatus(),
      pr.getStage(),
      pr.isEmergency(),
      pr.getJustification(),
      pr.getCancellationReason(),
      pr.getCreatedAt(),
      pr.getUpdatedAt()
    );
  }
}
