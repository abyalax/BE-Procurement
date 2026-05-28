package com.procurement.modules.purchase_orders.services;

import com.procurement.common.exception.BadRequestException;
import com.procurement.common.exception.ConflictException;
import com.procurement.common.exception.NotFoundException;
import com.procurement.common.pagination.PaginationSort;
import com.procurement.common.response.PageResponse;
import com.procurement.modules.audit.services.AuditService;
import com.procurement.modules.purchase_orders.dto.PurchaseOrderResponse;
import com.procurement.modules.purchase_orders.dto.UpdatePurchaseOrderRequest;
import com.procurement.modules.purchase_orders.entities.PurchaseOrder;
import com.procurement.modules.purchase_orders.repositories.PurchaseOrderRepository;
import com.procurement.modules.workflow.dto.DecisionRequest;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PurchaseOrderService {

  private static final Map<String, String> SORT_FIELDS = Map.ofEntries(
    Map.entry("id", "id"),
    Map.entry("qcfId", "qcf.id"),
    Map.entry("rfqId", "rfq.id"),
    Map.entry("vendorId", "vendor.id"),
    Map.entry("vendorName", "vendor.name"),
    Map.entry("poNumber", "poNumber"),
    Map.entry("amount", "amount"),
    Map.entry("quantity", "quantity"),
    Map.entry("remainingQuantity", "remainingQuantity"),
    Map.entry("status", "status"),
    Map.entry("stage", "stage"),
    Map.entry("createdAt", "createdAt"),
    Map.entry("updatedAt", "updatedAt")
  );

  private final PurchaseOrderRepository repository;
  private final AuditService auditService;

  public PageResponse<PurchaseOrderResponse> getPurchaseOrders(
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
    return PageResponse.from(repository.findAll(pageable).map(PurchaseOrderResponse::from));
  }

  public PurchaseOrderResponse getPurchaseOrderById(Long id) {
    return PurchaseOrderResponse.from(requirePurchaseOrder(id));
  }

  @Transactional
  public PurchaseOrderResponse update(Long id, UpdatePurchaseOrderRequest request) {
    PurchaseOrder po = requirePurchaseOrder(id);
    ensureOpen(po);

    if (request.poNumber() != null) {
      String poNumber = request.poNumber().trim();
      if (poNumber.isBlank()) throw new BadRequestException("PO number must not be blank");
      if (!poNumber.equals(po.getPoNumber()) && repository.existsByPoNumberAndIdNot(poNumber, id)) {
        throw new ConflictException("PO number already exists");
      }
      po.setPoNumber(poNumber);
    }

    if (request.amount() != null) po.setAmount(request.amount());
    if (request.quantity() != null) {
      po.setQuantity(request.quantity());
      po.setRemainingQuantity(request.quantity());
    }

    auditService.record("PO", id, "PO_UPDATED", request.actorLabel(), request.notes(), null);
    return PurchaseOrderResponse.from(po);
  }

  @Transactional
  public PurchaseOrderResponse cancel(Long id, DecisionRequest request) {
    PurchaseOrder po = requirePurchaseOrder(id);
    if ("DELIVERED".equals(po.getStatus())) throw new BadRequestException(
      "Delivered POs cannot be cancelled"
    );
    if ("CANCELLED".equals(po.getStatus())) throw new BadRequestException(
      "PO is already cancelled"
    );
    po.setStatus("CANCELLED");
    po.setStage("Closed");
    auditService.record("PO", id, "PO_CANCELLED", request.actorLabel(), request.notes(), null);
    return PurchaseOrderResponse.from(po);
  }

  @Transactional
  public void delete(Long id) {
    PurchaseOrder po = requirePurchaseOrder(id);
    ensureOpen(po);
    auditService.record("PO", id, "PO_DELETED", "SYSTEM", "PO deleted", null);
    repository.delete(po);
  }

  private PurchaseOrder requirePurchaseOrder(Long id) {
    return repository.findById(id).orElseThrow(() -> new NotFoundException("PO not found"));
  }

  private void ensureOpen(PurchaseOrder po) {
    if (!"OPEN".equals(po.getStatus())) {
      throw new BadRequestException("Only open purchase orders can be modified");
    }
  }
}
