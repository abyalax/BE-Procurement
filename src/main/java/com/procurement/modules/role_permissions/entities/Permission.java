package com.procurement.modules.role_permissions.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "permissions")
public class Permission {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "permission_key", nullable = false, unique = true, length = 120)
  private String key;

  @Column(name = "name", nullable = false, unique = true, length = 120)
  private String name;

  @Column(name = "description", length = 255)
  private String description;

  @OneToMany(mappedBy = "permission", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private Set<RolePermission> rolePermissions = new HashSet<>();

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @PrePersist
  void onCreate() {
    LocalDateTime now = LocalDateTime.now();
    this.createdAt = now;
    this.updatedAt = now;
  }

  @PreUpdate
  void onUpdate() {
    this.updatedAt = LocalDateTime.now();
  }
}
