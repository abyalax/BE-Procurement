package com.procurement.modules.rfq.entities;

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
@Table(name = "rfq_vendor_invitations")
public class RfqVendorInvitation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "rfq_id", nullable = false)
  private Rfq rfq;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "vendor_id", nullable = false)
  private Vendor vendor;

  @Column(nullable = false, length = 40)
  private String status;

  @Column(name = "response_notes", columnDefinition = "text")
  private String responseNotes;

  @Column(name = "responded_at")
  private LocalDateTime respondedAt;

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
