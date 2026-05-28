package com.procurement.modules.qcf.repositories;

import com.procurement.modules.qcf.entities.QcfDocument;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QcfDocumentRepository extends JpaRepository<QcfDocument, Long> {
  boolean existsByRfqId(Long rfqId);
  Optional<QcfDocument> findByRfqId(Long rfqId);
  long countByStatus(String status);
}
