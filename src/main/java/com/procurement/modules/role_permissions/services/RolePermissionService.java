package com.procurement.modules.role_permissions.services;

import com.procurement.common.exception.BadRequestException;
import com.procurement.common.exception.ConflictException;
import com.procurement.common.exception.NotFoundException;
import com.procurement.modules.role_permissions.dto.CreateRoleRequest;
import com.procurement.modules.role_permissions.dto.PermissionResponse;
import com.procurement.modules.role_permissions.dto.RoleResponse;
import com.procurement.modules.role_permissions.dto.UpdateRolePermissionsRequest;
import com.procurement.modules.role_permissions.entities.Permission;
import com.procurement.modules.role_permissions.entities.Role;
import com.procurement.modules.role_permissions.entities.RolePermission;
import com.procurement.modules.role_permissions.repositories.PermissionRepository;
import com.procurement.modules.role_permissions.repositories.RolePermissionRepository;
import com.procurement.modules.role_permissions.repositories.RoleRepository;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RolePermissionService {

  private final RoleRepository roleRepository;
  private final PermissionRepository permissionRepository;
  private final RolePermissionRepository rolePermissionRepository;

  @Cacheable(value = "roles", key = "'all'")
  public List<RoleResponse> getRoles() {
    return roleRepository.findAll().stream().map(this::toRoleResponse).toList();
  }

  @Cacheable(value = "permissions", key = "'all'")
  public List<PermissionResponse> getPermissions() {
    return permissionRepository.findAll().stream().map(this::toPermissionResponse).toList();
  }

  @Transactional
  @CacheEvict(value = { "roles", "users" }, allEntries = true)
  public RoleResponse createRole(CreateRoleRequest request) {
    String roleName = normalizeRoleName(request.name());
    if (roleRepository.existsByName(roleName)) {
      throw new ConflictException("Role already exists");
    }

    Role role = Role.builder().name(roleName).description(request.description()).build();
    Role savedRole = roleRepository.save(role);
    syncPermissions(savedRole, request.permissions() == null ? Set.of() : request.permissions());
    return toRoleResponse(savedRole);
  }

  @Transactional
  @CacheEvict(value = { "roles", "users" }, allEntries = true)
  public RoleResponse updateRolePermissions(Long roleId, UpdateRolePermissionsRequest request) {
    Role role = roleRepository
      .findById(roleId)
      .orElseThrow(() -> new NotFoundException("Role not found"));
    syncPermissions(role, request.permissions());
    return toRoleResponse(role);
  }

  private void syncPermissions(Role role, Set<String> permissionKeys) {
    rolePermissionRepository.deleteByRole(role);
    role.getRolePermissions().clear();

    Set<Permission> permissions = requirePermissions(permissionKeys);
    for (Permission permission : permissions) {
      RolePermission rolePermission = RolePermission.builder()
        .role(role)
        .permission(permission)
        .build();
      role.getRolePermissions().add(rolePermission);
    }
  }

  private Set<Permission> requirePermissions(Set<String> permissionKeys) {
    Set<String> normalizedKeys = permissionKeys
      .stream()
      .map(String::trim)
      .filter(key -> !key.isBlank())
      .collect(Collectors.toCollection(LinkedHashSet::new));
    List<Permission> permissions = permissionRepository.findByKeyIn(normalizedKeys);
    if (permissions.size() != normalizedKeys.size()) {
      throw new BadRequestException("One or more permissions are invalid");
    }
    return new LinkedHashSet<>(permissions);
  }

  private String normalizeRoleName(String value) {
    return value.trim().toUpperCase();
  }

  private RoleResponse toRoleResponse(Role role) {
    Set<String> permissions = role
      .getRolePermissions()
      .stream()
      .map(RolePermission::getPermission)
      .map(Permission::getKey)
      .collect(Collectors.toCollection(LinkedHashSet::new));
    return new RoleResponse(role.getId(), role.getName(), role.getDescription(), permissions);
  }

  private PermissionResponse toPermissionResponse(Permission permission) {
    return new PermissionResponse(
      permission.getId(),
      permission.getKey(),
      permission.getName(),
      permission.getDescription()
    );
  }
}
