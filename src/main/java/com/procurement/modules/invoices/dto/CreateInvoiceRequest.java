package com.procurement.modules.invoices.dto;

import com.procurement.common.request.RequestUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CreateInvoiceRequest(
  @NotBlank String invoiceNumber,
  @NotNull @DecimalMin("0.01") BigDecimal amount,
  @Valid @NotNull RequestUser actor,
  String notes
) {
  public String actorLabel() {
    return actor.label();
  }
}
