package com.procurement.modules.invoices.entities;

import com.procurement.modules.purchase_orders.entities.PurchaseOrder;
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
@Table(name = "invoices")
public class Invoice {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "po_id", nullable = false)
  private PurchaseOrder purchaseOrder;

  @Column(name = "invoice_number", nullable = false, unique = true, length = 100)
  private String invoiceNumber;

  @Column(nullable = false, precision = 18, scale = 2)
  private BigDecimal amount;

  @Column(nullable = false, length = 40)
  private String status;

  @Column(name = "match_status", nullable = false, length = 40)
  private String matchStatus;

  @Column(name = "submitted_by", nullable = false, length = 160)
  private String submittedBy;

  @Column(name = "verified_by", length = 160)
  private String verifiedBy;

  @Column(name = "paid_by", length = 160)
  private String paidBy;

  @Column(columnDefinition = "text")
  private String notes;

  @Column(name = "verified_at")
  private LocalDateTime verifiedAt;

  @Column(name = "paid_at")
  private LocalDateTime paidAt;

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
