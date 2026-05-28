package com.procurement.modules.rfq.dto;

import com.procurement.modules.rfq.entities.VendorQuotation;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record QuotationResponse(
  Long id,
  Long rfqId,
  Long vendorId,
  String vendorName,
  int version,
  BigDecimal amount,
  int leadTimeDays,
  BigDecimal technicalScore,
  BigDecimal commercialScore,
  String status,
  String notes,
  LocalDateTime submittedAt
) {
  public static QuotationResponse from(VendorQuotation quotation) {
    return new QuotationResponse(
      quotation.getId(),
      quotation.getRfq().getId(),
      quotation.getVendor().getId(),
      quotation.getVendor().getName(),
      quotation.getVersion(),
      quotation.getAmount(),
      quotation.getLeadTimeDays(),
      quotation.getTechnicalScore(),
      quotation.getCommercialScore(),
      quotation.getStatus(),
      quotation.getNotes(),
      quotation.getSubmittedAt()
    );
  }
}
