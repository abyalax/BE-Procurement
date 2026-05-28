package com.procurement.modules.pr.dto;

import com.procurement.common.request.RequestUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record UpdatePurchaseRequisitionRequest(
  @Size(max = 180) String title,
  String description,
  @Size(max = 120) String department,
  @Valid @NotNull RequestUser requestedBy,
  @DecimalMin("0.01") BigDecimal amount,
  @DecimalMin("0.01") BigDecimal quantity,
  Boolean emergency,
  String justification
) {
  public String requestedByLabel() {
    return requestedBy.label();
  }
}
