package com.procurement.modules.purchase_orders.dto;

import com.procurement.common.request.RequestUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record UpdatePurchaseOrderRequest(
  @Size(max = 80) String poNumber,
  @DecimalMin("0.01") BigDecimal amount,
  @DecimalMin("0.01") BigDecimal quantity,
  @Valid @NotNull RequestUser actor,
  String notes
) {
  public String actorLabel() {
    return actor.label();
  }
}
