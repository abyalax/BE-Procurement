package com.procurement.modules.pr.dto;

import com.procurement.common.request.RequestUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

public record CreatePurchaseRequisitionRequest(
  @NotBlank @Size(max = 180) String title,
  String description,
  @NotBlank @Size(max = 120) String department,
  @Valid @NotNull RequestUser requestedBy,
  boolean emergency,
  LocalDate requiredDate,
  String justification,
  @Valid @NotEmpty List<PurchaseRequisitionItemRequest> items
) {
  public String requestedByLabel() {
    return requestedBy.label();
  }
}
