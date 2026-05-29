package com.procurement.modules.rfq.dto;

import com.procurement.modules.rfq.entities.RfqItem;
import java.math.BigDecimal;
import java.time.LocalDate;

public record RfqItemResponse(
  Long id,
  Long prItemId,
  Integer lineNo,
  String itemType,
  String itemName,
  String description,
  String specification,
  BigDecimal quantity,
  String unitOfMeasure,
  LocalDate requiredDate,
  String deliveryLocation,
  String budgetCode,
  String notes
) {
  public static RfqItemResponse from(RfqItem item) {
    return new RfqItemResponse(
      item.getId(),
      item.getPurchaseRequisitionItem() == null ? null : item.getPurchaseRequisitionItem().getId(),
      item.getLineNo(),
      item.getItemType(),
      item.getItemName(),
      item.getDescription(),
      item.getSpecification(),
      item.getQuantity(),
      item.getUnitOfMeasure(),
      item.getRequiredDate(),
      item.getDeliveryLocation(),
      item.getBudgetCode(),
      item.getNotes()
    );
  }
}
