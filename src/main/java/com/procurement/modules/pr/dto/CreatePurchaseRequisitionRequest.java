package com.procurement.modules.pr.dto;

import com.procurement.common.request.RequestUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CreatePurchaseRequisitionRequest(
  @NotBlank @Size(max = 180) String title,
  String description,
  @NotBlank @Size(max = 120) String department,
  @Valid @NotNull RequestUser requestedBy,
  @NotNull @DecimalMin("0.01") BigDecimal amount,
  @NotNull @DecimalMin("0.01") BigDecimal quantity,
  boolean emergency,
  String justification
) {
  public String requestedByLabel() {
    return requestedBy.label();
  }
}
