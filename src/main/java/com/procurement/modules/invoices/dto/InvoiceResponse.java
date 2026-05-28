package com.procurement.modules.invoices.dto;

import com.procurement.modules.invoices.entities.Invoice;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InvoiceResponse(
  Long id,
  Long poId,
  String invoiceNumber,
  BigDecimal amount,
  String status,
  String matchStatus,
  String submittedBy,
  String verifiedBy,
  String paidBy,
  String notes,
  LocalDateTime verifiedAt,
  LocalDateTime paidAt
) {
  public static InvoiceResponse from(Invoice invoice) {
    return new InvoiceResponse(
      invoice.getId(),
      invoice.getPurchaseOrder().getId(),
      invoice.getInvoiceNumber(),
      invoice.getAmount(),
      invoice.getStatus(),
      invoice.getMatchStatus(),
      invoice.getSubmittedBy(),
      invoice.getVerifiedBy(),
      invoice.getPaidBy(),
      invoice.getNotes(),
      invoice.getVerifiedAt(),
      invoice.getPaidAt()
    );
  }
}
