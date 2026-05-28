package com.procurement.modules.rfq.dto;

import com.procurement.common.request.RequestUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record UpdateRfqRequest(
  @Size(max = 180) String title,
  String description,
  LocalDateTime deadline,
  @Valid @NotNull RequestUser actor,
  String notes
) {
  public String actorLabel() {
    return actor.label();
  }
}
