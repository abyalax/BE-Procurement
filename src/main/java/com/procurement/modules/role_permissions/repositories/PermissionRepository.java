package com.procurement.modules.role_permissions.repositories;

import com.procurement.modules.role_permissions.entities.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByKey(String key);

    List<Permission> findByKeyIn(Collection<String> keys);

    boolean existsByKey(String key);
}
