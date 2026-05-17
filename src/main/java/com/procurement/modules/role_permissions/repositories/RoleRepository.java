package com.procurement.modules.role_permissions.repositories;

import com.procurement.modules.role_permissions.entities.Role;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
  @Override
  @EntityGraph(attributePaths = { "rolePermissions", "rolePermissions.permission" })
  List<Role> findAll();

  @EntityGraph(attributePaths = { "rolePermissions", "rolePermissions.permission" })
  Optional<Role> findByName(String name);

  @EntityGraph(attributePaths = { "rolePermissions", "rolePermissions.permission" })
  List<Role> findByNameIn(Collection<String> names);

  boolean existsByName(String name);

  default Set<Role> requireAllByName(Collection<String> names) {
    return Set.copyOf(findByNameIn(names));
  }
}
