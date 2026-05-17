package com.procurement.modules.users.repositories;

import com.procurement.modules.users.entities.User;
import com.procurement.modules.users.entities.UserStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
  @Override
  @EntityGraph(
    attributePaths = { "roles", "roles.rolePermissions", "roles.rolePermissions.permission" }
  )
  Page<User> findAll(Pageable pageable);

  @EntityGraph(
    attributePaths = { "roles", "roles.rolePermissions", "roles.rolePermissions.permission" }
  )
  Optional<User> findByEmail(String email);

  @EntityGraph(
    attributePaths = { "roles", "roles.rolePermissions", "roles.rolePermissions.permission" }
  )
  Optional<User> findWithRolesById(Long id);

  boolean existsByEmail(String email);

  boolean existsByEmailAndIdNot(String email, Long id);

  long countByStatus(UserStatus status);
}
