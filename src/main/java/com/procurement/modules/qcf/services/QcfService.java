package com.procurement.modules.qcf.services;

import com.procurement.common.exception.*;
import com.procurement.common.pagination.PaginationSort;
import com.procurement.common.response.PageResponse;
import com.procurement.modules.audit.services.AuditService;
import com.procurement.modules.pr.entities.PurchaseRequisition;
import com.procurement.modules.purchase_orders.dto.PurchaseOrderResponse;
import com.procurement.modules.purchase_orders.entities.PurchaseOrder;
import com.procurement.modules.purchase_orders.repositories.PurchaseOrderRepository;
import com.procurement.modules.qcf.dto.*;
import com.procurement.modules.qcf.entities.*;
import com.procurement.modules.qcf.repositories.*;
import com.procurement.modules.rfq.entities.*;
import com.procurement.modules.rfq.repositories.*;
import com.procurement.modules.workflow.dto.DecisionRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QcfService {

  private static final Map<String, String> SORT_FIELDS = Map.ofEntries(
    Map.entry("id", "id"),
    Map.entry("rfqId", "rfq.id"),
    Map.entry("recommendedVendorId", "recommendedVendor.id"),
    Map.entry("recommendedVendorName", "recommendedVendor.name"),
    Map.entry("status", "status"),
    Map.entry("stage", "stage"),
    Map.entry("createdBy", "createdBy"),
    Map.entry("approvedBy", "approvedBy"),
    Map.entry("approvedAt", "approvedAt"),
    Map.entry("createdAt", "createdAt"),
    Map.entry("updatedAt", "updatedAt")
  );

  private final QcfDocumentRepository qcfRepository;
  private final QcfLineRepository lineRepository;
  private final RfqRepository rfqRepository;
  private final VendorQuotationRepository quotationRepository;
  private final PurchaseOrderRepository purchaseOrderRepository;
  private final AuditService auditService;

  public QcfResponse getById(Long id) {
    return QcfResponse.from(require(id));
  }

  public PageResponse<QcfResponse> getQcfs(int page, int limit, String sortBy, String sortOrder) {
    Pageable pageable = PaginationSort.pageRequest(
      page,
      limit,
      sortBy,
      sortOrder,
      SORT_FIELDS,
      "createdAt",
      Sort.Direction.DESC
    );
    return PageResponse.from(qcfRepository.findAll(pageable).map(QcfResponse::from));
  }

  public List<QcfLineResponse> getLines(Long qcfId) {
    return lineRepository
      .findByQcfIdOrderByRankNo(qcfId)
      .stream()
      .map(QcfLineResponse::from)
      .toList();
  }

  @Transactional
  public QcfResponse create(CreateQcfRequest request) {
    if (qcfRepository.existsByRfqId(request.rfqId())) throw new ConflictException(
      "QCF already exists for RFQ"
    );
    Rfq rfq = rfqRepository
      .findById(request.rfqId())
      .orElseThrow(() -> new NotFoundException("RFQ not found"));
    if (!List.of("EVALUATING", "MANUAL_SOURCING").contains(rfq.getStatus())) {
      throw new BadRequestException(
        "RFQ must be evaluating or in manual sourcing before QCF creation"
      );
    }
    var quotations = quotationRepository.findByRfqIdOrderByAmountAsc(rfq.getId());
    if (quotations.isEmpty()) throw new BadRequestException("QCF requires at least one quotation");

    VendorQuotation recommended = quotations
      .stream()
      .max(Comparator.comparing(this::score))
      .orElseThrow();
    QcfDocument qcf = qcfRepository.save(
      QcfDocument.builder()
        .rfq(rfq)
        .recommendedVendor(recommended.getVendor())
        .status("DRAFT")
        .stage("Evaluation")
        .recommendationNotes(request.notes())
        .createdBy(request.actorLabel())
        .build()
    );

    int rank = 1;
    for (VendorQuotation quotation : quotations
      .stream()
      .sorted(Comparator.comparing(this::score).reversed())
      .toList()) {
      BigDecimal score = score(quotation);
      lineRepository.save(
        QcfLine.builder()
          .qcf(qcf)
          .quotation(quotation)
          .vendor(quotation.getVendor())
          .amount(quotation.getAmount())
          .leadTimeDays(quotation.getLeadTimeDays())
          .technicalScore(quotation.getTechnicalScore())
          .commercialScore(quotation.getCommercialScore())
          .totalScore(score)
          .rankNo(rank++)
          .recommendation(quotation.getId().equals(recommended.getId()))
          .notes(quotation.getNotes())
          .build()
      );
    }

    rfq.setStatus("EVALUATING");
    rfq.setStage("QCF Evaluation");
    auditService.record(
      "QCF",
      qcf.getId(),
      "QCF_CREATED",
      request.actorLabel(),
      request.notes(),
      null
    );
    return QcfResponse.from(qcf);
  }

  @Transactional
  public QcfResponse update(Long id, UpdateQcfRequest request) {
    QcfDocument qcf = require(id);
    ensureDraft(qcf);
    if (request.recommendationNotes() != null) qcf.setRecommendationNotes(
      request.recommendationNotes()
    );
    auditService.record("QCF", id, "QCF_UPDATED", request.actorLabel(), request.notes(), null);
    return QcfResponse.from(qcf);
  }

  @Transactional
  public QcfResponse approve(Long id, DecisionRequest request) {
    QcfDocument qcf = qcfRepository
      .findById(id)
      .orElseThrow(() -> new NotFoundException("QCF not found"));
    if (!"DRAFT".equals(qcf.getStatus())) throw new BadRequestException(
      "Only draft QCFs can be approved"
    );
    qcf.setStatus("APPROVED");
    qcf.setStage("Awarding");
    qcf.setApprovedBy(request.actorLabel());
    qcf.setApprovedAt(LocalDateTime.now());
    auditService.record("QCF", id, "QCF_APPROVED", request.actorLabel(), request.notes(), null);
    return QcfResponse.from(qcf);
  }

  @Transactional
  public PurchaseOrderResponse award(Long id, DecisionRequest request) {
    QcfDocument qcf = qcfRepository
      .findById(id)
      .orElseThrow(() -> new NotFoundException("QCF not found"));
    if (!"APPROVED".equals(qcf.getStatus())) throw new BadRequestException(
      "QCF must be approved before award"
    );
    if (purchaseOrderRepository.existsByQcfId(id)) {
      return PurchaseOrderResponse.from(purchaseOrderRepository.findByQcfId(id).orElseThrow());
    }
    PurchaseRequisition pr = qcf.getRfq().getPurchaseRequisition();
    BigDecimal amount = lineRepository
      .findByQcfIdOrderByRankNo(id)
      .stream()
      .filter(QcfLine::isRecommendation)
      .findFirst()
      .map(QcfLine::getAmount)
      .orElse(pr.getTotalEstimatedAmount());
    BigDecimal quantity = pr.totalRequestedQuantity();
    PurchaseOrder po = purchaseOrderRepository.save(
      PurchaseOrder.builder()
        .qcf(qcf)
        .rfq(qcf.getRfq())
        .vendor(qcf.getRecommendedVendor())
        .poNumber("PO-" + qcf.getId())
        .amount(amount)
        .quantity(quantity)
        .remainingQuantity(quantity)
        .status("OPEN")
        .stage("Delivery")
        .build()
    );
    qcf.setStatus("AWARDED");
    qcf.getRfq().setStatus("AWARDED");
    qcf.getRfq().setStage("PO Processing");
    auditService.record("QCF", id, "VENDOR_AWARDED", request.actorLabel(), request.notes(), null);
    auditService.record(
      "PO",
      po.getId(),
      "PO_CREATED",
      request.actorLabel(),
      "PO generated from awarded QCF",
      null
    );
    return PurchaseOrderResponse.from(po);
  }

  @Transactional
  public void delete(Long id, DecisionRequest request) {
    QcfDocument qcf = require(id);
    ensureDraft(qcf);
    if (purchaseOrderRepository.existsByQcfId(id)) {
      throw new BadRequestException("Awarded QCFs cannot be deleted");
    }
    auditService.record("QCF", id, "QCF_DELETED", request.actorLabel(), request.notes(), null);
    qcfRepository.delete(qcf);
  }

  @Transactional
  public QcfResponse cancel(Long id, DecisionRequest request) {
    QcfDocument qcf = qcfRepository
      .findById(id)
      .orElseThrow(() -> new NotFoundException("QCF not found"));
    if ("AWARDED".equals(qcf.getStatus())) throw new BadRequestException(
      "Awarded QCFs cannot be cancelled"
    );
    qcf.setStatus("CANCELLED");
    qcf.setStage("Closed");
    qcf.getRfq().setStatus("EVALUATING");
    qcf.getRfq().setStage("Evaluation");
    auditService.record("QCF", id, "QCF_CANCELLED", request.actorLabel(), request.notes(), null);
    return QcfResponse.from(qcf);
  }

  private BigDecimal score(VendorQuotation quotation) {
    BigDecimal priceScore = BigDecimal.valueOf(100).subtract(
      quotation.getAmount().divide(BigDecimal.valueOf(1000000), 2, java.math.RoundingMode.HALF_UP)
    );
    return priceScore.add(quotation.getTechnicalScore()).add(quotation.getCommercialScore());
  }

  private QcfDocument require(Long id) {
    return qcfRepository.findById(id).orElseThrow(() -> new NotFoundException("QCF not found"));
  }

  private void ensureDraft(QcfDocument qcf) {
    if (!"DRAFT".equals(qcf.getStatus())) {
      throw new BadRequestException("Only draft QCFs can be modified");
    }
  }
}
