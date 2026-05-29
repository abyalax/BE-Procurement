package com.procurement.modules.pr.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PurchaseRequisitionItemRequest(
  @NotBlank @Pattern(regexp = "GOODS|SERVICE") String itemType,
  @NotBlank @Size(max = 180) String itemName,
  String description,
  String specification,
  @NotNull @DecimalMin("0.01") BigDecimal quantity,
  @NotBlank @Size(max = 40) String unitOfMeasure,
  @NotNull @DecimalMin("0.00") BigDecimal estimatedUnitPrice,
  LocalDate requiredDate,
  @Size(max = 180) String deliveryLocation,
  @Size(max = 80) String budgetCode,
  String notes
) {}
