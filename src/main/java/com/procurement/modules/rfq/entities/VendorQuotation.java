package com.procurement.modules.rfq.entities;

import com.procurement.modules.vendors.entities.Vendor;
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
@Table(name = "vendor_quotations")
public class VendorQuotation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "rfq_id", nullable = false)
  private Rfq rfq;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "vendor_id", nullable = false)
  private Vendor vendor;

  @Column(nullable = false)
  private int version;

  @Column(nullable = false, precision = 18, scale = 2)
  private BigDecimal amount;

  @Column(name = "lead_time_days", nullable = false)
  private int leadTimeDays;

  @Column(name = "technical_score", nullable = false, precision = 5, scale = 2)
  @Builder.Default
  private BigDecimal technicalScore = BigDecimal.ZERO;

  @Column(name = "commercial_score", nullable = false, precision = 5, scale = 2)
  @Builder.Default
  private BigDecimal commercialScore = BigDecimal.ZERO;

  @Column(nullable = false, length = 40)
  private String status;

  @Column(columnDefinition = "text")
  private String notes;

  @Column(name = "submitted_at", nullable = false)
  private LocalDateTime submittedAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @PrePersist
  void onCreate() {
    LocalDateTime now = LocalDateTime.now();
    submittedAt = submittedAt == null ? now : submittedAt;
    createdAt = now;
    updatedAt = now;
  }

  @PreUpdate
  void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
