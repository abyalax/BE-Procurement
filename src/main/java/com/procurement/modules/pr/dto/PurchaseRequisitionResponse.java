package com.procurement.modules.pr.dto;

import com.procurement.modules.pr.entities.PurchaseRequisition;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record PurchaseRequisitionResponse(
  Long id,
  String prNumber,
  String title,
  String description,
  String department,
  String requestedBy,
  BigDecimal totalEstimatedAmount,
  String status,
  String stage,
  boolean emergency,
  LocalDate requiredDate,
  String justification,
  String procurementReviewNotes,
  String cancellationReason,
  List<PurchaseRequisitionItemResponse> items,
  LocalDateTime createdAt,
  LocalDateTime updatedAt
) {
  public static PurchaseRequisitionResponse from(PurchaseRequisition pr) {
    return new PurchaseRequisitionResponse(
      pr.getId(),
      pr.getPrNumber(),
      pr.getTitle(),
      pr.getDescription(),
      pr.getDepartment(),
      pr.getRequestedBy(),
      pr.getTotalEstimatedAmount(),
      pr.getStatus(),
      pr.getStage(),
      pr.isEmergency(),
      pr.getRequiredDate(),
      pr.getJustification(),
      pr.getProcurementReviewNotes(),
      pr.getCancellationReason(),
      pr.getItems().stream().map(PurchaseRequisitionItemResponse::from).toList(),
      pr.getCreatedAt(),
      pr.getUpdatedAt()
    );
  }
}
