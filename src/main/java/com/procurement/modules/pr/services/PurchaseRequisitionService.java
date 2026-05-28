package com.procurement.modules.pr.services;

import com.procurement.common.exception.*;
import com.procurement.common.pagination.PaginationSort;
import com.procurement.common.response.PageResponse;
import com.procurement.common.utils.SearchUtils;
import com.procurement.modules.audit.services.AuditService;
import com.procurement.modules.pr.dto.*;
import com.procurement.modules.pr.entities.PurchaseRequisition;
import com.procurement.modules.pr.repositories.PurchaseRequisitionRepository;
import com.procurement.modules.workflow.dto.DecisionRequest;
import com.procurement.modules.workflow.services.WorkflowService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PurchaseRequisitionService {

  private static final Map<String, String> SORT_FIELDS = Map.ofEntries(
    Map.entry("id", "id"),
    Map.entry("title", "title"),
    Map.entry("department", "department"),
    Map.entry("requestedBy", "requestedBy"),
    Map.entry("amount", "amount"),
    Map.entry("quantity", "quantity"),
    Map.entry("status", "status"),
    Map.entry("stage", "stage"),
    Map.entry("emergency", "emergency"),
    Map.entry("createdAt", "createdAt"),
    Map.entry("updatedAt", "updatedAt")
  );

  private final PurchaseRequisitionRepository repository;
  private final WorkflowService workflowService;
  private final AuditService auditService;

  public PageResponse<PurchaseRequisitionResponse> getPurchaseRequisitions(
    int page,
    int limit,
    String sortBy,
    String sortOrder,
    String search,
    String status
  ) {
    Pageable pageable = PaginationSort.pageRequest(
      page,
      limit,
      sortBy,
      sortOrder,
      SORT_FIELDS,
      "createdAt",
      Sort.Direction.DESC
    );

    String normalizedStatus = normalizeStatus(status);
    String normalizedSearch = SearchUtils.normalizeSearch(search);

    if (normalizedStatus != null && normalizedSearch != null) {
      return PageResponse.from(
        repository
          .searchPurchaseRequisitionByStatus(
            normalizedStatus,
            SearchUtils.toSearchPattern(normalizedSearch),
            pageable
          )
          .map(PurchaseRequisitionResponse::from)
      );
    }

    if (normalizedStatus != null) {
      return PageResponse.from(
        repository.findByStatus(normalizedStatus, pageable).map(PurchaseRequisitionResponse::from)
      );
    }

    if (normalizedSearch != null) {
      return PageResponse.from(
        repository
          .searchPurchaseRequisition(SearchUtils.toSearchPattern(normalizedSearch), pageable)
          .map(PurchaseRequisitionResponse::from)
      );
    }

    return PageResponse.from(repository.findAll(pageable).map(PurchaseRequisitionResponse::from));
  }

  public PurchaseRequisitionResponse getById(Long id) {
    return PurchaseRequisitionResponse.from(require(id));
  }

  @Transactional
  public PurchaseRequisitionResponse create(CreatePurchaseRequisitionRequest request) {
    PurchaseRequisition pr = repository.save(
      PurchaseRequisition.builder()
        .title(request.title().trim())
        .description(request.description())
        .department(request.department().trim())
        .requestedBy(request.requestedByLabel())
        .amount(request.amount())
        .quantity(request.quantity())
        .status("DRAFT")
        .stage("PR Preparation")
        .emergency(request.emergency())
        .justification(request.justification())
        .build()
    );
    auditService.record(
      "PR",
      pr.getId(),
      "PR_CREATED",
      pr.getRequestedBy(),
      "Purchase requisition drafted",
      null
    );
    return PurchaseRequisitionResponse.from(pr);
  }

  @Transactional
  public PurchaseRequisitionResponse update(Long id, UpdatePurchaseRequisitionRequest request) {
    PurchaseRequisition pr = require(id);
    ensureDraft(pr);

    if (request.title() != null) pr.setTitle(request.title().trim());
    if (request.description() != null) pr.setDescription(request.description());
    if (request.department() != null) pr.setDepartment(request.department().trim());
    if (request.amount() != null) pr.setAmount(request.amount());
    if (request.quantity() != null) pr.setQuantity(request.quantity());
    if (request.emergency() != null) pr.setEmergency(request.emergency());
    if (request.justification() != null) pr.setJustification(request.justification());
    pr.setRequestedBy(request.requestedByLabel());

    auditService.record(
      "PR",
      id,
      "PR_UPDATED",
      request.requestedByLabel(),
      request.justification(),
      null
    );
    return PurchaseRequisitionResponse.from(pr);
  }

  @Transactional
  public void delete(Long id, DecisionRequest request) {
    PurchaseRequisition pr = require(id);
    ensureDraft(pr);
    auditService.record("PR", id, "PR_DELETED", request.actorLabel(), request.notes(), null);
    repository.delete(pr);
  }

  @Transactional
  public PurchaseRequisitionResponse submit(Long id, DecisionRequest request) {
    PurchaseRequisition pr = require(id);
    if (!"DRAFT".equals(pr.getStatus())) throw new BadRequestException("PR must be DRAFT");
    pr.setStatus("SUBMITTED");
    pr.setStage("Approval");
    workflowService.startPrApproval(
      pr.getId(),
      pr.getAmount(),
      pr.isEmergency(),
      request.actorLabel()
    );
    auditService.record(
      "PR",
      pr.getId(),
      "PR_SUBMITTED",
      request.actorLabel(),
      request.notes(),
      null
    );
    return PurchaseRequisitionResponse.from(pr);
  }

  @Transactional
  public PurchaseRequisitionResponse approve(Long id, DecisionRequest request) {
    PurchaseRequisition pr = require(id);
    if (!"SUBMITTED".equals(pr.getStatus())) throw new BadRequestException("PR must be SUBMITTED");
    boolean approved = workflowService.approveNext("PR", id, request);
    if (approved) {
      pr.setStatus("APPROVED");
      pr.setStage("RFQ Preparation");
    }
    return PurchaseRequisitionResponse.from(pr);
  }

  @Transactional
  public PurchaseRequisitionResponse reject(Long id, DecisionRequest request) {
    PurchaseRequisition pr = require(id);
    if (!"SUBMITTED".equals(pr.getStatus())) throw new BadRequestException("PR must be SUBMITTED");
    workflowService.reject("PR", id, request);
    pr.setStatus("REJECTED");
    pr.setStage("Closed");
    return PurchaseRequisitionResponse.from(pr);
  }

  @Transactional
  public PurchaseRequisitionResponse cancel(Long id, DecisionRequest request) {
    PurchaseRequisition pr = require(id);
    if (List.of("APPROVED", "REJECTED", "CANCELLED").contains(pr.getStatus())) {
      throw new BadRequestException("Final PRs cannot be cancelled");
    }
    pr.setStatus("CANCELLED");
    pr.setStage("Closed");
    pr.setCancellationReason(request.notes());
    auditService.record("PR", id, "PR_CANCELLED", request.actorLabel(), request.notes(), null);
    return PurchaseRequisitionResponse.from(pr);
  }

  private PurchaseRequisition require(Long id) {
    return repository
      .findById(id)
      .orElseThrow(() -> new NotFoundException("Purchase requisition not found"));
  }

  private void ensureDraft(PurchaseRequisition pr) {
    if (!"DRAFT".equals(pr.getStatus())) {
      throw new BadRequestException("Only draft purchase requisitions can be modified");
    }
  }

  private String normalizeStatus(String status) {
    if (status == null || status.isBlank()) return null;
    String normalized = status.trim().toUpperCase();
    return normalized;
  }
}
