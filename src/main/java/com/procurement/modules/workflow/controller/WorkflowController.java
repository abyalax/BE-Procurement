package com.procurement.modules.workflow.controller;

import com.procurement.common.response.ApiResponse;
import com.procurement.common.security.Guard;
import com.procurement.modules.workflow.dto.ApprovalStepResponse;
import com.procurement.modules.workflow.services.WorkflowService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/workflows")
@RequiredArgsConstructor
public class WorkflowController {

  private final WorkflowService service;

  @GetMapping("/{documentType}/{documentId}/steps")
  @Guard({ "audit:read" })
  public ApiResponse<List<ApprovalStepResponse>> getSteps(
    @PathVariable String documentType,
    @PathVariable Long documentId
  ) {
    return ApiResponse.success(
      "Approval steps fetched successfully",
      service.getSteps(documentType, documentId)
    );
  }
}
