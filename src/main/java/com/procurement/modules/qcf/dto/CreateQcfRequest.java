package com.procurement.modules.qcf.dto;

import com.procurement.common.request.RequestUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public record CreateQcfRequest(
  @NotNull Long rfqId,
  @Valid @NotNull RequestUser actor,
  String notes
) {
  public String actorLabel() {
    return actor.label();
  }
}
