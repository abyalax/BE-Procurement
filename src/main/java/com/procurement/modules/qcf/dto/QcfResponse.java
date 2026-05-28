package com.procurement.modules.qcf.dto;

import com.procurement.modules.qcf.entities.QcfDocument;
import java.time.LocalDateTime;

public record QcfResponse(
  Long id,
  Long rfqId,
  Long recommendedVendorId,
  String recommendedVendorName,
  String status,
  String stage,
  String recommendationNotes,
  String createdBy,
  String approvedBy,
  LocalDateTime approvedAt,
  LocalDateTime createdAt,
  LocalDateTime updatedAt
) {
  public static QcfResponse from(QcfDocument qcf) {
    return new QcfResponse(
      qcf.getId(),
      qcf.getRfq().getId(),
      qcf.getRecommendedVendor() == null ? null : qcf.getRecommendedVendor().getId(),
      qcf.getRecommendedVendor() == null ? null : qcf.getRecommendedVendor().getName(),
      qcf.getStatus(),
      qcf.getStage(),
      qcf.getRecommendationNotes(),
      qcf.getCreatedBy(),
      qcf.getApprovedBy(),
      qcf.getApprovedAt(),
      qcf.getCreatedAt(),
      qcf.getUpdatedAt()
    );
  }
}
