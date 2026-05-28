package com.procurement.modules.rfq.repositories;

import com.procurement.modules.rfq.entities.VendorQuotation;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VendorQuotationRepository extends JpaRepository<VendorQuotation, Long> {
  List<VendorQuotation> findByRfqIdOrderByAmountAsc(Long rfqId);
  List<VendorQuotation> findByRfqIdAndVendorId(Long rfqId, Long vendorId);
  Page<VendorQuotation> findByRfqId(Long rfqId, Pageable pageable);
}
