package com.procurement.modules.dashboard.services;

import com.procurement.modules.dashboard.dto.ProcurementSummaryResponse;
import com.procurement.modules.invoices.repositories.InvoiceRepository;
import com.procurement.modules.pr.repositories.PurchaseRequisitionRepository;
import com.procurement.modules.purchase_orders.repositories.PurchaseOrderRepository;
import com.procurement.modules.qcf.repositories.QcfDocumentRepository;
import com.procurement.modules.rfq.repositories.RfqRepository;
import com.procurement.modules.vendors.repositories.VendorRepository;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {

  private final PurchaseRequisitionRepository prRepository;
  private final RfqRepository rfqRepository;
  private final QcfDocumentRepository qcfRepository;
  private final PurchaseOrderRepository poRepository;
  private final InvoiceRepository invoiceRepository;
  private final VendorRepository vendorRepository;

  public ProcurementSummaryResponse procurementSummary() {
    return new ProcurementSummaryResponse(
      counts(
        prRepository.count(),
        Map.of(
          "DRAFT",
          prRepository.countByStatus("DRAFT"),
          "SUBMITTED",
          prRepository.countByStatus("SUBMITTED"),
          "APPROVED",
          prRepository.countByStatus("APPROVED"),
          "REJECTED",
          prRepository.countByStatus("REJECTED"),
          "CANCELLED",
          prRepository.countByStatus("CANCELLED")
        )
      ),
      counts(
        rfqRepository.count(),
        Map.of(
          "ACTIVE",
          rfqRepository.countByStatus("ACTIVE"),
          "EVALUATING",
          rfqRepository.countByStatus("EVALUATING"),
          "MANUAL_SOURCING",
          rfqRepository.countByStatus("MANUAL_SOURCING"),
          "AWARDED",
          rfqRepository.countByStatus("AWARDED"),
          "FAILED",
          rfqRepository.countByStatus("FAILED"),
          "CANCELLED",
          rfqRepository.countByStatus("CANCELLED")
        )
      ),
      counts(
        qcfRepository.count(),
        Map.of(
          "DRAFT",
          qcfRepository.countByStatus("DRAFT"),
          "APPROVED",
          qcfRepository.countByStatus("APPROVED"),
          "AWARDED",
          qcfRepository.countByStatus("AWARDED"),
          "CANCELLED",
          qcfRepository.countByStatus("CANCELLED")
        )
      ),
      counts(
        poRepository.count(),
        Map.of(
          "OPEN",
          poRepository.countByStatus("OPEN"),
          "PARTIALLY_DELIVERED",
          poRepository.countByStatus("PARTIALLY_DELIVERED"),
          "DELIVERED",
          poRepository.countByStatus("DELIVERED"),
          "CANCELLED",
          poRepository.countByStatus("CANCELLED")
        )
      ),
      counts(
        invoiceRepository.count(),
        Map.of(
          "SUBMITTED",
          invoiceRepository.countByStatus("SUBMITTED"),
          "VERIFIED",
          invoiceRepository.countByStatus("VERIFIED"),
          "PAID",
          invoiceRepository.countByStatus("PAID")
        )
      ),
      counts(
        vendorRepository.count(),
        Map.of(
          "ACTIVE",
          vendorRepository.countByStatus("ACTIVE"),
          "INACTIVE",
          vendorRepository.countByStatus("INACTIVE")
        )
      )
    );
  }

  private Map<String, Long> counts(long total, Map<String, Long> values) {
    Map<String, Long> result = new LinkedHashMap<>();
    result.put("TOTAL", total);
    values
      .entrySet()
      .stream()
      .sorted(Map.Entry.comparingByKey())
      .forEach(entry -> result.put(entry.getKey(), entry.getValue()));
    return result;
  }
}
