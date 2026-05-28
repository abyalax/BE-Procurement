package com.procurement.modules.purchase_orders.controller;

import com.procurement.common.response.*;
import com.procurement.common.security.Guard;
import com.procurement.modules.purchase_orders.dto.PurchaseOrderResponse;
import com.procurement.modules.purchase_orders.dto.UpdatePurchaseOrderRequest;
import com.procurement.modules.purchase_orders.services.PurchaseOrderService;
import com.procurement.modules.workflow.dto.DecisionRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

  private final PurchaseOrderService service;

  @GetMapping
  @Guard({ "po:read" })
  public ApiResponse<PageResponse<PurchaseOrderResponse>> getPurchaseOrders(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int limit,
    @RequestParam(name = "sort_by", required = false) String sortBy,
    @RequestParam(name = "sort_order", required = false) String sortOrder
  ) {
    return ApiResponse.success(
      "Purchase orders fetched successfully",
      service.getPurchaseOrders(page, limit, sortBy, sortOrder)
    );
  }

  @GetMapping("/{id}")
  @Guard({ "po:read" })
  public ApiResponse<PurchaseOrderResponse> getPurchaseOrderById(@PathVariable Long id) {
    return ApiResponse.success(
      "Purchase order fetched successfully",
      service.getPurchaseOrderById(id)
    );
  }

  @PatchMapping("/{id}")
  @Guard({ "po:write" })
  public ApiResponse<PurchaseOrderResponse> updatePurchaseOrder(
    @PathVariable Long id,
    @Valid @RequestBody UpdatePurchaseOrderRequest request
  ) {
    return ApiResponse.success("Purchase order updated successfully", service.update(id, request));
  }

  @DeleteMapping("/{id}")
  @Guard({ "po:write" })
  public ApiResponse<Boolean> deletePurchaseOrder(@PathVariable Long id) {
    service.delete(id);
    return ApiResponse.success("Purchase order deleted successfully", true);
  }

  @PostMapping("/{id}/cancel")
  @Guard({ "po:write" })
  public ApiResponse<PurchaseOrderResponse> cancel(
    @PathVariable Long id,
    @Valid @RequestBody DecisionRequest request
  ) {
    return ApiResponse.success(
      "Purchase order cancelled successfully",
      service.cancel(id, request)
    );
  }
}
