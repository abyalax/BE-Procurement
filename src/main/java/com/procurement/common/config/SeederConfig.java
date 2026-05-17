package com.procurement.common.config;

import com.procurement.modules.role_permissions.entities.Permission;
import com.procurement.modules.role_permissions.entities.Role;
import com.procurement.modules.role_permissions.entities.RolePermission;
import com.procurement.modules.role_permissions.repositories.PermissionRepository;
import com.procurement.modules.role_permissions.repositories.RoleRepository;
import com.procurement.modules.users.entities.User;
import com.procurement.modules.users.entities.UserStatus;
import com.procurement.modules.users.repositories.UserRepository;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@RequiredArgsConstructor
public class SeederConfig {

  private final PermissionRepository permissionRepository;
  private final RoleRepository roleRepository;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final SeedProperties seedProperties;

  @Bean
  CommandLineRunner seedDefaultAccessData() {
    return args -> seed();
  }

  @Transactional
  void seed() {
    Map<String, String> permissions = new LinkedHashMap<>();
    permissions.put("users:read", "Read users");
    permissions.put("users:write", "Write users");
    permissions.put("roles:read", "Read roles");
    permissions.put("roles:write", "Write roles");
    permissions.put("permissions:read", "Read permissions");

    for (Map.Entry<String, String> entry : permissions.entrySet()) {
      permissionRepository
        .findByKey(entry.getKey())
        .orElseGet(() ->
          permissionRepository.save(
            Permission.builder()
              .key(entry.getKey())
              .name(entry.getValue())
              .description(entry.getValue())
              .build()
          )
        );
    }

    Role adminRole = roleRepository
      .findByName("ADMIN")
      .orElseGet(() ->
        roleRepository.save(
          Role.builder().name("ADMIN").description("Full system administrator").build()
        )
      );
    Role userRole = roleRepository
      .findByName("USER")
      .orElseGet(() ->
        roleRepository.save(
          Role.builder().name("USER").description("Default authenticated user").build()
        )
      );

    attachPermissions(adminRole, permissions.keySet());
    attachPermissions(userRole, Set.of("users:read"));

    if (!userRepository.existsByEmail(seedProperties.adminEmail())) {
      userRepository.save(
        User.builder()
          .name("System Administrator")
          .email(seedProperties.adminEmail())
          .password(passwordEncoder.encode(seedProperties.adminPassword()))
          .status(UserStatus.ACTIVE)
          .roles(Set.of(adminRole))
          .build()
      );
    }
  }

  private void attachPermissions(Role role, Set<String> permissionKeys) {
    if (!role.getRolePermissions().isEmpty()) {
      return;
    }
    for (Permission permission : permissionRepository.findByKeyIn(permissionKeys)) {
      role
        .getRolePermissions()
        .add(RolePermission.builder().role(role).permission(permission).build());
    }
    roleRepository.save(role);
  }
}
