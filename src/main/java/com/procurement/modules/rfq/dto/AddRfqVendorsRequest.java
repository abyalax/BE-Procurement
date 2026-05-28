package com.procurement.modules.rfq.dto;

import com.procurement.common.request.RequestUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record AddRfqVendorsRequest(
  @NotEmpty List<Long> vendorIds,
  @Valid @NotNull RequestUser actor,
  String notes
) {
  public String actorLabel() {
    return actor.label();
  }
}
