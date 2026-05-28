package com.procurement.modules.warehouse.dto;

import com.procurement.common.request.RequestUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ReceiveGoodsRequest(
  @Valid @NotNull RequestUser actor,
  @NotNull @DecimalMin("0.01") BigDecimal receivedQuantity,
  String notes
) {
  public String actorLabel() {
    return actor.label();
  }
}
