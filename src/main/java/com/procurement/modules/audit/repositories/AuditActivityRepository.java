package com.procurement.modules.audit.repositories;

import com.procurement.modules.audit.entities.AuditActivity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditActivityRepository extends JpaRepository<AuditActivity, Long> {
  List<AuditActivity> findByDocumentTypeAndDocumentIdOrderByCreatedAtAsc(
    String documentType,
    Long documentId
  );
}
