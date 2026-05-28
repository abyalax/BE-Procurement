package com.procurement.modules.qcf.controller;

import com.procurement.common.response.*;
import com.procurement.common.security.Guard;
import com.procurement.modules.purchase_orders.dto.PurchaseOrderResponse;
import com.procurement.modules.qcf.dto.*;
import com.procurement.modules.qcf.services.QcfService;
import com.procurement.modules.workflow.dto.DecisionRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/qcfs")
@RequiredArgsConstructor
public class QcfController {

  private final QcfService service;

  @GetMapping
  @Guard({ "qcf:read" })
  public ApiResponse<PageResponse<QcfResponse>> getQcfs(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int limit,
    @RequestParam(name = "sort_by", required = false) String sortBy,
    @RequestParam(name = "sort_order", required = false) String sortOrder
  ) {
    return ApiResponse.success(
      "QCFs fetched successfully",
      service.getQcfs(page, limit, sortBy, sortOrder)
    );
  }

  @GetMapping("/{id}")
  @Guard({ "qcf:read" })
  public ApiResponse<QcfResponse> getById(@PathVariable Long id) {
    return ApiResponse.success("QCF fetched successfully", service.getById(id));
  }

  @PostMapping
  @Guard({ "qcf:write" })
  public ApiResponse<QcfResponse> create(@Valid @RequestBody CreateQcfRequest request) {
    return ApiResponse.success("QCF created successfully", service.create(request));
  }

  @PatchMapping("/{id}")
  @Guard({ "qcf:write" })
  public ApiResponse<QcfResponse> update(
    @PathVariable Long id,
    @Valid @RequestBody UpdateQcfRequest request
  ) {
    return ApiResponse.success("QCF updated successfully", service.update(id, request));
  }

  @GetMapping("/{id}/lines")
  @Guard({ "qcf:read" })
  public ApiResponse<List<QcfLineResponse>> getLines(@PathVariable Long id) {
    return ApiResponse.success("QCF lines fetched successfully", service.getLines(id));
  }

  @PostMapping("/{id}/approve")
  @Guard({ "qcf:approve" })
  public ApiResponse<QcfResponse> approve(
    @PathVariable Long id,
    @Valid @RequestBody DecisionRequest request
  ) {
    return ApiResponse.success("QCF approved successfully", service.approve(id, request));
  }

  @PostMapping("/{id}/award")
  @Guard({ "qcf:write" })
  public ApiResponse<PurchaseOrderResponse> award(
    @PathVariable Long id,
    @Valid @RequestBody DecisionRequest request
  ) {
    return ApiResponse.success("QCF awarded successfully", service.award(id, request));
  }

  @DeleteMapping("/{id}")
  @Guard({ "qcf:write" })
  public ApiResponse<Boolean> delete(
    @PathVariable Long id,
    @Valid @RequestBody DecisionRequest request
  ) {
    service.delete(id, request);
    return ApiResponse.success("QCF deleted successfully", true);
  }

  @PostMapping("/{id}/cancel")
  @Guard({ "qcf:write" })
  public ApiResponse<QcfResponse> cancel(
    @PathVariable Long id,
    @Valid @RequestBody DecisionRequest request
  ) {
    return ApiResponse.success("QCF cancelled successfully", service.cancel(id, request));
  }
}
