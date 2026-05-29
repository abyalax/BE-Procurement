package com.procurement.modules.pr.dto;

import com.procurement.common.request.RequestUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public record UpdatePurchaseRequisitionRequest(
  @Size(max = 180) String title,
  String description,
  @Size(max = 120) String department,
  @Valid @NotNull RequestUser requestedBy,
  Boolean emergency,
  LocalDate requiredDate,
  String justification,
  @Valid List<PurchaseRequisitionItemRequest> items
) {
  public String requestedByLabel() {
    return requestedBy.label();
  }
}
