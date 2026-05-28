package com.procurement.modules.rfq.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record SubmitQuotationRequest(
  @NotNull Long vendorId,
  @NotNull @DecimalMin("0.01") BigDecimal amount,
  @Min(1) int leadTimeDays,
  BigDecimal technicalScore,
  BigDecimal commercialScore,
  String notes
) {}
