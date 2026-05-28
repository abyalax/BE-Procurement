package com.procurement.modules.pr.controller;

import com.procurement.common.response.*;
import com.procurement.common.security.Guard;
import com.procurement.modules.pr.dto.*;
import com.procurement.modules.pr.services.PurchaseRequisitionService;
import com.procurement.modules.workflow.dto.DecisionRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pr")
@RequiredArgsConstructor
public class PurchaseRequisitionController {

  private final PurchaseRequisitionService service;

  @GetMapping
  @Guard({ "pr:read" })
  public ApiResponse<PageResponse<PurchaseRequisitionResponse>> getAll(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int limit,
    @RequestParam(name = "sort_by", required = false) String sortBy,
    @RequestParam(name = "sort_order", required = false) String sortOrder,
    @RequestParam(required = false) String search,
    @RequestParam(required = false) String status
  ) {
    return ApiResponse.success(
      "Purchase requisitions fetched successfully",
      service.getPurchaseRequisitions(page, limit, sortBy, sortOrder, search, status)
    );
  }

  @GetMapping("/{id}")
  @Guard({ "pr:read" })
  public ApiResponse<PurchaseRequisitionResponse> getById(@PathVariable Long id) {
    return ApiResponse.success("Purchase requisition fetched successfully", service.getById(id));
  }

  @PostMapping
  @Guard({ "pr:write" })
  public ApiResponse<PurchaseRequisitionResponse> create(
    @Valid @RequestBody CreatePurchaseRequisitionRequest request
  ) {
    return ApiResponse.success(
      "Purchase requisition created successfully",
      service.create(request)
    );
  }

  @PatchMapping("/{id}")
  @Guard({ "pr:write" })
  public ApiResponse<PurchaseRequisitionResponse> update(
    @PathVariable Long id,
    @Valid @RequestBody UpdatePurchaseRequisitionRequest request
  ) {
    return ApiResponse.success(
      "Purchase requisition updated successfully",
      service.update(id, request)
    );
  }

  @DeleteMapping("/{id}")
  @Guard({ "pr:write" })
  public ApiResponse<Boolean> delete(
    @PathVariable Long id,
    @Valid @RequestBody DecisionRequest request
  ) {
    service.delete(id, request);
    return ApiResponse.success("Purchase requisition deleted successfully", true);
  }

  @PostMapping("/{id}/submit")
  @Guard({ "pr:write" })
  public ApiResponse<PurchaseRequisitionResponse> submit(
    @PathVariable Long id,
    @Valid @RequestBody DecisionRequest request
  ) {
    return ApiResponse.success(
      "Purchase requisition submitted successfully",
      service.submit(id, request)
    );
  }

  @PostMapping("/{id}/approve")
  @Guard({ "pr:approve" })
  public ApiResponse<PurchaseRequisitionResponse> approve(
    @PathVariable Long id,
    @Valid @RequestBody DecisionRequest request
  ) {
    return ApiResponse.success(
      "Purchase requisition approval recorded successfully",
      service.approve(id, request)
    );
  }

  @PostMapping("/{id}/reject")
  @Guard({ "pr:approve" })
  public ApiResponse<PurchaseRequisitionResponse> reject(
    @PathVariable Long id,
    @Valid @RequestBody DecisionRequest request
  ) {
    return ApiResponse.success(
      "Purchase requisition rejected successfully",
      service.reject(id, request)
    );
  }

  @PostMapping("/{id}/cancel")
  @Guard({ "pr:write" })
  public ApiResponse<PurchaseRequisitionResponse> cancel(
    @PathVariable Long id,
    @Valid @RequestBody DecisionRequest request
  ) {
    return ApiResponse.success(
      "Purchase requisition cancelled successfully",
      service.cancel(id, request)
    );
  }
}
