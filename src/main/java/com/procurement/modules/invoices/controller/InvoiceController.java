package com.procurement.modules.invoices.controller;

import com.procurement.common.response.*;
import com.procurement.common.security.Guard;
import com.procurement.modules.invoices.dto.*;
import com.procurement.modules.invoices.services.InvoiceService;
import com.procurement.modules.workflow.dto.DecisionRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
public class InvoiceController {

  private final InvoiceService service;

  @GetMapping
  @Guard({ "invoices:read" })
  public ApiResponse<PageResponse<InvoiceResponse>> getInvoices(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int limit,
    @RequestParam(name = "sort_by", required = false) String sortBy,
    @RequestParam(name = "sort_order", required = false) String sortOrder
  ) {
    return ApiResponse.success(
      "Invoices fetched successfully",
      service.getInvoices(page, limit, sortBy, sortOrder)
    );
  }

  @GetMapping("/{id}")
  @Guard({ "invoices:read" })
  public ApiResponse<InvoiceResponse> getInvoiceById(@PathVariable Long id) {
    return ApiResponse.success("Invoice fetched successfully", service.getInvoiceById(id));
  }

  @PostMapping("/po/{poId}")
  @Guard({ "invoices:write" })
  public ApiResponse<InvoiceResponse> create(
    @PathVariable Long poId,
    @Valid @RequestBody CreateInvoiceRequest request
  ) {
    return ApiResponse.success("Invoice created successfully", service.create(poId, request));
  }

  @PatchMapping("/{id}")
  @Guard({ "invoices:write" })
  public ApiResponse<InvoiceResponse> update(
    @PathVariable Long id,
    @Valid @RequestBody UpdateInvoiceRequest request
  ) {
    return ApiResponse.success("Invoice updated successfully", service.update(id, request));
  }

  @DeleteMapping("/{id}")
  @Guard({ "invoices:write" })
  public ApiResponse<Boolean> delete(@PathVariable Long id) {
    service.delete(id);
    return ApiResponse.success("Invoice deleted successfully", true);
  }

  @PostMapping("/{id}/verify")
  @Guard({ "invoices:verify" })
  public ApiResponse<InvoiceResponse> verify(
    @PathVariable Long id,
    @Valid @RequestBody DecisionRequest request
  ) {
    return ApiResponse.success("Invoice verified successfully", service.verify(id, request));
  }

  @PostMapping("/{id}/pay")
  @Guard({ "invoices:write" })
  public ApiResponse<InvoiceResponse> pay(
    @PathVariable Long id,
    @Valid @RequestBody DecisionRequest request
  ) {
    return ApiResponse.success("Invoice paid successfully", service.pay(id, request));
  }
}
