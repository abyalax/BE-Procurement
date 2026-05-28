package com.procurement.modules.workflow.dto;

import com.procurement.modules.workflow.entities.ApprovalStep;
import java.time.LocalDateTime;

public record ApprovalStepResponse(
  Long id,
  int sequenceNo,
  String approverRole,
  String status,
  String decidedBy,
  LocalDateTime decidedAt,
  String notes
) {
  public static ApprovalStepResponse from(ApprovalStep step) {
    return new ApprovalStepResponse(
      step.getId(),
      step.getSequenceNo(),
      step.getApproverRole(),
      step.getStatus(),
      step.getDecidedBy(),
      step.getDecidedAt(),
      step.getNotes()
    );
  }
}
