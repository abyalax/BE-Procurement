package com.procurement.modules.qcf.dto;

import com.procurement.common.request.RequestUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record UpdateQcfRequest(
  String recommendationNotes,
  @Valid @NotNull RequestUser actor,
  String notes
) {
  public String actorLabel() {
    return actor.label();
  }
}
