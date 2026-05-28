package com.procurement.modules.rfq.controller;

import com.procurement.common.response.*;
import com.procurement.common.security.Guard;
import com.procurement.modules.rfq.dto.*;
import com.procurement.modules.rfq.services.RfqService;
import com.procurement.modules.workflow.dto.DecisionRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/rfqs")
@RequiredArgsConstructor
public class RfqController {

  private final RfqService service;

  @GetMapping
  @Guard({ "rfq:read" })
  public ApiResponse<PageResponse<RfqResponse>> getRfqs(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int limit,
    @RequestParam(name = "sort_by", required = false) String sortBy,
    @RequestParam(name = "sort_order", required = false) String sortOrder
  ) {
    return ApiResponse.success(
      "RFQs fetched successfully",
      service.getRfqs(page, limit, sortBy, sortOrder)
    );
  }

  @GetMapping("/{id}")
  @Guard({ "rfq:read" })
  public ApiResponse<RfqResponse> getById(@PathVariable Long id) {
    return ApiResponse.success("RFQ fetched successfully", service.getById(id));
  }

  @PostMapping
  @Guard({ "rfq:write" })
  public ApiResponse<RfqResponse> create(@Valid @RequestBody CreateRfqRequest request) {
    return ApiResponse.success("RFQ created successfully", service.create(request));
  }

  @PatchMapping("/{id}")
  @Guard({ "rfq:write" })
  public ApiResponse<RfqResponse> update(
    @PathVariable Long id,
    @Valid @RequestBody UpdateRfqRequest request
  ) {
    return ApiResponse.success("RFQ updated successfully", service.update(id, request));
  }

  @DeleteMapping("/{id}")
  @Guard({ "rfq:write" })
  public ApiResponse<Boolean> delete(
    @PathVariable Long id,
    @Valid @RequestBody DecisionRequest request
  ) {
    service.delete(id, request);
    return ApiResponse.success("RFQ deleted successfully", true);
  }

  @GetMapping("/{id}/invitations")
  @Guard({ "rfq:read" })
  public ApiResponse<List<RfqInvitationResponse>> getInvitations(@PathVariable Long id) {
    return ApiResponse.success("RFQ invitations fetched successfully", service.getInvitations(id));
  }

  @PostMapping("/{id}/vendors")
  @Guard({ "rfq:write" })
  public ApiResponse<RfqResponse> addVendors(
    @PathVariable Long id,
    @Valid @RequestBody AddRfqVendorsRequest request
  ) {
    return ApiResponse.success("RFQ vendors invited successfully", service.addVendors(id, request));
  }

  @PostMapping("/{id}/vendors/{vendorId}/accept")
  @Guard({ "rfq:write" })
  public ApiResponse<RfqInvitationResponse> acceptInvitation(
    @PathVariable Long id,
    @PathVariable Long vendorId,
    @Valid @RequestBody DecisionRequest request
  ) {
    return ApiResponse.success(
      "RFQ invitation accepted successfully",
      service.acceptInvitation(id, vendorId, request)
    );
  }

  @PostMapping("/{id}/vendors/{vendorId}/decline")
  @Guard({ "rfq:write" })
  public ApiResponse<RfqInvitationResponse> declineInvitation(
    @PathVariable Long id,
    @PathVariable Long vendorId,
    @Valid @RequestBody DecisionRequest request
  ) {
    return ApiResponse.success(
      "RFQ invitation declined successfully",
      service.declineInvitation(id, vendorId, request)
    );
  }

  @PostMapping("/{id}/expire")
  @Guard({ "rfq:write" })
  public ApiResponse<RfqResponse> expire(
    @PathVariable Long id,
    @Valid @RequestBody DecisionRequest request
  ) {
    return ApiResponse.success("RFQ expired successfully", service.expire(id, request));
  }

  @PostMapping("/{id}/reopen")
  @Guard({ "rfq:write" })
  public ApiResponse<RfqResponse> reopen(
    @PathVariable Long id,
    @Valid @RequestBody DecisionRequest request
  ) {
    return ApiResponse.success("RFQ reopened successfully", service.reopen(id, request));
  }

  @PostMapping("/{id}/manual-sourcing")
  @Guard({ "rfq:write" })
  public ApiResponse<RfqResponse> manualSourcing(
    @PathVariable Long id,
    @Valid @RequestBody DecisionRequest request
  ) {
    return ApiResponse.success(
      "Manual sourcing started successfully",
      service.manualSourcing(id, request)
    );
  }

  @PostMapping("/{id}/cancel")
  @Guard({ "rfq:write" })
  public ApiResponse<RfqResponse> cancel(
    @PathVariable Long id,
    @Valid @RequestBody DecisionRequest request
  ) {
    return ApiResponse.success("RFQ cancelled successfully", service.cancel(id, request));
  }

  @GetMapping("/{id}/quotations")
  @Guard({ "rfq:read" })
  public ApiResponse<PageResponse<QuotationResponse>> getQuotations(
    @PathVariable Long id,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "100") int limit,
    @RequestParam(name = "sort_by", required = false) String sortBy,
    @RequestParam(name = "sort_order", required = false) String sortOrder
  ) {
    return ApiResponse.success(
      "Quotations fetched successfully",
      service.getQuotations(id, page, limit, sortBy, sortOrder)
    );
  }

  @PostMapping("/{id}/quotations")
  @Guard({ "rfq:write" })
  public ApiResponse<QuotationResponse> submitQuotation(
    @PathVariable Long id,
    @Valid @RequestBody SubmitQuotationRequest request
  ) {
    return ApiResponse.success(
      "Quotation submitted successfully",
      service.submitQuotation(id, request)
    );
  }
}
