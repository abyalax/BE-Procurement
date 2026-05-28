package com.procurement.modules.audit.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "audit_activities")
public class AuditActivity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "document_type", nullable = false, length = 40)
  private String documentType;

  @Column(name = "document_id", nullable = false)
  private Long documentId;

  @Column(name = "activity_type", nullable = false, length = 80)
  private String activityType;

  @Column(length = 160)
  private String actor;

  @Column(columnDefinition = "text")
  private String notes;

  @Column(columnDefinition = "text")
  private String metadata;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @PrePersist
  void onCreate() {
    createdAt = LocalDateTime.now();
  }
}
