package com.procurement.modules.qcf.dto;

import com.procurement.modules.qcf.entities.QcfLine;
import java.math.BigDecimal;

public record QcfLineResponse(
  Long id,
  Long qcfId,
  Long quotationId,
  Long vendorId,
  String vendorName,
  BigDecimal amount,
  int leadTimeDays,
  BigDecimal technicalScore,
  BigDecimal commercialScore,
  BigDecimal totalScore,
  int rankNo,
  boolean recommendation,
  String notes
) {
  public static QcfLineResponse from(QcfLine line) {
    return new QcfLineResponse(
      line.getId(),
      line.getQcf().getId(),
      line.getQuotation().getId(),
      line.getVendor().getId(),
      line.getVendor().getName(),
      line.getAmount(),
      line.getLeadTimeDays(),
      line.getTechnicalScore(),
      line.getCommercialScore(),
      line.getTotalScore(),
      line.getRankNo(),
      line.isRecommendation(),
      line.getNotes()
    );
  }
}
