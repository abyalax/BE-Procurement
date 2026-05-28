package com.procurement.modules.workflow.repositories;

import com.procurement.modules.workflow.entities.ApprovalStep;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalStepRepository extends JpaRepository<ApprovalStep, Long> {
  List<ApprovalStep> findByWorkflowIdOrderBySequenceNo(Long workflowId);
  Optional<ApprovalStep> findFirstByWorkflowIdAndStatusOrderBySequenceNo(
    Long workflowId,
    String status
  );
}
