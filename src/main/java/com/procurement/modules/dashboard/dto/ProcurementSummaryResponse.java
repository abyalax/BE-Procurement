package com.procurement.modules.dashboard.dto;

import java.util.Map;

public record ProcurementSummaryResponse(
  Map<String, Long> purchaseRequisitions,
  Map<String, Long> rfqs,
  Map<String, Long> qcfs,
  Map<String, Long> purchaseOrders,
  Map<String, Long> invoices,
  Map<String, Long> vendors
) {}
