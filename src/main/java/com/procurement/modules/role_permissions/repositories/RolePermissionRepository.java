package com.procurement.modules.role_permissions.repositories;

import com.procurement.modules.role_permissions.entities.Role;
import com.procurement.modules.role_permissions.entities.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {
  void deleteByRole(Role role);
}
