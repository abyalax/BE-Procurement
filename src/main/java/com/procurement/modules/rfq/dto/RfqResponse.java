package com.procurement.modules.rfq.dto;

import com.procurement.modules.rfq.entities.Rfq;
import java.time.LocalDateTime;

public record RfqResponse(
  Long id,
  Long prId,
  String title,
  String description,
  LocalDateTime deadline,
  String status,
  String stage,
  String failureReason,
  LocalDateTime createdAt,
  LocalDateTime updatedAt
) {
  public static RfqResponse from(Rfq rfq) {
    return new RfqResponse(
      rfq.getId(),
      rfq.getPurchaseRequisition().getId(),
      rfq.getTitle(),
      rfq.getDescription(),
      rfq.getDeadline(),
      rfq.getStatus(),
      rfq.getStage(),
      rfq.getFailureReason(),
      rfq.getCreatedAt(),
      rfq.getUpdatedAt()
    );
  }
}
