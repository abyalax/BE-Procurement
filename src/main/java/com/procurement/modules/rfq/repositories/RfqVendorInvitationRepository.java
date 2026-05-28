package com.procurement.modules.rfq.repositories;

import com.procurement.modules.rfq.entities.RfqVendorInvitation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RfqVendorInvitationRepository extends JpaRepository<RfqVendorInvitation, Long> {
  List<RfqVendorInvitation> findByRfqId(Long rfqId);
  Optional<RfqVendorInvitation> findByRfqIdAndVendorId(Long rfqId, Long vendorId);
  boolean existsByRfqIdAndVendorId(Long rfqId, Long vendorId);
}
