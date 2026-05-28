package com.procurement.modules.workflow.dto;

import com.procurement.common.request.RequestUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record DecisionRequest(@Valid @NotNull RequestUser actor, String notes) {
  public String actorLabel() {
    return actor.label();
  }
}
