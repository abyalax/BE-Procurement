package com.procurement.modules.qcf.entities;

import com.procurement.modules.rfq.entities.Rfq;
import com.procurement.modules.vendors.entities.Vendor;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "qcf_documents")
public class QcfDocument {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "rfq_id", nullable = false)
  private Rfq rfq;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "recommended_vendor_id")
  private Vendor recommendedVendor;

  @Column(nullable = false, length = 40)
  private String status;

  @Column(nullable = false, length = 80)
  private String stage;

  @Column(name = "recommendation_notes", columnDefinition = "text")
  private String recommendationNotes;

  @Column(name = "created_by", nullable = false, length = 160)
  private String createdBy;

  @Column(name = "approved_by", length = 160)
  private String approvedBy;

  @Column(name = "approved_at")
  private LocalDateTime approvedAt;

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
