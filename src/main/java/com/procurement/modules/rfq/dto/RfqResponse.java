package com.procurement.modules.rfq.dto;

import com.procurement.modules.rfq.entities.Rfq;
import java.time.LocalDateTime;
import java.util.List;

public record RfqResponse(
  Long id,
  Long prId,
  String title,
  String description,
  LocalDateTime deadline,
  String status,
  String stage,
  String failureReason,
  List<RfqItemResponse> items,
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
      rfq.getItems().stream().map(RfqItemResponse::from).toList(),
      rfq.getCreatedAt(),
      rfq.getUpdatedAt()
    );
  }
}
