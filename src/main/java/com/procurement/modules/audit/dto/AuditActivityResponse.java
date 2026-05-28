package com.procurement.modules.audit.dto;

import com.procurement.modules.audit.entities.AuditActivity;
import java.time.LocalDateTime;

public record AuditActivityResponse(
  Long id,
  String documentType,
  Long documentId,
  String activityType,
  String actor,
  String notes,
  String metadata,
  LocalDateTime createdAt
) {
  public static AuditActivityResponse from(AuditActivity activity) {
    return new AuditActivityResponse(
      activity.getId(),
      activity.getDocumentType(),
      activity.getDocumentId(),
      activity.getActivityType(),
      activity.getActor(),
      activity.getNotes(),
      activity.getMetadata(),
      activity.getCreatedAt()
    );
  }
}
