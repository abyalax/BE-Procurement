package com.procurement.modules.warehouse.controller;

import com.procurement.common.response.ApiResponse;
import com.procurement.common.security.Guard;
import com.procurement.modules.warehouse.dto.*;
import com.procurement.modules.warehouse.services.WarehouseService;
import com.procurement.modules.workflow.dto.DecisionRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/purchase-orders/{poId}/goods-receipts")
@RequiredArgsConstructor
public class WarehouseController {

  private final WarehouseService service;

  @GetMapping
  @Guard({ "warehouse:read" })
  public ApiResponse<List<GoodsReceiptResponse>> getByPo(@PathVariable Long poId) {
    return ApiResponse.success("Goods receipts fetched successfully", service.getByPo(poId));
  }

  @GetMapping("/{id}")
  @Guard({ "warehouse:read" })
  public ApiResponse<GoodsReceiptResponse> getById(@PathVariable Long poId, @PathVariable Long id) {
    return ApiResponse.success("Goods receipt fetched successfully", service.getById(poId, id));
  }

  @PostMapping
  @Guard({ "warehouse:write" })
  public ApiResponse<GoodsReceiptResponse> receive(
    @PathVariable Long poId,
    @Valid @RequestBody ReceiveGoodsRequest request
  ) {
    return ApiResponse.success(
      "Goods receipt recorded successfully",
      service.receive(poId, request)
    );
  }

  @PatchMapping("/{id}")
  @Guard({ "warehouse:write" })
  public ApiResponse<GoodsReceiptResponse> update(
    @PathVariable Long poId,
    @PathVariable Long id,
    @Valid @RequestBody UpdateGoodsReceiptRequest request
  ) {
    return ApiResponse.success(
      "Goods receipt updated successfully",
      service.update(poId, id, request)
    );
  }

  @DeleteMapping("/{id}")
  @Guard({ "warehouse:write" })
  public ApiResponse<Boolean> delete(
    @PathVariable Long poId,
    @PathVariable Long id,
    @Valid @RequestBody DecisionRequest request
  ) {
    service.delete(poId, id, request);
    return ApiResponse.success("Goods receipt deleted successfully", true);
  }
}
