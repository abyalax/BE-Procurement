package com.procurement.modules.pr.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "purchase_requisitions")
public class PurchaseRequisition {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 180)
  private String title;

  @Column(columnDefinition = "text")
  private String description;

  @Column(nullable = false, length = 120)
  private String department;

  @Column(name = "requested_by", nullable = false, length = 160)
  private String requestedBy;

  @Column(nullable = false, precision = 18, scale = 2)
  private BigDecimal amount;

  @Column(nullable = false, precision = 18, scale = 2)
  private BigDecimal quantity;

  @Column(nullable = false, length = 40)
  @Builder.Default
  private String status = "DRAFT";

  @Column(nullable = false, length = 80)
  private String stage;

  @Column(nullable = false)
  private boolean emergency;

  @Column(columnDefinition = "text")
  private String justification;

  @Column(name = "cancellation_reason", columnDefinition = "text")
  private String cancellationReason;

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
