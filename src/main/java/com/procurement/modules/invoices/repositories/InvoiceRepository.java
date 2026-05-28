package com.procurement.modules.invoices.repositories;

import com.procurement.modules.invoices.entities.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
  boolean existsByInvoiceNumber(String invoiceNumber);
  boolean existsByInvoiceNumberAndIdNot(String invoiceNumber, Long id);
  long countByStatus(String status);
}
