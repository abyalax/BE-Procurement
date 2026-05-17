package com.procurement.modules.users.services;

import com.procurement.common.exception.BadRequestException;
import com.procurement.common.exception.ConflictException;
import com.procurement.common.exception.NotFoundException;
import com.procurement.common.response.PageResponse;
import com.procurement.modules.role_permissions.entities.Permission;
import com.procurement.modules.role_permissions.entities.Role;
import com.procurement.modules.role_permissions.entities.RolePermission;
import com.procurement.modules.role_permissions.repositories.RoleRepository;
import com.procurement.modules.users.dto.UpdateUserRequest;
import com.procurement.modules.users.dto.UserResponse;
import com.procurement.modules.users.entities.User;
import com.procurement.modules.users.repositories.UserRepository;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.*;
import org.springframework.cache.annotation.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

  private static final Logger log = LoggerFactory.getLogger(UserService.class);

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;

  @Cacheable(value = "users", key = "'page:' + #page + ':size:' + #size")
  public PageResponse<UserResponse> getUsers(int page, int size) {
    log.info("Fetching users from database");

    int normalizedSize = Math.min(Math.max(size, 1), 100);
    Pageable pageable = PageRequest.of(
      Math.max(page, 0),
      normalizedSize,
      Sort.by(Sort.Direction.DESC, "createdAt")
    );
    return PageResponse.from(userRepository.findAll(pageable).map(this::toResponse));
  }

  @Cacheable(value = "users", key = "#id")
  public UserResponse getUserById(Long id) {
    log.info("Fetching user from database id={}", id);

    User user = userRepository
      .findWithRolesById(id)
      .orElseThrow(() -> new NotFoundException("User not found"));

    return toResponse(user);
  }

  public UserResponse getCurrentUser(String email) {
    User user = userRepository
      .findByEmail(email)
      .orElseThrow(() -> new NotFoundException("User not found"));

    return toResponse(user);
  }

  @Transactional
  @CacheEvict(value = "users", allEntries = true)
  public UserResponse updateUser(Long id, UpdateUserRequest request) {
    log.info("Updating user id={}", id);

    User user = userRepository
      .findWithRolesById(id)
      .orElseThrow(() -> new NotFoundException("User not found"));

    if (request.name() != null) user.setName(request.name().trim());

    if (request.email() != null) {
      String email = request.email().toLowerCase();
      if (userRepository.existsByEmailAndIdNot(email, id)) {
        throw new ConflictException("Email already registered");
      }
      user.setEmail(email);
    }

    if (request.status() != null) {
      user.setStatus(request.status());
    }

    if (request.roles() != null) {
      Set<Role> roles = roleRepository.requireAllByName(normalizeRoles(request.roles()));
      if (roles.size() != request.roles().size()) {
        throw new BadRequestException("One or more roles are invalid");
      }
      user.setRoles(roles);
    }

    return toResponse(user);
  }

  private UserResponse toResponse(User user) {
    Set<String> roles = this.roleNames(user);
    Set<String> permissions = this.permissionKeys(user);

    return new UserResponse(
      user.getId(),
      user.getName(),
      user.getEmail(),
      user.getStatus(),
      roles,
      permissions,
      user.getCreatedAt(),
      user.getUpdatedAt()
    );
  }

  private Set<String> normalizeRoles(Set<String> roles) {
    return roles
      .stream()
      .map(String::trim)
      .map(String::toUpperCase)
      .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  private Set<String> roleNames(User user) {
    return user
      .getRoles()
      .stream()
      .map(Role::getName)
      .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  private Set<String> permissionKeys(User user) {
    return user
      .getRoles()
      .stream()
      .flatMap(role -> role.getRolePermissions().stream())
      .map(RolePermission::getPermission)
      .map(Permission::getKey)
      .collect(Collectors.toCollection(LinkedHashSet::new));
  }
}
