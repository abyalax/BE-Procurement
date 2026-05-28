package com.procurement.modules.warehouse.services;

import com.procurement.common.exception.*;
import com.procurement.modules.audit.services.AuditService;
import com.procurement.modules.purchase_orders.entities.PurchaseOrder;
import com.procurement.modules.purchase_orders.repositories.PurchaseOrderRepository;
import com.procurement.modules.warehouse.dto.*;
import com.procurement.modules.warehouse.entities.GoodsReceipt;
import com.procurement.modules.warehouse.repositories.GoodsReceiptRepository;
import com.procurement.modules.workflow.dto.DecisionRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WarehouseService {

  private final PurchaseOrderRepository purchaseOrderRepository;
  private final GoodsReceiptRepository goodsReceiptRepository;
  private final AuditService auditService;

  @Transactional
  public GoodsReceiptResponse receive(Long poId, ReceiveGoodsRequest request) {
    PurchaseOrder po = purchaseOrderRepository
      .findById(poId)
      .orElseThrow(() -> new NotFoundException("PO not found"));
    if ("CANCELLED".equals(po.getStatus())) throw new BadRequestException(
      "Cancelled POs cannot receive goods"
    );
    if ("DELIVERED".equals(po.getStatus())) throw new BadRequestException(
      "PO is already fully delivered"
    );
    if (
      request.receivedQuantity().compareTo(po.getRemainingQuantity()) > 0
    ) throw new BadRequestException("Received quantity exceeds remaining quantity");
    BigDecimal remaining = po.getRemainingQuantity().subtract(request.receivedQuantity());
    po.setRemainingQuantity(remaining);
    po.setStatus(remaining.compareTo(BigDecimal.ZERO) == 0 ? "DELIVERED" : "PARTIALLY_DELIVERED");
    po.setStage(remaining.compareTo(BigDecimal.ZERO) == 0 ? "Invoice" : "Delivery");
    GoodsReceipt receipt = goodsReceiptRepository.save(
      GoodsReceipt.builder()
        .purchaseOrder(po)
        .receivedQuantity(request.receivedQuantity())
        .receivedBy(request.actorLabel())
        .notes(request.notes())
        .build()
    );
    auditService.record(
      "PO",
      poId,
      "GOODS_RECEIVED",
      request.actorLabel(),
      request.notes(),
      "{\"receivedQuantity\":" + request.receivedQuantity() + "}"
    );
    return GoodsReceiptResponse.from(receipt);
  }

  public List<GoodsReceiptResponse> getByPo(Long poId) {
    return goodsReceiptRepository
      .findByPurchaseOrderId(poId)
      .stream()
      .map(GoodsReceiptResponse::from)
      .toList();
  }

  public GoodsReceiptResponse getById(Long poId, Long id) {
    return GoodsReceiptResponse.from(requireReceipt(poId, id));
  }

  @Transactional
  public GoodsReceiptResponse update(Long poId, Long id, UpdateGoodsReceiptRequest request) {
    GoodsReceipt receipt = requireReceipt(poId, id);
    PurchaseOrder po = receipt.getPurchaseOrder();
    if ("CANCELLED".equals(po.getStatus())) throw new BadRequestException(
      "Cancelled POs cannot be modified"
    );

    BigDecimal newQuantity = request.receivedQuantity();
    BigDecimal totalBefore = totalReceivedForPo(po.getId());
    BigDecimal totalAfter = totalBefore.subtract(receipt.getReceivedQuantity()).add(newQuantity);
    if (totalAfter.compareTo(po.getQuantity()) > 0) throw new BadRequestException(
      "Received quantity exceeds purchase order quantity"
    );

    receipt.setReceivedQuantity(newQuantity);
    receipt.setReceivedBy(request.actorLabel());
    receipt.setNotes(request.notes());
    recalculatePurchaseOrder(po);
    auditService.record(
      "PO",
      po.getId(),
      "GOODS_RECEIPT_UPDATED",
      request.actorLabel(),
      request.notes(),
      "{\"goodsReceiptId\":" + id + ",\"receivedQuantity\":" + newQuantity + "}"
    );
    return GoodsReceiptResponse.from(receipt);
  }

  @Transactional
  public void delete(Long poId, Long id, DecisionRequest request) {
    GoodsReceipt receipt = requireReceipt(poId, id);
    PurchaseOrder po = receipt.getPurchaseOrder();
    if ("CANCELLED".equals(po.getStatus())) throw new BadRequestException(
      "Cancelled POs cannot be modified"
    );
    goodsReceiptRepository.delete(receipt);
    recalculatePurchaseOrder(po);
    auditService.record(
      "PO",
      po.getId(),
      "GOODS_RECEIPT_DELETED",
      request.actorLabel(),
      request.notes(),
      "{\"goodsReceiptId\":" + id + "}"
    );
  }

  private GoodsReceipt requireReceipt(Long poId, Long receiptId) {
    GoodsReceipt receipt = goodsReceiptRepository
      .findById(receiptId)
      .orElseThrow(() -> new NotFoundException("Goods receipt not found"));
    if (!Objects.equals(receipt.getPurchaseOrder().getId(), poId)) {
      throw new NotFoundException("Goods receipt not found for the given purchase order");
    }
    return receipt;
  }

  private BigDecimal totalReceivedForPo(Long poId) {
    return goodsReceiptRepository
      .findByPurchaseOrderId(poId)
      .stream()
      .map(GoodsReceipt::getReceivedQuantity)
      .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private void recalculatePurchaseOrder(PurchaseOrder po) {
    BigDecimal totalReceived = totalReceivedForPo(po.getId());
    BigDecimal remaining = po.getQuantity().subtract(totalReceived);
    if (totalReceived.compareTo(BigDecimal.ZERO) == 0) {
      po.setRemainingQuantity(po.getQuantity());
      po.setStatus("OPEN");
      po.setStage("Delivery");
      return;
    }
    po.setRemainingQuantity(remaining);
    po.setStatus(remaining.compareTo(BigDecimal.ZERO) == 0 ? "DELIVERED" : "PARTIALLY_DELIVERED");
    po.setStage(remaining.compareTo(BigDecimal.ZERO) == 0 ? "Invoice" : "Delivery");
  }
}
