package com.procurement.modules.purchase_orders.entities;

import com.procurement.modules.qcf.entities.QcfDocument;
import com.procurement.modules.rfq.entities.Rfq;
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
@Table(name = "purchase_orders")
public class PurchaseOrder {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "qcf_id", nullable = false)
  private QcfDocument qcf;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "rfq_id", nullable = false)
  private Rfq rfq;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "vendor_id", nullable = false)
  private Vendor vendor;

  @Column(name = "po_number", nullable = false, unique = true, length = 80)
  private String poNumber;

  @Column(nullable = false, precision = 18, scale = 2)
  private BigDecimal amount;

  @Column(nullable = false, precision = 18, scale = 2)
  private BigDecimal quantity;

  @Column(name = "remaining_quantity", nullable = false, precision = 18, scale = 2)
  private BigDecimal remainingQuantity;

  @Column(nullable = false, length = 40)
  private String status;

  @Column(nullable = false, length = 80)
  private String stage;

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
