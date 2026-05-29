package com.procurement.modules.pr.dto;

import com.procurement.modules.pr.entities.PurchaseRequisitionItem;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PurchaseRequisitionItemResponse(
  Long id,
  Integer lineNo,
  String itemType,
  String itemName,
  String description,
  String specification,
  BigDecimal quantity,
  String unitOfMeasure,
  BigDecimal estimatedUnitPrice,
  BigDecimal estimatedTotalAmount,
  LocalDate requiredDate,
  String deliveryLocation,
  String budgetCode,
  String notes
) {
  public static PurchaseRequisitionItemResponse from(PurchaseRequisitionItem item) {
    return new PurchaseRequisitionItemResponse(
      item.getId(),
      item.getLineNo(),
      item.getItemType(),
      item.getItemName(),
      item.getDescription(),
      item.getSpecification(),
      item.getQuantity(),
      item.getUnitOfMeasure(),
      item.getEstimatedUnitPrice(),
      item.getEstimatedTotalAmount(),
      item.getRequiredDate(),
      item.getDeliveryLocation(),
      item.getBudgetCode(),
      item.getNotes()
    );
  }
}
