package com.procurement.modules.rfq.dto;

import com.procurement.common.request.RequestUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

public record CreateRfqRequest(
  @NotNull Long prId,
  @NotBlank @Size(max = 180) String title,
  String description,
  @NotNull LocalDateTime deadline,
  @NotEmpty List<Long> vendorIds,
  @Valid @NotNull RequestUser actor
) {
  public String actorLabel() {
    return actor.label();
  }
}
