package com.procurement.modules.vendors.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CreateVendorRequest(
  @NotBlank @Size(max = 160) String name,
  @NotBlank @Email @Size(max = 160) String email,
  @Pattern(regexp = "ACTIVE|INACTIVE", message = "Status must be ACTIVE or INACTIVE") String status,
  BigDecimal performanceScore
) {}
