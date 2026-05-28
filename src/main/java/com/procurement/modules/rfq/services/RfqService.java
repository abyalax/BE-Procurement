package com.procurement.modules.rfq.services;

import com.procurement.common.exception.*;
import com.procurement.common.pagination.PaginationSort;
import com.procurement.common.response.PageResponse;
import com.procurement.modules.audit.services.AuditService;
import com.procurement.modules.pr.entities.PurchaseRequisition;
import com.procurement.modules.pr.repositories.PurchaseRequisitionRepository;
import com.procurement.modules.qcf.repositories.QcfDocumentRepository;
import com.procurement.modules.rfq.dto.*;
import com.procurement.modules.rfq.entities.*;
import com.procurement.modules.rfq.repositories.*;
import com.procurement.modules.vendors.entities.Vendor;
import com.procurement.modules.vendors.repositories.VendorRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RfqService {

  private static final Map<String, String> RFQ_SORT_FIELDS = Map.ofEntries(
    Map.entry("id", "id"),
    Map.entry("prId", "purchaseRequisition.id"),
    Map.entry("title", "title"),
    Map.entry("deadline", "deadline"),
    Map.entry("status", "status"),
    Map.entry("stage", "stage"),
    Map.entry("createdAt", "createdAt"),
    Map.entry("updatedAt", "updatedAt")
  );
  private static final Map<String, String> QUOTATION_SORT_FIELDS = Map.ofEntries(
    Map.entry("id", "id"),
    Map.entry("rfqId", "rfq.id"),
    Map.entry("vendorId", "vendor.id"),
    Map.entry("vendorName", "vendor.name"),
    Map.entry("version", "version"),
    Map.entry("amount", "amount"),
    Map.entry("leadTimeDays", "leadTimeDays"),
    Map.entry("technicalScore", "technicalScore"),
    Map.entry("commercialScore", "commercialScore"),
    Map.entry("status", "status"),
    Map.entry("submittedAt", "submittedAt"),
    Map.entry("createdAt", "createdAt"),
    Map.entry("updatedAt", "updatedAt")
  );

  private final RfqRepository rfqRepository;
  private final RfqVendorInvitationRepository invitationRepository;
  private final VendorQuotationRepository quotationRepository;
  private final PurchaseRequisitionRepository prRepository;
  private final QcfDocumentRepository qcfRepository;
  private final VendorRepository vendorRepository;
  private final AuditService auditService;

  public RfqResponse getById(Long id) {
    return RfqResponse.from(require(id));
  }

  public PageResponse<RfqResponse> getRfqs(int page, int limit, String sortBy, String sortOrder) {
    Pageable pageable = PaginationSort.pageRequest(
      page,
      limit,
      sortBy,
      sortOrder,
      RFQ_SORT_FIELDS,
      "createdAt",
      Sort.Direction.DESC
    );
    return PageResponse.from(rfqRepository.findAll(pageable).map(RfqResponse::from));
  }

  @Transactional
  public RfqResponse create(CreateRfqRequest request) {
    PurchaseRequisition pr = prRepository
      .findById(request.prId())
      .orElseThrow(() -> new NotFoundException("PR not found"));
    if (!"APPROVED".equals(pr.getStatus())) throw new BadRequestException(
      "PR must be approved before RFQ creation"
    );
    if (request.deadline().isBefore(LocalDateTime.now())) throw new BadRequestException(
      "RFQ deadline must be in the future"
    );
    if (request.vendorIds().stream().distinct().toList().isEmpty()) throw new BadRequestException(
      "RFQ requires at least one vendor"
    );

    Rfq rfq = rfqRepository.save(
      Rfq.builder()
        .purchaseRequisition(pr)
        .title(request.title().trim())
        .description(request.description())
        .deadline(request.deadline())
        .status("ACTIVE")
        .stage("Vendor Response")
        .build()
    );

    inviteVendors(rfq, request.vendorIds());

    auditService.record(
      "RFQ",
      rfq.getId(),
      "RFQ_CREATED",
      request.actorLabel(),
      "RFQ created from approved PR",
      null
    );
    return RfqResponse.from(rfq);
  }

  @Transactional
  public RfqResponse update(Long id, UpdateRfqRequest request) {
    Rfq rfq = require(id);
    ensureMutable(rfq);

    if (request.title() != null) rfq.setTitle(request.title().trim());
    if (request.description() != null) rfq.setDescription(request.description());
    if (request.deadline() != null) {
      if (request.deadline().isBefore(LocalDateTime.now())) throw new BadRequestException(
        "RFQ deadline must be in the future"
      );
      rfq.setDeadline(request.deadline());
    }

    auditService.record("RFQ", id, "RFQ_UPDATED", request.actorLabel(), request.notes(), null);
    return RfqResponse.from(rfq);
  }

  @Transactional
  public void delete(Long id, com.procurement.modules.workflow.dto.DecisionRequest request) {
    Rfq rfq = require(id);
    ensureDeletable(rfq);
    auditService.record("RFQ", id, "RFQ_DELETED", request.actorLabel(), request.notes(), null);
    rfqRepository.delete(rfq);
  }

  public List<RfqInvitationResponse> getInvitations(Long rfqId) {
    return invitationRepository
      .findByRfqId(rfqId)
      .stream()
      .map(RfqInvitationResponse::from)
      .toList();
  }

  @Transactional
  public RfqResponse addVendors(Long rfqId, AddRfqVendorsRequest request) {
    Rfq rfq = require(rfqId);
    if (isClosed(rfq)) throw new BadRequestException("Cannot invite vendors to a closed RFQ");
    inviteVendors(rfq, request.vendorIds());
    rfq.setStatus("ACTIVE");
    rfq.setStage("Vendor Response");
    auditService.record(
      "RFQ",
      rfqId,
      "VENDORS_INVITED",
      request.actorLabel(),
      request.notes(),
      "{\"vendorCount\":" + request.vendorIds().size() + "}"
    );
    return RfqResponse.from(rfq);
  }

  @Transactional
  public RfqInvitationResponse acceptInvitation(
    Long rfqId,
    Long vendorId,
    com.procurement.modules.workflow.dto.DecisionRequest request
  ) {
    RfqVendorInvitation invitation = requireInvitation(rfqId, vendorId);
    if (isClosed(invitation.getRfq())) throw new BadRequestException(
      "Cannot respond to a closed RFQ"
    );
    invitation.setStatus("ACCEPTED");
    invitation.setResponseNotes(request.notes());
    invitation.setRespondedAt(LocalDateTime.now());
    auditService.record(
      "RFQ",
      rfqId,
      "VENDOR_ACCEPTED",
      request.actorLabel(),
      request.notes(),
      "{\"vendorId\":" + vendorId + "}"
    );
    return RfqInvitationResponse.from(invitation);
  }

  @Transactional
  public RfqInvitationResponse declineInvitation(
    Long rfqId,
    Long vendorId,
    com.procurement.modules.workflow.dto.DecisionRequest request
  ) {
    RfqVendorInvitation invitation = requireInvitation(rfqId, vendorId);
    if (isClosed(invitation.getRfq())) throw new BadRequestException(
      "Cannot respond to a closed RFQ"
    );
    invitation.setStatus("DECLINED");
    invitation.setResponseNotes(request.notes());
    invitation.setRespondedAt(LocalDateTime.now());
    auditService.record(
      "RFQ",
      rfqId,
      "VENDOR_DECLINED",
      request.actorLabel(),
      request.notes(),
      "{\"vendorId\":" + vendorId + "}"
    );
    failIfNoViableVendor(invitation.getRfq(), "All invited vendors declined");
    return RfqInvitationResponse.from(invitation);
  }

  @Transactional
  public RfqResponse expire(
    Long rfqId,
    com.procurement.modules.workflow.dto.DecisionRequest request
  ) {
    Rfq rfq = require(rfqId);
    if (isClosed(rfq)) throw new BadRequestException("RFQ is already closed");
    boolean hasQuotation = !quotationRepository.findByRfqIdOrderByAmountAsc(rfqId).isEmpty();
    if (hasQuotation) {
      rfq.setStatus("EVALUATING");
      rfq.setStage("Evaluation");
      auditService.record(
        "RFQ",
        rfqId,
        "RFQ_EXPIRED_WITH_QUOTATIONS",
        request.actorLabel(),
        request.notes(),
        null
      );
      return RfqResponse.from(rfq);
    }
    rfq.setStatus("FAILED");
    rfq.setStage("Manual Sourcing");
    rfq.setFailureReason(
      request.notes() == null || request.notes().isBlank()
        ? "RFQ expired with no quotations"
        : request.notes()
    );
    auditService.record(
      "RFQ",
      rfqId,
      "RFQ_FAILED",
      request.actorLabel(),
      rfq.getFailureReason(),
      null
    );
    return RfqResponse.from(rfq);
  }

  @Transactional
  public RfqResponse reopen(
    Long rfqId,
    com.procurement.modules.workflow.dto.DecisionRequest request
  ) {
    Rfq rfq = require(rfqId);
    if (!List.of("FAILED", "CANCELLED").contains(rfq.getStatus())) throw new BadRequestException(
      "Only failed or cancelled RFQs can be reopened"
    );
    rfq.setStatus("ACTIVE");
    rfq.setStage("Vendor Response");
    rfq.setFailureReason(null);
    auditService.record("RFQ", rfqId, "RFQ_REOPENED", request.actorLabel(), request.notes(), null);
    return RfqResponse.from(rfq);
  }

  @Transactional
  public RfqResponse manualSourcing(
    Long rfqId,
    com.procurement.modules.workflow.dto.DecisionRequest request
  ) {
    Rfq rfq = require(rfqId);
    if ("AWARDED".equals(rfq.getStatus())) throw new BadRequestException(
      "Awarded RFQs cannot move to manual sourcing"
    );
    rfq.setStatus("MANUAL_SOURCING");
    rfq.setStage("Manual Sourcing");
    rfq.setFailureReason(request.notes());
    auditService.record(
      "RFQ",
      rfqId,
      "MANUAL_SOURCING_STARTED",
      request.actorLabel(),
      request.notes(),
      null
    );
    return RfqResponse.from(rfq);
  }

  @Transactional
  public RfqResponse cancel(
    Long rfqId,
    com.procurement.modules.workflow.dto.DecisionRequest request
  ) {
    Rfq rfq = require(rfqId);
    if ("AWARDED".equals(rfq.getStatus())) throw new BadRequestException(
      "Awarded RFQs cannot be cancelled"
    );
    rfq.setStatus("CANCELLED");
    rfq.setStage("Closed");
    rfq.setFailureReason(request.notes());
    auditService.record("RFQ", rfqId, "RFQ_CANCELLED", request.actorLabel(), request.notes(), null);
    return RfqResponse.from(rfq);
  }

  @Transactional
  public QuotationResponse submitQuotation(Long rfqId, SubmitQuotationRequest request) {
    Rfq rfq = require(rfqId);
    if (isClosed(rfq)) throw new BadRequestException("Cannot submit quotation to a closed RFQ");
    if (rfq.getDeadline().isBefore(LocalDateTime.now())) throw new BadRequestException(
      "RFQ deadline has passed"
    );
    Vendor vendor = vendorRepository
      .findById(request.vendorId())
      .orElseThrow(() -> new NotFoundException("Vendor not found"));
    RfqVendorInvitation invitation = requireInvitation(rfqId, vendor.getId());
    if ("DECLINED".equals(invitation.getStatus())) throw new BadRequestException(
      "Declined vendors cannot submit quotations"
    );
    if ("INVITED".equals(invitation.getStatus())) {
      invitation.setStatus("ACCEPTED");
      invitation.setRespondedAt(LocalDateTime.now());
    }
    int version =
      quotationRepository
        .findByRfqIdAndVendorId(rfqId, vendor.getId())
        .stream()
        .mapToInt(VendorQuotation::getVersion)
        .max()
        .orElse(0) +
      1;

    VendorQuotation quotation = quotationRepository.save(
      VendorQuotation.builder()
        .rfq(rfq)
        .vendor(vendor)
        .version(version)
        .amount(request.amount())
        .leadTimeDays(request.leadTimeDays())
        .technicalScore(
          request.technicalScore() == null ? BigDecimal.ZERO : request.technicalScore()
        )
        .commercialScore(
          request.commercialScore() == null ? BigDecimal.ZERO : request.commercialScore()
        )
        .status("SUBMITTED")
        .notes(request.notes())
        .build()
    );
    rfq.setStatus("EVALUATING");
    rfq.setStage("Evaluation");
    auditService.record(
      "RFQ",
      rfqId,
      version == 1 ? "QUOTATION_SUBMITTED" : "QUOTATION_REVISED",
      vendor.getName(),
      request.notes(),
      null
    );
    return QuotationResponse.from(quotation);
  }

  public PageResponse<QuotationResponse> getQuotations(
    Long rfqId,
    int page,
    int limit,
    String sortBy,
    String sortOrder
  ) {
    Pageable pageable = PaginationSort.pageRequest(
      page,
      limit,
      sortBy,
      sortOrder,
      QUOTATION_SORT_FIELDS,
      "amount",
      Sort.Direction.ASC
    );
    return PageResponse.from(
      quotationRepository.findByRfqId(rfqId, pageable).map(QuotationResponse::from)
    );
  }

  private Rfq require(Long rfqId) {
    return rfqRepository.findById(rfqId).orElseThrow(() -> new NotFoundException("RFQ not found"));
  }

  private void ensureMutable(Rfq rfq) {
    if (!"ACTIVE".equals(rfq.getStatus())) {
      throw new BadRequestException("Only active RFQs can be modified");
    }
    if (qcfRepository.existsByRfqId(rfq.getId())) {
      throw new BadRequestException("RFQ cannot be modified after QCF creation");
    }
    if (!quotationRepository.findByRfqIdOrderByAmountAsc(rfq.getId()).isEmpty()) {
      throw new BadRequestException("RFQ cannot be modified after quotations are submitted");
    }
  }

  private void ensureDeletable(Rfq rfq) {
    ensureMutable(rfq);
  }

  private RfqVendorInvitation requireInvitation(Long rfqId, Long vendorId) {
    return invitationRepository
      .findByRfqIdAndVendorId(rfqId, vendorId)
      .orElseThrow(() -> new NotFoundException("RFQ vendor invitation not found"));
  }

  private void inviteVendors(Rfq rfq, List<Long> vendorIds) {
    for (Long vendorId : vendorIds.stream().distinct().toList()) {
      Vendor vendor = vendorRepository
        .findById(vendorId)
        .orElseThrow(() -> new NotFoundException("Vendor not found"));
      if (invitationRepository.existsByRfqIdAndVendorId(rfq.getId(), vendorId)) {
        continue;
      }
      invitationRepository.save(
        RfqVendorInvitation.builder().rfq(rfq).vendor(vendor).status("INVITED").build()
      );
    }
  }

  private void failIfNoViableVendor(Rfq rfq) {
    failIfNoViableVendor(rfq, "No vendor remains available for this RFQ");
  }

  private void failIfNoViableVendor(Rfq rfq, String reason) {
    List<RfqVendorInvitation> invitations = invitationRepository.findByRfqId(rfq.getId());
    boolean hasViableVendor = invitations
      .stream()
      .anyMatch(invitation -> !"DECLINED".equals(invitation.getStatus()));
    if (
      !hasViableVendor && quotationRepository.findByRfqIdOrderByAmountAsc(rfq.getId()).isEmpty()
    ) {
      rfq.setStatus("FAILED");
      rfq.setStage("Manual Sourcing");
      rfq.setFailureReason(reason);
      auditService.record("RFQ", rfq.getId(), "RFQ_FAILED", "System", reason, null);
    }
  }

  private boolean isClosed(Rfq rfq) {
    return List.of("AWARDED", "CANCELLED", "FAILED").contains(rfq.getStatus());
  }
}
