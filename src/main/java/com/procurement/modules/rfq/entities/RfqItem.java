package com.procurement.modules.rfq.entities;

import com.procurement.modules.pr.entities.PurchaseRequisitionItem;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "rfq_items")
public class RfqItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "rfq_id", nullable = false)
  private Rfq rfq;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "pr_item_id")
  private PurchaseRequisitionItem purchaseRequisitionItem;

  @Column(name = "line_no", nullable = false)
  private Integer lineNo;

  @Column(name = "item_type", nullable = false, length = 20)
  private String itemType;

  @Column(name = "item_name", nullable = false, length = 180)
  private String itemName;

  @Column(columnDefinition = "text")
  private String description;

  @Column(columnDefinition = "text")
  private String specification;

  @Column(nullable = false, precision = 18, scale = 2)
  private BigDecimal quantity;

  @Column(name = "unit_of_measure", nullable = false, length = 40)
  private String unitOfMeasure;

  @Column(name = "required_date")
  private LocalDate requiredDate;

  @Column(name = "delivery_location", length = 180)
  private String deliveryLocation;

  @Column(name = "budget_code", length = 80)
  private String budgetCode;

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
