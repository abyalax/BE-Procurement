package com.procurement.modules.auth.services;

import com.procurement.common.exception.ConflictException;
import com.procurement.common.exception.NotFoundException;
import com.procurement.modules.auth.dto.AuthResponse;
import com.procurement.modules.auth.dto.LoginRequest;
import com.procurement.modules.auth.dto.RegisterRequest;
import com.procurement.modules.role_permissions.entities.Permission;
import com.procurement.modules.role_permissions.entities.Role;
import com.procurement.modules.role_permissions.entities.RolePermission;
import com.procurement.modules.role_permissions.repositories.RoleRepository;
import com.procurement.modules.users.dto.UserResponse;
import com.procurement.modules.users.entities.User;
import com.procurement.modules.users.entities.UserStatus;
import com.procurement.modules.users.repositories.UserRepository;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.*;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

  private static final Logger log = LoggerFactory.getLogger(AuthService.class);

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;
  private final JWTService jwtService;

  @Transactional
  public AuthResponse register(RegisterRequest request) {
    log.info("Register request received for email={}", request.email());

    String email = request.email().toLowerCase();
    if (userRepository.existsByEmail(email)) {
      throw new ConflictException("Email already registered");
    }

    Role userRole = roleRepository
      .findByName("USER")
      .orElseThrow(() -> new NotFoundException("Default USER role is missing"));

    User user = User.builder()
      .name(request.name().trim())
      .email(email)
      .password(passwordEncoder.encode(request.password()))
      .status(UserStatus.ACTIVE)
      .roles(Set.of(userRole))
      .build();

    User savedUser = userRepository.save(user);

    String token = jwtService.generateToken(
      savedUser.getEmail(),
      roleNames(savedUser),
      permissionKeys(savedUser)
    );

    log.info(
      "User registered successfully id={} email={}",
      savedUser.getId(),
      savedUser.getEmail()
    );

    return new AuthResponse(
      token,
      "Bearer",
      jwtService.getExpirationMs(),
      toUserResponse(savedUser)
    );
  }

  public AuthResponse login(LoginRequest request) {
    log.info("Login attempt email={}", request.email());

    String email = request.email().toLowerCase();
    User user = userRepository
      .findByEmail(email)
      .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
    if (
      user.getStatus() != UserStatus.ACTIVE ||
      !passwordEncoder.matches(request.password(), user.getPassword())
    ) {
      throw new BadCredentialsException("Invalid credentials");
    }

    String token = jwtService.generateToken(user.getEmail(), roleNames(user), permissionKeys(user));

    log.info("Login success userId={} email={}", user.getId(), user.getEmail());

    return new AuthResponse(token, "Bearer", jwtService.getExpirationMs(), toUserResponse(user));
  }

  private UserResponse toUserResponse(User user) {
    return new UserResponse(
      user.getId(),
      user.getName(),
      user.getEmail(),
      user.getStatus(),
      roleNames(user),
      permissionKeys(user),
      user.getCreatedAt(),
      user.getUpdatedAt()
    );
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
