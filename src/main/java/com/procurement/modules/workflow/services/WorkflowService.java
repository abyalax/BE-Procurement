package com.procurement.modules.workflow.services;

import com.procurement.common.exception.*;
import com.procurement.modules.audit.services.AuditService;
import com.procurement.modules.workflow.dto.*;
import com.procurement.modules.workflow.entities.*;
import com.procurement.modules.workflow.repositories.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkflowService {

  private final ApprovalWorkflowRepository workflowRepository;
  private final ApprovalStepRepository stepRepository;
  private final AuditService auditService;

  @Transactional
  public void startPrApproval(Long prId, BigDecimal amount, boolean emergency, String actor) {
    if (workflowRepository.findByDocumentTypeAndDocumentId("PR", prId).isPresent()) {
      throw new ConflictException("Approval workflow already exists");
    }

    ApprovalWorkflow workflow = workflowRepository.save(
      ApprovalWorkflow.builder().documentType("PR").documentId(prId).status("PENDING").build()
    );

    List<String> roles = new ArrayList<>();
    roles.add("SUPERVISOR");
    if (amount.compareTo(new BigDecimal("5000000")) >= 0) roles.add("MANAGER");
    if (amount.compareTo(new BigDecimal("20000000")) > 0 || emergency) roles.add("DIRECTOR");

    for (int i = 0; i < roles.size(); i++) {
      stepRepository.save(
        ApprovalStep.builder()
          .workflow(workflow)
          .sequenceNo(i + 1)
          .approverRole(roles.get(i))
          .status("WAITING")
          .build()
      );
    }

    auditService.record(
      "PR",
      prId,
      "APPROVAL_WORKFLOW_STARTED",
      actor,
      "Approval workflow started",
      null
    );
  }

  @Transactional
  public boolean approveNext(String documentType, Long documentId, DecisionRequest request) {
    ApprovalWorkflow workflow = requireWorkflow(documentType, documentId);
    ApprovalStep step = stepRepository
      .findFirstByWorkflowIdAndStatusOrderBySequenceNo(workflow.getId(), "WAITING")
      .orElseThrow(() -> new BadRequestException("No pending approval step"));

    step.setStatus("APPROVED");
    step.setDecidedBy(request.actorLabel());
    step.setDecidedAt(LocalDateTime.now());
    step.setNotes(request.notes());
    auditService.record(
      documentType,
      documentId,
      "APPROVAL_STEP_APPROVED",
      request.actorLabel(),
      step.getApproverRole(),
      null
    );

    boolean done = stepRepository
      .findFirstByWorkflowIdAndStatusOrderBySequenceNo(workflow.getId(), "WAITING")
      .isEmpty();
    if (done) {
      workflow.setStatus("APPROVED");
      auditService.record(
        documentType,
        documentId,
        "APPROVAL_WORKFLOW_APPROVED",
        request.actorLabel(),
        request.notes(),
        null
      );
    }
    return done;
  }

  @Transactional
  public void reject(String documentType, Long documentId, DecisionRequest request) {
    ApprovalWorkflow workflow = requireWorkflow(documentType, documentId);
    ApprovalStep step = stepRepository
      .findFirstByWorkflowIdAndStatusOrderBySequenceNo(workflow.getId(), "WAITING")
      .orElseThrow(() -> new BadRequestException("No pending approval step"));
    step.setStatus("REJECTED");
    step.setDecidedBy(request.actorLabel());
    step.setDecidedAt(LocalDateTime.now());
    step.setNotes(request.notes());
    workflow.setStatus("REJECTED");
    auditService.record(
      documentType,
      documentId,
      "APPROVAL_WORKFLOW_REJECTED",
      request.actorLabel(),
      request.notes(),
      null
    );
  }

  public List<ApprovalStepResponse> getSteps(String documentType, Long documentId) {
    return workflowRepository
      .findByDocumentTypeAndDocumentId(documentType, documentId)
      .map(workflow -> stepRepository.findByWorkflowIdOrderBySequenceNo(workflow.getId()))
      .orElse(List.of())
      .stream()
      .map(ApprovalStepResponse::from)
      .toList();
  }

  private ApprovalWorkflow requireWorkflow(String documentType, Long documentId) {
    return workflowRepository
      .findByDocumentTypeAndDocumentId(documentType, documentId)
      .orElseThrow(() -> new NotFoundException("Approval workflow not found"));
  }
}
