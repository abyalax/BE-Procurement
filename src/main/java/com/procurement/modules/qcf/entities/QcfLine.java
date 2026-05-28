package com.procurement.modules.qcf.entities;

import com.procurement.modules.rfq.entities.VendorQuotation;
import com.procurement.modules.vendors.entities.Vendor;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "qcf_lines")
public class QcfLine {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "qcf_id", nullable = false)
  private QcfDocument qcf;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "quotation_id", nullable = false)
  private VendorQuotation quotation;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "vendor_id", nullable = false)
  private Vendor vendor;

  @Column(nullable = false, precision = 18, scale = 2)
  private BigDecimal amount;

  @Column(name = "lead_time_days", nullable = false)
  private int leadTimeDays;

  @Column(name = "technical_score", nullable = false, precision = 5, scale = 2)
  private BigDecimal technicalScore;

  @Column(name = "commercial_score", nullable = false, precision = 5, scale = 2)
  private BigDecimal commercialScore;

  @Column(name = "total_score", nullable = false, precision = 6, scale = 2)
  private BigDecimal totalScore;

  @Column(name = "rank_no", nullable = false)
  private int rankNo;

  @Column(nullable = false)
  private boolean recommendation;

  @Column(columnDefinition = "text")
  private String notes;
}
