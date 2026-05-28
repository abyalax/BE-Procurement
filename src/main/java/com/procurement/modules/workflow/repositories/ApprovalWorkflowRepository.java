package com.procurement.modules.workflow.repositories;

import com.procurement.modules.workflow.entities.ApprovalWorkflow;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalWorkflowRepository extends JpaRepository<ApprovalWorkflow, Long> {
  Optional<ApprovalWorkflow> findByDocumentTypeAndDocumentId(String documentType, Long documentId);
}
