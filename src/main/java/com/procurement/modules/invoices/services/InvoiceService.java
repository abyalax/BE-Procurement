package com.procurement.modules.invoices.services;

import com.procurement.common.exception.*;
import com.procurement.common.pagination.PaginationSort;
import com.procurement.common.response.PageResponse;
import com.procurement.modules.audit.services.AuditService;
import com.procurement.modules.invoices.dto.*;
import com.procurement.modules.invoices.entities.Invoice;
import com.procurement.modules.invoices.repositories.InvoiceRepository;
import com.procurement.modules.purchase_orders.entities.PurchaseOrder;
import com.procurement.modules.purchase_orders.repositories.PurchaseOrderRepository;
import com.procurement.modules.workflow.dto.DecisionRequest;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InvoiceService {

  private static final Map<String, String> SORT_FIELDS = Map.ofEntries(
    Map.entry("id", "id"),
    Map.entry("poId", "purchaseOrder.id"),
    Map.entry("invoiceNumber", "invoiceNumber"),
    Map.entry("amount", "amount"),
    Map.entry("status", "status"),
    Map.entry("matchStatus", "matchStatus"),
    Map.entry("submittedBy", "submittedBy"),
    Map.entry("verifiedBy", "verifiedBy"),
    Map.entry("paidBy", "paidBy"),
    Map.entry("verifiedAt", "verifiedAt"),
    Map.entry("paidAt", "paidAt"),
    Map.entry("createdAt", "createdAt"),
    Map.entry("updatedAt", "updatedAt")
  );

  private final InvoiceRepository invoiceRepository;
  private final PurchaseOrderRepository purchaseOrderRepository;
  private final AuditService auditService;

  public PageResponse<InvoiceResponse> getInvoices(
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
      SORT_FIELDS,
      "createdAt",
      Sort.Direction.DESC
    );
    return PageResponse.from(invoiceRepository.findAll(pageable).map(InvoiceResponse::from));
  }

  public InvoiceResponse getInvoiceById(Long id) {
    return InvoiceResponse.from(requireInvoice(id));
  }

  @Transactional
  public InvoiceResponse create(Long poId, CreateInvoiceRequest request) {
    String invoiceNumber = request.invoiceNumber().trim();
    if (invoiceRepository.existsByInvoiceNumber(invoiceNumber)) throw new ConflictException(
      "Invoice number already exists"
    );
    PurchaseOrder po = purchaseOrderRepository
      .findById(poId)
      .orElseThrow(() -> new NotFoundException("PO not found"));
    if (!"DELIVERED".equals(po.getStatus())) throw new BadRequestException(
      "PO must be delivered before invoice submission"
    );
    Invoice invoice = invoiceRepository.save(
      Invoice.builder()
        .purchaseOrder(po)
        .invoiceNumber(invoiceNumber)
        .amount(request.amount())
        .status("SUBMITTED")
        .matchStatus("PENDING")
        .submittedBy(request.actorLabel())
        .notes(request.notes())
        .build()
    );
    auditService.record(
      "INVOICE",
      invoice.getId(),
      "INVOICE_SUBMITTED",
      request.actorLabel(),
      request.notes(),
      null
    );
    return InvoiceResponse.from(invoice);
  }

  @Transactional
  public InvoiceResponse update(Long id, UpdateInvoiceRequest request) {
    Invoice invoice = requireInvoice(id);
    ensureMutable(invoice);

    if (request.invoiceNumber() != null) {
      String invoiceNumber = request.invoiceNumber().trim();
      if (invoiceNumber.isBlank()) throw new BadRequestException(
        "Invoice number must not be blank"
      );
      if (
        !invoiceNumber.equals(invoice.getInvoiceNumber()) &&
        invoiceRepository.existsByInvoiceNumberAndIdNot(invoiceNumber, id)
      ) {
        throw new ConflictException("Invoice number already exists");
      }
      invoice.setInvoiceNumber(invoiceNumber);
    }

    if (request.amount() != null) invoice.setAmount(request.amount());
    if (request.notes() != null) invoice.setNotes(request.notes());

    auditService.record(
      "INVOICE",
      id,
      "INVOICE_UPDATED",
      request.actorLabel(),
      request.notes(),
      null
    );
    return InvoiceResponse.from(invoice);
  }

  @Transactional
  public InvoiceResponse verify(Long id, DecisionRequest request) {
    Invoice invoice = requireInvoice(id);
    if (!"SUBMITTED".equals(invoice.getStatus())) throw new BadRequestException(
      "Only submitted invoices can be verified"
    );
    PurchaseOrder po = invoice.getPurchaseOrder();
    if (!"DELIVERED".equals(po.getStatus())) throw new BadRequestException(
      "PO must be fully delivered before invoice verification"
    );
    if (invoice.getAmount().compareTo(po.getAmount()) != 0) {
      invoice.setMatchStatus("MISMATCHED");
      auditService.record(
        "INVOICE",
        id,
        "INVOICE_MATCH_FAILED",
        request.actorLabel(),
        "Invoice amount does not match PO amount",
        null
      );
      throw new BadRequestException("Invoice amount does not match PO amount");
    }
    invoice.setStatus("VERIFIED");
    invoice.setMatchStatus("MATCHED");
    invoice.setVerifiedBy(request.actorLabel());
    invoice.setVerifiedAt(LocalDateTime.now());
    auditService.record(
      "INVOICE",
      id,
      "INVOICE_VERIFIED",
      request.actorLabel(),
      request.notes(),
      null
    );
    return InvoiceResponse.from(invoice);
  }

  @Transactional
  public void delete(Long id) {
    Invoice invoice = requireInvoice(id);
    ensureMutable(invoice);
    auditService.record("INVOICE", id, "INVOICE_DELETED", "SYSTEM", "Invoice deleted", null);
    invoiceRepository.delete(invoice);
  }

  @Transactional
  public InvoiceResponse pay(Long id, DecisionRequest request) {
    Invoice invoice = requireInvoice(id);
    if (!"VERIFIED".equals(invoice.getStatus())) throw new BadRequestException(
      "Invoice must be verified before payment"
    );
    invoice.setStatus("PAID");
    invoice.setPaidBy(request.actorLabel());
    invoice.setPaidAt(LocalDateTime.now());
    auditService.record(
      "INVOICE",
      id,
      "PAYMENT_RECORDED",
      request.actorLabel(),
      request.notes(),
      null
    );
    return InvoiceResponse.from(invoice);
  }

  private Invoice requireInvoice(Long id) {
    return invoiceRepository
      .findById(id)
      .orElseThrow(() -> new NotFoundException("Invoice not found"));
  }

  private void ensureMutable(Invoice invoice) {
    if (!"SUBMITTED".equals(invoice.getStatus())) {
      throw new BadRequestException("Only submitted invoices can be modified");
    }
  }
}
