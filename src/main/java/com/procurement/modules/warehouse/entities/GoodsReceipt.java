package com.procurement.modules.warehouse.entities;

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
@Table(name = "goods_receipts")
public class GoodsReceipt {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "po_id", nullable = false)
  private PurchaseOrder purchaseOrder;

  @Column(name = "received_quantity", nullable = false, precision = 18, scale = 2)
  private BigDecimal receivedQuantity;

  @Column(name = "received_by", nullable = false, length = 160)
  private String receivedBy;

  @Column(columnDefinition = "text")
  private String notes;

  @Column(name = "received_at", nullable = false)
  private LocalDateTime receivedAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @PrePersist
  void onCreate() {
    LocalDateTime now = LocalDateTime.now();
    receivedAt = receivedAt == null ? now : receivedAt;
    createdAt = now;
  }
}
