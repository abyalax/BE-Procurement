package com.procurement.modules.rfq.entities;

import com.procurement.modules.pr.entities.PurchaseRequisition;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "rfqs")
public class Rfq {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "pr_id", nullable = false)
  private PurchaseRequisition purchaseRequisition;

  @Column(nullable = false, length = 180)
  private String title;

  @Column(columnDefinition = "text")
  private String description;

  @Column(nullable = false)
  private LocalDateTime deadline;

  @Column(nullable = false, length = 40)
  private String status;

  @Column(nullable = false, length = 80)
  private String stage;

  @Column(name = "failure_reason", columnDefinition = "text")
  private String failureReason;

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
