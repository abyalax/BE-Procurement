package com.procurement.modules.workflow.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "approval_steps")
public class ApprovalStep {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "workflow_id", nullable = false)
  private ApprovalWorkflow workflow;

  @Column(name = "sequence_no", nullable = false)
  private int sequenceNo;

  @Column(name = "approver_role", nullable = false, length = 80)
  private String approverRole;

  @Column(nullable = false, length = 32)
  private String status;

  @Column(name = "decided_by", length = 160)
  private String decidedBy;

  @Column(name = "decided_at")
  private LocalDateTime decidedAt;

  @Column(columnDefinition = "text")
  private String notes;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @PrePersist
  void onCreate() {
    LocalDateTime now = LocalDateTime.now();
    createdAt = now;
    updatedAt = now;
  }

  @PreUpdate
  void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
