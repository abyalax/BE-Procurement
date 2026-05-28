package com.procurement.modules.users.services;

import com.procurement.common.exception.BadRequestException;
import com.procurement.common.exception.ConflictException;
import com.procurement.common.exception.NotFoundException;
import com.procurement.common.pagination.PaginationSort;
import com.procurement.common.response.PageResponse;
import com.procurement.modules.role_permissions.entities.Permission;
import com.procurement.modules.role_permissions.entities.Role;
import com.procurement.modules.role_permissions.entities.RolePermission;
import com.procurement.modules.role_permissions.repositories.RoleRepository;
import com.procurement.modules.users.dto.CreateUserRequest;
import com.procurement.modules.users.dto.UpdateUserRequest;
import com.procurement.modules.users.dto.UserResponse;
import com.procurement.modules.users.entities.User;
import com.procurement.modules.users.repositories.UserRepository;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.*;
import org.springframework.cache.annotation.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

  private static final Logger log = LoggerFactory.getLogger(UserService.class);
  private static final Map<String, String> SORT_FIELDS = Map.of(
    "id",
    "id",
    "name",
    "name",
    "email",
    "email",
    "status",
    "status",
    "createdAt",
    "createdAt",
    "updatedAt",
    "updatedAt"
  );

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;

  @Cacheable(
    value = "users",
    key = "'page:' + #page + ':limit:' + #limit + ':sortBy:' + #sortBy + ':sortOrder:' + #sortOrder"
  )
  public PageResponse<UserResponse> getUsers(int page, int limit, String sortBy, String sortOrder) {
    log.info("Fetching users from database");

    Pageable pageable = PaginationSort.pageRequest(
      page,
      limit,
      sortBy,
      sortOrder,
      SORT_FIELDS,
      "createdAt",
      Sort.Direction.DESC
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
  public UserResponse createUser(CreateUserRequest request) {
    log.info("Creating user email={}", request.email());

    String email = request.email().trim().toLowerCase();
    if (userRepository.existsByEmail(email)) {
      throw new ConflictException("Email already registered");
    }

    Set<Role> roles = resolveRoles(request.roles());
    User user = userRepository.save(
      User.builder()
        .name(request.name().trim())
        .email(email)
        .password(passwordEncoder.encode(request.password()))
        .status(com.procurement.modules.users.entities.UserStatus.ACTIVE)
        .roles(roles)
        .build()
    );

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
      String email = request.email().trim().toLowerCase();
      if (userRepository.existsByEmailAndIdNot(email, id)) {
        throw new ConflictException("Email already registered");
      }
      user.setEmail(email);
    }

    if (request.status() != null) {
      user.setStatus(request.status());
    }

    if (request.roles() != null) {
      user.setRoles(resolveRoles(request.roles()));
    }

    return toResponse(user);
  }

  @Transactional
  @CacheEvict(value = "users", allEntries = true)
  public void deactivateUser(Long id) {
    User user = userRepository
      .findById(id)
      .orElseThrow(() -> new NotFoundException("User not found"));
    user.setStatus(com.procurement.modules.users.entities.UserStatus.INACTIVE);
  }

  private Set<Role> resolveRoles(Set<String> requestedRoles) {
    Set<String> normalizedRoles =
      requestedRoles == null || requestedRoles.isEmpty()
        ? Set.of("USER")
        : normalizeRoles(requestedRoles);
    Set<Role> roles = roleRepository.requireAllByName(normalizedRoles);
    if (roles.size() != normalizedRoles.size()) {
      throw new BadRequestException("One or more roles are invalid");
    }
    return roles;
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
