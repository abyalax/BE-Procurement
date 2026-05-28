package com.procurement.modules.audit.controller;

import com.procurement.common.response.ApiResponse;
import com.procurement.common.security.Guard;
import com.procurement.modules.audit.dto.AuditActivityResponse;
import com.procurement.modules.audit.services.AuditService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
public class AuditController {

  private final AuditService service;

  @GetMapping("/{documentType}/{documentId}")
  @Guard({ "audit:read" })
  public ApiResponse<List<AuditActivityResponse>> getTimeline(
    @PathVariable String documentType,
    @PathVariable Long documentId
  ) {
    return ApiResponse.success(
      "Audit timeline fetched successfully",
      service.getTimeline(documentType, documentId)
    );
  }
}
