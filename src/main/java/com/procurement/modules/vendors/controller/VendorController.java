package com.procurement.modules.vendors.controller;

import com.procurement.common.response.*;
import com.procurement.common.security.Guard;
import com.procurement.modules.vendors.dto.*;
import com.procurement.modules.vendors.services.VendorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/vendors")
@RequiredArgsConstructor
public class VendorController {

  private final VendorService service;

  @GetMapping
  @Guard({ "vendors:read" })
  public ApiResponse<PageResponse<VendorResponse>> getVendors(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int limit,
    @RequestParam(required = false) String search,
    @RequestParam(required = false) String status,
    @RequestParam(name = "sort_by", required = false) String sortBy,
    @RequestParam(name = "sort_order", required = false) String sortOrder
  ) {
    return ApiResponse.success(
      "Vendors fetched successfully",
      service.getVendors(page, limit, sortBy, sortOrder, search, status)
    );
  }

  @GetMapping("/{id}")
  @Guard({ "vendors:read" })
  public ApiResponse<VendorResponse> getVendorById(@PathVariable Long id) {
    return ApiResponse.success("Vendor fetched successfully", service.getVendorById(id));
  }

  @PostMapping
  @Guard({ "vendors:write" })
  public ApiResponse<VendorResponse> createVendor(@Valid @RequestBody CreateVendorRequest request) {
    return ApiResponse.success("Vendor created successfully", service.createVendor(request));
  }

  @PatchMapping("/{id}")
  @Guard({ "vendors:write" })
  public ApiResponse<VendorResponse> updateVendor(
    @PathVariable Long id,
    @Valid @RequestBody UpdateVendorRequest request
  ) {
    return ApiResponse.success("Vendor updated successfully", service.updateVendor(id, request));
  }

  @DeleteMapping("/{id}")
  @Guard({ "vendors:write" })
  public ApiResponse<Boolean> deactivateVendor(@PathVariable Long id) {
    service.deactivateVendor(id);
    return ApiResponse.success("Vendor deactivated successfully", true);
  }
}
