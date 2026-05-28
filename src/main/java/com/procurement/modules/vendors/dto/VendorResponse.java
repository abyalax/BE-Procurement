package com.procurement.modules.vendors.dto;

import com.procurement.modules.vendors.entities.Vendor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record VendorResponse(
  Long id,
  String name,
  String email,
  String status,
  BigDecimal performanceScore,
  LocalDateTime createdAt,
  LocalDateTime updatedAt
) {
  public static VendorResponse from(Vendor vendor) {
    return new VendorResponse(
      vendor.getId(),
      vendor.getName(),
      vendor.getEmail(),
      vendor.getStatus(),
      vendor.getPerformanceScore(),
      vendor.getCreatedAt(),
      vendor.getUpdatedAt()
    );
  }
}
